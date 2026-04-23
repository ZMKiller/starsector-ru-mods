package data.scripts.weapons.ai;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.util.SWP_Util;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_FlareBurstWeaponAI implements AutofireAIPlugin {

    private static final float AIMING_RANGE = 2000f;
    private static final float OPTIMAL_RANGE = 1000f;

    private boolean shouldFire = false;
    private CombatEntityAPI target = null;
    private final WeaponAPI weapon;

    public SWP_FlareBurstWeaponAI(WeaponAPI weapon) {
        this.weapon = weapon;
    }

    @Override
    public void advance(float amount) {
        float aimingRange = weapon.getShip().getMutableStats().getMissileWeaponRangeBonus().computeEffective(
                AIMING_RANGE);
        shouldFire = false;

        if (weapon.getAmmo() <= 0) {
            return;
        }

        float totalWeight = 0f;
        float bestWeight = 0f;
        MissileAPI bestMissile = null;
        List<MissileAPI> missiles = SWP_Util.getMissilesWithinRange(weapon.getLocation(), aimingRange);
        for (MissileAPI missile : missiles) {
            if (missile.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }
            if (missile.getOwner() != weapon.getShip().getOwner()) {
                float distanceFromArc = weapon.distanceFromArc(missile.getLocation());
                float distance = MathUtils.getDistance(missile, weapon.getLocation());

                float weight = (480f - distanceFromArc) / 480f;
                weight *= (OPTIMAL_RANGE - Math.abs(distance - OPTIMAL_RANGE)) / OPTIMAL_RANGE;

                if (weight > 0f) {
                    if (missile.isFlare()) {
                        weight *= (float) Math.sqrt(200f);
                        totalWeight += weight;

                        if (weight > bestWeight) {
                            bestMissile = missile;
                            bestWeight = weight;
                        }
                    } else {
                        weight *= ((missile.getDamageType() == DamageType.FRAGMENTATION) ? 0.25f : 1f)
                                * missile.getDamageAmount() + 0.5f * missile.getEmpAmount();
                        totalWeight += weight;

                        if (weight > bestWeight) {
                            bestMissile = missile;
                            bestWeight = weight;
                        }
                    }
                }
            }
        }

        if (bestWeight >= 400f || totalWeight >= 1500f) {
            target = bestMissile;

            if (bestMissile != null) {
                if (MathUtils.getShortestRotation(weapon.getCurrAngle(),
                        VectorUtils.getAngleStrict(weapon.getLocation(),
                                bestMissile.getLocation())) <= 30f
                        || weapon.getSlot().isHardpoint()) {
                    shouldFire = true;
                }
            }
        }
    }

    @Override
    public void forceOff() {
        shouldFire = false;
    }

    @Override
    public Vector2f getTarget() {
        if (target == null) {
            return null;
        } else {
            return target.getLocation();
        }
    }

    @Override
    public ShipAPI getTargetShip() {
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public boolean shouldFire() {
        return shouldFire;
    }

    @Override
    public MissileAPI getTargetMissile() {
        if (target instanceof MissileAPI) {
            return (MissileAPI) target;
        }
        return null;
    }
}
