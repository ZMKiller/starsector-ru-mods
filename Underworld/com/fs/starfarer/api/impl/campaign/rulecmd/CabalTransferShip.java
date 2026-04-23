package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * CabalTransferShip
 */
public class CabalTransferShip extends BaseCommandPlugin {

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

        String uuid = params.get(0).getString(memoryMap);

        FleetMemberAPI match = null;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(uuid)) {
                match = member;
            }
        }
        if (match == null) {
            return false;
        }

        if (match.getCaptain() != null) {
            if (match.getCaptain().isAICore() && (fleet.getCargo() != null)) {
                fleet.getCargo().addCommodity(match.getCaptain().getAICoreId(), 1);
            }
        }
        match.setCaptain(null);
        Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(match);
        fleet.getFleetData().addFleetMember(match);
        Global.getSector().getPlayerFleet().forceSync();
        Global.getSector().getPlayerFleet().updateCounts();
        fleet.forceSync();
        fleet.updateCounts();
        return true;
    }
}
