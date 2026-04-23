package data.scripts.campaign.intel.missions.RossQuests;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.TASK_FORCE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.campaign.intel.missions.dpl_Artworks.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

public class dpl_BeatTheClock extends HubMissionWithBarEvent implements FleetEventListener {

	public static float MISSION_DAYS = 180f;	
	
	public static enum Stage {
		GO_TO_OUTPOST,
		KILL_FLEET,
		BOARD_DERELICT,
		RETURN_TO_FACTORY,
		COMPLETED,
		FAILED,
	}
	
	protected CampaignFleetAPI ark_fleet;
	protected CampaignFleetAPI target;
	protected PlanetAPI planet;
	protected MarketAPI factory;
	protected PersonAPI attackers_leader;
	protected PersonAPI ares_arcturus;
	protected StarSystemAPI system2;
	protected Vector2f last_loc_ark;
    protected SectorEntityToken wreck;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if this mission type was already accepted by the player, abort
		if (!setGlobalReference("$dpl_btcl_ref")) {
			return false;
		}
		
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_btcl_TakenAndFailed")) {
			return false;
		}
		
		PersonAPI person = getPerson();
        if (person == null) return false;
        factory = person.getMarket();
        if (factory == null) return false;
        if (!factory.getFactionId().equals("dpl_phase_lab")) return false;
        
        ares_arcturus = getImportantPerson("ares_arcturus");
		if (ares_arcturus == null) return false;
		
		// set up the leader
        attackers_leader = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        attackers_leader.setRankId(Ranks.SPECIAL_AGENT);
        attackers_leader.setPostId(Ranks.POST_SPECIAL_AGENT);
        attackers_leader.getMemoryWithoutUpdate().set("$dpl_tlwf_leader", true);
        
        attackers_leader.getStats().setLevel(8);
        attackers_leader.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        attackers_leader.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        attackers_leader.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        attackers_leader.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        attackers_leader.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        attackers_leader.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
        attackers_leader.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
        attackers_leader.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
        attackers_leader.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		
        system2 = Global.getSector().getStarSystem("helheim");
        if (system2 == null) return false;
        
		resetSearch();
		preferSystemInteresting();
		preferSystemOnFringeOfSector();
		preferSystemUnexplored();
		requirePlanetNotStar();
		requirePlanetUnpopulated();
		requirePlanetNotGasGiant();
		preferPlanetNotFullySurveyed();
		preferPlanetInDirectionOfOtherMissions();

		planet = pickPlanet();
		
		if (planet == null) {
			return false;
		}
		
		beginStageTrigger(Stage.GO_TO_OUTPOST);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				if (!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$dpl_btcl_spawnedStation")) {
					spawn_derelicts();
					Global.getSector().getPlayerMemoryWithoutUpdate().set("$dpl_btcl_spawnedStation", true);
				}
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_ark_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.RETURN_TO_FACTORY);
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
				Global.getSector().getMemoryWithoutUpdate().set("$dpl_btcl_TakenAndFailed", true);
			}
		});
        endTrigger();
		
		setStartingStage(Stage.GO_TO_OUTPOST);
		addSuccessStages(Stage.COMPLETED);
		addFailureStages(Stage.FAILED);
		
		makeImportant(getPerson(), "$dpl_btcl_returnHere", Stage.RETURN_TO_FACTORY);
		
		setStageOnGlobalFlag(Stage.KILL_FLEET, "$dpl_btcl_knowArkLoc");
		setStageOnGlobalFlag(Stage.BOARD_DERELICT, "$dpl_btcl_won");
		setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_btcl_gotSOC");
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_btcl_completed");
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null);
        setCreditReward(1000000);
		
		return true;
	}

	protected void updateInteractionDataImpl() {
		set("$dpl_btcl_planetId", planet.getId());
		set("$dpl_btcl_planetName", planet.getName());
		set("$dpl_btcl_systemName", planet.getStarSystem().getNameWithNoType());
		set("$dpl_btcl_dist", getDistanceLY(planet));
		set("$dpl_btcl_system2Name", system2.getNameWithNoType());
		set("$dpl_btcl_reward", Misc.getWithDGS(getCreditsReward()));
	}
	
	protected void Victory() {
		Vector2f loc = last_loc_ark;
    	wreck = dpl_phase_labAddEntities.spawnUniqueWreck(loc, system2, Factions.DERELICT, "dpl_ark_Hull", "Ark", false);
    	Misc.makeImportant(wreck, "$dpl_btcl");
    	wreck.getMemoryWithoutUpdate().set("$dpl_btcl_derelict", true);
        setEntityMissionRef(wreck, "$dpl_btcl_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_btcl_won", true);
	}
	
	protected void spawn_ark_fleet() {
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.DERELICT, "", true);
		fleet.getFleetData().addFleetMember("dpl_ark_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("Ark");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.DERELICT,
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
    	
    	ark_fleet = FleetFactoryV3.createFleet(params);
    	ark_fleet.setName("Ark Fleet");
    	ark_fleet.setNoFactionInName(true);
    	ark_fleet.getFleetData().addFleetMember(member);
    	ark_fleet.getFleetData().setFlagship(member);
    	member.setCaptain(ares_arcturus);
    	ark_fleet.getFleetData().sort();
    	ark_fleet.addEventListener(this);
    	List<FleetMemberAPI> members = ark_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.getShipName().equalsIgnoreCase("Ark")) {
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
				PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, getGenRandom());
				curr.setCaptain(person);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(ark_fleet);
		Misc.makeNoRepImpact(ark_fleet, "$dpl_btcl");
        Misc.makeImportant(ark_fleet, "$dpl_btcl");

        ark_fleet.getMemoryWithoutUpdate().set("$dpl_btcl_arkfleet", true);
        ark_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        ark_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        ark_fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        ark_fleet.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, system2.getCenter(), 200f, null);
        system2.addEntity(ark_fleet);
        Vector2f pos = system2.getCenter().getLocation();
        ark_fleet.setLocation(pos.x+3500f, pos.y+3500f);
	}
	
	protected void despawn_fleets() {
    	if (ark_fleet != null) {
    		ark_fleet.getAI().clearAssignments();
    		ark_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getCenter(), 200f, null);
        }
    }
	
	protected void spawn_derelicts() {
		CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec("dpl_station_research");
        float orbitRadius = planet.getRadius() + spec.getDefaultRadius() + 100f;
        float orbitDays = orbitRadius / 20f;
        SectorEntityToken entity;
        entity = BaseThemeGenerator.addSalvageEntity(planet.getStarSystem(), "dpl_station_research", Factions.NEUTRAL);
        entity.setCircularOrbitPointingDown(planet, 270f, orbitRadius, orbitDays);
        CampaignFleetAPI defenders = CreateDefenders();
        entity.getMemory().set("$defenderFleet", defenders);
        entity.getMemory().set("$hasDefenders", true);
        
        Misc.makeImportant(entity, "$dpl_btcl_targetStation");
        entity.getMemoryWithoutUpdate().set("$dpl_btcl_targetStation", true);
        setEntityMissionRef(entity, "$dpl_btcl_ref");
    }
	
	protected CampaignFleetAPI CreateDefenders() {
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.REMNANTS, "", true);
		fleet.getFleetData().addFleetMember("dpl_radiant_boss_assault");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("Blood Star");
		
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE);
		PersonAPI person = plugin.createPerson(Commodities.ALPHA_CORE, Factions.REMNANTS, getGenRandom());
		member.setCaptain(person);
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.INDEPENDENT,
                null,
                PATROL_LARGE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	
    	List<String> AddShips = new ArrayList<>();
    	for (int i=0; i<10; i++) {
    		AddShips.add("dpl_super_hyperion_standard");
    	}
    	AddShips.add("dpl_super_hyperion_flagship");
    	AddShips.add("dpl_super_hyperion_flagship");
    	params.addShips = AddShips;
    	
    	params.averageSMods = 3;
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName("The Darktides");
        target.setNoFactionInName(true);
        target.addTag("dpl_btcl_attackerFleet");
        
        target.setCommander(attackers_leader);
        target.getFlagship().setCaptain(attackers_leader);
		target.getFlagship().getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		
		target.getFleetData().addFleetMember(member);
		
        List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.isFlagship() && !curr.getVariant().hasHullMod("automated")) {
				PersonAPI person1 = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
				person1.setPersonality(Personalities.RECKLESS);
				person1.getStats().setLevel(6);
				person1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
				person1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
				person1.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 2);
				person1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
				person1.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
				person1.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
				person1.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
				person1.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
				person1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
				person1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		        
				curr.setVariant(curr.getVariant().clone(), false, false);
				curr.getVariant().setSource(VariantSource.REFIT);
				curr.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
				curr.setCaptain(person1);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        Misc.makeHostile(target);
        return target;
    }
	
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;

        if (fleet.equals(ark_fleet)) {
			Victory();
		}
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();
        boolean isKeyFleetsInvolved = (!battle.isInvolved(ark_fleet) || battle.onPlayerSide(ark_fleet)) && (!battle.isInvolved(ark_fleet) || battle.onPlayerSide(ark_fleet));

        if (!playerInvolved || isKeyFleetsInvolved) {
            return;
        }
        
        if (ark_fleet != null) {
        	last_loc_ark = ark_fleet.getLocation();
        }
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextPre(planet) +
					", and recover data from a fallen research station located there.", opad);
		} else if (currentStage == Stage.KILL_FLEET) {
			info.addPara("Eliminate Ares' forces in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
		} else if (currentStage == Stage.BOARD_DERELICT) {
			info.addPara("Explore the derelict of Ares' flagship in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
		} else if (currentStage == Stage.RETURN_TO_FACTORY) {
			info.addPara("Return to Lab Factory and talk to " + 
						 getPerson().getNameString() + " to receive your reward.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_OUTPOST) {
			info.addPara(getGoToPlanetTextShort(planet), tc, pad);
			return true;
		} else if (currentStage == Stage.KILL_FLEET) {
			info.addPara("Eliminate Ares' forces in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", tc, pad);
			return true;
		} else if (currentStage == Stage.BOARD_DERELICT) {
			info.addPara("Explore the derelict of Ares' flagship in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_FACTORY) {
			info.addPara("Return to the Lab Factory and talk to " + getPerson().getNameString(), tc, pad);
			return true;
		}
		return false;
	}
	
	@Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.GO_TO_OUTPOST) {
            return getMapLocationFor(planet.getStarSystem().getCenter());
        } else if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.BOARD_DERELICT) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(factory.getStarSystem().getCenter());
        }
        return null;
    }

	@Override
	public String getBaseName() {
		return "Beat The Clock";
	}
}





