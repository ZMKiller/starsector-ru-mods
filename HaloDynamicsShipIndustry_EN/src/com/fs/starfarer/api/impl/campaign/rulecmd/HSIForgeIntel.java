package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;

public class HSIForgeIntel extends ProductionReportIntel {

    public HSIForgeIntel(MarketAPI gatheringPoint, ProductionData data, int totalCost, int accrued, boolean noProductionThisMonth){
        super(gatheringPoint, data, totalCost, accrued, noProductionThisMonth);
    }
    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getFaction("HSI");
    }
}
