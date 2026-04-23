package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.SWP_Multi;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_LightningGunOnFireEffect implements OnFireEffectPlugin {

    private static final float BASE_DAMAGE = 225f;
    private static final Color HIT_COLOR = new Color(110, 140, 255);
    private static final float SLOP_RANGE = 200f;
    private static final EnumSet<CollisionClass> ALLOWED_COLLISIONS
            = EnumSet.of(CollisionClass.ASTEROID, CollisionClass.FIGHTER, CollisionClass.MISSILE_FF, CollisionClass.MISSILE_NO_FF, CollisionClass.SHIP);
    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if (projectile == null) {
            return;
        }

        if (!engine.getListenerManager().hasListenerOfClass(LightningGunDamageListener.class)) {
            engine.getListenerManager().addListener(new LightningGunDamageListener());
        }

        ShipAPI source = projectile.getSource();
        CombatEntityAPI target = null;
        Vector2f endpoint = new Vector2f(projectile.getWeapon().getRange() + SLOP_RANGE, 0f);
        VectorUtils.rotate(endpoint, projectile.getFacing(), endpoint);
        Vector2f.add(projectile.getSpawnLocation(), endpoint, endpoint);

        Vector2f point = new Vector2f(endpoint);
        Vector2f visualPoint = point;

        List<CombatEntityAPI> entitiesToCheck = CombatUtils.getEntitiesWithinRange(projectile.getSpawnLocation(), projectile.getWeapon().getRange() + SLOP_RANGE);

        /* Easy filters */
        Iterator<CombatEntityAPI> iter = entitiesToCheck.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI entity = iter.next();
            if ((entity == source) || (entity == projectile)) {
                iter.remove();
                continue;
            }
            if (!ALLOWED_COLLISIONS.contains(entity.getCollisionClass())) {
                iter.remove();
                continue;
            }
            if ((entity.getCollisionClass() != CollisionClass.SHIP) && (entity.getOwner() == projectile.getOwner())) {
                iter.remove();
                continue;
            }
            if (entity instanceof ShipAPI ship) {
                if ((source != null) && (SWP_Multi.getRoot(ship) == SWP_Multi.getRoot(source))) {
                    iter.remove();
                }
            }
        }

        /* Hard filters */
        Collections.sort(entitiesToCheck, new CollectionUtils.SortEntitiesByDistance(projectile.getSpawnLocation()));
        iter = entitiesToCheck.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI entity = iter.next();
            if (CollisionUtils.getCollides(projectile.getSpawnLocation(), endpoint, entity.getLocation(), entity.getCollisionRadius())) {
                if (CollisionUtils.isPointWithinCollisionCircle(projectile.getSpawnLocation(), entity)) {
                    if (CollisionUtils.isPointWithinBounds(projectile.getSpawnLocation(), entity)) {
                        point = projectile.getSpawnLocation();
                        visualPoint = projectile.getSpawnLocation();
                        Vector2f extra = new Vector2f(25f, 0f);
                        VectorUtils.rotate(extra, projectile.getFacing(), extra);
                        Vector2f.add(visualPoint, extra, visualPoint);
                        target = entity;
                        break;
                    }
                }
                if (entity instanceof ShipAPI ship) {
                    /* Shield (near side) */
                    if ((ship.getShield() != null) && ship.getShield().isOn()) {
                        Vector2f collision = SWP_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, ship.getShield().getLocation(), ship.getShield().getRadius(), true);
                        if ((collision != null) && Misc.isInArc(ship.getShield().getFacing(), ship.getShield().getActiveArc(), ship.getShield().getLocation(), collision)) {
                            point = collision;
                            visualPoint = collision;
                            target = entity;
                            break;
                        }
                    }
                    // Workaround until LazyLib is patched
                    BoundsAPI bounds = entity.getExactBounds();
                    if (bounds != null) {
                        bounds.update(entity.getLocation(), entity.getFacing());
                    }
                    Vector2f collision = CollisionUtils.getCollisionPoint(projectile.getSpawnLocation(), endpoint, entity);
                    if (collision != null) {
                        point = collision;
                        visualPoint = collision;
                        target = entity;
                        break;
                    }
                    /* Shield (back side) */
                    if ((ship.getShield() != null) && ship.getShield().isOn()) {
                        collision = SWP_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, ship.getShield().getLocation(), ship.getShield().getRadius(), false);
                        if ((collision != null) && Misc.isInArc(ship.getShield().getFacing(), ship.getShield().getActiveArc(), ship.getShield().getLocation(), collision)) {
                            point = collision;
                            visualPoint = collision;
                            target = entity;
                            break;
                        }
                    }
                } else {
                    Vector2f collision = SWP_Util.getCollisionRayCircle(projectile.getSpawnLocation(), endpoint, entity.getLocation(), entity.getCollisionRadius(), true);
                    if (collision != null) {
                        point = collision;
                        visualPoint = collision;
                        target = entity;
                        break;
                    }
                }
            }
        }

        /* Move back to sanity */
        Vector2f difference = Vector2f.sub(point, projectile.getSpawnLocation(), new Vector2f());
        float length = difference.length();
        if (length >= (projectile.getWeapon().getRange() + SLOP_RANGE)) {
            difference.scale((projectile.getWeapon().getRange() + SLOP_RANGE) / length);
            Vector2f.add(projectile.getSpawnLocation(), difference, point);
            visualPoint = point;
        }

        float distance = MathUtils.getDistance(projectile.getSpawnLocation(), point);
        if (distance < 0.1f) {
            distance = 10f;
        }
        float atten = 1f;
        if (distance > projectile.getWeapon().getRange()) {
            atten = 1f - ((distance - projectile.getWeapon().getRange()) / (SLOP_RANGE * 2f));
        }
        float thickness = 50f * atten;
        float coreWidth = thickness * 0.65f;
        int brightness = (int) (255f * atten);

        StandardLight light = new StandardLight(projectile.getSpawnLocation(), point, ZERO, ZERO, null);
        light.setIntensity(0.6f * atten);
        light.setSize(80f * atten);
        light.setColor(0.65f, 0.75f, 0.9f);
        light.fadeOut(0.35f);
        LightShader.addLight(light);

        EmpArcParams params = new EmpArcParams();
        params.zigZagReductionFactor = 0.15f;
        params.glowSizeMult = 1.5f;
        params.flickerRateMult = 0.75f;
        EmpArcEntityAPI arc = engine.spawnEmpArcVisual(projectile.getSpawnLocation(), source, visualPoint, target, thickness,
                new Color(100, 125, 200, SWP_Util.clamp255(brightness)), new Color(240, 250, 255, SWP_Util.clamp255(brightness)), params);
        arc.setCoreWidthOverride(coreWidth);
        arc.setSingleFlickerMode();

        if (target != null) {
            for (int i = 0; i < 15; i++) {
                Vector2f vel = new Vector2f(MathUtils.getRandomNumberInRange(300f, 800f), 0f);
                VectorUtils.rotate(vel, projectile.getFacing() + 180f + MathUtils.getRandomNumberInRange(-90f, 90f));
                Color sparkColor = new Color(MathUtils.getRandomNumberInRange(100, 140), MathUtils.getRandomNumberInRange(100, 140), 255);
                engine.addHitParticle(point, vel, 10f, 1f, 0.25f, sparkColor);
            }

            float emp = projectile.getEmpAmount() * atten;
            float dam = projectile.getDamageAmount() * atten;
            projectile.getLocation().set(point);
            engine.applyDamage(projectile, target, point, dam, DamageType.ENERGY, emp, false, false, source, true);
            if (!(target instanceof ShipAPI)) {
                float hitGlowSize = Misc.getHitGlowSize(100f, BASE_DAMAGE, DamageType.ENERGY, 0f, 0f, dam, emp);
                Global.getCombatEngine().addHitParticle(point, target.getVelocity(), hitGlowSize, 1f, HIT_COLOR);
            }
            Global.getSoundPlayer().playSound("swp_lightning_gun_arc", 1f, 0.55f * atten, visualPoint, ZERO);
        }

        projectile.setCollisionClass(CollisionClass.NONE);
    }

    private static final class LightningGunDamageListener implements DamageListener {

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if ((target instanceof ShipAPI ship) && (ship.getParamAboutToApplyDamage() instanceof DamagingProjectileAPI proj)) {
                if ((proj.getProjectileSpecId() != null) && proj.getProjectileSpecId().contentEquals("swp_lightninggun_shot")) {
                    float hitGlowSize = Misc.getHitGlowSize(100f, BASE_DAMAGE, result);
                    Global.getCombatEngine().addHitParticle(proj.getLocation(), ship.getVelocity(), hitGlowSize, 1f, HIT_COLOR);

                    /* Convert half the damage dealt from hard to soft ( ͡° ͜ʖ ͡°) */
                    if (result.getDamageToShields() > 0f) {
                        float halfHardFlux = (1f - ship.getMutableStats().getShieldSoftFluxConversion().getModifiedValue()) * (result.getDamageToShields() / 2f);
                        ship.getFluxTracker().setHardFlux(Math.min(ship.getFluxTracker().getHardFlux(), Math.max(0, ship.getFluxTracker().getHardFlux() - halfHardFlux)));
                    }
                }
            }
        }
    }
}
