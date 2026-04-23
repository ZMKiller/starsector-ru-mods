package data.scripts.weapons.ai;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_RedeemerSubAI extends SWP_BaseMissile {

    private static final float ANTI_CLUMP_RANGE = 100f;
    private static final float LEAD_GUIDANCE_FACTOR = 0.6f;
    private static final float LEAD_GUIDANCE_FACTOR_FROM_ECCM = 0.4f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.15f;
    private static final float WEAVE_DISTANCE_MAX = 1400f;
    private static final float WEAVE_SINE_A_AMPLITUDE = 30f; // degrees offset
    private static final float WEAVE_SINE_A_PERIOD = 3f;
    private static final float WEAVE_SINE_B_AMPLITUDE = 10f; // degrees offset
    private static final float WEAVE_SINE_B_PERIOD = 1f;

    private MissileAPI parent = null;
    private final IntervalUtil antiClumpInterval = new IntervalUtil(0.1f, 0.15f);
    private float nearestMissileAngle = 180f;
    private float nearestMissileDistance = Float.MAX_VALUE;
    private float timeAccum = 0f;
    private final float weaveSineAPhase;
    private final float weaveSineBPhase;

    public SWP_RedeemerSubAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
        missile.setEmpResistance(missile.getEmpResistance() + 2);

        weaveSineAPhase = (float) (Math.random() * Math.PI * 2.0);
        weaveSineBPhase = (float) (Math.random() * Math.PI * 2.0);

        target = null;

        /* Hack to find the parent... */
        List<MissileAPI> nearbyMissiles = SWP_Util.getMissilesWithinRange(missile.getLocation(), 500f);
        float distanceToParent = Float.MAX_VALUE;
        for (MissileAPI nearbyMissile : nearbyMissiles) {
            if (nearbyMissile.getProjectileSpecId().contentEquals("swp_redeemer_missile")) {
                float distance = MathUtils.getDistance(missile.getLocation(), nearbyMissile.getLocation());
                if (distance < distanceToParent) {
                    distanceToParent = distance;
                    parent = nearbyMissile;
                }
            }
        }
    }

    @Override
    public void advance(float amount) {
        boolean noEngines = missile.isFizzling() || missile.isFading();
        if (missile.getEngineController() != null) {
            noEngines &= missile.getEngineController().isFlamedOut() || missile.getEngineController().isFlamingOut();
        }

        float maxSpeed = missile.getMaxSpeed();

        timeAccum += amount;

        if (!acquireTarget(amount)) {
            if (!noEngines) {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = LEAD_GUIDANCE_FACTOR;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f)
                    * LEAD_GUIDANCE_FACTOR_FROM_ECCM;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), maxSpeed, target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / maxSpeed;
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float weaveFactor = Math.max(0.5f, Math.min(1f, 0.5f + 0.5f * distance / WEAVE_DISTANCE_MAX));

        float weaveSineA = WEAVE_SINE_A_AMPLITUDE
                * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_A_PERIOD) + weaveSineAPhase);
        float weaveSineB = WEAVE_SINE_B_AMPLITUDE
                * (float) FastTrig.sin((2.0 * Math.PI * timeAccum / WEAVE_SINE_B_PERIOD) + weaveSineBPhase);
        float weaveOffset = (weaveSineA + weaveSineB) * weaveFactor;

        if (antiClumpInterval.intervalElapsed()) {
            nearestMissileDistance = Float.MAX_VALUE;
            nearestMissileAngle = 180f;
            List<MissileAPI> nearbyMissiles = SWP_Util.getMissilesWithinRange(missile.getLocation(), ANTI_CLUMP_RANGE);
            for (MissileAPI nearbyMissile : nearbyMissiles) {
                if (nearbyMissile == missile) {
                    continue;
                }

                if ((nearbyMissile.getProjectileSpecId() != null) && (missile.getProjectileSpecId() != null)
                        && (nearbyMissile.getProjectileSpecId().contentEquals(missile.getProjectileSpecId())
                        || nearbyMissile.getProjectileSpecId().contentEquals("swp_redeemer_missile"))) {
                    float nearbyMissileDistance = MathUtils.getDistance(missile.getLocation(), nearbyMissile.getLocation());
                    if (nearbyMissileDistance < nearestMissileDistance) {
                        nearestMissileDistance = nearbyMissileDistance;
                        nearestMissileAngle = VectorUtils.getAngleStrict(missile.getLocation(), nearbyMissile.getLocation());
                    }
                }
            }
        }

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                MathUtils.clampAngle(VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget) + weaveOffset));

        float nearestMissileAngularDistance = MathUtils.getShortestRotation(missile.getFacing(), nearestMissileAngle);
        if ((nearestMissileDistance <= ANTI_CLUMP_RANGE) && (Math.abs(nearestMissileAngularDistance) <= 135f)) {
            if (nearestMissileAngularDistance <= 0f) {
                angularDistance += 0.75f * (1f - nearestMissileDistance / ANTI_CLUMP_RANGE) * (135f + nearestMissileAngularDistance);
            } else {
                angularDistance += 0.75f * (1f - nearestMissileDistance / ANTI_CLUMP_RANGE) * (-135f + nearestMissileAngularDistance);
            }
        }

        float absDAng = Math.abs(angularDistance);

        if (!noEngines) {
            missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValid(target)) {
            if (parent != null) {
                if (parent.getMissileAI() instanceof GuidedMissileAI guidedMissileAI) {
                    CombatEntityAPI parentTarget = guidedMissileAI.getTarget();
                    if (target != parentTarget) {
                        setTarget(parentTarget);
                        if (target == null) {
                            return false;
                        }
                    }
                }
            }
            if (target instanceof ShipAPI ship) {
                if (ship.isPhased() && ship.isAlive()) {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (parent != null) {
            if (parent.getMissileAI() instanceof GuidedMissileAI guidedMissileAI) {
                CombatEntityAPI parentTarget = guidedMissileAI.getTarget();
                if (target != parentTarget) {
                    return false;
                }
            }
        }

        return super.isTargetValid(target);
    }
}
