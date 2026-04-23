package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.intel.UW_StarlightGala;
import java.util.List;
import java.util.Map;

// UW_UpdateGalaFacts
public class UW_UpdateGalaFacts extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        if (dialog == null) {
            return false;
        }
        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI campaignFleetAPI) {
            fleet = campaignFleetAPI;
        } else {
            return false;
        }
        FleetMemberAPI palace;
        if ((fleet.getFlagship() != null) && fleet.getFlagship().getHullId().contains("uw_palace")) {
            palace = fleet.getFlagship();
        } else {
            return false;
        }

        memoryMap.get(MemKeys.LOCAL).set("$uwEntryFeeDGS", Misc.getWithDGS(UW_StarlightGala.ENTRY_FEE), 0);
        memoryMap.get(MemKeys.LOCAL).set("$uwStoryEntryFeeDGS", Misc.getWithDGS(UW_StarlightGala.STORY_ENTRY_FEE), 0);
        memoryMap.get(MemKeys.LOCAL).set("$uwEntryFee", UW_StarlightGala.ENTRY_FEE, 0);
        memoryMap.get(MemKeys.LOCAL).set("$uwStoryEntryFee", UW_StarlightGala.STORY_ENTRY_FEE, 0);
        memoryMap.get(MemKeys.LOCAL).set("$uwPalaceShipName", palace.getShipName(), 0);

        return true;
    }
}
