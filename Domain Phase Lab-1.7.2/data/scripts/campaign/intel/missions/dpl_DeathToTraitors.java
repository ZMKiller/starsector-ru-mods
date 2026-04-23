package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
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

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.MERC_ARMADA;

public class dpl_DeathToTraitors extends HubMissionWithBarEvent implements FleetEventListener {
	// time we have to complete the mission
    public static float MISSION_DAYS = 150f;
    
    // mission stages
    public static enum Stage {
        GO_TO_SINDRIA,
        GO_TO_TSE,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected PersonAPI traitor;
    protected PersonAPI macario;
	protected MarketAPI sindria;
	protected MarketAPI tibicena;
    protected StarSystemAPI system;
    protected StarSystemAPI system1;
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
		
		tibicena = Global.getSector().getEconomy().getMarket("tibicena");
        if (tibicena == null) return false;
        
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_dtot_ref")) {
            return false;
        }

        // set up the traitor       
        traitor = Global.getSector().getFaction(Factions.DIKTAT).createRandomPerson();
        traitor.setRankId(Ranks.SPACE_COMMANDER);
        traitor.setPostId(Ranks.POST_EXECUTIVE);
        traitor.getMemoryWithoutUpdate().set("$dpl_dtot_traitor", true);

        // pick the target fleet's system
        system1 = sindria.getStarSystem();
        if (system1 == null) return false;
        system2 = tibicena.getStarSystem();
        if (system2 == null) return false;

        // set up the stage triggers, so that the fleet only gets spawned at certain stage.
        beginStageTrigger(Stage.GO_TO_TSE);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_traitor_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.RETURN_TO_RSV);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_fleets();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_fleets();
			}
		});
        endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_dtot_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_SINDRIA);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver

        makeImportant(sindria, "$dpl_dtot", Stage.GO_TO_SINDRIA);
        makeImportant(macario, "$dpl_dtot", Stage.GO_TO_SINDRIA);
	    setStageOnGlobalFlag(Stage.GO_TO_TSE, "$dpl_dtotAsked");
        setStageOnMemoryFlag(Stage.RETURN_TO_RSV, person, "$dpl_dtot_killed");
        makeImportant(market, "$dpl_dtot", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_dtot", Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_dtot_completed");
        // set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_RSV);
        setCreditReward(90000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_dtot_ref updateData", these values will be put in memory

    protected void updateInteractionDataImpl() {
        set("$dpl_dtot_barEvent", isBarEvent());
        set("$dpl_dtot_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_dtot_heOrShe", getPerson().getHeOrShe());
        set("$dpl_dtot_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_dtot_personName", getPerson().getNameString());
        set("$dpl_dtot_traitorName", traitor.getNameString());
        set("$dpl_dtot_systemName", system1.getNameWithLowercaseTypeShort());
        set("$dpl_dtot_dist", getDistanceLY(system1));
    }
    
    public void spawn_traitor_fleet() {
    	// set up the traitor fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
        // and we need to know when this fleet dies or despawns. You may bypass the fleet.
        FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                TASK_FORCE,
                120f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
        
		target = FleetFactoryV3.createFleet(params);
		target.getFleetData().addFleetMember("dpl_bassoon_defence");
		target.setCommander(traitor);
        target.getFlagship().setCaptain(traitor);
		
        
        target.setName("Traitor's Fleet");
        target.setNoFactionInName(true);

        Misc.makeNoRepImpact(target, "$dpl_dtot");

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_dtot");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_dtot");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_dtot");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_dtot");
        target.getMemoryWithoutUpdate().set("$dpl_dtot_traitorfleet", true);
        target.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, tibicena.getPrimaryEntity(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
        Vector2f pos = tibicena.getLocation();
        target.setLocation(pos.x, pos.y);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }
    
    // used to detect when the traitor's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();

        if (!playerInvolved || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }
        
        if (fleet.getFlagship() == null || (!fleet.getFlagship().getCaptain().equals(traitor)) ) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            getPerson().getMemoryWithoutUpdate().set("$dpl_dtot_killed", true);
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain().equals(traitor)) return;
        getPerson().getMemoryWithoutUpdate().set("$dpl_dtot_killed", true);

    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_SINDRIA) {
            info.addPara("Find clues at Sindria, try to ask Macario in " +
                    system1.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_TSE) {
            info.addPara("Eliminate the traitor in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Report back to Research Site V.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: TRAITOR IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_SINDRIA) {
            info.addPara("Talk to Macario in Sindria " +
                    system1.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_TSE) {
            info.addPara("Eliminate the traitor in " +
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
            return getMapLocationFor(system1.getCenter());
        } else if (currentStage == Stage.GO_TO_TSE) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Death to Traitors";
    }

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		// TODO Auto-generated method stub
		
	}

}
