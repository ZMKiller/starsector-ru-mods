package data.scripts.campaign.intel.missions.RossQuests;

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
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;
import data.scripts.world.dpl_phase_labAddEntities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_FirstStrike extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        BOARD_THE_WRECK,
        RETURN_TO_FACTORY,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected PersonAPI kapteyn_greater;
    protected PersonAPI rusty_hook;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected MarketAPI station_kapteyn;
    protected Vector2f last_loc_rh;
    protected SectorEntityToken wreck;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
		
        MarketAPI market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_fssk_ref")) {
            return false;
        }
        
        rusty_hook = getImportantPerson("rusty_hook");
        if (rusty_hook == null) return false;

        // set up Greater Kapteyn
        kapteyn_greater = getImportantPerson("kapteyn_greater");
        if (kapteyn_greater == null) return false;

        // pick the target fleet's system

        system2 = Global.getSector().getStarSystem("isirah");
        if (system2 == null) return false;
        
        station_kapteyn = Global.getSector().getEconomy().getMarket("station_kapteyn");
		if (station_kapteyn == null) return false;
		if (!station_kapteyn.getFactionId().equals(Factions.PIRATES)) return false;
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_pirate_fleet();
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
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
	        	Misc.makeNonStoryCritical(station_kapteyn, "dpl_FirstStrike");
			}
		});
		endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_fssk_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.BOARD_THE_WRECK, "$dpl_fssk_won");
        setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_fssk_boarded");
        makeImportant(market, "$dpl_fssk", Stage.RETURN_TO_FACTORY);
		makeImportant(person, "$dpl_fssk", Stage.RETURN_TO_FACTORY);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_fssk_completed");
        
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_FACTORY);
        setCreditReward(500000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_pirate_fleet() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.PIRATES,
                null,
                PATROL_LARGE,
                900f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                -0.25f // qualityMod
        );
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName(rusty_hook.getNameString() + "'s Fleet");
        target.setNoFactionInName(true);
        
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.PIRATES, "f1", true);
		fleet.getFleetData().addFleetMember("dpl_mad_hooks_boss_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.getVariant().addTag(Tags.TAG_NO_AUTOFIT);
		member.setShipName("ISS Mad Hooks");
		target.getFleetData().addFleetMember(member);
		target.getFleetData().setFlagship(member);
        
        CargoAPI cargo1 = Global.getFactory().createCargo(true);
		cargo1.addSpecial(new SpecialItemData("dpl_data_archive", null), 2);
		BaseSalvageSpecial.addExtraSalvage(target, cargo1);

        target.setCommander(kapteyn_greater);
        target.getFlagship().setCaptain(kapteyn_greater);
        
        target.getFleetData().sort();
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_fssk");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_fssk");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_fssk");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_fssk");
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_fssk_pirate_fleet", true);
        target.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, station_kapteyn.getPrimaryEntity(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_fssk_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_fssk_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_fssk_barEvent", isBarEvent());
        set("$dpl_fssk_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_fssk_heOrShe", getPerson().getHeOrShe());
        set("$dpl_fssk_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_fssk_personName", getPerson().getNameString());
        set("$dpl_fssk_pirateName", rusty_hook.getNameString());
        set("$dpl_fssk_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_fssk_dist", getDistanceLY(system2));
    }

    // used to detect when the pather's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    	if (isDone() || result != null) return;
    	
    	if (!battle.isInvolved(target)) {
    		return;
    	}
    	
    	if (target != null) {
    		last_loc_rh = target.getLocation();
    	}
	}
    
  //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
  	@Override
  	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
  		if (isDone() || result != null) return;
  		if (fleet.equals(target)) {
  			Victory();
  		}
  	}
  	
  	public void Victory() {
    	Vector2f loc = last_loc_rh;
    	wreck = dpl_phase_labAddEntities.spawnUniqueWreck(loc, system2, Factions.PIRATES, "dpl_mad_hooks_Hull", "ISS Mad Hooks", true);
    	Misc.makeImportant(wreck, "$dpl_fssk");
    	wreck.getMemoryWithoutUpdate().set("$dpl_fssk_derelict", true);
        setEntityMissionRef(wreck, "$dpl_fssk_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_fssk_won", true);
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Engage Rusty Hook's armada in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            info.addPara("Board the wreck of Rusty Hook's flagship in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Return to Lab Factory.", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Engage Rusty Hook's armada in the " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            info.addPara("Board the wreck of Rusty Hook's flagship in the " +
                    system2.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Return to Lab Factory.", tc, pad);
            return true;
        }
        return false;
    }

    // where on the map the intel screen tells us to go
    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
    	if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.BOARD_THE_WRECK) {
            return getMapLocationFor(system2.getCenter());
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "First Strike";
    }
}
