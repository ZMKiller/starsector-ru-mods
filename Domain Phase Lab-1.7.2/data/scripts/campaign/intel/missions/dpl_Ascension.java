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
import com.fs.starfarer.api.loading.VariantSource;
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

public class dpl_Ascension extends HubMissionWithBarEvent implements FleetEventListener {

    // time we have to complete the mission
    public static float MISSION_DAYS = 180f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        BOARD_THE_SHIP,
        DECODE_THE_DATA,
        KILL_OMEGA,
        BOARD_OMEGA,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected PersonAPI gargoyle;
    protected CampaignFleetAPI drone_fleet;
    protected CampaignFleetAPI omega_fleet;
    protected SectorEntityToken wreck;
    protected SectorEntityToken wreck_omega;
    protected MarketAPI market;
    protected MarketAPI academy;
    protected StarSystemAPI academy_system;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected StarSystemAPI system3;
    protected Vector2f last_loc_drones;
    protected Vector2f last_loc_omega;
    protected LocationAPI last_pos_drones;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        gargoyle = getImportantPerson(People.GARGOYLE);
        if (gargoyle == null) return false;
        market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        academy = gargoyle.getMarket();
        if (academy == null) return false;
        academy_system = academy.getStarSystem();
        if (academy_system == null) return false;
        system = person.getMarket().getStarSystem();
        if (system == null) return false;
        system2 = Global.getSector().getStarSystem("capella");
        if (system2 == null) return false;
        
        //Make sure that this mission does not begin before the Galactia quest line.
        if (!Global.getSector().getMemoryWithoutUpdate().getBoolean("$gaATG_missionCompleted")) return false;
        
        // pick the system.
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemUnexplored();
        preferSystemInDirectionOfOtherMissions();

        system3 = pickSystem(true);
        if (system3 == null) return false;

        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_ascension_ref")) {
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
        
        beginStageTrigger(Stage.KILL_OMEGA);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_omega_fleet();
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
        if (!setGlobalReference("$dpl_ascension_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.BOARD_THE_SHIP, "$dpl_ascension_won");
        setStageOnGlobalFlag(Stage.DECODE_THE_DATA, "$dpl_ascension_visited");
        setStageOnGlobalFlag(Stage.KILL_OMEGA, "$dpl_ascension_decoded");
        setStageOnGlobalFlag(Stage.BOARD_OMEGA, "$dpl_ascension_won_omega");
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_ascension_visited_omega");
        makeImportant(academy, "$dpl_ascension", Stage.DECODE_THE_DATA);
		makeImportant(gargoyle, "$dpl_ascension", Stage.DECODE_THE_DATA);
        makeImportant(market, "$dpl_ascension", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_ascension", Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_ascension_completed");
		setFailureStage(Stage.FAILED);
        // set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.BOARD_OMEGA);
        setCreditReward(1000000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_ascension_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_ascension_execName and automatically insert the disgraced executive's name
    protected void updateInteractionDataImpl() {
        set("$dpl_ascension_barEvent", isBarEvent());
        set("$dpl_ascension_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_ascension_heOrShe", getPerson().getHeOrShe());
        set("$dpl_ascension_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_ascension_personName", getPerson().getNameString());
        set("$dpl_ascension_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_ascension_system2Name", system2.getNameWithLowercaseTypeShort());
        set("$dpl_ascension_system3Name", system3.getNameWithLowercaseTypeShort());
        set("$dpl_ascension_dist", getDistanceLY(system2));
        set("$dpl_ascension_dist2", getDistanceLY(system3));
    }
    
    public void Victory() {
    	Vector2f loc = last_loc_drones;
    	LocationAPI pos = last_pos_drones;
    	wreck = dpl_phase_labAddEntities.spawnOlympias(pos,loc);
    	Misc.makeImportant(wreck, "$dpl_ascension");
    	wreck.getMemoryWithoutUpdate().set("$dpl_ascension_derelict", true);
        setEntityMissionRef(wreck, "$dpl_ascension_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_ascension_won", true);
    }
    
    public void Victory_Omega() {
    	Vector2f loc = last_loc_omega;
    	wreck_omega = dpl_phase_labAddEntities.spawnNamedWreck(loc, system3, "dpl_persean_imperium", "dpl_aulochrome_Hull", "HSNS Conqueror", false);
    	Misc.makeImportant(wreck_omega, "$dpl_ascension");
    	wreck_omega.getMemoryWithoutUpdate().set("$dpl_ascension_omega_derelict", true);
        setEntityMissionRef(wreck_omega, "$dpl_ascension_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_ascension_won_omega", true);
    }
    
    public void spawn_omega_fleet(){
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "", true);
    	PersonAPI faraday_holmes = getImportantPerson("faraday_holmes");
    	faraday_holmes.getStats().setLevel(10);
    	faraday_holmes.setFaction("dpl_persean_imperium");
    	faraday_holmes.setRankId("factionLeader");
    	faraday_holmes.setPostId("secondLeader");
		fleet.getFleetData().addFleetMember("dpl_aulochrome_salazar_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("HSNS Conqueror");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.OMEGA,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	params.averageSMods = 3;
    	omega_fleet = FleetFactoryV3.createFleet(params);
    	omega_fleet.setName("Mysterious 'Ascension' Fleet");
    	omega_fleet.setNoFactionInName(true);
    	omega_fleet.getFleetData().addFleetMember(member);
    	omega_fleet.getFleetData().setFlagship(member);
    	member.setCaptain(faraday_holmes);
    	omega_fleet.getFleetData().addFleetMember("tesseract_Attack");
    	omega_fleet.getFleetData().addFleetMember("tesseract_Disruptor");
    	omega_fleet.getFleetData().addFleetMember("tesseract_Shieldbreaker");
    	omega_fleet.getFleetData().addFleetMember("tesseract_Strike");
    	omega_fleet.getFleetData().sort();
    	omega_fleet.addEventListener(this);
    	List<FleetMemberAPI> members = omega_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.getShipName().equalsIgnoreCase("HSNS Conqueror")) {
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
				PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, getGenRandom());
				curr.setCaptain(person);
				curr.setVariant(curr.getVariant().clone(), false, false);
				curr.getVariant().setSource(VariantSource.REFIT);
				curr.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
				curr.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(omega_fleet);
		Misc.makeNoRepImpact(omega_fleet, "$dpl_ascension");
        Misc.makeImportant(omega_fleet, "$dpl_ascension");

        omega_fleet.getMemoryWithoutUpdate().set("$dpl_ascension_omegafleet", true);
        omega_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        omega_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_ascension");
        omega_fleet.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system3.getCenter(), 200f, null);
        system3.addEntity(omega_fleet);
        Vector2f pos = system3.getCenter().getLocation();
        omega_fleet.setLocation(pos.x+3500f, pos.y+3500f);
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
		fleet.getFleetData().addFleetMember("dpl_olympias_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("Olympias");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.REMNANTS,
                null,
                TASK_FORCE,
                600f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	params.averageSMods = 3;
    	drone_fleet = FleetFactoryV3.createFleet(params);
    	drone_fleet.setName("Olympias Fleet");
    	drone_fleet.setNoFactionInName(true);
    	drone_fleet.getFleetData().addFleetMember(member);
    	drone_fleet.getFleetData().setFlagship(member);
    	member.setCaptain(helheim_agent);
    	drone_fleet.getFleetData().sort();
    	drone_fleet.addEventListener(this);
    	List<FleetMemberAPI> members = drone_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.getShipName().equalsIgnoreCase("Olympias")) {
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, getGenRandom());
				curr.setCaptain(person);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(drone_fleet);
		Misc.makeNoRepImpact(drone_fleet, "$dpl_ascension");
        Misc.makeImportant(drone_fleet, "$dpl_ascension");

        drone_fleet.getMemoryWithoutUpdate().set("$dpl_ascension_dronefleet", true);
        drone_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        drone_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_ascension");
        drone_fleet.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
        system2.addEntity(drone_fleet);
        Vector2f pos = system2.getCenter().getLocation();
        drone_fleet.setLocation(pos.x+3500f, pos.y+3500f);
    }
    
    protected void despawn_fleets() {
    	if (omega_fleet != null) {
    		omega_fleet.getAI().clearAssignments();
    		omega_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system3.getStar(), 200f, null);
        }
    	
    	if (drone_fleet != null) {
    		drone_fleet.getAI().clearAssignments();
    		drone_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }

    // used to detect when the executive's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();
        boolean isKeyFleetsInvolved = (!battle.isInvolved(drone_fleet) || battle.onPlayerSide(drone_fleet)) && (!battle.isInvolved(omega_fleet) || battle.onPlayerSide(omega_fleet));

        if (!playerInvolved || isKeyFleetsInvolved) {
            return;
        }

        if (drone_fleet != null) {
        	last_loc_drones = drone_fleet.getLocation();
        	last_pos_drones = drone_fleet.getContainingLocation();
    	}
        
        if (omega_fleet != null) {
        	last_loc_omega = omega_fleet.getLocation();
        }

    }

    // if the fleet despawns for whatever reason, fail the mission
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) return;

        if (fleet.equals(drone_fleet)) {
			Victory();
		} else if (fleet.equals(omega_fleet)) {
			Victory_Omega();
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
        } else if (currentStage == Stage.DECODE_THE_DATA) {
            info.addPara("Seek help for decoding the data in " +
            		academy_system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.KILL_OMEGA) {
            info.addPara("Find the 'Ascension' fleet in " +
                    system3.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BOARD_OMEGA) {
            info.addPara("Examine the derelict in " +
                    system3.getNameWithLowercaseTypeShort() + ".", opad);
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
        } else if (currentStage == Stage.DECODE_THE_DATA) {
            info.addPara("Seek help for decoding the data in " +
            		academy_system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.KILL_OMEGA) {
            info.addPara("Find the 'Ascension' fleet in " +
            		system3.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.BOARD_OMEGA) {
            info.addPara("Examine the derelict in " +
                    system3.getNameWithLowercaseTypeShort(), tc, pad);
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
        } else if (currentStage == Stage.DECODE_THE_DATA) {
            return getMapLocationFor(academy_system.getCenter());
        } else if (currentStage == Stage.KILL_OMEGA) {
            return getMapLocationFor(system3.getCenter());
        } else if (currentStage == Stage.BOARD_OMEGA) {
            return getMapLocationFor(system3.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Ascension";
    }
}
