package data.scripts.campaign.intel.missions.RossQuests;

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
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_FoxHunt extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission
    public static float MISSION_DAYS = 120f;

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        RETURN_TO_FACTORY,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected PersonAPI rusty_hook;
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
        if (!setPersonMissionRef(person, "$dpl_fxht_ref")) {
            return false;
        }

        // set up the pather
        rusty_hook = getImportantPerson("rusty_hook");
        if (rusty_hook == null) return false;

        // pick the target fleet's system

        system2 = Global.getSector().getStarSystem("capella");
        if (system2 == null) return false;
        
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

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_fxht_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnMemoryFlag(Stage.RETURN_TO_FACTORY, person, "$dpl_fxht_killed");
        makeImportant(market, "$dpl_fxht", Stage.RETURN_TO_FACTORY);
		makeImportant(person, "$dpl_fxht", Stage.RETURN_TO_FACTORY);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_fxht_completed");
        
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_FACTORY);
        setCreditReward(350000);

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
                600f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                -0.25f // qualityMod
        );
    	
    	List<String> shipVariants = new ArrayList<String>();
    	shipVariants.add("onslaught_xiv_Elite");
    	shipVariants.add("onslaught_xiv_Elite");
    	shipVariants.add("onslaught_xiv_Elite");
    	params.addShips = shipVariants;
    	
    	params.averageSMods = 3;
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName(rusty_hook.getNameString() + "'s Fleet");
        target.setNoFactionInName(true);
        
        CargoAPI cargo1 = Global.getFactory().createCargo(true);
		cargo1.addSpecial(new SpecialItemData("dpl_data_archive", null), 4);
		BaseSalvageSpecial.addExtraSalvage(target, cargo1);

        target.setCommander(rusty_hook);
        target.getFlagship().setCaptain(rusty_hook);

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_fxht");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_fxht");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_fxht");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_fxht");
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_fxht_pirate_fleet", true);
        target.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, system2.getStar(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
    }
    
    protected void despawn_fleets() {
    	if (target != null) {
    		target.getAI().clearAssignments();
    		target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, system2.getStar(), 200f, null);
        }
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_fxht_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_fxht_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_fxht_barEvent", isBarEvent());
        set("$dpl_fxht_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_fxht_heOrShe", getPerson().getHeOrShe());
        set("$dpl_fxht_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_fxht_personName", getPerson().getNameString());
        set("$dpl_fxht_pirateName", rusty_hook.getNameString());
        set("$dpl_fxht_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_fxht_dist", getDistanceLY(system2));
    }

    // used to detect when the pather's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        boolean playerInvolved = battle.isPlayerInvolved();

        if (!playerInvolved || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }
        
        if (fleet.getFlagship() == null || (!fleet.getFlagship().getCaptain().equals(rusty_hook)) ) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            getPerson().getMemoryWithoutUpdate().set("$dpl_fxht_killed", true);
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain().equals(rusty_hook)) return;
        
        getPerson().getMemoryWithoutUpdate().set("$dpl_fxht_killed", true);

    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Engage Rusty Hook's armada in the " +
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
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Fox Hunt";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
