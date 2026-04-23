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

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.MERC_ARMADA;

public class dpl_PeaceThroughStrength extends HubMissionWithBarEvent {
    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        GO_TO_EB,
        GO_TO_RSV,
        BACK_TO_EB,
        BACK_TO_RSV,
        GO_TO_TSE,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected CampaignFleetAPI target1;
    protected CampaignFleetAPI target2;
    protected PersonAPI merc;
    protected PersonAPI agent;
    protected PersonAPI executive;
	protected PersonAPI arroyo;
	protected PersonAPI person;
	protected MarketAPI market;
	protected MarketAPI eochu_bres;
	protected MarketAPI port_tse;
    protected StarSystemAPI system;
    protected StarSystemAPI system1;
    protected StarSystemAPI system2;
    protected boolean met_arroyo;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

    	met_arroyo = Global.getSector().getMemoryWithoutUpdate().getBoolean("$gaATG_missionCompleted");
    	if (!met_arroyo) return false;
        person = getPerson();
        if (person == null) return false;
        arroyo = getImportantPerson(People.ARROYO);
        if (arroyo == null) return false;
        eochu_bres = Global.getSector().getEconomy().getMarket("eochu_bres");
        if (eochu_bres == null) return false;
        if (!eochu_bres.getFactionId().equals(Factions.TRITACHYON)) return false;
        port_tse = Global.getSector().getEconomy().getMarket("port_tse");
        if (port_tse == null) return false;
        if (!port_tse.getFactionId().equals(Factions.TRITACHYON)) return false;
        market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        system = market.getStarSystem();
        
        //not repeatable after failure.
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_ptrs_failed")) return false;
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_ptrs_ref")) {
            return false;
        }

        // set up the characters
        merc = Global.getSector().getFaction(Factions.MERCENARY).createRandomPerson();
        merc.setRankId(Ranks.SPACE_CAPTAIN);
        merc.setPostId(Ranks.POST_FLEET_COMMANDER);
        merc.getMemoryWithoutUpdate().set("$dpl_ptrs_merc", true);
        
        agent = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        agent.setRankId(Ranks.SPACE_CAPTAIN);
        agent.setPostId(Ranks.POST_FLEET_COMMANDER);
        agent.getMemoryWithoutUpdate().set("$dpl_ptrs_agent", true);
        
        executive = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        executive.setRankId(Ranks.SPACE_ADMIRAL);
        executive.setPostId(Ranks.POST_FLEET_COMMANDER);
        executive.getMemoryWithoutUpdate().set("$dpl_ptrs_exec", true);

        // pick the target fleet's system
        system1 = eochu_bres.getStarSystem();
        if (system1 == null) return false;
        system2 = port_tse.getStarSystem();
        if (system2 == null) return false;
        
        beginStageTrigger(Stage.GO_TO_EB);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_merc_fleet();
				spawn_agent_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.GO_TO_TSE);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_executive_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.RETURN_TO_RSV);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_all_fleets();
			}
		});
        endTrigger(); 
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				Misc.makeNonStoryCritical(eochu_bres, "dpl_PeaceThroughStrength");
	        	Misc.makeNonStoryCritical(port_tse, "dpl_PeaceThroughStrength");
			}
		});
		endTrigger();
		
		beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_all_fleets();
				Global.getSector().getMemoryWithoutUpdate().set("$dpl_ptrs_failed", true);
			}
		});
        endTrigger();
        
        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_ptrs_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.GO_TO_EB);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        makeImportant(eochu_bres, "$dpl_ptrs", Stage.GO_TO_EB);
        makeImportant(arroyo, "$dpl_ptrs", Stage.GO_TO_EB);
		setStageOnGlobalFlag(Stage.GO_TO_RSV, "$dpl_ptrsAsked");
        makeImportant(market, "$dpl_ptrs", Stage.GO_TO_RSV);
		makeImportant(person, "$dpl_ptrs", Stage.GO_TO_RSV);
		setStageOnGlobalFlag(Stage.BACK_TO_EB, "$dpl_ptrsAskedEliza");
		makeImportant(eochu_bres, "$dpl_ptrs", Stage.BACK_TO_EB);
		makeImportant(arroyo, "$dpl_ptrs", Stage.BACK_TO_EB);
		setStageOnGlobalFlag(Stage.BACK_TO_RSV, "$dpl_ptrsAskedArroyo");
	    makeImportant(market, "$dpl_ptrs", Stage.BACK_TO_RSV);
	    makeImportant(person, "$dpl_ptrs", Stage.BACK_TO_RSV);
	    setStageOnGlobalFlag(Stage.GO_TO_TSE, "$dpl_ptrsDiplomatsArranged");
        makeImportant(port_tse, "$dpl_ptrs", Stage.GO_TO_TSE);
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_ptrsRecoveredAgent");
        makeImportant(market, "$dpl_ptrs", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_ptrs", Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_ptrs_completed");
		
        // set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_RSV);
        setCreditReward(120000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_ptrs_ref updateData", these values will be put in memory

    protected void updateInteractionDataImpl() {
        set("$dpl_ptrs_barEvent", isBarEvent());
        set("$dpl_ptrs_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_ptrs_heOrShe", getPerson().getHeOrShe());
        set("$dpl_ptrs_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_ptrs_personName", getPerson().getNameString());
        set("$dpl_ptrs_mercName", merc.getNameString());
        set("$dpl_ptrs_execName", executive.getNameString());
        set("$dpl_ptrs_systemName", system1.getNameWithLowercaseTypeShort());
        set("$dpl_ptrs_dist", getDistanceLY(system1));
    }

    public void spawn_merc_fleet(){
    	// set up the merc fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
        // and we need to know when this fleet dies or despawns. You may bypass the fleet.
        FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.MERCENARY,
                null,
                MERC_ARMADA,
                300f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
        target = FleetFactoryV3.createFleet(params);
        target.setName("Elite Mercenary Fleet");
        target.setNoFactionInName(true);

        target.setCommander(merc);
        Misc.makeNoRepImpact(target, "$dpl_ptrs");
        target.getFlagship().setCaptain(merc);

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_ptrs");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_ptrs");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_ptrs");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_ptrs");
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_ptrsmercfleet", true);
        target.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, market.getPrimaryEntity(), 200f, null);
        system.addEntity(target);
    }
    
    public void spawn_agent_fleet(){
    	// set up the executive's agent fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
        // and we need to know when this fleet dies or despawns. You may bypass the fleet.
        FleetParamsV3 params1 = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                TASK_FORCE,
                300f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
        target1 = FleetFactoryV3.createFleet(params1);
        target1.setName("Company Affair Flotilla");
        target1.setNoFactionInName(true);

        target1.setCommander(agent);
        Misc.makeNoRepImpact(target1, "$dpl_ptrs");
        target1.getFlagship().setCaptain(agent);

        Misc.makeHostile(target1);
        Misc.makeImportant(target1, "$dpl_ptrs");

        target1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_ptrs");
        target1.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_ptrs");
        target1.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_ptrs");
        //Must be set to true, or some improper tithe check will ruin the story.
        target1.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target1.getMemoryWithoutUpdate().set("$dpl_ptrsagentfleet", true);
        target1.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, eochu_bres.getPrimaryEntity(), 200f, null);
        system1.addEntity(target1);
    }
    
    public void spawn_executive_fleet(){
    	// set up the executive fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
        // and we need to know when this fleet dies or despawns. You may bypass the fleet.
        FleetParamsV3 params2 = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                TASK_FORCE,
                400f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
        target2 = FleetFactoryV3.createFleet(params2);
        target2.setName(executive.getNameString() + "'s Armada");
        target2.setNoFactionInName(true);

        target2.setCommander(executive);
        Misc.makeNoRepImpact(target2, "$dpl_ptrs");
        target2.getFlagship().setCaptain(executive);

        Misc.makeHostile(target2);
        Misc.makeImportant(target2, "$dpl_ptrs");

        target2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_ptrs");
        target2.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_ptrs");
        target2.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_ptrs");
        //Must be set to true, or some improper tithe check will ruin the story.
        target2.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target2.getMemoryWithoutUpdate().set("$dpl_ptrsexecfleet", true);
        target2.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, port_tse.getPrimaryEntity(), 200f, null);
        system2.addEntity(target2);
        Vector2f pos = port_tse.getPrimaryEntity().getLocation();
        target2.setLocation(pos.x, pos.y);
    }
    
    public void despawn_all_fleets(){
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, false);
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, market.getPrimaryEntity(), 200f, null);
    	}
    	
    	if (target1 != null) {
    		target1.getAI().clearAssignments();
    		target1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, false);
    		target1.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, eochu_bres.getPrimaryEntity(), 200f, null);
    	}
    	
    	if (target2 != null) {
    		target2.getAI().clearAssignments();
    		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, false);
    		target2.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, port_tse.getPrimaryEntity(), 200f, null);
    	}
    }
    
    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_EB) {
            info.addPara("Find clues at Eochu Bres, try to ask Arroyo in " +
                    system1.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Go back to RSV, ask Eliza about her ideas in " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BACK_TO_EB) {
            info.addPara("Tell Arroyo about Eliza's decision in " +
                    system1.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BACK_TO_RSV) {
            info.addPara("Go back to RSV, ask Eliza about the diplomatic meeting in " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_TSE) {
            info.addPara("Transport the diplomatic team to " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Take the team back to Research Site V, and to release the agent.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: EXECUTIVE IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.GO_TO_EB) {
            info.addPara("Talk to Arroyo in Eochu Bres " +
                    system1.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Ask Eliza in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.BACK_TO_EB) {
            info.addPara("Talk to Arroyo in Eochu Bres " +
                    system1.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.BACK_TO_RSV) {
            info.addPara("Ask Eliza in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_TSE) {
            info.addPara("Take the diplomats to " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_RSV) {
        	info.addPara("Peace Treaty between Phase Lab and TriTachyon made.", tc, pad);
            info.addPara("Return to Research Site V.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.GO_TO_EB) {
            return getMapLocationFor(system1.getCenter());
        } else if (currentStage == Stage.GO_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.BACK_TO_EB) {
            return getMapLocationFor(system1.getCenter());
        } else if (currentStage == Stage.BACK_TO_RSV) {
            return getMapLocationFor(system.getCenter());
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
        return "Peace Through Strength";
    }

}
