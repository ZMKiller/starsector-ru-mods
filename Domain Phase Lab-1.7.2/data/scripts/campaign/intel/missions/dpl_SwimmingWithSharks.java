package data.scripts.campaign.intel.missions;

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

import data.scripts.campaign.intel.missions.dpl_LightMeetsDark.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_SwimmingWithSharks extends HubMissionWithBarEvent {

    // mission stages
    public static enum Stage {
        GO_TO_KAZERON,
        GO_TO_RSV,
        GO_FOR_AGENT,
        WAIT_FOR_AGENT,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected PersonAPI horus_yaribay;
	protected MarketAPI kazeron;
	protected MarketAPI dpl_research_site_v;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected boolean met_yaribay;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

    	met_yaribay = Global.getSector().getMemoryWithoutUpdate().getBoolean("$gaATG_missionCompleted");
    	if (!met_yaribay) return false;
        PersonAPI person = getPerson();
        if (person == null) return false;
        horus_yaribay = getImportantPerson(People.HORUS_YARIBAY);
		if (horus_yaribay == null) return false;
		kazeron = getMarket("kazeron");
		if (kazeron == null) return false;
		if (!kazeron.getFactionId().equals("persean")) return false;
		dpl_research_site_v = getMarket("dpl_research_site_v");
		if (dpl_research_site_v == null) return false;
		if (!dpl_research_site_v.getFactionId().equals("dpl_phase_lab")) return false;
        system = dpl_research_site_v.getStarSystem();
        system2 = kazeron.getStarSystem();
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_swts_ref")) {
            return false;
        }

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_swts_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_KAZERON);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
		
        makeImportant(kazeron, "$dpl_swts", Stage.GO_TO_KAZERON);
		setStageOnGlobalFlag(Stage.GO_TO_RSV, "$dpl_swtsAsked");
        makeImportant(dpl_research_site_v, "$dpl_swts", Stage.GO_TO_RSV);
		makeImportant(person, "$dpl_swts", Stage.GO_TO_RSV);
		setStageOnGlobalFlag(Stage.GO_FOR_AGENT, "$dpl_swtsAskedEliza");
        makeImportant(kazeron, "$dpl_swts", Stage.GO_FOR_AGENT);
		makeImportant(horus_yaribay, "$dpl_swts", Stage.GO_FOR_AGENT);
		setStageOnGlobalFlag(Stage.WAIT_FOR_AGENT, "$dpl_swtsAskedFighter");
        makeImportant(kazeron, "$dpl_swts", Stage.WAIT_FOR_AGENT);
		makeImportant(horus_yaribay, "$dpl_swts", Stage.WAIT_FOR_AGENT);
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_swts_RecoveredAgent");
        makeImportant(dpl_research_site_v, "$dpl_swts", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_swts", Stage.RETURN_TO_RSV);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_swts_completed");
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				Misc.makeNonStoryCritical(kazeron, "dpl_SwimmingWithSharks");
			}
		});
		endTrigger();
        
		// No time limit. But it has credit reward
		setStoryMission();
        setCreditReward(120000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_bidw_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_bidw_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_swts_barEvent", isBarEvent());
        set("$dpl_swts_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_swts_heOrShe", getPerson().getHeOrShe());
        set("$dpl_swts_reward", Misc.getWithDGS(getCreditsReward()));
        set("$dpl_swts_personName", getPerson().getNameString());
        set("$dpl_swts_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_swts_dist", getDistanceLY(system2));
    }


    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_KAZERON) {
            info.addPara("Find clues in Kazeron. Try to ask some gens you know first in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Go back to Research Site V to ask Eliza about Yaribay's demands.", opad);
        } else if (currentStage == Stage.GO_FOR_AGENT) {
            info.addPara("Go to Kazeron to show Yaribay the documents.", opad);
        } else if (currentStage == Stage.WAIT_FOR_AGENT) {
            info.addPara("Yaribay is looking for the agent at Kazeron, be patient.", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to release the agent.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: YARIBAY IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_KAZERON) {
            info.addPara("Go to " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Ask Eliza in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_FOR_AGENT) {
            info.addPara("Tell Yaribay about the fighter in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.WAIT_FOR_AGENT) {
            info.addPara("Wait for Yaribay to find the agent, he is in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.GO_TO_KAZERON) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.GO_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.GO_FOR_AGENT) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.WAIT_FOR_AGENT) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Swimming With Sharks";
    }

}
