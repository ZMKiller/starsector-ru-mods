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

public class dpl_Invention extends HubMissionWithBarEvent implements FleetEventListener {

    // mission stages
    public static enum Stage {
        SCAN_ASTRAL_BODIES,
        RETURN_TO_FACTORY,
        WAIT_FOR_CONDENSER,
        COMPLETED,
    }

    // important objects, systems and people
	protected MarketAPI factory;
    protected StarSystemAPI system;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_invention_ref")) {
            return false;
        }

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_invention_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.SCAN_ASTRAL_BODIES);
        setSuccessStage(Stage.COMPLETED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_invention_scanned");
        makeImportant(market, "$dpl_invention", Stage.RETURN_TO_FACTORY);
		makeImportant(person, "$dpl_invention", Stage.RETURN_TO_FACTORY);
		setStageOnGlobalFlag(Stage.WAIT_FOR_CONDENSER, "$dpl_invention_last_step");
        makeImportant(market, "$dpl_invention", Stage.WAIT_FOR_CONDENSER);
		makeImportant(person, "$dpl_invention", Stage.WAIT_FOR_CONDENSER);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_invention_completed");
		setStoryMission();
        setCreditReward(250000);

        return true;
    }
    
    protected void updateInteractionDataImpl() {
        set("$dpl_invention_barEvent", isBarEvent());
        set("$dpl_invention_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_invention_heOrShe", getPerson().getHeOrShe());
        set("$dpl_invention_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_invention_personName", getPerson().getNameString());
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
        if (currentStage == Stage.SCAN_ASTRAL_BODIES) {
            info.addPara("Investigate a neutron star, a black hole, and a blue giant for Ross.", opad);
            info.addPara("To do so, you need to visit a planet that orbits these celestial objects.", opad);
            if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_dwarf")) {
            	info.addPara("You have investigated a neutron star already.", opad);
            }
            if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_blackhole")) {
            	info.addPara("You have investigated a blackhole already.", opad);
            }
            if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_invention_blue_giant")) {
            	info.addPara("You have investigated a blue giant already.", opad);
            }
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
        	info.addPara("Return to Lab Factory.", opad);
        } else if (currentStage == Stage.WAIT_FOR_CONDENSER) {
            info.addPara("Wait for Ross to complete the project, she is in " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.SCAN_ASTRAL_BODIES) {
            info.addPara("Investigate a neutron star, a black hole, and a blue giant for Ross.", tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
        	info.addPara("Return to Lab Factory.", tc, pad);
            return true;
        } else if (currentStage == Stage.WAIT_FOR_CONDENSER) {
            info.addPara("Wait for the project to complete at Lab Factory.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.WAIT_FOR_CONDENSER) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Invention";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
