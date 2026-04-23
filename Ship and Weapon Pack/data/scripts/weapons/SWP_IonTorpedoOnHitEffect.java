package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import data.scripts.util.SWP_Util;
import data.scripts.weapons.SWP_TrebuchetOnHitEffect.EMPBlast;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_IonTorpedoOnHitEffect implements OnHitEffectPlugin {

    private static final float AREA_EFFECT = 350f;
    private static final float AREA_EFFECT_INNER = 150f;

    private static final Color EXPLOSION_COLOR = new Color(125, 200, 255, 155);
    private static final float FLUX_DAMAGE = 1000f;
    private static final float FRIENDLY_FIRE_MULT = 0.25f;
    private static final Color PARTICLE_COLOR = new Color(150, 215, 255, 255);
    private static final Vector2f ZERO = new Vector2f();

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult,
            CombatEngineAPI engine) {
        if (point == null) {
            return;
        }

        float emp = projectile.getEmpAmount() * 0.25f;
        float dam = projectile.getDamageAmount() * 0.25f;

        List<ShipAPI> targets = SWP_Util.getShipsWithinRange(point, AREA_EFFECT);

        SWP_Util.filterObscuredTargets(target, point, targets, true, true, false);

        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            targets.remove(ship);

            if (projectile.getSource() == null) {
                ship.getFluxTracker().increaseFlux(FLUX_DAMAGE, shieldHit);
            } else {
                ship.getFluxTracker().increaseFlux(FLUX_DAMAGE * projectile.getSource().getMutableStats().getEnergyWeaponDamageMult().getModifiedValue(), shieldHit);
            }
            for (int x = 0; x < 4; x++) {
                ShipAPI empTarget = ship;
                engine.spawnEmpArc(projectile.getSource(), point, empTarget, empTarget,
                        DamageType.ENERGY, dam, emp, 100000f, null, 20f,
                        PARTICLE_COLOR, EXPLOSION_COLOR);
            }
        }

        for (ShipAPI ship : targets) {
            float distance = MathUtils.getDistance(ship.getLocation(), point);
            float reduction = 1;
            if (distance > (AREA_EFFECT_INNER + ship.getCollisionRadius())) {
                reduction = (AREA_EFFECT - distance / 2f) / (AREA_EFFECT - AREA_EFFECT_INNER);
            }
            if (ship.getOwner() == projectile.getOwner()) {
                reduction *= FRIENDLY_FIRE_MULT;
            }

            if (ship.isFighter() || ship.isDrone()) {
                engine.spawnEmpArc(projectile.getSource(), point, null, ship,
                        DamageType.ENERGY, dam * reduction, emp * reduction, 500f, null, 40f,
                        PARTICLE_COLOR, EXPLOSION_COLOR);
                for (int x = 0; x < 3; x++) {
                    Vector2f point1 = MathUtils.getRandomPointInCircle(point, ship.getCollisionRadius());
                    engine.applyDamage(ship, point1, dam * reduction, DamageType.ENERGY, emp * reduction, false, false, projectile.getSource(), false);
                }
            } else {
                for (int x = 0; x < 4; x++) {
                    engine.spawnEmpArc(projectile.getSource(), point, null, ship,
                            DamageType.ENERGY, dam * reduction, emp * reduction, 500f, null, 40f,
                            PARTICLE_COLOR, EXPLOSION_COLOR);
                }
            }
        }

        EMPBlast empBlast = new EMPBlast(point, 1.25f, AREA_EFFECT * 1.15f, 0.65f, 25f, 0.2f, 0.75f);
        empBlast.init(Global.getCombatEngine().addLayeredRenderingPlugin(empBlast));

        if (shieldHit) {
            Global.getSoundPlayer().playSound("swp_ionblaster_hit", 1.1f, 0.6f, point, ZERO);
        } else {
            Global.getSoundPlayer().playSound("swp_ionblaster_hit", 0.9f, 0.7f, point, ZERO);
        }
        for (int x = 0; x < 6; x++) {
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * 100f + 150f;
            Vector2f point1 = MathUtils.getPointOnCircumference(point, distance, angle);
            Vector2f point2 = new Vector2f(point);
            engine.spawnEmpArc(projectile.getSource(), point1, new SimpleEntity(point1), new SimpleEntity(point2),
                    DamageType.ENERGY, 0f, 0f, 1000f, null, 15f,
                    EXPLOSION_COLOR, PARTICLE_COLOR);
        }
    }
}
