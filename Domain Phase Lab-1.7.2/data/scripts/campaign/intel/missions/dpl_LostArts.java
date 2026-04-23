package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Celebration.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;

public class dpl_LostArts extends HubMissionWithBarEvent implements FleetEventListener {

    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        BOARD_THE_SHIP,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI drone_fleet;
    protected SectorEntityToken wreck;
    protected MarketAPI market;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected Vector2f last_loc_drones;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        system = person.getMarket().getStarSystem();
        if (system == null) return false;
        system2 = Global.getSector().getStarSystem("capella");
        if (system2 == null) return false;

        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_lostarts_ref")) {
            return false;
        }
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_drone_fleet();
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
        if (!setGlobalReference("$dpl_lostarts_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.BOARD_THE_SHIP, "$dpl_lostarts_won");
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_lostarts_visited");
        makeImportant(market, "$dpl_lostarts", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_lostarts", Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_lostarts_completed");
        // set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.BOARD_THE_SHIP);
        setCreditReward(500000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_lostarts_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_lostarts_execName and automatically insert the disgraced executive's name
    protected void updateInteractionDataImpl() {
        set("$dpl_lostarts_barEvent", isBarEvent());
        set("$dpl_lostarts_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_lostarts_heOrShe", getPerson().getHeOrShe());
        set("$dpl_lostarts_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_lostarts_personName", getPerson().getNameString());
        set("$dpl_lostarts_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_lostarts_system2Name", system2.getNameWithLowercaseTypeShort());
        set("$dpl_lostarts_dist", getDistanceLY(system2));
    }
    
    public void Victory() {
    	Vector2f loc = last_loc_drones;
    	wreck = dpl_phase_labAddEntities.spawnNamedWreck(loc, system2, "dpl_persean_imperium", "dpl_aulochrome_Hull", "HSNS Supremacy", false);
    	Misc.makeImportant(wreck, "$dpl_lostarts");
    	wreck.getMemoryWithoutUpdate().set("$dpl_lostarts_derelict", true);
        setEntityMissionRef(wreck, "$dpl_lostarts_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_lostarts_won", true);
    }
    
    public void spawn_drone_fleet(){
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "", true);
    	PersonAPI helheim_agent = Global.getSector().getFaction("dpl_persean_imperium").createRandomPerson();
    	helheim_agent.setRankId(Ranks.SPACE_ADMIRAL);
    	helheim_agent.setPostId(Ranks.POST_FLEET_COMMANDER);
    	helheim_agent.getStats().setLevel(6);
    	helheim_agent.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
    	helheim_agent.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
    	helheim_agent.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
    	helheim_agent.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        helheim_agent.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        helheim_agent.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        helheim_agent.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        helheim_agent.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		fleet.getFleetData().addFleetMember("dpl_aulochrome_salazar_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("HSNS Supremacy");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.REMNANTS,
                null,
                TASK_FORCE,
                550f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	params.averageSMods = 3;
    	drone_fleet = FleetFactoryV3.createFleet(params);
    	drone_fleet.setName("Strange Drone Fleet");
    	drone_fleet.setNoFactionInName(true);
    	drone_fleet.getFleetData().addFleetMember(member);
    	member.setCaptain(helheim_agent);
    	drone_fleet.getFleetData().sort();
    	drone_fleet.addEventListener(this);
    	List<FleetMemberAPI> members = drone_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.getShipName().equalsIgnoreCase("HSNS Supremacy")) {
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, genRandom);
				curr.setCaptain(person);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(drone_fleet);
		Misc.makeNoRepImpact(drone_fleet, "$dpl_lostarts");
        Misc.makeImportant(drone_fleet, "$dpl_lostarts");

        drone_fleet.getMemoryWithoutUpdate().set("$dpl_lostarts_dronefleet", true);
        drone_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        drone_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_lostarts");
        drone_fleet.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
        system2.addEntity(drone_fleet);
        Vector2f pos = system2.getCenter().getLocation();
        drone_fleet.setLocation(pos.x+3500f, pos.y+3500f);
    }
    
    protected void despawn_fleets() {
    	if (drone_fleet != null) {
    		drone_fleet.getAI().clearAssignments();
    		drone_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }

    // used to detect when the executive's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();

        if (!playerInvolved || !battle.isInvolved(drone_fleet) || battle.onPlayerSide(drone_fleet)) {
            return;
        }

        if (drone_fleet != null) {
        	last_loc_drones = drone_fleet.getLocation();
    	}

    }

    // if the fleet despawns for whatever reason, fail the mission
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) return;

        if (fleet.equals(drone_fleet)) {
			Victory();
		}
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Start investigation in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BOARD_THE_SHIP) {
            info.addPara("Examine the derelict in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Go back to Research Site V.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: THE DRONE FLEET IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Start investigation in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.BOARD_THE_SHIP) {
            info.addPara("Examine the derelict in " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Go back to Research Site V in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.BOARD_THE_SHIP) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Lost Arts";
    }
}
