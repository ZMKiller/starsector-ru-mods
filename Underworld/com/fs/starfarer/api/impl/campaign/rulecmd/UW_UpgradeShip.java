package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.rulecmd.UW_RestoreOrUpgradeAvailable.UpgradeType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

// UW_UpgradeShip <upgradeId>
public class UW_UpgradeShip extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String timId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(timId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        String upgradeId = params.get(0).getString(memoryMap);
        UpgradeType upgrade = UpgradeType.getUpgrade(upgradeId);
        if (upgrade == null) {
            return false;
        }

        float needed = UpgradeType.getCost(upgrade, targetMember.getVariant());
        boolean swapped = false;
        EnumSet<UpgradeType> currentUpgrades = UpgradeType.getCurrentUpgrades(targetMember.getVariant());
        currentUpgrades.add(upgrade);
        switch (upgrade) {
            case F1_CHOOSE_TERMINATOR_WINGS -> {
                if (currentUpgrades.contains(UpgradeType.F2_CHOOSE_FLIGHT_DECKS_20_OP)) {
                    currentUpgrades.remove(UpgradeType.F2_CHOOSE_FLIGHT_DECKS_20_OP);
                    swapped = true;
                }
            }
            case F2_CHOOSE_FLIGHT_DECKS_20_OP -> {
                if (currentUpgrades.contains(UpgradeType.F1_CHOOSE_TERMINATOR_WINGS)) {
                    currentUpgrades.remove(UpgradeType.F1_CHOOSE_TERMINATOR_WINGS);
                    swapped = true;
                }
            }
            case S1_CHOOSE_ADVANCED_TARGETING_CORE -> {
                if (currentUpgrades.contains(UpgradeType.S2_CHOOSE_MANEUVERABILITY)) {
                    currentUpgrades.remove(UpgradeType.S2_CHOOSE_MANEUVERABILITY);
                    swapped = true;
                }
            }
            case S2_CHOOSE_MANEUVERABILITY -> {
                if (currentUpgrades.contains(UpgradeType.S1_CHOOSE_ADVANCED_TARGETING_CORE)) {
                    currentUpgrades.remove(UpgradeType.S1_CHOOSE_ADVANCED_TARGETING_CORE);
                    swapped = true;
                }
            }
            case W1_CHOOSE_DIABLO_CANNON -> {
                if (currentUpgrades.contains(UpgradeType.W2_CHOOSE_WEAPON_SLOTS_20_OP)) {
                    currentUpgrades.remove(UpgradeType.W2_CHOOSE_WEAPON_SLOTS_20_OP);
                    swapped = true;
                }
            }
            case W2_CHOOSE_WEAPON_SLOTS_20_OP -> {
                if (currentUpgrades.contains(UpgradeType.W1_CHOOSE_DIABLO_CANNON)) {
                    currentUpgrades.remove(UpgradeType.W1_CHOOSE_DIABLO_CANNON);
                    swapped = true;
                }
            }
            case A1_CHOOSE_INFERNAL_MINEFIELD -> {
                if (currentUpgrades.contains(UpgradeType.A2_CHOOSE_INFERNAL_SHIELD)) {
                    currentUpgrades.remove(UpgradeType.A2_CHOOSE_INFERNAL_SHIELD);
                    swapped = true;
                }
            }
            case A2_CHOOSE_INFERNAL_SHIELD -> {
                if (currentUpgrades.contains(UpgradeType.A1_CHOOSE_INFERNAL_MINEFIELD)) {
                    currentUpgrades.remove(UpgradeType.A1_CHOOSE_INFERNAL_MINEFIELD);
                    swapped = true;
                }
            }
            default -> {
            }
        }

        ShipHullSpecAPI hullSpec = UpgradeType.getHullSpec(currentUpgrades);
        MutableCharacterStatsAPI charStats = null;
        if (Global.getSector().getPlayerPerson() != null) {
            charStats = Global.getSector().getPlayerPerson().getStats();
        }

        if (upgrade == UpgradeType.U2_FIXED_OPERATIONS_CENTER) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.OPERATIONS_CENTER))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.OPERATIONS_CENTER), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.OPERATIONS_CENTER);
            } else if (targetMember.getVariant().hasHullMod(HullMods.OPERATIONS_CENTER)) {
                targetMember.getVariant().removeMod(HullMods.OPERATIONS_CENTER);
            }
        }
        if (upgrade == UpgradeType.U4_FIXED_WEAPON_SLOTS_25_OP) {
            /* Large slot does not support small weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
        }
        if ((upgrade == UpgradeType.F1_CHOOSE_TERMINATOR_WINGS) && swapped) {
            /* Clear disappearing modular flight decks */
            for (String wingId : targetMember.getVariant().getFittedWings()) {
                playerFleet.getCargo().addFighters(wingId, 1);
            }
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);

            /* Avoid over-OP */
            int OP = targetMember.getVariant().computeOPCost(charStats);
            int maxOP = targetMember.getVariant().getHullSpec().getOrdnancePoints(charStats) - 10;
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxCapacitors(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxVents(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().clearHullMods();
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                for (String slotId : targetMember.getVariant().getFittedWeaponSlots()) {
                    WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec(slotId);
                    if (weapon != null) {
                        playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
                    }
                }
                targetMember.getVariant().clear();
            }
        }
        if (upgrade == UpgradeType.S1_CHOOSE_ADVANCED_TARGETING_CORE) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.DEDICATED_TARGETING_CORE))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.DEDICATED_TARGETING_CORE), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.DEDICATED_TARGETING_CORE);
            } else if (targetMember.getVariant().hasHullMod(HullMods.DEDICATED_TARGETING_CORE)) {
                targetMember.getVariant().removeMod(HullMods.DEDICATED_TARGETING_CORE);
            }
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.INTEGRATED_TARGETING_UNIT))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.INTEGRATED_TARGETING_UNIT), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.INTEGRATED_TARGETING_UNIT);
            } else if (targetMember.getVariant().hasHullMod(HullMods.INTEGRATED_TARGETING_UNIT)) {
                targetMember.getVariant().removeMod(HullMods.INTEGRATED_TARGETING_UNIT);
            }
        }
        if (upgrade == UpgradeType.S2_CHOOSE_MANEUVERABILITY) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.AUXILIARY_THRUSTERS))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.AUXILIARY_THRUSTERS), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.AUXILIARY_THRUSTERS);
            } else if (targetMember.getVariant().hasHullMod(HullMods.AUXILIARY_THRUSTERS)) {
                targetMember.getVariant().removeMod(HullMods.AUXILIARY_THRUSTERS);
            }
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.UNSTABLE_INJECTOR))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.UNSTABLE_INJECTOR), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.UNSTABLE_INJECTOR);
            } else if (targetMember.getVariant().hasHullMod(HullMods.UNSTABLE_INJECTOR)) {
                targetMember.getVariant().removeMod(HullMods.UNSTABLE_INJECTOR);
            }
        }
        if ((upgrade == UpgradeType.W1_CHOOSE_DIABLO_CANNON) && swapped) {
            /* Ballistic slots do not support missile weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getType() == WeaponType.MISSILE)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0010");
            if ((weapon != null) && (weapon.getType() == WeaponType.MISSILE)) {
                targetMember.getVariant().clearSlot("WS0010");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Medium slots do not support large weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0004");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.LARGE)) {
                targetMember.getVariant().clearSlot("WS0004");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0007");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.LARGE)) {
                targetMember.getVariant().clearSlot("WS0007");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Ballistic slots do not support energy weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0005");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0005");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0006");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0006");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0008");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0008");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0009");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0009");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0013");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0013");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0014");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0014");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0020");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0020");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0021");
            if ((weapon != null) && (weapon.getType() == WeaponType.ENERGY)) {
                targetMember.getVariant().clearSlot("WS0021");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Ballistic slots do not support missile/energy weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0018");
            if ((weapon != null) && ((weapon.getType() == WeaponType.MISSILE) || (weapon.getType() == WeaponType.ENERGY))) {
                targetMember.getVariant().clearSlot("WS0018");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0019");
            if ((weapon != null) && ((weapon.getType() == WeaponType.MISSILE) || (weapon.getType() == WeaponType.ENERGY))) {
                targetMember.getVariant().clearSlot("WS0019");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Avoid over-OP */
            int OP = targetMember.getVariant().computeOPCost(charStats);
            int maxOP = targetMember.getVariant().getHullSpec().getOrdnancePoints(charStats) - 20;
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxCapacitors(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().setNumFluxVents(0);
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                targetMember.getVariant().clearHullMods();
                OP = targetMember.getVariant().computeOPCost(charStats);
            }
            if (OP > maxOP) {
                for (String slotId : targetMember.getVariant().getFittedWeaponSlots()) {
                    weapon = targetMember.getVariant().getWeaponSpec(slotId);
                    if (weapon != null) {
                        playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
                    }
                }
                for (String wingId : targetMember.getVariant().getFittedWings()) {
                    playerFleet.getCargo().addFighters(wingId, 1);
                }
                targetMember.getVariant().clear();
            }
        }
        if (upgrade == UpgradeType.W2_CHOOSE_WEAPON_SLOTS_20_OP) {
            /* Large composite slot does not support medium weapons */
            WeaponSpecAPI weapon = targetMember.getVariant().getWeaponSpec("WS0003");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.MEDIUM)) {
                targetMember.getVariant().clearSlot("WS0003");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Large slots do not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0004");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0004");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0007");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0007");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }

            /* Medium hybrid/composite slots do not support small weapons */
            weapon = targetMember.getVariant().getWeaponSpec("WS0005");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0005");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0006");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0006");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0008");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0008");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0009");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0009");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
            weapon = targetMember.getVariant().getWeaponSpec("WS0010");
            if ((weapon != null) && (weapon.getSize() == WeaponSize.SMALL)) {
                targetMember.getVariant().clearSlot("WS0010");
                playerFleet.getCargo().addWeapons(weapon.getWeaponId(), 1);
            }
        }
        if ((upgrade == UpgradeType.A1_CHOOSE_INFERNAL_MINEFIELD) && swapped) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.FRONT_SHIELD_CONVERSION))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.FRONT_SHIELD_CONVERSION), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.FRONT_SHIELD_CONVERSION);
            } else if (targetMember.getVariant().hasHullMod(HullMods.FRONT_SHIELD_CONVERSION)) {
                targetMember.getVariant().removeMod(HullMods.FRONT_SHIELD_CONVERSION);
            }
        }
        if (upgrade == UpgradeType.A2_CHOOSE_INFERNAL_SHIELD) {
            if (Misc.isSpecialMod(targetMember.getVariant(), Global.getSettings().getHullModSpec(HullMods.OMNI_SHIELD_CONVERSION))) {
                float bonusXP = 1f - Misc.getBuildInBonusXP(Global.getSettings().getHullModSpec(HullMods.OMNI_SHIELD_CONVERSION), targetMember.getHullSpec().getHullSize());
                Global.getSector().getPlayerStats().addStoryPoints(1);
                Global.getSector().getPlayerStats().spendStoryPoints(1, false, dialog.getTextPanel(), false, bonusXP, "Refunded built-in hullmod");
                dialog.getTextPanel().addPara("Refunded s-mod.");
                targetMember.getVariant().removePermaMod(HullMods.OMNI_SHIELD_CONVERSION);
            } else if (targetMember.getVariant().hasHullMod(HullMods.OMNI_SHIELD_CONVERSION)) {
                targetMember.getVariant().removeMod(HullMods.OMNI_SHIELD_CONVERSION);
            }
        }

        targetMember.getVariant().setHullSpecAPI(hullSpec);
        if (upgrade == UpgradeType.U1_FIXED_BORER_WINGS) {
            targetMember.getVariant().removeMod("uw_remove_decks");
            targetMember.getVariant().setWingId(0, "borer_wing");
            targetMember.getVariant().setWingId(1, "borer_wing");
        }
        if (upgrade == UpgradeType.U2_FIXED_OPERATIONS_CENTER) {
            targetMember.getVariant().addMod(HullMods.OPERATIONS_CENTER);
        }
        if (upgrade == UpgradeType.U3_FIXED_HELLFIRE_CANNON) {
            targetMember.getVariant().addWeapon("WS0001", "uw_hellfire");
        }
        if (upgrade == UpgradeType.F1_CHOOSE_TERMINATOR_WINGS) {
            targetMember.getVariant().setWingId(0, "terminator_wing");
            targetMember.getVariant().setWingId(1, "terminator_wing");
        }
        if (upgrade == UpgradeType.F2_CHOOSE_FLIGHT_DECKS_20_OP) {
            targetMember.getVariant().setWingId(0, null);
            targetMember.getVariant().setWingId(1, null);
        }
        if (upgrade == UpgradeType.S1_CHOOSE_ADVANCED_TARGETING_CORE) {
            if (swapped) {
                targetMember.getVariant().removeMod(HullMods.AUXILIARY_THRUSTERS);
                targetMember.getVariant().removeMod(HullMods.UNSTABLE_INJECTOR);
            }
            targetMember.getVariant().addMod(HullMods.ADVANCED_TARGETING_CORE);
        }
        if (upgrade == UpgradeType.S2_CHOOSE_MANEUVERABILITY) {
            if (swapped) {
                targetMember.getVariant().removeMod(HullMods.ADVANCED_TARGETING_CORE);
            }
            targetMember.getVariant().addMod(HullMods.AUXILIARY_THRUSTERS);
            targetMember.getVariant().addMod(HullMods.UNSTABLE_INJECTOR);
        }
        if (upgrade == UpgradeType.W1_CHOOSE_DIABLO_CANNON) {
            targetMember.getVariant().addWeapon("WS0001", "uw_diablo");
        }
        if (upgrade == UpgradeType.W2_CHOOSE_WEAPON_SLOTS_20_OP) {
            if (swapped) {
                targetMember.getVariant().addWeapon("WS0001", "uw_hellfire");
            }
        }
        if (upgrade == UpgradeType.A1_CHOOSE_INFERNAL_MINEFIELD) {
            if (swapped) {
                targetMember.getVariant().removeMod("uw_infernal_shield");
            }
            targetMember.getVariant().addMod("uw_minefield");
        }
        if (upgrade == UpgradeType.A2_CHOOSE_INFERNAL_SHIELD) {
            if (swapped) {
                targetMember.getVariant().removeMod("uw_minefield");
            }
            targetMember.getVariant().addMod("uw_infernal_shield");
        }
        targetMember.setStatUpdateNeeded(true);

        if (needed > 0) {
            playerFleet.getCargo().getCredits().subtract(needed);
            MemoryAPI memory = Global.getSector().getCharacterData().getMemory();
            memory.set("$credits", (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get(), 0);
            memory.set("$creditsStr", Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), 0);
        }

        return true;
    }
}
