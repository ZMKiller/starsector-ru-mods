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

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_LightMeetsDark extends HubMissionWithBarEvent {

    // mission stages
    public static enum Stage {
        GO_TO_DEN,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected PersonAPI kanta;
	protected MarketAPI kantasDen;
	protected PersonAPI kantasDenStationCommander;
	protected MarketAPI dpl_research_site_v;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        kanta = getImportantPerson(People.KANTA);
		if (kanta == null) return false;
		
		kantasDen = Global.getSector().getEconomy().getMarket("kantas_den");
		if (kantasDen == null) return false;
		
		kantasDenStationCommander = getPersonAtMarketPost(kantasDen, Ranks.POST_STATION_COMMANDER);
		if (kantasDenStationCommander == null) return false;
		
		dpl_research_site_v = person.getMarket();
		if (dpl_research_site_v == null) return false;
		if (!dpl_research_site_v.getFactionId().equals("dpl_phase_lab")) return false;
		
        system = dpl_research_site_v.getStarSystem();
        system2 = kantasDen.getStarSystem();
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_lmtd_ref")) {
            return false;
        }

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_lmtd_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_DEN);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
		
        makeImportant(kantasDen, "$dpl_lmtd", Stage.GO_TO_DEN);
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_lmtdRecoveredAgent");
        makeImportant(dpl_research_site_v, "$dpl_lmtd", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_lmtd", Stage.RETURN_TO_RSV);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_lmtd_completed");
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
	        	Misc.makeNonStoryCritical(kantasDen, "dpl_LightMeetsDark");
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
        set("$dpl_lmtd_barEvent", isBarEvent());
        set("$dpl_lmtd_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_lmtd_heOrShe", getPerson().getHeOrShe());
        set("$dpl_lmtd_reward", Misc.getWithDGS(getCreditsReward()));
        set("$dpl_lmtd_personName", getPerson().getNameString());
        set("$dpl_lmtd_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_lmtd_dist", getDistanceLY(system2));
    }


    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_DEN) {
            info.addPara("Take Eliza to Kanta's Den located in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to drop Eliza off.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: KANTA IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_DEN) {
            info.addPara("Take Eliza to Kanta's Den located in " +
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
    	if (currentStage == Stage.GO_TO_DEN) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Light Meets Dark";
    }

}
