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

public class dpl_JustAsPlanned extends HubMissionWithBarEvent {

    // mission stages
    public static enum Stage {
        GO_TO_SINDRIA,
        GO_TO_RSV,
        GO_FOR_EXECUTOR,
        GO_FOR_ANTHEM,
        WAIT_FOR_ANTHEM,
        RETURN_TO_SINDRIA,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected PersonAPI macario;
	protected MarketAPI sindria;
	protected MarketAPI dpl_research_site_v;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected boolean met_macario;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
    	
    	met_macario = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sdtu_missionCompleted");
    	if (!met_macario) return false;

        PersonAPI person = getPerson();
        if (person == null) return false;
        
        macario = getImportantPerson(People.MACARIO);
		if (macario == null) return false;
		
		sindria = getMarket("sindria");
		if (sindria == null) return false;
		if (!sindria.getFactionId().equals("sindrian_diktat")) return false;
		
		dpl_research_site_v = getMarket("dpl_research_site_v");
		if (dpl_research_site_v == null) return false;
		if (!dpl_research_site_v.getFactionId().equals("dpl_phase_lab")) return false;
		
        system = dpl_research_site_v.getStarSystem();
        system2 = sindria.getStarSystem();
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_jasp_ref")) {
            return false;
        }

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_jasp_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_SINDRIA);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
		
        makeImportant(sindria, "$dpl_jasp", Stage.GO_TO_SINDRIA);
		makeImportant(macario, "$dpl_jasp", Stage.GO_TO_SINDRIA);
		setStageOnGlobalFlag(Stage.GO_TO_RSV, "$dpl_jaspAsked");
        makeImportant(dpl_research_site_v, "$dpl_jasp", Stage.GO_TO_RSV);
		makeImportant(person, "$dpl_jasp", Stage.GO_TO_RSV);
		setStageOnGlobalFlag(Stage.GO_FOR_EXECUTOR, "$dpl_jaspAskedEliza");
        makeImportant(sindria, "$dpl_jasp", Stage.GO_FOR_EXECUTOR);
		makeImportant(macario, "$dpl_jasp", Stage.GO_FOR_EXECUTOR);
		setStageOnGlobalFlag(Stage.GO_FOR_ANTHEM, "$dpl_jaspAskedMacarioShip");
        makeImportant(dpl_research_site_v, "$dpl_jasp", Stage.GO_FOR_ANTHEM);
		makeImportant(person, "$dpl_jasp", Stage.GO_FOR_ANTHEM);
		setStageOnGlobalFlag(Stage.WAIT_FOR_ANTHEM, "$dpl_jaspAskedElizaShip");
        makeImportant(dpl_research_site_v, "$dpl_jasp", Stage.WAIT_FOR_ANTHEM);
		makeImportant(person, "$dpl_jasp", Stage.WAIT_FOR_ANTHEM);
		setStageOnGlobalFlag(Stage.RETURN_TO_SINDRIA, "$dpl_jasp_AskedPrototype");
        makeImportant(sindria, "$dpl_jasp", Stage.RETURN_TO_SINDRIA);
		makeImportant(macario, "$dpl_jasp", Stage.RETURN_TO_SINDRIA);
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_jasp_RecoveredAgent");
        makeImportant(dpl_research_site_v, "$dpl_jasp", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_jasp", Stage.RETURN_TO_RSV);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_jasp_completed");
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
	        	Misc.makeNonStoryCritical(sindria, "dpl_JustAsPlanned");
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
        set("$dpl_jasp_barEvent", isBarEvent());
        set("$dpl_jasp_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_jasp_heOrShe", getPerson().getHeOrShe());
        set("$dpl_jasp_reward", Misc.getWithDGS(getCreditsReward()));
        set("$dpl_jasp_personName", getPerson().getNameString());
        set("$dpl_jasp_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_jasp_dist", getDistanceLY(system2));
    }


    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_SINDRIA) {
            info.addPara("Find clues in Sindria. Try to ask Macario first in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Go back to Research Site V to ask Eliza about the hymn.", opad);
        } else if (currentStage == Stage.GO_FOR_EXECUTOR) {
            info.addPara("Report to Macario about the hymn system, he is in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_FOR_ANTHEM) {
            info.addPara("Go to Research Site V to ask Eliza about the prototype.", opad);
        } else if (currentStage == Stage.WAIT_FOR_ANTHEM) {
            info.addPara("Eliza is preparing the prototype at Research Site V, be patient.", opad);
        } else if (currentStage == Stage.RETURN_TO_SINDRIA) {
            info.addPara("Report to Macario about the prototype, he is in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to release the agent.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: MACARIO IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_SINDRIA) {
            info.addPara("Ask Macario in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Ask Eliza about the 'hymn' in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_FOR_EXECUTOR) {
            info.addPara("Tell Macario about the hymn system in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_FOR_ANTHEM) {
            info.addPara("Ask Eliza about the prototype in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.WAIT_FOR_ANTHEM) {
            info.addPara("Wait for Eliza to finish the prototype, she is in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_SINDRIA) {
            info.addPara("Find clues in sindria. Try to ask Macario first in " +
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
    	if (currentStage == Stage.GO_TO_SINDRIA) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.GO_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.GO_FOR_EXECUTOR) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.GO_FOR_ANTHEM) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.WAIT_FOR_ANTHEM) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.RETURN_TO_SINDRIA) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Just As Planned";
    }

}
