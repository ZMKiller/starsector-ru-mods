package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CabalNormalTalk
 */
public class CabalNormalTalk extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        List<String> marketList = new ArrayList<>(10);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.hasCondition("cabal_influence")) {
                marketList.add(market.getName());
            }
        }

        float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCargoValue = CabalCargoCalc.totalCargoValue(Global.getSector().getPlayerFleet().getCargo(), shieldedFraction);
        float totalWeaponsValue = CabalWeaponCalc.totalWeaponsValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalFightersValue = CabalFighterCalc.totalFightersValue(Global.getSector().getPlayerFleet(), false, shieldedFraction);
        float totalShipsValue = CabalShipCalc.totalShipsValue(Global.getSector().getPlayerFleet());

        MonthlyReport report = SharedData.getData().getCurrentReport();
        report.computeTotals();
        float profit = Math.max(0f, report.getRoot().totalIncome - report.getRoot().totalUpkeep);

        float netWorth = credits + totalCargoValue + totalWeaponsValue + totalFightersValue + totalShipsValue + profit;

        String marketListStr = Misc.getAndJoined(marketList.toArray(String[]::new));
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_markets", marketListStr, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_netWorthString", Misc.getDGSCredits(UW_Util.roundToSignificantFiguresLong(netWorth, 4)), 0);
        return true;
    }
}
