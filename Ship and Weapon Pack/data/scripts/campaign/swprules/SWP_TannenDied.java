package data.scripts.campaign.swprules;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.intel.missions.SWP_BuriedTreasure;
import java.util.List;
import java.util.Map;

/**
 * SWP_TannenDied
 */
public class SWP_TannenDied extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.getVariant().hasTag(SWP_BuriedTreasure.TANNEN_SHIP)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
