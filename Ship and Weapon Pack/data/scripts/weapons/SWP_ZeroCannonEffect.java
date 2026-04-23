package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.SWP_Util;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ZeroCannonEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private static final float ACCELERATION = 1500f;
    private static final String DATA_KEY_PREFIX = "SWP_ZeroCannon_";
    private static final float MAX_SPEED = 1000f;
    private static final Vector2f ZERO = new Vector2f();

    private static ShipAPI findBestTarget(DamagingProjectileAPI proj) {
        ShipAPI source = proj.getSource();
        if (source != null && source.getShipTarget() != null && !source.getShipTarget().isHulk()) {
            return source.getShipTarget();
        }

        return AIUtils.getNearestEnemy(proj);
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) {
            return;
        }

        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Map<DamagingProjectileAPI, CombatEntityAPI> pulses = localData.pulses;

        pulses.put(projectile, findBestTarget(projectile));
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Map<DamagingProjectileAPI, CombatEntityAPI> pulses = localData.pulses;

        if (pulses.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<DamagingProjectileAPI, CombatEntityAPI>> iter = pulses.entrySet().iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next().getKey();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }

        iter = pulses.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<DamagingProjectileAPI, CombatEntityAPI> entry = iter.next();
            DamagingProjectileAPI projectile = entry.getKey();
            CombatEntityAPI target = entry.getValue();

            if (projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
                iter.remove();
                continue;
            }

            if ((target == null) || ((target instanceof ShipAPI) && ((ShipAPI) target).isHulk())
                    || (projectile.getOwner() == target.getOwner())
                    || !Global.getCombatEngine().isEntityInPlay(target)) {
                entry.setValue(findBestTarget(projectile));
                continue;
            }

            float distance = MathUtils.getDistance(target.getLocation(), projectile.getLocation());
            Vector2f guidedTarget = SWP_Util.intercept(projectile.getLocation(), projectile.getVelocity().length(),
                    ACCELERATION, MAX_SPEED, target.getLocation(), target.getVelocity());
            if (guidedTarget == null) {
                Vector2f projection = new Vector2f(target.getVelocity());
                float scalar = distance / (projectile.getVelocity().length() + 1f);
                projection.scale(scalar);
                guidedTarget = Vector2f.add(target.getLocation(), projection, null);
            }

            Vector2f acceleration = VectorUtils.getDirectionalVector(projectile.getLocation(), guidedTarget);
            acceleration.scale(ACCELERATION * amount);
            Vector2f.add(acceleration, projectile.getVelocity(), acceleration);
            float speed = acceleration.length();
            float maxSpeed = MAX_SPEED;
            if (projectile.getSource() != null) {
                maxSpeed *= projectile.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
            }
            if (speed > maxSpeed) {
                acceleration.scale(maxSpeed / speed);
            }

            projectile.setFacing(VectorUtils.getAngle(ZERO, acceleration));
            projectile.getVelocity().set(acceleration);
        }
    }

    private static final class LocalData {

        final Map<DamagingProjectileAPI, CombatEntityAPI> pulses = new LinkedHashMap<>(25);
    }
}
