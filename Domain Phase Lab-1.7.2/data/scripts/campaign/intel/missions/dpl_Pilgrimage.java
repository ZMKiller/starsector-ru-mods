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

public class dpl_Pilgrimage extends HubMissionWithBarEvent implements FleetEventListener {

    // time we have to complete the mission
    public static float MISSION_DAYS = 240f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        BOARD_THE_SHIP,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI vlad_fleet;
    protected PersonAPI vladimir_vassiliev;
    protected SectorEntityToken wreck;
    protected MarketAPI market;
    protected StarSystemAPI system;
    protected StarSystemAPI system2;
    protected Vector2f last_loc_vlad;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        market = person.getMarket();
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        vladimir_vassiliev = getImportantPerson("vladimir_vassiliev");
        if (vladimir_vassiliev == null) return false;
        system = person.getMarket().getStarSystem();
        if (system == null) return false;
        system2 = Global.getSector().getStarSystem("voidbreak");
        if (system2 == null) return false;

        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_pilgrimage_ref")) {
            return false;
        }
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_vladimir_fleet();
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
        if (!setGlobalReference("$dpl_pilgrimage_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.BOARD_THE_SHIP, "$dpl_pilgrimage_won");
        setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_pilgrimage_visited");
        makeImportant(market, "$dpl_pilgrimage", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_pilgrimage", Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_pilgrimage_completed");
        // set time limit and credit reward
		setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.BOARD_THE_SHIP);
        setCreditReward(300000);

        return true;
    }

    // during the initial dialogue and in any dialogue where we use "Call $dpl_pilgrimage_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_pilgrimage_execName and automatically insert the disgraced executive's name
    protected void updateInteractionDataImpl() {
        set("$dpl_pilgrimage_barEvent", isBarEvent());
        set("$dpl_pilgrimage_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_pilgrimage_heOrShe", getPerson().getHeOrShe());
        set("$dpl_pilgrimage_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_pilgrimage_personName", getPerson().getNameString());
        set("$dpl_pilgrimage_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_pilgrimage_system2Name", system2.getNameWithLowercaseTypeShort());
        set("$dpl_pilgrimage_dist", getDistanceLY(system2));
    }
    
    public void Victory() {
    	Vector2f loc = last_loc_vlad;
    	wreck = dpl_phase_labAddEntities.spawnNamedWreck(loc, system2, "dpl_phase_lab", "dpl_vibraphone_wreck_Hull", "DPLS Lyre of Orpheus", true);
    	Misc.makeImportant(wreck, "$dpl_pilgrimage");
    	wreck.getMemoryWithoutUpdate().set("$dpl_pilgrimage_derelict", true);
        setEntityMissionRef(wreck, "$dpl_pilgrimage_ref");
        Global.getSector().getMemoryWithoutUpdate().set("$dpl_pilgrimage_won", true);
    }
    
    public void spawn_vladimir_fleet(){
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_phase_lab", "", true);
		fleet.getFleetData().addFleetMember("dpl_vibraphone_boss_standard");
		FleetMemberAPI member = fleet.getFlagship();
		member.setShipName("DSS Lyre of Orpheus");
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_phase_lab_old",
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
    	vlad_fleet = FleetFactoryV3.createFleet(params);
    	vlad_fleet.setName(vladimir_vassiliev.getNameString() + "'s Defence Fleet");
    	vlad_fleet.getFleetData().addFleetMember(member);
    	vlad_fleet.getFleetData().setFlagship(member);
    	vlad_fleet.setNoFactionInName(true);
    	vlad_fleet.setCommander(vladimir_vassiliev);
    	vlad_fleet.getFlagship().setCaptain(vladimir_vassiliev);
    	vlad_fleet.getFleetData().sort();
    	vlad_fleet.addEventListener(this);
    	List<FleetMemberAPI> members = vlad_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

		Misc.makeHostile(vlad_fleet);
		Misc.makeNoRepImpact(vlad_fleet, "$dpl_pilgrimage");
        Misc.makeImportant(vlad_fleet, "$dpl_pilgrimage");

        vlad_fleet.getMemoryWithoutUpdate().set("$dpl_pilgrimage_vladfleet", true);
        vlad_fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        vlad_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_pilgrimage");
        vlad_fleet.getAI().addAssignment(FleetAssignment.PATROL_SYSTEM, system2.getCenter(), 200f, null);
        system2.addEntity(vlad_fleet);
        Vector2f pos = system2.getCenter().getLocation();
        vlad_fleet.setLocation(pos.x+2500f, pos.y+2500f);
    }
    
    protected void despawn_fleets() {
    	if (vlad_fleet != null) {
    		vlad_fleet.getAI().clearAssignments();
    		vlad_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }

    // used to detect when the executive's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();

        if (!playerInvolved || !battle.isInvolved(vlad_fleet) || battle.onPlayerSide(vlad_fleet)) {
            return;
        }

        if (vlad_fleet != null) {
        	last_loc_vlad = vlad_fleet.getLocation();
    	}

    }

    // if the fleet despawns for whatever reason, fail the mission
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) return;

        if (fleet.equals(vlad_fleet)) {
			Victory();
		}
    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Take Eliza to " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.BOARD_THE_SHIP) {
            info.addPara("Examine the derelict in " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Go back to Research Site V.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: VLADIMIR VASSILIEV IS LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Take Eliza to " +
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
        return "Pilgrimage";
    }
}
