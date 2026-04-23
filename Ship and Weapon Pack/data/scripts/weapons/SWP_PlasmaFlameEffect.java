package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.SWP_Trails;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_PlasmaFlameEffect implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin, OnFireEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(255, 100, 50, 50);
    private static final Color MUZZLE_FLASH_COLOR = new Color(255, 200, 100, 50);
    private static final Color MUZZLE_FLASH_COLOR_ALT = new Color(100, 100, 255, 100);
    private static final Color MUZZLE_FLASH_COLOR_GLOW = new Color(255, 75, 0, 50);
    private static final float MUZZLE_FLASH_DURATION = 0.15f;
    private static final float MUZZLE_FLASH_SIZE = 50.0f;
    private static final Vector2f MUZZLE_OFFSET_HARDPOINT = new Vector2f(37.0f, -4.5f);
    private static final Vector2f MUZZLE_OFFSET_TURRET = new Vector2f(35.0f, -4.5f);

    private int lastWeaponAmmo = 0;

    protected List<SWP_PlasmaFlameEffect> trails;

    public SWP_PlasmaFlameEffect() {
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }

        Vector2f vel = new Vector2f();
        if (target instanceof ShipAPI) {
            vel.set(target.getVelocity());
        }

        if (!shieldHit) {
            float sizeMult = Misc.getHitGlowSize(100f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;
            engine.spawnExplosion(point, new Vector2f(vel.x * 0.45f, vel.y * 0.45f), EXPLOSION_COLOR, 50f * sizeMult, 0.5f);
        }

        Misc.playSound(damageResult, point, vel,
                "swp_plasmaflame_hit_shield_light",
                "swp_plasmaflame_hit_shield_solid",
                "swp_plasmaflame_hit_shield_heavy",
                "swp_plasmaflame_hit_light",
                "swp_plasmaflame_hit_solid",
                "swp_plasmaflame_hit_heavy");
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        int weaponAmmo = weapon.getAmmo();

        if (weaponAmmo < lastWeaponAmmo) {
            Vector2f weaponLocation = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            float weaponFacing = weapon.getCurrAngle();
            Vector2f muzzleLocation = new Vector2f(weapon.getSlot().isHardpoint() ? MUZZLE_OFFSET_HARDPOINT : MUZZLE_OFFSET_TURRET);
            VectorUtils.rotate(muzzleLocation, weaponFacing, muzzleLocation);
            Vector2f.add(muzzleLocation, weaponLocation, muzzleLocation);

            if (weaponAmmo < lastWeaponAmmo) {
                Vector2f shipVelocity = MathUtils.getPointOnCircumference(ship.getVelocity(), (float) Math.random() * 20f, weaponFacing + 90f
                        - (float) Math.random()
                        * 180f);
                if (Math.random() > 0.75) {
                    engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR_ALT, MUZZLE_FLASH_SIZE * 0.5f, MUZZLE_FLASH_DURATION);
                } else {
                    engine.spawnExplosion(muzzleLocation, shipVelocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE, MUZZLE_FLASH_DURATION);
                }
                engine.addSmoothParticle(muzzleLocation, shipVelocity, MUZZLE_FLASH_SIZE * 3f, 1f, MUZZLE_FLASH_DURATION * 2f, MUZZLE_FLASH_COLOR_GLOW);
            }
        }

        lastWeaponAmmo = weaponAmmo;

        if (trails == null) {
            return;
        }

        Iterator<SWP_PlasmaFlameEffect> iter = trails.iterator();
        while (iter.hasNext()) {
            if (iter.next().isExpired()) {
                iter.remove();
            }
        }

        float numIter = 1f; // more doesn't actually change anything
        amount /= numIter;
        // drag along the previous projectile, starting with the most recently launched; new ones are added at the start
        // note: prev is fired before and so is in front of proj
        for (int i = 0; i < numIter; i++) {
            for (SWP_PlasmaFlameEffect trail : trails) {
                if (trail.prev != null && !trail.prev.isExpired() && Global.getCombatEngine().isEntityInPlay(trail.prev)) {
                    float dist1 = Misc.getDistance(trail.prev.getLocation(), trail.proj.getLocation());
                    if (dist1 < trail.proj.getProjectileSpec().getLength() * 1f) {
                        float maxSpeed = trail.prev.getMoveSpeed() * 0.5f;// * Math.max(0.5f, 1f - trail.prev.getElapsed() * 0.5f);
                        // goal here is to prevent longer shot series (e.g. from Paragon) from moving too unnaturally
                        float e = trail.prev.getElapsed();
                        float t = 0.5f;
                        if (e > t) {
                            maxSpeed *= Math.max(0.25f, 1f - (e - t) * 0.5f);
                        }
                        if (dist1 < 20f && e > t) {
                            maxSpeed *= dist1 / 20f;
                        }

                        Vector2f driftTo = Misc.closestPointOnLineToPoint(trail.proj.getLocation(), trail.proj.getTailEnd(), trail.prev.getLocation());
                        float dist = Misc.getDistance(driftTo, trail.prev.getLocation());
                        Vector2f diff = Vector2f.sub(driftTo, trail.prev.getLocation(), new Vector2f());
                        diff = Misc.normalise(diff);
                        diff.scale(Math.min(dist, maxSpeed * amount));
                        Vector2f.add(trail.prev.getLocation(), diff, trail.prev.getLocation());
                        Vector2f.add(trail.prev.getTailEnd(), diff, trail.prev.getTailEnd());
                    }
                }
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        String prevKey = "swp_plasmaflame_prev_" + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        DamagingProjectileAPI prevProj = (DamagingProjectileAPI) engine.getCustomData().get(prevKey);

        SWP_Trails.createIfNeeded();

        SWP_PlasmaFlameEffect trail = new SWP_PlasmaFlameEffect(projectile, prevProj);

        engine.getCustomData().put(prevKey, projectile);

        if (trails == null) {
            trails = new ArrayList<>();
        }
        trails.add(0, trail);
    }

    protected DamagingProjectileAPI proj;
    protected DamagingProjectileAPI prev;
    protected float baseFacing = 0f;

    public SWP_PlasmaFlameEffect(DamagingProjectileAPI proj, DamagingProjectileAPI prev) {
        this.proj = proj;
        this.prev = prev;

        baseFacing = proj.getFacing();
    }

    public boolean isExpired() {
        return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj);
    }
}
