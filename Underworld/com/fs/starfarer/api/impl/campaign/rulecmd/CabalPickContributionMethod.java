package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * CabalPickContributionMethod
 */
public class CabalPickContributionMethod extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(CabalPickContributionMethod.class);

    public static boolean playerHasAbilityToPayContribution(CampaignFleetAPI fleet) {
        List<String> extortionMethods = new ArrayList<>(2);

        float powerLevel = UW_Util.calculatePowerLevel(fleet);

        float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float lowerThreshold = Math.min(100000f, powerLevel * 600f);
        if (credits > lowerThreshold) {
            extortionMethods.add("tithe");
        }

        lowerThreshold = Math.min(12500f, powerLevel * 600f);
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float cargoValue = CabalCargoCalc.valueOfBiggestStack(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        if (cargoValue > lowerThreshold) {
            extortionMethods.add("cargo");
        }

        return !extortionMethods.isEmpty();
    }

    public static float playerNetWorth() {
        float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCargoValue = CabalCargoCalc.totalCargoValue(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        float totalWeaponsValue = CabalWeaponCalc.totalWeaponsValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalFightersValue = CabalFighterCalc.totalFightersValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalShipsValue = CabalShipCalc.totalShipsValue(Global.getSector().getPlayerFleet());

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);

        return credits + totalCargoValue + totalWeaponsValue + totalFightersValue + totalShipsValue + profit;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI campaignFleetAPI) {
            fleet = campaignFleetAPI;
        } else {
            return false;
        }

        WeightedRandomPicker<String> extortionMethods = new WeightedRandomPicker<>();
        float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCargoValue = CabalCargoCalc.totalCargoValue(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        float totalWeaponsValue = CabalWeaponCalc.totalWeaponsValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalFightersValue = CabalFighterCalc.totalFightersValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalShipsValue = CabalShipCalc.totalShipsValue(Global.getSector().getPlayerFleet());

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);

        float netWorth = playerNetWorth();
        float targetContribution = (float) CabalPickExtortionMethod.extortionAmount(netWorth);

        float powerLevel = UW_Util.calculatePowerLevel(fleet);

        log.info("Seen credit value of " + credits);
        log.info("Calculated cargo value of " + totalCargoValue);
        log.info("Calculated loose weapons value of " + totalWeaponsValue);
        log.info("Calculated loose fighters value of " + totalFightersValue);
        log.info("Calculated ships value of " + totalShipsValue);
        log.info("Seen monthly profit of " + profit);
        log.info("Evaluated net worth at " + netWorth);
        log.info("Targeting extortion value at " + targetContribution);
        log.info("Evaluated Cabal fleet power level of " + powerLevel);

        // de-emphasize contribution for poor players
        if ((targetContribution < 25000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }
        if ((targetContribution < 50000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }
        if ((targetContribution < 100000f) && (Math.random() > 0.5)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }

        float lowerThreshold = Math.min(100000f, powerLevel * 600f);
        float upperThreshold = powerLevel * 3500f;
        if (credits > lowerThreshold) {
            float weight = (float) Math.sqrt(Math.min(credits, upperThreshold) / targetContribution) * 1.5f;
            extortionMethods.add("tithe", weight);
            log.info("Tithe extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(12500f, powerLevel * 600f);
        upperThreshold = powerLevel * 2500f;
        float cargoValue = CabalCargoCalc.valueOfBiggestStack(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        log.info("Calculated biggest cargo stack value of " + cargoValue);
        if (cargoValue > lowerThreshold) {
            float weight = (float) Math.sqrt(Math.min(cargoValue, upperThreshold) / targetContribution);
            extortionMethods.add("cargo", weight);
            log.info("Cargo extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(25f, powerLevel * 1.5f);
        float weaponFanciness = CabalWeaponCalc.bestWeaponFanciness(Global.getSector().getPlayerFleet(), shieldedFraction);
        log.info("Calculated best weapon fanciness of " + weaponFanciness);
        if (weaponFanciness > lowerThreshold) {
            float weight = (float) Math.sqrt(weaponFanciness / (targetContribution / 500f));
            extortionMethods.add("weapon", weight);
            log.info("Weapon extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(20f, powerLevel);
        float fighterFanciness = CabalFighterCalc.bestFighterFanciness(Global.getSector().getPlayerFleet(), shieldedFraction);
        log.info("Calculated best fighter fanciness of " + weaponFanciness);
        if (fighterFanciness > lowerThreshold) {
            float weight = (float) Math.sqrt(fighterFanciness / (targetContribution / 500f));
            extortionMethods.add("fighter", weight);
            log.info("Fighter extortion method at weight " + weight);
        }

        lowerThreshold = Math.min(12500f, powerLevel * 350f);
        upperThreshold = powerLevel * 1000f;
        float shipValue = CabalShipCalc.bestShipValue(Global.getSector().getPlayerFleet(), upperThreshold);
        log.info("Calculated best ship value of " + shipValue);
        if (shipValue > lowerThreshold && CabalShipCalc.usableShips(Global.getSector().getPlayerFleet()) > 1) {
            float weight = (float) Math.sqrt(Math.min(shipValue, upperThreshold) / targetContribution) * 0.25f;
            extortionMethods.add("ship", weight);
            log.info("Ship extortion method at weight " + weight);
        }

        if (extortionMethods.isEmpty()) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", "none", 0);
            return false;
        }

        memoryMap.get(MemKeys.LOCAL).set("$Cabal_extortionMethod", extortionMethods.pick(), 7);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_netWorthString", Misc.getDGSCredits(
                UW_Util.roundToSignificantFiguresLong(netWorth, 4)), 0);
        return true;
    }
}
