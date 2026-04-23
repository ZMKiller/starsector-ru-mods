package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SWP_LinkedHull extends BaseHullMod {

    private static void advanceChild(ShipAPI child, ShipAPI parent) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            if (parent.isAlive()) {
                if (ec.isAccelerating()) {
                    child.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
                if (ec.isAcceleratingBackwards()) {
                    child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                }
                if (ec.isDecelerating()) {
                    child.giveCommand(ShipCommand.DECELERATE, null, 0);
                }
                if (ec.isStrafingLeft()) {
                    child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
                }
                if (ec.isStrafingRight()) {
                    child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
                }
                if (ec.isTurningLeft()) {
                    child.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                }
                if (ec.isTurningRight()) {
                    child.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
                }
            }

            ShipEngineControllerAPI cec = child.getEngineController();
            if (cec != null) {
                if ((ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
                    child.getEngineController().forceFlameout(true);
                }
            }
        }

        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            objectiveAmount = 0f;
        }
        objectiveAmount *= Global.getCombatEngine().getTimeMult().getModifiedValue();
        StatMod stat = child.getMutableStats().getZeroFluxMinimumFluxLevel().getFlatStatMod("swp_linkedhull");
        float currLevel = 0f;
        if (stat != null) {
            currLevel = stat.getValue();
        }
        if ((parent.getFluxLevel() > parent.getMutableStats().getZeroFluxMinimumFluxLevel().getModifiedValue())
                || ((parent.getMutableStats().getZeroFluxMinimumFluxLevel().getModifiedValue() == 0f) && (parent.getCurrFlux() > 0f))) {
            currLevel -= objectiveAmount * 2f;
            if (currLevel < -2f) {
                currLevel = -2f;
            }
        } else {
            currLevel += objectiveAmount * 2f;
            if (currLevel > 2f) {
                currLevel = 2f;
            }
        }
        child.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("swp_linkedhull", currLevel);

        /* Mirror parent's fighter commands */
        if (child.hasLaunchBays()) {
            if (child.isPullBackFighters() ^ parent.isPullBackFighters()) {
                child.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
            }
            if (child.getAIFlags() != null) {
                if (((Global.getCombatEngine().getPlayerShip() == parent) || (parent.getAIFlags() == null))
                        && (parent.getShipTarget() != null)) {
                    child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getShipTarget());
                } else if ((parent.getAIFlags() != null)
                        && parent.getAIFlags().hasFlag(AIFlags.CARRIER_FIGHTER_TARGET)
                        && (parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
                    child.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, parent.getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET));
                }
            }
        }

        /* Mirror parent's hold-fire commands */
        child.setHoldFire(parent.isHoldFire());
        if (parent.isHoldFire()) {
            child.blockCommandForOneFrame(ShipCommand.FIRE);
        }
//        if (parent.isHulk() && parent.getHullSpec().getBaseHullId().contentEquals("swp_wall")) {
//            child.setParentStation(null);
//            child.setStationSlot(null);
//        }

//        /* Dangerous hack to get D-mods to function on the modules */
//        boolean addedDMods = false;
//        for (String modId : parent.getVariant().getHullMods()) {
//            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
//            if (modSpec.hasTag(Tags.HULLMOD_DMOD) && !child.getVariant().hasHullMod(modId) && !child.getVariant().isStockVariant()) {
//                child.getVariant().addPermaMod(modId);
//                modSpec.getEffect().applyEffectsBeforeShipCreation(child.getHullSize(), child.getMutableStats(), "swp_linkedhull_" + modSpec.getId());
//                modSpec.getEffect().applyEffectsAfterShipCreation(child, "swp_linkedhull_" + modSpec.getId());
//                addedDMods = true;
//            }
//        }
//
//        if (addedDMods) {
//            int numDMods = 0;
//            for (String modId : child.getVariant().getHullMods()) {
//                HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
//                if (modSpec.hasTag(Tags.HULLMOD_DMOD)) {
//                    numDMods++;
//                }
//            }
//
//            switch (numDMods) {
//                case 1:
//                    child.setLightDHullOverlay();
//                    break;
//                case 2:
//                    child.setMediumDHullOverlay();
//                    break;
//                default:
//                    child.setHeavyDHullOverlay();
//                    break;
//            }
//        }

        /* Dirty hack to potentially improve macro-level AI behavior around the Cathedral */
        if (Global.getCombatEngine().getTotalElapsedTime(false) > 1f) {
            switch (child.getHullSpec().getBaseHullId()) {
                case "swp_cathedral_rear":
                case "swp_cathedral_gun_left":
                case "swp_cathedral_gun_right":
                case "swp_cathedral_fighter_left":
                case "swp_cathedral_fighter_right":
                case "swp_cathedral_front":
                    if (child.getHullSize() != HullSize.FRIGATE) {
                        child.setHullSize(HullSize.FRIGATE);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private static void advanceParent(ShipAPI parent, List<ShipAPI> children) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            float originalMass;
            int originalEngines;
            switch (parent.getHullSpec().getBaseHullId()) {
                default:
                case "swp_cathedral":
                    originalMass = 9650f;
                    originalEngines = 12;
                    break;
                case "swp_wall":
                    originalMass = 5500f;
                    originalEngines = 19;
                    break;
                case "swp_boss_sporeship":
                    originalMass = 14700f;
                    originalEngines = 21;
                    break;
            }
            float thrustPerEngine = originalMass / originalEngines;

            /* Don't count parent's engines for this stuff - game already affects stats */
            float workingEngines = ec.getShipEngines().size();
            for (ShipAPI child : children) {
                if ((child.getParentStation() == parent) && (child.getStationSlot() != null) && child.isAlive()) {
                    ShipEngineControllerAPI cec = child.getEngineController();
                    if (cec != null) {
                        float contribution = 0f;
                        for (ShipEngineAPI ce : cec.getShipEngines()) {
                            if (ce.isActive() && !ce.isDisabled() && !ce.isPermanentlyDisabled() && !ce.isSystemActivated()) {
                                contribution += ce.getContribution();
                            }
                        }
                        workingEngines += cec.getShipEngines().size() * contribution;
                    }
                }
            }

            float thrust = workingEngines * thrustPerEngine;
            float enginePerformance = thrust / Math.max(1f, parent.getMassWithModules());
            parent.getMutableStats().getAcceleration().modifyMult("swp_linkedhull", enginePerformance);
            parent.getMutableStats().getDeceleration().modifyMult("swp_linkedhull", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("swp_linkedhull", enginePerformance);
            parent.getMutableStats().getMaxTurnRate().modifyMult("swp_linkedhull", enginePerformance);
            parent.getMutableStats().getMaxSpeed().modifyMult("swp_linkedhull", enginePerformance);
            parent.getMutableStats().getZeroFluxSpeedBoost().modifyMult("swp_linkedhull", enginePerformance);
        }

        float desiredRadius = 315f;
        if (parent.getHullSpec().getBaseHullId().contentEquals("swp_cathedral")) {
            float totalModuleCap = 0f;
            float moduleCapOverThreshold = 0f;
            for (ShipAPI child : children) {
                if (!child.isAlive()) {
                    continue;
                }

                switch (child.getHullSpec().getBaseHullId()) {
                    case "swp_cathedral_rear":
                        desiredRadius = Math.max(desiredRadius, 280f);
                        break;
                    case "swp_cathedral_gun_left":
                    case "swp_cathedral_gun_right":
                        desiredRadius = Math.max(desiredRadius, 283f);
                        break;
                    case "swp_cathedral_fighter_left":
                    case "swp_cathedral_fighter_right":
                        desiredRadius = Math.max(desiredRadius, 357f);
                        break;
                    case "swp_cathedral_front":
                        desiredRadius = Math.max(desiredRadius, 484f);
                        break;
                    default:
                        break;
                }

                totalModuleCap += child.getFluxTracker().getMaxFlux();
                if (child.getFluxLevel() > 0f) {
                    moduleCapOverThreshold += child.getFluxTracker().getMaxFlux() * 0.5f;
                } else if (child.getFluxLevel() >= 0.01f) {
                    moduleCapOverThreshold += child.getFluxTracker().getMaxFlux();
                }
            }

            if (totalModuleCap > 0f) {
                parent.getMutableStats().getZeroFluxSpeedBoost().modifyMult("swp_linkedhull2", 1f - (moduleCapOverThreshold / totalModuleCap));
            } else {
                parent.getMutableStats().getZeroFluxSpeedBoost().unmodify("swp_linkedhull2");
            }

            if (Math.abs(parent.getCollisionRadius() - desiredRadius) >= 1f) {
                parent.setCollisionRadius(desiredRadius);
            }
            if (parent.getShield() != null) {
                if (Math.abs(parent.getShield().getRadius() - desiredRadius) >= 1f) {
                    parent.getShield().setRadius(desiredRadius);
                }
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI parent = ship.getParentStation();
        if (parent != null) {
            advanceChild(ship, parent);
        }

        List<ShipAPI> children = ship.getChildModulesCopy();
        if (children != null && !children.isEmpty()) {
            advanceParent(ship, children);
        }
    }

    private static final Set<String> BLOCKED_OMNI = new HashSet<>();
    private static final Set<String> BLOCKED_OTHER = new HashSet<>();
    private static final Set<String> BLOCKED_CORE = new HashSet<>();
    private static final Set<String> BLOCKED_CATH = new HashSet<>();

    static {
        //BLOCKED_FRONT.add("frontemitter");
        //BLOCKED_FRONT.add("frontshield");

        /* Not designed for omni arcs */
        BLOCKED_OMNI.add("adaptiveshields");

        /* Modules don't move on their own */
        BLOCKED_OTHER.add("auxiliarythrusters");
        BLOCKED_OTHER.add("unstable_injector");
        BLOCKED_OTHER.add("SCY_lightArmor");
        /* Modules can't provide ECM/Nav */
        BLOCKED_OTHER.add("ecm");
        BLOCKED_OTHER.add("nav_relay");
        /* Logistics mods partially or completely don't apply on modules */
        BLOCKED_OTHER.add("operations_center");
        BLOCKED_OTHER.add("recovery_shuttles");
        BLOCKED_OTHER.add("additional_berthing");
        BLOCKED_OTHER.add("augmentedengines");
        BLOCKED_OTHER.add("auxiliary_fuel_tanks");
        BLOCKED_OTHER.add("efficiency_overhaul");
        BLOCKED_OTHER.add("expanded_cargo_holds");
        BLOCKED_OTHER.add("hiressensors");
        //BLOCKED_OTHER.add("insulatedengine"); // Niche use
        BLOCKED_OTHER.add("militarized_subsystems");
        //BLOCKED_OTHER.add("solar_shielding"); // Niche use
        BLOCKED_OTHER.add("surveying_equipment");
        BLOCKED_OTHER.add("converted_fighterbay");
        BLOCKED_OTHER.add("converted_hangar");
        BLOCKED_OTHER.add("expanded_deck_crew");
        BLOCKED_OTHER.add("TSC_converted_hangar");
        BLOCKED_OTHER.add("roider_fighterClamps");

        BLOCKED_CORE.add("SCY_lightArmor");

        BLOCKED_CATH.add("eis_aquila");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        switch (ship.getHullSpec().getBaseHullId()) {
            case "swp_cathedral_rear":
            case "swp_cathedral_gun_left":
            case "swp_cathedral_gun_right":
            case "swp_cathedral_fighter_left":
            case "swp_cathedral_fighter_right":
            case "swp_cathedral_front":
            case "swp_wall":
            case "swp_wall_left":
            case "swp_wall_right":
                for (String tmp : BLOCKED_OMNI) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        ship.getVariant().removeMod(tmp);
                        SWP_BlockedHullmodDisplayScript.showBlocked(ship);
                    }
                }
                break;
            default:
                break;
        }
        switch (ship.getHullSpec().getBaseHullId()) {
            case "swp_cathedral_rear":
            case "swp_cathedral_gun_left":
            case "swp_cathedral_gun_right":
            case "swp_cathedral_fighter_left":
            case "swp_cathedral_fighter_right":
            case "swp_cathedral_front":
            case "swp_wall_left":
            case "swp_wall_right":
                for (String tmp : BLOCKED_OTHER) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        ship.getVariant().removeMod(tmp);
                        SWP_BlockedHullmodDisplayScript.showBlocked(ship);
                    }
                }
                break;
            default:
                break;
        }
        switch (ship.getHullSpec().getBaseHullId()) {
            case "swp_cathedral":
            case "swp_wall":
                for (String tmp : BLOCKED_CORE) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        ship.getVariant().removeMod(tmp);
                        SWP_BlockedHullmodDisplayScript.showBlocked(ship);
                    }
                }
                break;
            default:
                break;
        }
        switch (ship.getHullSpec().getBaseHullId()) {
            case "swp_cathedral":
            case "swp_cathedral_rear":
            case "swp_cathedral_gun_left":
            case "swp_cathedral_gun_right":
            case "swp_cathedral_fighter_left":
            case "swp_cathedral_fighter_right":
            case "swp_cathedral_front":
                for (String tmp : BLOCKED_CATH) {
                    if (ship.getVariant().getHullMods().contains(tmp)) {
                        ship.getVariant().removeMod(tmp);
                        SWP_BlockedHullmodDisplayScript.showBlocked(ship);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getStat(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyMult(id, 0);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if (member == null) {
            return;
        }

        /* This whole thing crashes and burns due to a Starsector bug unless it's the player fleet */
        if (member.getFleetData() == null) {
            return;
        }
        if (member.getFleetData().getFleet() == null) {
            return;
        }
        if (!member.getFleetData().getFleet().isPlayerFleet()) {
            return;
        }

        ShipVariantAPI memberVariant = member.getVariant();
        if (memberVariant.isStockVariant()) {
            memberVariant = memberVariant.clone();
            memberVariant.setSource(VariantSource.REFIT);
            member.setVariant(memberVariant, false, false);
        }

        boolean changesMade = false;
        if (memberVariant.getStationModules() != null) {
            int index = 0;
            for (String slotId : memberVariant.getStationModules().keySet()) {
                ShipVariantAPI childVariant = memberVariant.getModuleVariant(slotId);
                if (childVariant == null) {
                    continue;
                }

                boolean childChangesMade = false;
                for (String modId : memberVariant.getHullMods()) {
                    HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                    if (modSpec.hasTag(Tags.HULLMOD_DMOD) && !childVariant.hasHullMod(modId)) {
                        if (childVariant.isStockVariant()) {
                            childVariant = childVariant.clone();
                            childVariant.setSource(VariantSource.REFIT);
                            childVariant.setOriginalVariant(null);
                            childVariant.setHullVariantId(childVariant.getHullVariantId() + "_" + index);
                        }

                        childVariant.addPermaMod(modId);
                        changesMade = true;
                        childChangesMade = true;
                    }
                }

                Collection<String> childHullMods = new ArrayList<>();
                childHullMods.addAll(childVariant.getHullMods());
                for (String modId : childHullMods) {
                    HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                    if (modSpec.hasTag(Tags.HULLMOD_DMOD) && !memberVariant.hasHullMod(modId)) {
                        if (childVariant.isStockVariant()) {
                            childVariant = childVariant.clone();
                            childVariant.setSource(VariantSource.REFIT);
                            childVariant.setOriginalVariant(null);
                            childVariant.setHullVariantId(childVariant.getHullVariantId() + "_" + index);
                        }

                        childVariant.removePermaMod(modId);
                        changesMade = true;
                        childChangesMade = true;
                    }
                }

                index++;
                if (childChangesMade) {
                    memberVariant.setModuleVariant(slotId, childVariant);
                }
            }
        }

        if (changesMade) {
            member.getFleetData().setSyncNeeded();
        }
    }
}
