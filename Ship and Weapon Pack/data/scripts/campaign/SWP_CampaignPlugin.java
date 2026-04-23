package data.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.scripts.SWPModPlugin;

public class SWP_CampaignPlugin extends BaseCampaignPlugin {

    @Override
    public String getId() {
        return "SWP_CampaignPlugin";
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if ((interactionTarget instanceof CampaignFleetAPI) && interactionTarget.getFaction().getId().contentEquals("famous_bounty")) {
            if (SWPModPlugin.isExerelin) {
                return null;    // Nex takes care of this
            } else {
                return new PluginPick<>(new SWP_IBBInteractionDialogPlugin(), PickPriority.MOD_SPECIFIC);
            }
        }
        return null;
    }
}
