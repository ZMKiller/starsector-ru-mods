package data.scripts.campaign.intel.missions.RossQuests;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_CallToArms extends HubMissionWithBarEvent implements FleetEventListener {

    // mission stages
    public static enum Stage {
        COLLECT_WEAPONS,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
	protected MarketAPI factory;
    protected StarSystemAPI system;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        factory = person.getMarket();
        if (factory == null) return false;
        if (!factory.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = factory.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_ctam_ref")) {
            return false;
        }

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_ctam_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.COLLECT_WEAPONS);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);
        
        setStoryMission();

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        makeImportant(factory, "$dpl_ctam", Stage.COLLECT_WEAPONS);
		makeImportant(person, "$dpl_ctam", Stage.COLLECT_WEAPONS);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_ctam_completed");
        
        // set time limit and credit reward
        setCreditReward(750000);

        return true;
    }
    
    protected void updateInteractionDataImpl() {
        set("$dpl_ctam_barEvent", isBarEvent());
        set("$dpl_ctam_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_ctam_heOrShe", getPerson().getHeOrShe());
        set("$dpl_ctam_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_ctam_personName", getPerson().getNameString());
    }

    // used to detect when the pather's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.COLLECT_WEAPONS) {
            info.addPara("Collect 2500 marine units for Ross.", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.COLLECT_WEAPONS) {
            info.addPara("Collect 2500 marine units for Ross, then return to Lab Factory", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.COLLECT_WEAPONS) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Call To Arms";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
