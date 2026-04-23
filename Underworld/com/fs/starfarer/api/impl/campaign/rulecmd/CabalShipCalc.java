package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import static com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickExtortionMethod.extortionAmount;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

/**
 * CabalShipCalc
 */
public class CabalShipCalc extends BaseCommandPlugin {

    public static float bestShipValue(CampaignFleetAPI fleet, float maxValue) {
        float bestShipValue = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!shipValidToTake(member)) {
                continue;
            }
            float value = member.getBaseValue();

            if (value > bestShipValue && value <= maxValue) {
                bestShipValue = value;
            }
        }
        return bestShipValue;
    }

    public static float totalShipsValue(CampaignFleetAPI fleet) {
        float totalShipValue = 0f;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!shipValidToTake(member)) {
                continue;
            }
            float value = member.getBaseValue();
            totalShipValue += value;
        }
        return totalShipValue;
    }

    public static int usableShips(CampaignFleetAPI fleet) {
        int usableShips = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (member.isFighterWing()) {
                continue;
            }
            if (!shipValidToTake(member)) {
                continue;
            }
            usableShips++;
        }
        return usableShips;
    }

    private static boolean shipValidToTake(FleetMemberAPI member) {
        if (member.getVariant().hasTag(Tags.SHIP_CAN_NOT_SCUTTLE)) {
            return false;
        }
        return !Misc.isUnremovable(member.getCaptain());
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI campaignFleetAPI) {
            fleet = campaignFleetAPI;
        } else {
            return false;
        }

        float totalCreditsValue = totalShipsValue(Global.getSector().getPlayerFleet());
        double valueToTake = extortionAmount(totalCreditsValue);
        float powerLevel = UW_Util.calculatePowerLevel(fleet);
        valueToTake = Math.min(valueToTake, powerLevel * 1000.0);
        double minimumToTake = Math.min(valueToTake, powerLevel * 350.0);

        float thresholdForMultipleChoice = Math.max((float) minimumToTake, totalCreditsValue / 2f);
        FleetMemberAPI bestShip = null;
        float bestShipValue = 0f;
        int validShips = 0;
        List<FleetMemberAPI> multipleChoice = new ArrayList<>(Global.getSector().getPlayerFleet().getFleetData().getNumMembers());
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (!shipValidToTake(member)) {
                continue;
            }
            validShips++;

            float value = member.getBaseValue();
            if (value > valueToTake) {
                continue;
            }

            if (value > bestShipValue) {
                bestShip = member;
                bestShipValue = value;
            }
            if (value > thresholdForMultipleChoice) {
                multipleChoice.add(member);
            }
        }
        if (validShips <= 1) {
            return false;
        }

        if (!multipleChoice.isEmpty()) {
            int index = MathUtils.getRandomNumberInRange(0, multipleChoice.size() - 1);

            bestShipValue = multipleChoice.get(index).getBaseValue();
            bestShip = multipleChoice.get(index);
        }
        if (bestShip == null) {
            return false;
        }

        float repImpact;
        if (bestShipValue <= 20000f) {
            repImpact = 0.02f;
        } else if (bestShipValue <= 50000f) {
            repImpact = 0.04f;
        } else if (bestShipValue <= 90000f) {
            repImpact = 0.06f;
        } else if (bestShipValue <= 140000f) {
            repImpact = 0.08f;
        } else if (bestShipValue <= 200000f) {
            repImpact = 0.1f;
        } else if (bestShipValue <= 270000f) {
            repImpact = 0.12f;
        } else if (bestShipValue <= 350000f) {
            repImpact = 0.14f;
        } else if (bestShipValue <= 440000f) {
            repImpact = 0.16f;
        } else if (bestShipValue <= 540000f) {
            repImpact = 0.18f;
        } else if (bestShipValue <= 650000f) {
            repImpact = 0.2f;
        } else if (bestShipValue <= 770000f) {
            repImpact = 0.22f;
        } else if (bestShipValue <= 900000f) {
            repImpact = 0.24f;
        } else if (bestShipValue <= 1040000f) {
            repImpact = 0.26f;
        } else if (bestShipValue <= 1190000f) {
            repImpact = 0.28f;
        } else {
            repImpact = 0.30f;
        }
        float repNegImpact = repImpact * 0.5f;
        switch (Global.getSector().getFaction("cabal").getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
                repImpact = repImpact * 1.5f;
                repNegImpact = repNegImpact / 1.5f;
                break;
            case HOSTILE:
                break;
            case INHOSPITABLE:
            case SUSPICIOUS:
                repImpact = Math.max(0.01f, repImpact - 0.01f);
                repNegImpact = repNegImpact + 0.01f;
                break;
            case NEUTRAL:
                repImpact = Math.max(0.01f, repImpact - 0.02f);
                repNegImpact = repNegImpact + 0.02f;
                break;
            case FAVORABLE:
                repImpact = Math.max(0.01f, repImpact - 0.03f);
                repNegImpact = repNegImpact + 0.03f;
                break;
            case WELCOMING:
                repImpact = Math.max(0.01f, repImpact - 0.04f);
                repNegImpact = repNegImpact + 0.04f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                repImpact = Math.max(0.01f, repImpact - 0.05f);
                repNegImpact = repNegImpact + 0.05f;
                break;
        }

        memoryMap.get(MemKeys.LOCAL).set("$Cabal_ship_name", bestShip.getShipName(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_ship_hull", bestShip.getHullSpec().getHullName(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_ship_hull_full", bestShip.getVariant().getFullDesignationWithHullName(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_ship_uuid", bestShip.getId(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_ship_is_fighter", bestShip.isFighterWing(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repImpact", repImpact, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repNegImpact", -repNegImpact, 0);
        return true;
    }
}
