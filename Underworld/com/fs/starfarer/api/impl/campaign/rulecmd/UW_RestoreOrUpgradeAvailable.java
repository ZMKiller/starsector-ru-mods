package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.UW_Util;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class UW_RestoreOrUpgradeAvailable extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (UW_Util.getNonDHullId(member.getHullSpec()).startsWith("uw_tim")) {
                    Global.getSector().getMemoryWithoutUpdate().set("$uwTIMMember", member.getId());
                    break;
                }
            }
        }

        FleetMemberAPI targetMember = null;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            String targetId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.getId().contentEquals(targetId)) {
                    targetMember = member;
                    break;
                }
            }
        }

        boolean restore = restoreAvailable(ruleId, dialog, params, memoryMap);
        boolean upgrade = anyUpgradeAvailable(ruleId, dialog, params, memoryMap);
        if (restore && upgrade) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeStr", "Restore or upgrade", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeTooltip", "Explore options to clear d-mods from " + targetMember.getShipName() + " or to apply upgrades to her hull.", 0);
            }
        } else if (restore) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeStr", "Restore", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeTooltip", "Explore options to clear d-mods from " + targetMember.getShipName() + ".", 0);
            }
        } else if (upgrade) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeStr", "Upgrade", 0);
            if (targetMember != null) {
                memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreOrUpgradeTooltip", "Explore options to apply upgrades to " + targetMember.getShipName() + ".", 0);
            }
        }

        return restore || upgrade;
    }

    protected boolean restoreAvailable(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            boolean found = false;
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (UW_Util.getNonDHullId(member.getHullSpec()).startsWith("uw_tim")) {
                    Global.getSector().getMemoryWithoutUpdate().set("$uwTIMMember", member.getId());
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String targetId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(targetId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        int numDMods = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }
        memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreShip", targetMember.getShipName(), 0);
        if (numDMods > 1) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreModsStr", "one random", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreModsStr", "the last", 0);
        }
        long startingDMods = 7;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$uwStartingDMods")) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong("$uwStartingDMods");
        }
        if (numDMods == 0) {
            return false;
        }

        float needed = targetMember.getHullSpec().getBaseHull().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 7f / (float) startingDMods;
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        memoryMap.get(MemKeys.GLOBAL).set("$uwRestoreCostStr", Misc.getWithDGS(needed), 0);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if ((entity.getMarket() != null) && !entity.getMarket().hasSpaceport()) {
            return false;
        }

        RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
        if (!level.isAtWorst(RepLevel.SUSPICIOUS)) {
            return false;
        }

        return needed > 0;
    }

    protected boolean anyUpgradeAvailable(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String targetId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(targetId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        memoryMap.get(MemKeys.GLOBAL).set("$uwUpgradeShip", targetMember.getShipName(), 0);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if ((entity.getMarket() != null) && !entity.getMarket().hasSpaceport()) {
            return false;
        }

        RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
        if (!level.isAtWorst(RepLevel.SUSPICIOUS)) {
            return false;
        }

        EnumSet<UpgradeType> possibleUpgrades = UpgradeType.getPossibleUpgrades(targetMember.getVariant());
        for (UpgradeType upgrade : possibleUpgrades) {
            memoryMap.get(MemKeys.GLOBAL).set("$uwUpgrade" + upgrade.id + "CostStr", Misc.getWithDGS(upgrade.credits), 0);
            memoryMap.get(MemKeys.GLOBAL).set("$uwUpgrade" + upgrade.id + "SwapCostStr", Misc.getWithDGS(upgrade.swapCredits), 0);
        }

        return !possibleUpgrades.isEmpty();
    }

    public static enum UpgradeType {
        U1_FIXED_BORER_WINGS("U1", 50000, 0),
        U2_FIXED_OPERATIONS_CENTER("U2", 100000, 0),
        U3_FIXED_HELLFIRE_CANNON("U3", 200000, 0),
        U4_FIXED_WEAPON_SLOTS_25_OP("U4", 400000, 0),
        F1_CHOOSE_TERMINATOR_WINGS("F1", 500000, 100000),
        F2_CHOOSE_FLIGHT_DECKS_20_OP("F2", 500000, 100000),
        S1_CHOOSE_ADVANCED_TARGETING_CORE("S1", 1000000, 150000),
        S2_CHOOSE_MANEUVERABILITY("S2", 1000000, 150000),
        W1_CHOOSE_DIABLO_CANNON("W1", 2000000, 200000),
        W2_CHOOSE_WEAPON_SLOTS_20_OP("W2", 2000000, 200000),
        A1_CHOOSE_INFERNAL_MINEFIELD("A1", 3000000, 250000),
        A2_CHOOSE_INFERNAL_SHIELD("A2", 3000000, 250000);

        public final String id;
        public final int credits;
        public final int swapCredits;

        UpgradeType(String id, int credits, int swapCredits) {
            this.id = id;
            this.credits = credits;
            this.swapCredits = swapCredits;
        }

        public static EnumSet<UpgradeType> getCurrentUpgrades(ShipVariantAPI variant) {
            EnumSet<UpgradeType> upgrades = EnumSet.noneOf(UpgradeType.class);

            if (variant == null) {
                return upgrades;
            }

            String name = UW_Util.getNonDHullId(variant.getHullSpec());
            String token = "uw_tim_u";
            if (!name.startsWith(token)) {
                return upgrades;
            }

            if (name.startsWith(token + "1")) {
                token += "1";
                upgrades.add(U1_FIXED_BORER_WINGS);
            }
            if (name.startsWith(token + "2")) {
                token += "2";
                upgrades.add(U2_FIXED_OPERATIONS_CENTER);
            }
            if (name.startsWith(token + "3")) {
                token += "3";
                upgrades.add(U3_FIXED_HELLFIRE_CANNON);
            }
            if (name.startsWith(token + "4")) {
                token += "4";
                upgrades.add(U4_FIXED_WEAPON_SLOTS_25_OP);
            }

            if (name.startsWith(token + "_f1")) {
                token += "_f1";
                upgrades.add(F1_CHOOSE_TERMINATOR_WINGS);
            } else if (name.startsWith(token + "_f2")) {
                token += "_f2";
                upgrades.add(F2_CHOOSE_FLIGHT_DECKS_20_OP);
            } else {
                return upgrades;
            }

            if (name.startsWith(token + "_s1")) {
                token += "_s1";
                upgrades.add(S1_CHOOSE_ADVANCED_TARGETING_CORE);
            } else if (name.startsWith(token + "_s2")) {
                token += "_s2";
                upgrades.add(S2_CHOOSE_MANEUVERABILITY);
            } else {
                return upgrades;
            }

            if (name.startsWith(token + "_w1")) {
                token += "_w1";
                upgrades.add(W1_CHOOSE_DIABLO_CANNON);
            } else if (name.startsWith(token + "_w2")) {
                token += "_w2";
                upgrades.add(W2_CHOOSE_WEAPON_SLOTS_20_OP);
            } else {
                return upgrades;
            }

            if (name.startsWith(token + "_a1")) {
                upgrades.add(A1_CHOOSE_INFERNAL_MINEFIELD);
            } else if (name.startsWith(token + "_a2")) {
                upgrades.add(A2_CHOOSE_INFERNAL_SHIELD);
            }

            return upgrades;
        }

        public static EnumSet<UpgradeType> getPossibleUpgrades(ShipVariantAPI variant) {
            EnumSet<UpgradeType> upgrades = EnumSet.noneOf(UpgradeType.class);

            if (variant == null) {
                return upgrades;
            }

            String name = UW_Util.getNonDHullId(variant.getHullSpec());
            String token = "uw_tim";
            if (!name.startsWith(token)) {
                return upgrades;
            }

            upgrades.add(U1_FIXED_BORER_WINGS);
            upgrades.add(U2_FIXED_OPERATIONS_CENTER);
            upgrades.add(U3_FIXED_HELLFIRE_CANNON);
            upgrades.add(U4_FIXED_WEAPON_SLOTS_25_OP);
            if (name.startsWith(token + "_u")) {
                token += "_u";
            }
            if (name.startsWith(token + "1")) {
                token += "1";
                upgrades.remove(U1_FIXED_BORER_WINGS);
            }
            if (name.startsWith(token + "2")) {
                token += "2";
                upgrades.remove(U2_FIXED_OPERATIONS_CENTER);
            }
            if (name.startsWith(token + "3")) {
                token += "3";
                upgrades.remove(U3_FIXED_HELLFIRE_CANNON);
            }
            if (name.startsWith(token + "4")) {
                token += "4";
                upgrades.remove(U4_FIXED_WEAPON_SLOTS_25_OP);
            }
            if (!upgrades.isEmpty()) {
                return upgrades;
            }

            upgrades.add(F1_CHOOSE_TERMINATOR_WINGS);
            upgrades.add(F2_CHOOSE_FLIGHT_DECKS_20_OP);
            if (name.startsWith(token + "_f1")) {
                token += "_f1";
                upgrades.remove(F1_CHOOSE_TERMINATOR_WINGS);
            } else if (name.startsWith(token + "_f2")) {
                token += "_f2";
                upgrades.remove(F2_CHOOSE_FLIGHT_DECKS_20_OP);
            } else {
                return upgrades;
            }

            upgrades.add(S1_CHOOSE_ADVANCED_TARGETING_CORE);
            upgrades.add(S2_CHOOSE_MANEUVERABILITY);
            if (name.startsWith(token + "_s1")) {
                token += "_s1";
                upgrades.remove(S1_CHOOSE_ADVANCED_TARGETING_CORE);
            } else if (name.startsWith(token + "_s2")) {
                token += "_s2";
                upgrades.remove(S2_CHOOSE_MANEUVERABILITY);
            } else {
                return upgrades;
            }

            upgrades.add(W1_CHOOSE_DIABLO_CANNON);
            upgrades.add(W2_CHOOSE_WEAPON_SLOTS_20_OP);
            if (name.startsWith(token + "_w1")) {
                token += "_w1";
                upgrades.remove(W1_CHOOSE_DIABLO_CANNON);
            } else if (name.startsWith(token + "_w2")) {
                token += "_w2";
                upgrades.remove(W2_CHOOSE_WEAPON_SLOTS_20_OP);
            } else {
                return upgrades;
            }

            upgrades.add(A1_CHOOSE_INFERNAL_MINEFIELD);
            upgrades.add(A2_CHOOSE_INFERNAL_SHIELD);
            if (name.startsWith(token + "_a1")) {
                upgrades.remove(A1_CHOOSE_INFERNAL_MINEFIELD);
            } else if (name.startsWith(token + "_a2")) {
                upgrades.remove(A2_CHOOSE_INFERNAL_SHIELD);
            }

            return upgrades;
        }

        public static ShipHullSpecAPI getHullSpec(EnumSet<UpgradeType> upgrades) {
            if (upgrades == null) {
                return null;
            }

            String name = "uw_tim";

            if (upgrades.contains(U1_FIXED_BORER_WINGS) || upgrades.contains(U2_FIXED_OPERATIONS_CENTER)
                    || upgrades.contains(U3_FIXED_HELLFIRE_CANNON) || upgrades.contains(U4_FIXED_WEAPON_SLOTS_25_OP)) {
                name += "_u";
            }
            if (upgrades.contains(U1_FIXED_BORER_WINGS)) {
                name += "1";
            }
            if (upgrades.contains(U2_FIXED_OPERATIONS_CENTER)) {
                name += "2";
            }
            if (upgrades.contains(U3_FIXED_HELLFIRE_CANNON)) {
                name += "3";
            }
            if (upgrades.contains(U4_FIXED_WEAPON_SLOTS_25_OP)) {
                name += "4";
            }

            if (upgrades.contains(F1_CHOOSE_TERMINATOR_WINGS)) {
                name += "_f1";
            } else if (upgrades.contains(F2_CHOOSE_FLIGHT_DECKS_20_OP)) {
                name += "_f2";
            }

            if (upgrades.contains(S1_CHOOSE_ADVANCED_TARGETING_CORE)) {
                name += "_s1";
            } else if (upgrades.contains(S2_CHOOSE_MANEUVERABILITY)) {
                name += "_s2";
            }

            if (upgrades.contains(W1_CHOOSE_DIABLO_CANNON)) {
                name += "_w1";
            } else if (upgrades.contains(W2_CHOOSE_WEAPON_SLOTS_20_OP)) {
                name += "_w2";
            }

            if (upgrades.contains(A1_CHOOSE_INFERNAL_MINEFIELD)) {
                name += "_a1";
            } else if (upgrades.contains(A2_CHOOSE_INFERNAL_SHIELD)) {
                name += "_a2";
            }

            ShipHullSpecAPI hullSpec;
            try {
                hullSpec = Global.getSettings().getHullSpec(name);
            } catch (Exception e) {
                hullSpec = null;
            }

            return hullSpec;
        }

        public static UpgradeType getUpgrade(String id) {
            if (id == null) {
                return null;
            }

            for (UpgradeType upgrade : UpgradeType.values()) {
                if (upgrade.id.contentEquals(id)) {
                    return upgrade;
                }
            }

            return null;
        }

        public static int getCost(UpgradeType type, ShipVariantAPI variant) {
            if (variant == null) {
                return 0;
            }

            EnumSet<UpgradeType> currentUpgrades = getCurrentUpgrades(variant);
            switch (type) {
                case F1_CHOOSE_TERMINATOR_WINGS -> {
                    if (currentUpgrades.contains(UpgradeType.F2_CHOOSE_FLIGHT_DECKS_20_OP)) {
                        return type.swapCredits;
                    }
                }
                case F2_CHOOSE_FLIGHT_DECKS_20_OP -> {
                    if (currentUpgrades.contains(UpgradeType.F1_CHOOSE_TERMINATOR_WINGS)) {
                        return type.swapCredits;
                    }
                }
                case S1_CHOOSE_ADVANCED_TARGETING_CORE -> {
                    if (currentUpgrades.contains(UpgradeType.S2_CHOOSE_MANEUVERABILITY)) {
                        return type.swapCredits;
                    }
                }
                case S2_CHOOSE_MANEUVERABILITY -> {
                    if (currentUpgrades.contains(UpgradeType.S1_CHOOSE_ADVANCED_TARGETING_CORE)) {
                        return type.swapCredits;
                    }
                }
                case W1_CHOOSE_DIABLO_CANNON -> {
                    if (currentUpgrades.contains(UpgradeType.W2_CHOOSE_WEAPON_SLOTS_20_OP)) {
                        return type.swapCredits;
                    }
                }
                case W2_CHOOSE_WEAPON_SLOTS_20_OP -> {
                    if (currentUpgrades.contains(UpgradeType.W1_CHOOSE_DIABLO_CANNON)) {
                        return type.swapCredits;
                    }
                }
                case A1_CHOOSE_INFERNAL_MINEFIELD -> {
                    if (currentUpgrades.contains(UpgradeType.A2_CHOOSE_INFERNAL_SHIELD)) {
                        return type.swapCredits;
                    }
                }
                case A2_CHOOSE_INFERNAL_SHIELD -> {
                    if (currentUpgrades.contains(UpgradeType.A1_CHOOSE_INFERNAL_MINEFIELD)) {
                        return type.swapCredits;
                    }
                }
                default -> {
                }
            }

            return type.credits;
        }
    }
}
