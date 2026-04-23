/*
code by Xaiier
*/

package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

import java.awt.*;
import java.util.List;

public class XHAN_MindControl extends BaseShipSystemScript {

    private final boolean DEBUG = false;

    public static final Color JITTER_UNDER_COLOR = new Color(115, 76, 255, 155);
    public static final Color JITTER_COLOR = new Color(119, 43, 255, 164);
    private static final float JITTER_MAX_RANGE_BONUS = 25f;

    public static Color TEXT_COLOR = new Color(129, 0, 255, 121);

    private static final Color TARGET_EXPLOSION_EFFECT_COLOUR = new Color(120, 42, 255, 62);
    private static final float TARGET_JITTER_RANGE = 25f;

    private static final float TARGET_JITTER_MIN = 0.4f;

    protected static float RANGE = 2000f;

    public static float getMaxRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
    }

    public static ShipAPI getBaseModule(ShipAPI ship) {
        if (ship.isStationModule()) {
            return getBaseModule(ship.getParentStation());
        } else {
            return ship;
        }
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //not sure why we need to check for the ship this way, but this is how vanilla does it
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        //make sure we only apply the effect once - this method may be called multiple times during IN state
        if (state == State.IN && ship.getCustomData().get("XHAN_MindControl_applied") == null) {
            ship.setCustomData("XHAN_MindControl_applied", ""); //set applied flag

            ShipAPI target = findTarget(ship);
            if (target != null) { //possibility findTarget no longer has a valid target when this is called
                MindControlData mindControlData = (MindControlData) target.getCustomData().get("XHAN_MindControl");
                if (mindControlData == null) {
                    target = getBaseModule(target); //if we've targeted a submodule, change the target to the base module
                    mindControlData = new MindControlData(ship, target);
                    target.setCustomData("XHAN_MindControl", mindControlData);

                    ship.getSystem().setCooldown(Math.min((float) (Math.sqrt(target.getHullSpec().getFleetPoints()) * 3f), 20f)); //system cooldown is dependent on target FP
                }

                //draw expanding circle
                for (int i = 0; i < 360; i++) {
                    Vector2f loc = MathUtils.getPointOnCircumference(target.getLocation(), target.getShieldRadiusEvenIfNoShield(), i);
                    Vector2f vel = Vector2f.sub(loc, target.getLocation(), new Vector2f());
                    if (vel.lengthSquared() != 0f) { //fix crash with zero radius shields I guess?
                        vel.normalise();
                        vel.scale(310f);
                    }

                    Global.getCombatEngine().addSmoothParticle(loc, vel, 40f, 0.1f, 0.7f, TARGET_EXPLOSION_EFFECT_COLOUR);
                }

                final MindControlData mindControlDataFinal = mindControlData; //for use within inline BaseEveryFrameCombatPlugin
                if (mindControlDataFinal.targetEffectPlugin == null) {
                    mindControlDataFinal.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {

                        @Override
                        public void advance(float amount, List<InputEventAPI> events) {
                            if (Global.getCombatEngine().isPaused()) return;

                            //fix for buggy missiles, hopefully more efficient than everyframe spam
                            for (MissileAPI missile : AIUtils.getNearbyEnemyMissiles(mindControlDataFinal.controlledShip, mindControlDataFinal.controlledShip.getCollisionRadius() * 2f)) {
                                missile.setOwner(missile.getSource().getOwner());
                            }

                            //handle player taking over ship as it is mind controlled
                            if (mindControlDataFinal.controlledShip == Global.getCombatEngine().getPlayerShip() && !mindControlDataFinal.controlledShip.getFluxTracker().isOverloaded() && mindControlDataFinal.controlledShip.getOwner() != 0) {
                                mindControlDataFinal.recursiveDisable(mindControlDataFinal.controlledShip);
                                mindControlDataFinal.restoreOwnership();
                            }

                            mindControlDataFinal.elapsedAfterInState += amount;

                            if (DEBUG) {
                                Global.getCombatEngine().addFloatingText(mindControlDataFinal.controlledShip.getLocation(), Math.floor((mindControlDataFinal.durationEnd - mindControlDataFinal.elapsedAfterInState)) + "s", 50f, Color.white, mindControlDataFinal.controlledShip, 0f, 2f);
                            }

                            float jitterLevel = 1.0f - (mindControlDataFinal.elapsedAfterInState / mindControlDataFinal.durationEnd);
                            jitterLevel = MagicAnim.offsetToRange(jitterLevel, TARGET_JITTER_MIN, 1.0f);

                            float jitterRangeBonus = jitterLevel * TARGET_JITTER_RANGE;

                            mindControlDataFinal.controlledShip.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
                            mindControlDataFinal.controlledShip.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

                            if ((mindControlDataFinal.elapsedAfterInState > mindControlDataFinal.durationEnd || !mindControlDataFinal.controllingShip.isAlive()) && mindControlDataFinal.controlledShip.isAlive()) { //duration over or controlling ship dies, but target is still alive
                                mindControlDataFinal.controlledShip.removeCustomData("XHAN_MindControl");
                                mindControlDataFinal.restoreOwnership();
                                Global.getCombatEngine().removePlugin(mindControlDataFinal.targetEffectPlugin);
                            } else if (!mindControlDataFinal.controlledShip.isAlive()) { //target is dead
                                Global.getCombatEngine().removePlugin(mindControlDataFinal.targetEffectPlugin);
                            }
                        }
                    };
                    Global.getCombatEngine().addPlugin(mindControlData.targetEffectPlugin);
                } else {
                    mindControlData.applyDuration(); //another charge being applied to a ship that is already under mind control
                }
            }
        }

        //visuals for ship
        float jitterLevel = effectLevel;
        if (state == State.OUT) {
            jitterLevel *= jitterLevel;
            ship.removeCustomData("XHAN_MindControl_applied");
        }
        float jitterRangeBonus = jitterLevel * JITTER_MAX_RANGE_BONUS;

        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

        if (DEBUG) {
            Vector2f offset = new Vector2f(0f, -100f);
            Vector2f.add(offset, ship.getLocation(), offset);
            Global.getCombatEngine().addFloatingText(offset, "CD: " + ship.getSystem().getCooldownRemaining(), 50f, Color.white, ship, 0f, 2f);
        }
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        ShipAPI targetShip = findTarget(ship);
        Vector2f target = null;
        if (targetShip != null) target = targetShip.getLocation();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target);
            float radSum = ship.getCollisionRadius() + targetShip.getCollisionRadius();
            if (dist > getMaxRange(ship) + radSum) {
                return "OUT OF RANGE";
            } else {
                return "READY";
            }
        }
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship;
    }

    protected ShipAPI findTarget(ShipAPI ship) {
        float range = getMaxRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();

        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum) target = null; //our valid target is out of range, invalid
        } else { //no target selected, try to find something
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > range + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, range, true);
            }
        }

        //invalidate unacceptable targets
        if (target == null) {
            //no target, or selected target is out of range or no targets in range
        } else if (target.getHullSize() == ShipAPI.HullSize.FIGHTER) {
            //target is a fighter, invalid
            target = null;
        } else if (ship.getOwner() == target.getOwner() && target.getCustomData().get("XHAN_MindControl") == null) {
            //target is a regular ally, invalid
            target = null;
        } else if (target.isHulk()) {
            //target is already dead
            target = null;
        }

        return target;
    }

    public static class MindControlData {
        public ShipAPI controllingShip;
        public ShipAPI controlledShip;
        public int formerOwner;
        public boolean formerAlly;
        public EveryFrameCombatPlugin targetEffectPlugin;
        public float elapsedAfterInState = 0f;
        public float durationEnd = 0f;

        public MindControlData(ShipAPI controllingShip, ShipAPI controlledShip) {
            this.controllingShip = controllingShip;
            this.controlledShip = controlledShip;
            this.formerOwner = controlledShip.getOwner();
            this.formerAlly = controlledShip.isAlly();
            applyDuration();
        }

        public void applyDuration() {
            float fp = this.controlledShip.getHullSpec().getFleetPoints();
            this.durationEnd += 50f - fp;

            //the player, stations, and overpowered mod ships are simply disabled instead of being taken over
            if (this.controlledShip == Global.getCombatEngine().getPlayerShip() || this.controlledShip.isStation() || fp >= 50f) {
                recursiveDisable(this.controlledShip);
            } else {
                changeSides(controlledShip, controlledShip.getOriginalOwner() == 0 ? 1 : 0, controlledShip.getOriginalOwner() == 1); //this ensures stacks don't cause weird shenanigans
            }
        }

        private void changeSides(ShipAPI ship, int newOwner, boolean ally) {
            //changes to the player side should be allies to prevent shenanigans
            ship.setAlly(ally);

            ship.setOwner(newOwner);

            //switch sides of newly deployed fighters
            for (FighterWingAPI wing : ship.getAllWings()) {
                wing.setWingOwner(ship.getOwner());
            }

            //also switch sides of any drones (doesn't affect any new ones)
            if (ship.getDeployedDrones() != null) {
                for (ShipAPI drone : ship.getDeployedDrones()) {
                    drone.setOwner(newOwner);
                    drone.getShipAI().forceCircumstanceEvaluation();
                }
            }

            //recurse downwards for all submodules
            for (ShipAPI child : ship.getChildModulesCopy()) {
                changeSides(child, newOwner, ally);
            }

            //force AI to re-evaluate surroundings
            if (ship.getShipAI() != null) {

                //cancel orders so the AI doesn't get confused
                DeployedFleetMemberAPI member_a = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDeployedFleetMember(ship);
                if (member_a != null)
                    Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(false).orderSearchAndDestroy(member_a, false);

                DeployedFleetMemberAPI member_aa = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDeployedFleetMember(ship);
                if (member_aa != null)
                    Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(true).orderSearchAndDestroy(member_aa, false);

                DeployedFleetMemberAPI member_b = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(ship);
                if (member_b != null)
                    Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getTaskManager(false).orderSearchAndDestroy(member_b, false);

                ship.getShipAI().forceCircumstanceEvaluation();
            }
        }

        public void restoreOwnership() {
            changeSides(controlledShip, formerOwner, formerAlly);
        }

        private void recursiveDisable(ShipAPI s) {
            s.getFluxTracker().forceOverload(0f);
            s.getEngineController().forceFlameout(true);

            for (ShipAPI child : s.getChildModulesCopy()) {
                recursiveDisable(child);
            }
        }
    }
}
