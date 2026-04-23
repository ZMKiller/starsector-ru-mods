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
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.Market;

import data.scripts.campaign.intel.missions.dpl_Pilgrimage.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_Artworks extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission

    // mission stages
    public static enum Stage {
    	MEET_BANACH,
        KILL_FLEET,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected Random genRandom = null;
    protected CampaignFleetAPI target;
    protected CampaignFleetAPI banach_fleet;
    protected PersonAPI banach_salazar;
    protected PersonAPI commander1;
    protected PersonAPI commander2;
    protected PlanetAPI research_site_v;
	protected MarketAPI market;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    public static float MISSION_DAYS = 180f;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        
        banach_salazar = getImportantPerson("banach_salazar");
		if (banach_salazar == null) return false;
		
		market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
     // pick the system.
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemUnexplored();
        preferSystemInDirectionOfOtherMissions();

        system = pickSystem(true);
        if (system == null) return false;
        
        system2 = market.getStarSystem();
        research_site_v = market.getPlanetEntity();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_artworks_ref")) {
            return false;
        }

        beginStageTrigger(Stage.MEET_BANACH);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_banach_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.KILL_FLEET);
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
        if (!setGlobalReference("$dpl_artworks_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.MEET_BANACH);
        setSuccessStage(Stage.COMPLETED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.KILL_FLEET, "$dpl_artworksMetSalazar");
        setStageOnMemoryFlag(Stage.RETURN_TO_RSV, person, "$dpl_artworks_killed");
        makeImportant(market, "$dpl_artworks", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_artworks", Stage.RETURN_TO_RSV);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_artworks_completed");
        setFailureStage(Stage.FAILED);
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_RSV);
        setCreditReward(250000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_omega_fleet() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.REMNANTS,
                null,
                PATROL_LARGE,
                300f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                3f // qualityMod
        );
    	
    	params.averageSMods = 3;
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName("Unknown Fleet");
        target.setNoFactionInName(true);
        
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.OMEGA, "f1", true);
		fleet.getFleetData().addFleetMember("facet_Attack");
		FleetMemberAPI member = fleet.getFlagship();
		
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, genRandom);
		member.setCaptain(person);
		
		target.setCommander(person);
		target.getFleetData().addFleetMember(member);
		
		target.getFleetData().sort();
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		member.setVariant(member.getVariant().clone(), false, false);
		member.getVariant().setSource(VariantSource.REFIT);
		member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);

        Misc.makeHostile(target);
        Misc.makeHostileToFaction(target, "dpl_phase_lab", 200f);
        Misc.makeNoRepImpact(target, "$dpl_artworks");
        Misc.makeImportant(target, "$dpl_artworks");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_artworks");
        // otherwise, remnant dialog which isn't appropriate with an Omega in charge
     	target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_artworks_omegafleet", true);
        target.getAI().addAssignment(FleetAssignment.INTERCEPT, banach_fleet, 200f, null);
        target.addEventListener(this);
        system.addEntity(target);
        Vector2f pos = banach_fleet.getLocation();
        target.setLocation(pos.x-500f, pos.y);
    }
    
    public void spawn_banach_fleet() {
    	//Set up the first commander
        commander1 = Global.getSector().getFaction("dpl_phase_lab").createRandomPerson();
        commander1.setPersonality(Personalities.RECKLESS);
        commander1.getStats().setLevel(6);
        commander1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        commander1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        commander1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        commander1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        commander1.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        commander1.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        commander1.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        commander1.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        commander1.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        
        //Set up the second commander
        commander2 = Global.getSector().getFaction("dpl_phase_lab").createRandomPerson();
        commander2.setPersonality(Personalities.RECKLESS);
        commander2.getStats().setLevel(6);
        commander2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        commander2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        commander2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        commander2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        commander2.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        commander2.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        commander2.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        commander2.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        commander2.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
    	
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_phase_lab", "f1", true);
		fleet.getFleetData().addFleetMember("dpl_aulochrome_salazar_standard");
		FleetMemberAPI member = fleet.getFlagship();
		
		CampaignFleetAPI fleet2 = Global.getFactory().createEmptyFleet("dpl_phase_lab", "f2", true);
		fleet2.getFleetData().addFleetMember("dpl_aulochrome_salazar_standard");
		FleetMemberAPI member2 = fleet2.getFlagship();
		
		CampaignFleetAPI fleet3 = Global.getFactory().createEmptyFleet("dpl_phase_lab", "f3", true);
		fleet3.getFleetData().addFleetMember("dpl_aulochrome_salazar_standard");
		FleetMemberAPI member3 = fleet3.getFlagship();
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_phase_lab",
                null,
                PATROL_LARGE,
                220f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	banach_fleet = FleetFactoryV3.createFleet(params);
    	banach_fleet.setName(banach_salazar.getNameString() + "'s Task Force");
    	banach_fleet.getFleetData().addFleetMember(member);
    	banach_fleet.getFleetData().addFleetMember(member2);
    	banach_fleet.getFleetData().addFleetMember(member3);
    	banach_fleet.getFleetData().setFlagship(member);
    	banach_fleet.setNoFactionInName(true);
    	banach_fleet.setCommander(banach_salazar);
    	member.setCaptain(banach_salazar);
    	member2.setCaptain(commander1);	
		member3.setCaptain(commander2);
		
    	banach_fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = banach_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeImportant(banach_fleet, "$dpl_artworks");
        Misc.makeHostileToFaction(banach_fleet, Factions.OMEGA, 200f);

        banach_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_artworks");
        //Must be set to true, or some improper tithe check will ruin the story.
        banach_fleet.getMemoryWithoutUpdate().set("$dpl_artworks_banachfleet", true);
        banach_fleet.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system.getCenter(), 200f, null);
        system.addEntity(banach_fleet);
        Vector2f pos = system.getCenter().getLocation();
        banach_fleet.setLocation(pos.x-3000f, pos.y);
    }
    
    protected void despawn_fleets() {
    	if (banach_fleet != null) {
    		banach_fleet.getAI().clearAssignments();
        	banach_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, research_site_v, 200f, null);
        }
    	
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system.getStar(), 200f, null);
        }
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_artworks_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_artworks_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_artworks_barEvent", isBarEvent());
        set("$dpl_artworks_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_artworks_heOrShe", getPerson().getHeOrShe());
        set("$dpl_artworks_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_artworks_personName", getPerson().getNameString());
        set("$dpl_artworks_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_artworks_dist", getDistanceLY(system));
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
        if (currentStage == Stage.MEET_BANACH) {
            info.addPara("Find Banach Salazar's fleet in the " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Engage the unknown enemy fleet in the " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to release the agent.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: BANACH IS LOCATED IN THE " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.MEET_BANACH) {
            info.addPara("Find Banach Salazar's fleet in the " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Engage the unknown enemy fleet in the " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
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
    	if (currentStage == Stage.MEET_BANACH) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system2.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Artworks";
    }
    
    public void Victory() {
    	getPerson().getMemoryWithoutUpdate().set("$dpl_artworks_killed", true);
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
		
		if (fleet.equals(target)) {
			Victory();
		}
	}
}
