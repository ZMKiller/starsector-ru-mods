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

public class dpl_Home extends HubMissionWithBarEvent {
    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        GO_TO_GILEAD,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
	protected MarketAPI gilead;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        gilead = Global.getSector().getEconomy().getMarket("gilead");
		if (!gilead.getFactionId().equals(Factions.LUDDIC_CHURCH)) return false;
		
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_home_ref")) {
            return false;
        }

        // pick the target fleet's system

        system2 = gilead.getStarSystem();
        if (system2 == null) return false;

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_home_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_GILEAD);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        makeImportant(gilead, "$dpl_home", Stage.GO_TO_GILEAD);
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_home_picked");
        makeImportant(market, "$dpl_home", Stage.RETURN_TO_RSV);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_home_completed");
        // set time limit and credit reward
        setCreditReward(50000);

        return true;
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_home_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_home_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_home_barEvent", isBarEvent());
        set("$dpl_home_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_home_heOrShe", getPerson().getHeOrShe());
        set("$dpl_home_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_home_personName", getPerson().getNameString());
        set("$dpl_home_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_home_dist", getDistanceLY(system2));
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_GILEAD) {
            info.addPara("Pick up the furnitures in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V.", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_GILEAD) {
            info.addPara("Pick up the furnitures in the " +
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
    	if (currentStage == Stage.GO_TO_GILEAD) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "New Home";
    }
}
