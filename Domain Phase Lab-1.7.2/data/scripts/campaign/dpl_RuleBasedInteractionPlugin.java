package data.scripts.campaign;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;

import data.scripts.crisis.dpl_ActivityCause;
import data.scripts.crisis.dpl_HostileActivityFactor;

public class dpl_RuleBasedInteractionPlugin extends BaseCampaignPlugin {
	public String getId() {
		return "dpl_RuleBasedInteractionPlugin";
	}
	
	public boolean isTransient() {
	    return false;
	}
	
    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
		if ((interactionTarget instanceof PlanetAPI) && interactionTarget.hasTag("dpl_HasInteractionDialog")) {
			return new PluginPick<InteractionDialogPlugin>(new RuleBasedInteractionDialogPluginImpl(), PickPriority.MOD_SPECIFIC);
		}
		return null;
	}
}
