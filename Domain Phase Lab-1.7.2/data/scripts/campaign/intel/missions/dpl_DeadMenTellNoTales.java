package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_DeadMenTellNoTales extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission
    public static float MISSION_DAYS = 180f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected PersonAPI nelson_bonaparte;
    protected PersonAPI commander1;
    protected PersonAPI commander2;
    protected PersonAPI commander3;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_dmtnt_ref")) {
            return false;
        }

        nelson_bonaparte = getImportantPerson("nelson_bonaparte");
        if (nelson_bonaparte == null) return false;
        
        //Set up the first commander
        commander1 = Global.getSector().getFaction("dpl_persean_imperium").createRandomPerson();
        commander1.setPersonality(Personalities.RECKLESS);
        commander1.getStats().setLevel(6);
        commander1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        commander1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        commander1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        commander1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        commander1.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        commander1.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        
        //Set up the second commander
        commander2 = Global.getSector().getFaction("dpl_persean_imperium").createRandomPerson();
        commander2.setPersonality(Personalities.RECKLESS);
        commander2.getStats().setLevel(6);
        commander2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        commander2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        commander2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        commander2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        commander2.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        commander2.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
        
        //Set up the third commander
        commander3 = Global.getSector().getFaction("dpl_persean_imperium").createRandomPerson();
        commander3.setPersonality(Personalities.RECKLESS);
        commander3.getStats().setLevel(6);
        commander3.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        commander3.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        commander3.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        commander3.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        commander3.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        commander3.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);

        // pick the system.
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemInInnerSector();
        preferSystemUnexplored();
        preferSystemInDirectionOfOtherMissions();

        system2 = pickSystem(true);
        if (system2 == null) return false;
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_nelson_fleet();
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
        if (!setGlobalReference("$dpl_dmtnt_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnMemoryFlag(Stage.RETURN_TO_RSV, person, "$dpl_dmtnt_killed");
        makeImportant(market, "$dpl_dmtnt", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_dmtnt", Stage.RETURN_TO_RSV);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_dmtnt_completed");
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_RSV);
        setCreditReward(350000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_nelson_fleet() {
    	//Generate the flagship
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "f1", true);
		fleet.getFleetData().addFleetMember("dpl_piano_dmtnt_elite");
		FleetMemberAPI member = fleet.getFlagship();
		
		CampaignFleetAPI fleet2 = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "f2", true);
		fleet2.getFleetData().addFleetMember("dpl_tenoroon_dmtnt_exotic");
		FleetMemberAPI member2 = fleet2.getFlagship();
		
		CampaignFleetAPI fleet3 = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "f3", true);
		fleet3.getFleetData().addFleetMember("dpl_tenoroon_dmtnt_exotic");
		FleetMemberAPI member3 = fleet3.getFlagship();
		
		CampaignFleetAPI fleet4 = Global.getFactory().createEmptyFleet("dpl_persean_imperium", "f4", true);
		fleet4.getFleetData().addFleetMember("dpl_dudelsa_dmtnt_std");
		FleetMemberAPI member4 = fleet4.getFlagship();
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_persean_imperium",
                null,
                PATROL_LARGE,
                80f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName("Supreme Conqueror's Armada");
        target.setNoFactionInName(true);
        target.getFleetData().addFleetMember(member);
        target.getFleetData().addFleetMember(member2);
        target.getFleetData().addFleetMember(member3);
        target.getFleetData().addFleetMember(member4);
        target.getFleetData().setFlagship(member);
        target.setCommander(nelson_bonaparte);
		
        member.setCaptain(nelson_bonaparte);
		member2.setCaptain(commander1);	
		member3.setCaptain(commander2);
		member4.setCaptain(commander3);
        
		target.getFleetData().sort();
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_dmtnt");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE_ONE_BATTLE_ONLY, true);
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        target.getMemoryWithoutUpdate().set("$dpl_dmtnt_chairfleet", true);
        target.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
        Vector2f pos = system2.getStar().getLocation();
        target.setLocation(pos.x+2500f, pos.y+2500f);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_dmtnt_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_dmtnt_chairName and automatically insert the chair's name
    protected void updateInteractionDataImpl() {
        set("$dpl_dmtnt_barEvent", isBarEvent());
        set("$dpl_dmtnt_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_dmtnt_heOrShe", getPerson().getHeOrShe());
        set("$dpl_dmtnt_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_dmtnt_personName", getPerson().getNameString());
        set("$dpl_dmtnt_chairName", nelson_bonaparte.getNameString());
        set("$dpl_dmtnt_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_dmtnt_dist", getDistanceLY(system2));
    }

    // used to detect when the chair's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();
        
        //If the fleet encounters anything besides the player fleet, it kills its enemy.
        if (!playerInvolved && battle.isInvolved(fleet)) {
        	List<CampaignFleetAPI> AllEnemies = battle.getOtherSide(battle.pickSide(fleet));
        	for (CampaignFleetAPI enemyFleet: AllEnemies) {
        		List<FleetMemberAPI> allMembers = enemyFleet.getFleetData().getMembersListCopy();
        		for (FleetMemberAPI enemy : allMembers) {
        			enemyFleet.removeFleetMemberWithDestructionFlash(enemy);
        		}
        	}
        	return;
        } else if (!battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }
        
        if (fleet.getFlagship() == null || (!fleet.getFlagship().getCaptain().equals(nelson_bonaparte)) ) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            getPerson().getMemoryWithoutUpdate().set("$dpl_dmtnt_killed", true);
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain().equals(nelson_bonaparte)) return;
        
        getPerson().getMemoryWithoutUpdate().set("$dpl_dmtnt_killed", true);

    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Start investigation in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to report the situations.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: CHAIR IS LOCATED IN THE " +
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
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Dead Men Tell No Tales";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
