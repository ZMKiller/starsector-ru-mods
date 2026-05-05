package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.PursuitOption;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.OptionId;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAFID;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;

import java.util.Iterator;
import java.util.List;

public class HSIForceCombat extends HSISAFID {
    protected boolean shownTooLargeToRetreatMessage;

    public HSIForceCombat() {
        this((FleetInteractionDialogPluginImpl.FIDConfig) null);
    }

    public HSIForceCombat(FleetInteractionDialogPluginImpl.FIDConfig params) {
        super(params);
        this.shownTooLargeToRetreatMessage = true;

    }
    protected void updatePreCombat() {                                                                     
        this.options.clearOptions();                                                                        
        this.options.addOption(HSII18nUtil.getCampaignString("HSITC0FightOption"), OptionId.CONTINUE_INTO_BATTLE, null);
    }


    protected void updateEngagementChoice(boolean withText) {
        this.options.addOption(HSII18nUtil.getCampaignString("HSITC0FightEngagement"), OptionId.CLEAN_DISENGAGE, null);
        //goToEncounterEndPath();               
    }

}



