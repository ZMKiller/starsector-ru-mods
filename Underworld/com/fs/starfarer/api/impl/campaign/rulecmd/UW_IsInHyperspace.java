package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// UW_IsInHyperspace
public class UW_IsInHyperspace extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return false;
        }
        return fleet.isInHyperspace();
    }
}
