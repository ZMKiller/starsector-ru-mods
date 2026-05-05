package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.dem.DEMScript;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class HWIDEMScript extends DEMScript {
    public HWIDEMScript(MissileAPI missile, ShipAPI ship, WeaponAPI weapon) {
        super(missile, ship, weapon);
    }
    @Override
    protected void doMissileControl(float amount) {
        if (state == State.TURN_TO_TARGET || state == State.SIGNAL ||
                (state == State.FIRE && !bombPumped && !fadeOutEngineWhenFiring)) {

            float dist = Misc.getDistance(Global.getCombatEngine().getAimPointWithLeadForAutofire(missile,1f,fireTarget,1000f), missile.getLocation());
            dist -= Global.getSettings().getTargetingRadius(missile.getLocation(), fireTarget, false);
            if (dist < preferredMinFireDistance) {
                missile.giveCommand(ShipCommand.ACCELERATE_BACKWARDS);
            } else if (dist > preferredMaxFireDistance) {
                missile.giveCommand(ShipCommand.ACCELERATE);
            } else if (missile.getVelocity().length() > missile.getMaxSpeed() * allowedDriftFraction) {
                missile.giveCommand(ShipCommand.DECELERATE);
            }
            float dir = Misc.getAngleInDegrees(missile.getLocation(), Global.getCombatEngine().getAimPointWithLeadForAutofire(missile,1f,fireTarget,1000f));
            float diff = Misc.getAngleDiff(missile.getFacing(), dir);
            float rate = missile.getMaxTurnRate() * amount;
//			float turnDir1 = Misc.getClosestTurnDirection(missile.getFacing(), dir);
//			boolean turningTowardsDesiredFacing = Math.signum(turnDir1) == Math.signum(missile.getAngularVelocity());
            boolean turningTowardsDesiredFacing = true;
            //snapFacingToTargetIfCloseEnough = true;
            boolean phased = fireTarget instanceof ShipAPI && ((ShipAPI)fireTarget).isPhased();
            if (!phased) {
                if (diff <= rate * 0.25f && turningTowardsDesiredFacing && snapFacingToTargetIfCloseEnough) {
                    missile.setFacing(dir);
                } else {
                    Misc.turnTowardsPointV2(missile, Global.getCombatEngine().getAimPointWithLeadForAutofire(missile,1f,fireTarget,1000f), 0f);
                }
            }

            if (randomStrafe) {
                if (strafeDur <= 0) {
                    float r = (float) Math.random();

                    if (strafeDir == 0) {
                        if (r < 0.4f) {
                            strafeDir = 1f;
                        } else if (r < 0.8f) {
                            strafeDir = -1f;
                        } else {
                            strafeDir = 0f;
                        }
                    } else {
                        if (r < 0.8f) {
                            strafeDir = -strafeDir;
                        } else {
                            strafeDir = 0f;
                        }
                    }

                    strafeDur = 0.5f + (float) Math.random() * 0.5f;
                    //strafeDur *= 0.5f;
                }

                Vector2f driftDir = Misc.getUnitVectorAtDegreeAngle(missile.getFacing() + 90f);
                if (strafeDir == 1f) driftDir.negate();

                float distToShip = Misc.getDistance(ship.getLocation(), missile.getLocation());
                float shipToFireTarget = Misc.getDistance(ship.getLocation(), fireTarget.getLocation());
                float extra = 0f;
                if (dist > shipToFireTarget) extra = dist - shipToFireTarget;
                if (distToShip < ship.getCollisionRadius() * 1f + extra) {
                    float away = Misc.getAngleInDegrees(ship.getLocation(), missile.getLocation());
                    float turnDir = Misc.getClosestTurnDirection(away, missile.getFacing());
                    strafeDir = turnDir;
                }

                float maxDrift = missile.getMaxSpeed() * allowedDriftFraction;
                float speedInDir = Vector2f.dot(driftDir, missile.getVelocity());

                if (speedInDir < maxDrift) {
                    if (strafeDir == 1f) {
                        missile.giveCommand(ShipCommand.STRAFE_RIGHT);
                    } else if (strafeDir == -1f) {
                        missile.giveCommand(ShipCommand.STRAFE_LEFT);
                    }
                }

                strafeDur -= amount;
            }
        }
    }
}
