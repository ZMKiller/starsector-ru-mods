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
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Celebration.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;

public class dpl_Justice extends HubMissionWithBarEvent implements FleetEventListener {

    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        FIND_CLUE,
        GO_TO_RSV,
        GO_TO_CHICOMOZTOC,
        KILL_FLEETS,
        BOARD_THE_WRECK,
        RETURN_TO_RSV,
        TURN_IN_NELSON,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected boolean spawnedNelsonFleet;
    protected Set<CampaignFleetAPI> strikeFleets = new HashSet<>();
    
    protected CampaignFleetAPI station;
    protected CampaignFleetAPI banach_fleet;
    protected CampaignFleetAPI TT_fleet;
    protected CampaignFleetAPI PL_fleet;
    protected CampaignFleetAPI SD_fleet;
    protected CampaignFleetAPI LC_fleet;
    protected CampaignFleetAPI Heg_fleet1;
    protected CampaignFleetAPI Heg_fleet2;
    protected CampaignFleetAPI Heg_fleet3;
    protected CampaignFleetAPI nelson_fleet;
    protected CampaignFleetAPI PI_fleet1;
    protected CampaignFleetAPI PI_fleet2;
    protected CampaignFleetAPI PI_fleet3;
    protected CampaignFleetAPI PI_fleet4;
    protected CampaignFleetAPI PI_fleet5;
    protected CampaignFleetAPI PI_fleet6;
    protected CampaignFleetAPI PI_fleet7;
    protected CampaignFleetAPI PI_fleet8;
    protected CampaignFleetAPI PI_fleet9;
    protected SectorEntityToken wreck;
	protected MarketAPI chicomoztoc;
	protected MarketAPI market;
	protected PersonAPI daud;
	protected PersonAPI person;
    protected PersonAPI banach_salazar;
    protected PersonAPI nelson_bonaparte;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected StarSystemAPI system3;
    protected Vector2f last_loc_nelson;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        person = getPerson();
        if (person == null) return false;
        
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_justice_TakenAndFailed")) return false;
        
        market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        banach_salazar = getImportantPerson("banach_salazar");
		if (banach_salazar == null) return false;
		
        nelson_bonaparte = getImportantPerson("nelson_bonaparte");
        if (nelson_bonaparte == null) return false;
        
        daud = getImportantPerson(People.DAUD);
		if (daud == null) return false;
		
        chicomoztoc = Global.getSector().getEconomy().getMarket("chicomoztoc");
		if (chicomoztoc == null) return false;
		if (!chicomoztoc.getFactionId().equals(Factions.HEGEMONY)) return false;

        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_justice_ref")) {
            return false;
        }

        // pick the system with the clues inside
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemInInnerSector();
        preferSystemUnexplored();
        preferSystemInDirectionOfOtherMissions();

        system = pickSystem(true);
        if (system == null) return false;

        system2 = person.getMarket().getStarSystem();
        
        system3 = chicomoztoc.getStarSystem();

        beginStageTrigger(Stage.FIND_CLUE);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_station();
			}
		});
        endTrigger(); 
        
        beginStageTrigger(Stage.KILL_FLEETS);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_Heg_fleet1();
				spawn_Heg_fleet2();
				spawn_Heg_fleet3();
			}
		});
        triggerRunScriptAfterDelay(1, new Script() {
			@Override
			public void run() {
				spawn_banach_fleet();
				spawn_TT_fleet();
				spawn_PL_fleet();
				spawn_SD_fleet();
				spawn_LC_fleet();
			}
		});
        triggerRunScriptAfterDelay(6, new Script() {
			@Override
			public void run() {
				spawn_PI_fleet1();
				spawn_PI_fleet2();
				spawn_PI_fleet3();
				spawn_PI_fleet4();
				spawn_PI_fleet5();
				spawn_PI_fleet6();
				spawn_PI_fleet7();
				spawn_PI_fleet8();
				spawn_PI_fleet9();
				spawnedNelsonFleet = false;
			}
		});
        triggerRunScriptAfterDelay(10, new Script() {
			@Override
			public void run() {
				spawn_nelson_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.BOARD_THE_WRECK);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_fleets();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
	        	Misc.makeNonStoryCritical(chicomoztoc, "dpl_Justice");
			}
		});
		endTrigger();
        
        beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				ramIntoPlanet();
			}
		});
        endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_justice_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.FIND_CLUE);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.GO_TO_RSV, "$dpl_justice_destroyedFactory");
        makeImportant(market, "$dpl_justice", Stage.GO_TO_RSV);
		makeImportant(person, "$dpl_justice", Stage.GO_TO_RSV);
        setStageOnGlobalFlag(Stage.GO_TO_CHICOMOZTOC, "$dpl_justice_reported");
        makeImportant(chicomoztoc, "$dpl_justice", Stage.GO_TO_CHICOMOZTOC);
		makeImportant(daud, "$dpl_justice", Stage.GO_TO_CHICOMOZTOC);
        setStageOnGlobalFlag(Stage.KILL_FLEETS, "$dpl_justice_discussed");
        setStageOnGlobalFlag(Stage.BOARD_THE_WRECK, "$dpl_justice_won");
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_justice_arrested");
        makeImportant(market, "$dpl_justice", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_justice", Stage.RETURN_TO_RSV);
        setStageOnGlobalFlag(Stage.TURN_IN_NELSON, "$dpl_justice_delivered");
        makeImportant(chicomoztoc, "$dpl_justice", Stage.TURN_IN_NELSON);
        setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_justice_completed");
        setStageOnGlobalFlag(Stage.FAILED, "$dpl_justice_failed");
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.KILL_FLEETS);
        setCreditReward(200000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_justice_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_justice_execName and automatically insert the disgraced executive's name
    protected void updateInteractionDataImpl() {
        set("$dpl_justice_barEvent", isBarEvent());
        set("$dpl_justice_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_justice_heOrShe", getPerson().getHeOrShe());
        set("$dpl_justice_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_justice_personName", getPerson().getNameString());
        set("$dpl_justice_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_justice_system2Name", system2.getNameWithLowercaseTypeShort());
        set("$dpl_justice_dist", getDistanceLY(system));
    }
    
    public void spawn_station(){
    	Vector2f loc = system.getCenter().getLocation();
    	station = dpl_phase_labAddEntities.spawnStation(loc.x + 2000f, loc.y + 2000f, system, "dpl_factory_station_standard", "dpl_persean_imperium");
    	Misc.makeHostile(station);
    	Misc.makeImportant(station, "$dpl_justice");
    	FleetMemberAPI ship_station = station.getFlagship();
    	ship_station.setShipName("Valhalla");
    	station.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
    	station.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_justice");
    	station.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_justice");
    	station.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
    	station.getMemoryWithoutUpdate().set("$dpl_justice_factory", true);
    	station.addEventListener(this);
    }
    
    public void spawn_banach_fleet() {
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_phase_lab", "", true);
		fleet.getFleetData().addFleetMember("dpl_cimbasso_banach_AA");
		FleetMemberAPI member = fleet.getFlagship();
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_phase_lab",
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
    	
    	CampaignFleetAPI B_fleet = FleetFactoryV3.createFleet(params);
    	B_fleet.getFleetData().addFleetMember(member);
    	B_fleet.getFleetData().setFlagship(member);
    	B_fleet.getFleetData().addFleetMember("dpl_cimbasso_banach_AA");
    	B_fleet.getFleetData().sort();
    	FleetMemberAPI B_member = B_fleet.getFlagship();
    	
    	FleetParamsV3 true_params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	banach_fleet = FleetFactoryV3.createFleet(true_params);
    	List<FleetMemberAPI> B_members = B_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : B_members) {
			banach_fleet.getFleetData().addFleetMember(curr);
		}
    	banach_fleet.getFleetData().setFlagship(B_member);
    	banach_fleet.setCommander(banach_salazar);
    	banach_fleet.getFlagship().setCaptain(banach_salazar);
    	banach_fleet.setName("United Fleet (Phase Lab)");
    	banach_fleet.setNoFactionInName(true);
    	banach_fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = banach_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeImportant(banach_fleet, "$dpl_justice");
        setEntityMissionRef(banach_fleet, "$dpl_justice_ref");
        banach_fleet.getMemoryWithoutUpdate().set("$dpl_justice_banachfleet", true);
        banach_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        banach_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(banach_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        banach_fleet.setLocation(pos.x, pos.y+1500f);
    }
    
    public void spawn_TT_fleet() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	CampaignFleetAPI B_fleet = FleetFactoryV3.createFleet(params);
    	
    	FleetParamsV3 true_params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	TT_fleet = FleetFactoryV3.createFleet(true_params);
    	List<FleetMemberAPI> B_members = B_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : B_members) {
			TT_fleet.getFleetData().addFleetMember(curr);
		}
		List<FleetMemberAPI> members = TT_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		TT_fleet.getFleetData().sort();
		
		TT_fleet.setName("United Fleet (Tri-Tachyon)");
		TT_fleet.setNoFactionInName(true);

		TT_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		TT_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
		TT_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
		system3.addEntity(TT_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        TT_fleet.setLocation(pos.x, pos.y+1500f);
    }
    
    public void spawn_PL_fleet() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.PERSEAN,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	CampaignFleetAPI B_fleet = FleetFactoryV3.createFleet(params);
    	
    	FleetParamsV3 true_params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	PL_fleet = FleetFactoryV3.createFleet(true_params);
    	List<FleetMemberAPI> B_members = B_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : B_members) {
			PL_fleet.getFleetData().addFleetMember(curr);
		}
		List<FleetMemberAPI> members = PL_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		PL_fleet.getFleetData().sort();
		
		PL_fleet.setName("United Fleet (Persean League)");
		PL_fleet.setNoFactionInName(true);

		PL_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		PL_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
		PL_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
		system3.addEntity(PL_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PL_fleet.setLocation(pos.x, pos.y+1500f);
    }
    
    public void spawn_SD_fleet() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.DIKTAT,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	CampaignFleetAPI B_fleet = FleetFactoryV3.createFleet(params);
    	
    	FleetParamsV3 true_params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	SD_fleet = FleetFactoryV3.createFleet(true_params);
    	List<FleetMemberAPI> B_members = B_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : B_members) {
			SD_fleet.getFleetData().addFleetMember(curr);
		}
		List<FleetMemberAPI> members = SD_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		SD_fleet.getFleetData().sort();
		
		SD_fleet.setName("United Fleet (Sindrian Diktat)");
		SD_fleet.setNoFactionInName(true);

		SD_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		SD_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
		SD_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
		system3.addEntity(SD_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        SD_fleet.setLocation(pos.x, pos.y+1500f);
    }
    
    public void spawn_LC_fleet() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.LUDDIC_CHURCH,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	CampaignFleetAPI B_fleet = FleetFactoryV3.createFleet(params);
    	
    	FleetParamsV3 true_params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	LC_fleet = FleetFactoryV3.createFleet(true_params);
    	List<FleetMemberAPI> B_members = B_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : B_members) {
			LC_fleet.getFleetData().addFleetMember(curr);
		}
		List<FleetMemberAPI> members = LC_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		LC_fleet.getFleetData().sort();
		
		LC_fleet.setName("United Fleet (Luddic Church)");
		LC_fleet.setNoFactionInName(true);

		LC_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		LC_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
		LC_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
		system3.addEntity(LC_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        LC_fleet.setLocation(pos.x, pos.y+1500f);
    }
    
    public void spawn_Heg_fleet1() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	Heg_fleet1 = FleetFactoryV3.createFleet(params);
    	
    	Heg_fleet1.setName("United Fleet (Hegemony TF-06)");
    	Heg_fleet1.setNoFactionInName(true);

    	Heg_fleet1.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
    	Heg_fleet1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
    	Heg_fleet1.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
    	system3.addEntity(Heg_fleet1);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        Heg_fleet1.setLocation(pos.x+1000f, pos.y+1200f);
    }
    
    public void spawn_Heg_fleet2() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	Heg_fleet2 = FleetFactoryV3.createFleet(params);
    	
    	Heg_fleet2.setName("United Fleet (Hegemony TF-18)");
    	Heg_fleet2.setNoFactionInName(true);

    	Heg_fleet2.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
    	Heg_fleet2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
    	Heg_fleet2.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
    	system3.addEntity(Heg_fleet2);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        Heg_fleet2.setLocation(pos.x+1200f, pos.y+1000f);
    }
    
    public void spawn_Heg_fleet3() {
    	
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.HEGEMONY,
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	Heg_fleet3 = FleetFactoryV3.createFleet(params);
    	
    	Heg_fleet3.setName("United Fleet (Hegemony TF-21)");
    	Heg_fleet3.setNoFactionInName(true);

    	Heg_fleet3.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
    	Heg_fleet3.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
    	Heg_fleet3.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
    	system3.addEntity(Heg_fleet3);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        Heg_fleet3.setLocation(pos.x+1200f, pos.y+1200f);
    }
    
    public void spawn_nelson_fleet() {
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "", true);
		fleet.getFleetData().addFleetMember("dpl_theater_organ_boss_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("PIS Imperium Doom Star");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                80f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 3;
    	nelson_fleet = FleetFactoryV3.createFleet(params);
    	nelson_fleet.setName(nelson_bonaparte.getNameString() + "'s Doom Armada");
    	nelson_fleet.getFleetData().addFleetMember(member);
    	nelson_fleet.getFleetData().setFlagship(member);
    	nelson_fleet.setNoFactionInName(true);
    	nelson_fleet.setCommander(nelson_bonaparte);
    	nelson_fleet.getFlagship().setCaptain(nelson_bonaparte);
    	nelson_fleet.getFleetData().sort();
    	nelson_fleet.addEventListener(this);
    	strikeFleets.add(nelson_fleet);
    	List<FleetMemberAPI> members = nelson_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(nelson_fleet);
		Misc.makeHostileToFaction(nelson_fleet, Factions.HEGEMONY, 200f);
        Misc.makeImportant(nelson_fleet, "$dpl_justice");

        nelson_fleet.getMemoryWithoutUpdate().set("$dpl_justice_chairfleet", true);
        nelson_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        nelson_fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        nelson_fleet.addAssignment(FleetAssignment.DELIVER_FUEL, chicomoztoc.getPlanetEntity(), 60f);
        nelson_fleet.addAssignment(FleetAssignment.HOLD, chicomoztoc.getPlanetEntity(), 0.25f, "Attacking Chicomoztoc", 
        		new Script() {
					public void run() {
						checkRamPlanet(nelson_fleet, chicomoztoc);
					}
				});
        system3.addEntity(nelson_fleet);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        nelson_fleet.setLocation(pos.x+3000f, pos.y);
        spawnedNelsonFleet = true;
    }

	public void spawn_PI_fleet1() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet1 = FleetFactoryV3.createFleet(params);
    	PI_fleet1.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet1.setNoFactionInName(true);
    	strikeFleets.add(PI_fleet1);
    	PI_fleet1.addEventListener(this);
    	Misc.makeHostile(PI_fleet1);
    	Misc.makeHostileToFaction(PI_fleet1, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet1, "$dpl_justice");

        PI_fleet1.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet1.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet1.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet1);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet1.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet2() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet2 = FleetFactoryV3.createFleet(params);
    	PI_fleet2.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet2.setNoFactionInName(true);
    	strikeFleets.add(PI_fleet2);
    	PI_fleet2.addEventListener(this);
    	Misc.makeHostile(PI_fleet2);
    	Misc.makeHostileToFaction(PI_fleet2, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet2, "$dpl_justice");

        PI_fleet2.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet2.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet2.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet2);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet2.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet3() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet3 = FleetFactoryV3.createFleet(params);
    	PI_fleet3.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet3.setNoFactionInName(true);
    	PI_fleet3.addEventListener(this);
    	strikeFleets.add(PI_fleet3);
    	Misc.makeHostile(PI_fleet3);
    	Misc.makeHostileToFaction(PI_fleet3, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet3, "$dpl_justice");

        PI_fleet3.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet3.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet3.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet3);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet3.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet4() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet4 = FleetFactoryV3.createFleet(params);
    	PI_fleet4.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet4.setNoFactionInName(true);
    	PI_fleet4.addEventListener(this);
    	strikeFleets.add(PI_fleet4);
    	Misc.makeHostile(PI_fleet4);
    	Misc.makeHostileToFaction(PI_fleet4, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet4, "$dpl_justice");

        PI_fleet4.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet4.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet4.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet4);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet4.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet5() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet5 = FleetFactoryV3.createFleet(params);
    	PI_fleet5.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet5.setNoFactionInName(true);
    	strikeFleets.add(PI_fleet5);
    	PI_fleet5.addEventListener(this);
    	Misc.makeHostile(PI_fleet5);
    	Misc.makeHostileToFaction(PI_fleet5, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet5, "$dpl_justice");

        PI_fleet5.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet5.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet5.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet5);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet5.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet6() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet6 = FleetFactoryV3.createFleet(params);
    	PI_fleet6.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet6.setNoFactionInName(true);
    	PI_fleet6.addEventListener(this);
    	strikeFleets.add(PI_fleet6);
    	Misc.makeHostile(PI_fleet6);
    	Misc.makeHostileToFaction(PI_fleet6, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet6, "$dpl_justice");

        PI_fleet6.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet6.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet6.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet6);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet6.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet7() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet7 = FleetFactoryV3.createFleet(params);
    	PI_fleet7.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet7.setNoFactionInName(true);
    	PI_fleet7.addEventListener(this);
    	strikeFleets.add(PI_fleet7);
    	Misc.makeHostile(PI_fleet7);
    	Misc.makeHostileToFaction(PI_fleet7, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet7, "$dpl_justice");

        PI_fleet7.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet7.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet7.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet7);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet7.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet8() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet8 = FleetFactoryV3.createFleet(params);
    	PI_fleet8.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet8.setNoFactionInName(true);
    	PI_fleet8.addEventListener(this);
    	strikeFleets.add(PI_fleet8);
    	Misc.makeHostile(PI_fleet8);
    	Misc.makeHostileToFaction(PI_fleet8, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet8, "$dpl_justice");

        PI_fleet8.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet8.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet8.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet8);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet8.setLocation(pos.x+3000f, pos.y);
    }
    
    public void spawn_PI_fleet9() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                TASK_FORCE,
                240f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	params.averageSMods = 1;
    	PI_fleet9 = FleetFactoryV3.createFleet(params);
    	PI_fleet9.setName("Helheim Supremacy Strike Fleet");
    	PI_fleet9.setNoFactionInName(true);
    	PI_fleet9.addEventListener(this);
    	strikeFleets.add(PI_fleet9);
    	Misc.makeHostile(PI_fleet9);
    	Misc.makeHostileToFaction(PI_fleet9, Factions.HEGEMONY, 200f);
        Misc.makeImportant(PI_fleet9, "$dpl_justice");

        PI_fleet9.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        PI_fleet9.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_justice");
        PI_fleet9.getAI().addAssignment(FleetAssignment.ATTACK_LOCATION, chicomoztoc.getPlanetEntity(), 200f, null);
        system3.addEntity(PI_fleet9);
        Vector2f pos = chicomoztoc.getPlanetEntity().getLocation();
        PI_fleet9.setLocation(pos.x+3000f, pos.y);
    }
    
    public void despawn_fleets() {
    	if (banach_fleet != null) {
        	banach_fleet.getAI().clearAssignments();
        	banach_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, chicomoztoc.getPrimaryEntity(), 200f, null);
        }
    	if (TT_fleet != null) {
    		TT_fleet.getAI().clearAssignments();
    		TT_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, chicomoztoc.getPrimaryEntity(), 200f, null);
        }
    	if (PL_fleet != null) {
    		PL_fleet.getAI().clearAssignments();
    		PL_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, chicomoztoc.getPrimaryEntity(), 200f, null);
        }
    	if (SD_fleet != null) {
    		SD_fleet.getAI().clearAssignments();
    		SD_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, chicomoztoc.getPrimaryEntity(), 200f, null);
        }
    	if (LC_fleet != null) {
    		LC_fleet.getAI().clearAssignments();
    		LC_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, chicomoztoc.getPrimaryEntity(), 200f, null);
        }
    }
    
    public void checkVictory() {
		boolean won = spawnedNelsonFleet && strikeFleets.isEmpty();
		
		if (won) {
			Victory();
		} else {
			return;
		}
	}
    
    public void Victory() {
    	Vector2f loc = last_loc_nelson;
    	StarSystemAPI aztlan = chicomoztoc.getStarSystem();
    	wreck = dpl_phase_labAddEntities.spawnUniqueWreck(loc, aztlan, "dpl_persean_imperium", "dpl_theater_organ_Hull", "PIS Imperium Doom Star", true);
    	Misc.makeImportant(wreck, "$dpl_justice");
    	wreck.getMemoryWithoutUpdate().set("$dpl_justice_derelict", true);
        setEntityMissionRef(wreck, "$dpl_justice_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_justice_won", true);
    }
    
    protected void convertPlanet(PlanetAPI planet) {
    	planet.changeType("barren-bombarded", null);
    	planet.setCustomDescriptionId("barren-bombarded");
    	chicomoztoc.removeCondition(Conditions.HABITABLE);
    	chicomoztoc.removeCondition(Conditions.ORE_MODERATE);
    	chicomoztoc.removeCondition(Conditions.RARE_ORE_SPARSE);
    	chicomoztoc.removeCondition(Conditions.FARMLAND_POOR);
    	chicomoztoc.addCondition(Conditions.HOT);
    	chicomoztoc.addCondition(Conditions.EXTREME_WEATHER);
    	chicomoztoc.addCondition(Conditions.DECIVILIZED);
        // keep the pre-existing ruins
    	chicomoztoc.addCondition(Conditions.RUINS_WIDESPREAD);
    }

    protected void ramIntoPlanet() {
        PlanetAPI planet = chicomoztoc.getPlanetEntity();
        MarketCMD.addBombardVisual(planet);
        
        for (Object o : chicomoztoc.getMemoryWithoutUpdate().getKeys().toArray()) {
            String memFlag = (String) o;
            if (memFlag.startsWith(MemFlags.STORY_CRITICAL)) {
                market.getMemoryWithoutUpdate().unset(memFlag);
            }
        }

        DecivTracker.decivilize(chicomoztoc, true, true);

        // relationship effects
        FactionAPI hegemony = Global.getSector().getFaction(Factions.HEGEMONY);
        FactionAPI dpl_persean_imperium = Global.getSector().getFaction("dpl_persean_imperium");
        FactionAPI dpl_phase_lab = person.getFaction();
        hegemony.setRelationship(dpl_persean_imperium.getId(), -1f);
        hegemony.setRelationship(dpl_phase_lab.getId(), -0.8f);

        convertPlanet(planet);

        sendUpdateIfPlayerHasIntel(null, false);
        
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_justice_failed", true);
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_justice_TakenAndFailed", true);
    }
    
    protected void checkRamPlanet(CampaignFleetAPI fleet, MarketAPI target) {
        if (fleet != null) {
            // if not in actual contact with the target, try again
            if (Misc.getDistance(fleet.getLocation(), target.getPlanetEntity().getLocation()) > 500)
                return;

            // engage any nearby fleets before attempting Destroying the planet
            for (CampaignFleetAPI otherFleet : target.getContainingLocation().getFleets()) {
                if (otherFleet.equals(fleet)) continue;
                if (!otherFleet.isHostileTo(fleet)) continue;
                if (Misc.getDistance(fleet.getLocation(), otherFleet.getLocation()) > 350f) continue;

                // engage station if there's one
                if (otherFleet.equals(Misc.getStationFleet(target)) || otherFleet.equals(banach_fleet)) {
                    Global.getFactory().createBattle(fleet, otherFleet);
                    return;
                }
            }
        }
        ramIntoPlanet();
    }

    // used to detect when the fleets are destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    	if (isDone() || result != null) return;
    	
    	if (!battle.isInvolved(station) && !battle.isInvolved(nelson_fleet)) {
    		return;
    	}
    	
    	if (station != null) {
    		if (station.getFlagship() == null) {
	    		Global.getSector().getMemoryWithoutUpdate().set("$dpl_justice_destroyedFactory", true);
	    	}
    	}
    	
    	if (nelson_fleet != null) {
        	last_loc_nelson = nelson_fleet.getLocation();
    	}
	}

    // must kill them all
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) return;
        if (strikeFleets.contains(fleet)) {
        	strikeFleets.remove(fleet);
			checkVictory();
		}
    }

    // description when selected in intel screen
    @Override
    
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.FIND_CLUE) {
            info.addPara("Find out what 'Valhalla' is and destroy it if necessary. It's located in " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Return to Research Site V to report the situations.", opad);
        } else if (currentStage == Stage.GO_TO_CHICOMOZTOC) {
            info.addPara("Report to Chicomoztoc and discuss with the High Hegemon about details of defence plan.", opad);
        } else if (currentStage == Stage.KILL_FLEETS) {
            info.addPara("Defend Chicomoztoc and engage enemy fleets.", opad);
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            info.addPara("Investigate the wreck of the Theater Organ class space ship.", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to report the victory to Eliza.", opad);
        } else if (currentStage == Stage.TURN_IN_NELSON) {
            info.addPara("Go to Chicomoztoc and turn Nelson in. The portmaster should be able to handle this.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: VALHALLA IS LOCATED IN THE " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.FIND_CLUE) {
            info.addPara("Find and destroy 'Valhalla' in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_RSV) {
            info.addPara("Report to Research Site V.", tc, pad);
            return true;
        } else if (currentStage == Stage.GO_TO_CHICOMOZTOC) {
            info.addPara("Report to Chicomoztoc.", tc, pad);
            return true;
        } else if (currentStage == Stage.KILL_FLEETS) {
            info.addPara("Destroy enemy fleets.", tc, pad);
            return true;
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            info.addPara("Board the wreck.", tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Report to Research Site V.", tc, pad);
            return true;
        } else if (currentStage == Stage.TURN_IN_NELSON) {
            info.addPara("Go to Chicomoztoc and turn Nelson in.", tc, pad);
            return true;
        }
        return false;
    }
    
    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (currentStage == Stage.FIND_CLUE) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.GO_TO_RSV) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.GO_TO_CHICOMOZTOC) {
            return getMapLocationFor(system3.getCenter());
        } else if (currentStage == Stage.KILL_FLEETS) {
            return getMapLocationFor(system3.getCenter());
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            return getMapLocationFor(system3.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.TURN_IN_NELSON) {
            return getMapLocationFor(system3.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Justice";
    }
}
