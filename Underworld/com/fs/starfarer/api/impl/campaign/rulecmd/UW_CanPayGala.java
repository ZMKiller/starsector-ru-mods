package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.intel.UW_StarlightGala;
import java.util.List;
import java.util.Map;

// UW_CanPayGala <storyOption>
public class UW_CanPayGala extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        float needed = UW_StarlightGala.ENTRY_FEE;
        if (!params.isEmpty() && params.get(0).getBoolean(memoryMap)) {
            needed = UW_StarlightGala.STORY_ENTRY_FEE;
        }
        float credits = playerFleet.getCargo().getCredits().get();
        return credits >= needed;
    }
}
