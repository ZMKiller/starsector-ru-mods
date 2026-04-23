package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class UW_StyxRepairAvailable extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        float needed = playerFleet.getLogistics().getTotalRepairAndRecoverySupplyCost();
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }
        memoryMap.get(MemKeys.GLOBAL).set("$repairSupplyCost", (int) needed, 0);

        return true;
    }
}
