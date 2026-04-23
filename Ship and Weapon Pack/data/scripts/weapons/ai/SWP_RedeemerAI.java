package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_RedeemerAI extends SWP_BaseMissile {

    private static final Color MIRV_SMOKE = new Color(90, 100, 80, 125);
    private static final float MIRV_DISTANCE = 1000f;
    private static final int MAX_MIRVS = 8;
    private static final float TIME_BETWEEN_MIRVS = 0.05f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.45f;
    private static final Vector2f ZERO = new Vector2f();

    private boolean aspectLocked = true;
    private float retargetTimer = 1f;
    private float timeLive = 0f;
    private final float minTimeToMirv;
    private float mirvCooldown = 0f;
    private int mirvIndex = 0;

    public SWP_RedeemerAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
        missile.setEmpResistance(missile.getEmpResistance() + 4);
        minTimeToMirv = MathUtils.getRandomNumberInRange(3f, 4f);
    }

    public void mirv(MissileAPI missile) {

        float angleOffset;
        switch (mirvIndex) {
            default:
            case 0:
                angleOffset = 140f;
                break;
            case 1:
                angleOffset = -140f;
                break;
            case 2:
                angleOffset = 100f;
                break;
            case 3:
                angleOffset = -100f;
                break;
            case 4:
                angleOffset = 60f;
                break;
            case 5:
                angleOffset = -60f;
                break;
            case 6:
                angleOffset = 20f;
                break;
            case 7:
                angleOffset = -20f;
                break;
        }
        float angle = missile.getFacing() + angleOffset;
        if (angle < 0f) {
            angle += 360f;
        } else if (angle >= 360f) {
            angle -= 360f;
        }
        Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 4.5f, angle);
        Global.getCombatEngine().addSmokeParticle(location, missile.getVelocity(), 20f, 0.4f, 0.5f, MIRV_SMOKE);
        Global.getSoundPlayer().playSound("swp_redeemer_sub_fire", 1f, 1f, location, missile.getVelocity());

        MissileAPI newMissile = (MissileAPI) Global.getCombatEngine().spawnProjectile(
                missile.getSource(), missile.getWeapon(), "swp_redeemer_sub", location, angle, missile.getVelocity());
        newMissile.setFromMissile(true);

        GuidedMissileAI subAI = (GuidedMissileAI) newMissile.getMissileAI();
        subAI.setTarget(target);

        mirvIndex++;
        mirvCooldown = TIME_BETWEEN_MIRVS;
    }

    @Override
    public void advance(float amount) {
        mirvCooldown -= amount;

        if (missile.isFizzling() || missile.isFading() || ((missile.getEngineController() != null)
                && (missile.getEngineController().isFlamedOut() || missile.getEngineController().isFlamingOut()))) {
            if (target == null) {
                return;
            }
            if ((mirvCooldown <= 0f) && (mirvIndex < MAX_MIRVS)) {
                mirv(missile);
            }
            return;
        }

        timeLive += amount;

        if (!acquireTarget(amount)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float acceleration = missile.getAcceleration();
        float maxSpeed = missile.getMaxSpeed();

        Vector2f calculationVelocity = new Vector2f(missile.getVelocity());
        if (calculationVelocity.length() <= maxSpeed * 0.5f) {
            if (calculationVelocity.length() <= maxSpeed * 0.25f) {
                calculationVelocity.set(maxSpeed * 0.5f, 0f);
                VectorUtils.rotate(calculationVelocity, missile.getFacing(), calculationVelocity);
            } else {
                calculationVelocity.scale((maxSpeed * 0.5f) / calculationVelocity.length());
            }
        }

        Vector2f guidedTarget = interceptAdvanced(missile.getLocation(), calculationVelocity.length(), acceleration,
                maxSpeed, target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (calculationVelocity.length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }

        float targetingRadius = Misc.getTargetingRadius(missile.getLocation(), target, true);
        float targetingDistance = distance - targetingRadius;
        if ((targetingDistance <= MIRV_DISTANCE) && (timeLive >= minTimeToMirv) && (mirvCooldown <= 0f) && (mirvIndex < MAX_MIRVS)) {
            mirv(missile);
        }

        float velocityFacing = VectorUtils.getFacing(calculationVelocity);
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing,
                VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                VectorUtils.getAngleStrict(missile.getLocation(), guidedTarget));
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f) {
            angularDistance += 0.5f * compensationDifference;
        }
        float absDAng = Math.abs(angularDistance);

        if (aspectLocked && absDAng > 75f) {
            aspectLocked = false;
        }

        if (!aspectLocked && absDAng <= 30f) {
            aspectLocked = true;
        }

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);
        float turnRadius = missile.getMaxSpeed() * (360f / missile.getMaxTurnRate()) / (2f * (float) Math.PI);
        if (aspectLocked || distance > 2.5f * turnRadius) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }
        if (absDAng < 5) {
            float MFlightAng = VectorUtils.getAngleStrict(ZERO, calculationVelocity);
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected boolean acquireTarget(float amount) {
        if (!isTargetValidAlternate(target)) {
            if (retargetTimer > 0f) {
                retargetTimer -= amount;
                return false;
            } else {
                retargetTimer = 1f;
            }
            setTarget(findBestTarget());
            if (target == null) {
                setTarget(findBestTargetAlternate());
            }
            if (target == null) {
                return false;
            }
        } else {
            retargetTimer = 1f;
            if (!isTargetValidAlternate(target)) {
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null) {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    protected ShipAPI findBestTargetAlternate() {
        ShipAPI closest = null;
        float range = getRemainingRange();
        float distance, closestDistance = getRemainingRange() + missile.getMaxSpeed() * 2f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.isFighter() || tmp.isDrone()) {
                mod = range / 2f;
            }
            if (!isTargetValidAlternate(tmp)) {
                mod = range;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof ShipAPI ship) {
            if (ship.isFighter() || ship.isDrone()) {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target) {
        return super.isTargetValid(target);
    }
}
