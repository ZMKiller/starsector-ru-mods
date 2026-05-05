package data.campaign.HSITTMission;

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAFID;
import data.kit.HSII18nUtil;

public class HSITTMissionCombat extends HSISAFID {
    public HSITTMissionCombat(FleetInteractionDialogPluginImpl.FIDConfig params) {
        super(params);
        this.shownTooLargeToRetreatMessage = true;

    }
    protected void updatePreCombat() {
        this.options.clearOptions();
        this.options.addOption(HSII18nUtil.getCampaignString("HSITTMissionStartFight"), OptionId.CONTINUE_INTO_BATTLE, null);
    }


    protected void updateEngagementChoice(boolean withText) {
        this.options.addOption(HSII18nUtil.getCampaignString("HSITC0FightEngagement"), OptionId.CLEAN_DISENGAGE, null);
        //goToEncounterEndPath();
    }
}
