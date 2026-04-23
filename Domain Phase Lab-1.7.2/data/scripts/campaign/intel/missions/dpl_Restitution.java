package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
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

import data.scripts.campaign.intel.missions.dpl_DeathToTraitors.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_Restitution extends HubMissionWithBarEvent implements FleetEventListener {

    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        FIND_CLUE,
        KILL_FLEET,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected SectorEntityToken clarinet;
    protected PersonAPI executive;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean("dpl_restitution_TakenAndFailed")) return false;

        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_restitution_ref")) {
            return false;
        }

        // set up the disgraced executive
        executive = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        executive.setRankId(Ranks.SPACE_ADMIRAL);
        executive.setPostId(Ranks.POST_SENIOR_EXECUTIVE);
        executive.getMemoryWithoutUpdate().set("$dpl_restitution_exec", true);

        // pick the system with the clues inside
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemInInnerSector();
        preferSystemUnexplored();
        preferSystemInDirectionOfOtherMissions();

        system = pickSystem(true);
        if (system == null) return false;

        // pick the target fleet's system
        requireSystemInterestingAndNotUnsafeOrCore();
        preferSystemWithinRangeOf(system.getLocation(), 3f);
        preferSystemUnexplored();
        requireSystemNot(system);

        system2 = pickSystem(true);
        if (system2 == null) return false;

        beginStageTrigger(Stage.FIND_CLUE);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_derelicts();
			}
		});
        endTrigger(); 
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_executive_fleet();
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
        if (!setGlobalReference("$dpl_restitution_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.FIND_CLUE);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.KILL_FLEET, "$dpl_restitution_foundclue");
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_restitution_completed");
        setStageOnMemoryFlag(Stage.FAILED, person, "$dpl_restitution_failed" );
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, system2);
        setCreditReward(CreditReward.HIGH);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_restitution_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_restitution_execName and automatically insert the disgraced executive's name
    protected void updateInteractionDataImpl() {
        set("$dpl_restitution_barEvent", isBarEvent());
        set("$dpl_restitution_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_restitution_heOrShe", getPerson().getHeOrShe());
        set("$dpl_restitution_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_restitution_personName", getPerson().getNameString());
        set("$dpl_restitution_execName", executive.getNameString());
        set("$dpl_restitution_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_restitution_system2Name", system2.getNameWithLowercaseTypeShort());
        set("$dpl_restitution_dist", getDistanceLY(system));
    }
    
    public void spawn_derelicts(){
    	// spawn a recoverable derelict ship, serving as clues. They have memory flags that are checked for in rules.csv
        // I need to add mission tag to the clarinet ship, and so I have to spawn this ship in the tedious way
        clarinet = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, new DerelictShipData(new PerShipData("dpl_clarinet_standard", ShipCondition.AVERAGE), false));
        clarinet.setDiscoverable(true);
        clarinet.setCircularOrbit(system.getStar(), (float) Math.random() * 360f, 1500f, 1500f / (10f + (float) Math.random() * 5f));
        Misc.setSalvageSpecial(clarinet, new ShipRecoverySpecialCreator(null, 0, 0, false, null, null).createSpecial(clarinet, null));
        Misc.makeImportant(clarinet, "$dpl_restitution");
        clarinet.getMemoryWithoutUpdate().set("$dpl_restitution_clarinet", true);
        setEntityMissionRef(clarinet, "$dpl_restitution_ref");
        
        //other ships can be spawned fine
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "apex_Overdriven", ShipCondition.WRECKED, 1550f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 1540f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "fulgent_Assault", ShipCondition.WRECKED, 1530f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 1520f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "glimmer_Support", ShipCondition.WRECKED, 1535f, false, false, null);
        dpl_phase_labAddEntities.addDerelict(system, system.getStar(), "lumen_Standard", ShipCondition.WRECKED, 1545f, false, false, null);
    }
    
    public void spawn_executive_fleet(){
    	// set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
        // and we need to know when this fleet dies or despawns
        FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                PATROL_LARGE,
                120f, // combatPts
                20f, // freighterPts
                20f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.25f // qualityMod
        );
        target = FleetFactoryV3.createFleet(params);

        target.setName(executive.getNameString() + "'s Fleet");
        target.setNoFactionInName(true);

        target.setCommander(executive);
        target.getFlagship().setCaptain(executive);

        Misc.makeHostile(target);
        Misc.makeNoRepImpact(target, "$dpl_restitution");
        Misc.makeImportant(target, "$dpl_restitution");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_restitution");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_restitution");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_restitution");

        target.getMemoryWithoutUpdate().set("$dpl_restitution_execfleet", true);
        target.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    	Global.getSector().getMemoryWithoutUpdate().set("dpl_restitution_TakenAndFailed", true);
    }

    // used to detect when the executive's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        // also credit the player if they're in the same location as the fleet and nearby
        float distToPlayer = Misc.getDistance(fleet, Global.getSector().getPlayerFleet());
        boolean playerInvolved = battle.isPlayerInvolved() || (fleet.isInCurrentLocation() && distToPlayer < 2000f);

        if (battle.isInvolved(fleet) && !playerInvolved) {
            if (fleet.getFlagship() == null || (!fleet.getFlagship().getCaptain().equals(executive)) ) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                getPerson().getMemoryWithoutUpdate().set("$dpl_restitution_completed", true);
                return;
            }
        }

        if (!playerInvolved || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain().equals(executive)) return;

        getPerson().getMemoryWithoutUpdate().set("$dpl_restitution_completed", true);

    }

    // if the fleet despawns for whatever reason, fail the mission
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) return;

        if (fleet.getMemoryWithoutUpdate().contains("$dpl_restitution_execfleet")) {
            getPerson().getMemoryWithoutUpdate().set("$dpl_restitution_completed", true);
        }
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.FIND_CLUE) {
            info.addPara("Look for the stolen clarinet class ship around the star in " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Hunt down and eliminate the executive in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
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
        if (currentStage == Stage.FIND_CLUE) {
            info.addPara("Look for the stolen clarinet class ship around the star in " +
                    system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Hunt down the executive in the " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (currentStage == Stage.FIND_CLUE) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Restitution";
    }
}
