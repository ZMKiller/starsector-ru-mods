package data.scripts.campaign.swprules;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SWP_AddDefeatTrigger extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) {
            return false;
        }

        String trigger = params.get(0).getString(memoryMap);
        CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
        Misc.addDefeatTrigger(fleet, trigger);

        return true;
    }
}
