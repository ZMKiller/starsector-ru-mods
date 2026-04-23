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
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.Market;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_Celebration extends HubMissionWithBarEvent implements FleetEventListener {
    // time we have to complete the mission

    // mission stages
    public static enum Stage {
        KILL_FLEET,
        RETURN_TO_RSV,
        COMPLETED,
        FAILED,
    }

    // important objects, systems and people
    protected CampaignFleetAPI target;
    protected CampaignFleetAPI banach_fleet;
    protected PersonAPI pather;
    protected PersonAPI banach_salazar;
    protected PlanetAPI research_site_v;
	protected MarketAPI market;
    protected StarSystemAPI system;
    public static float MISSION_DAYS = 10f;

    // run when the bar event starts / when we ask a contact about the mission
    protected boolean create(MarketAPI createdAt, boolean barEvent) {

        PersonAPI person = getPerson();
        if (person == null) return false;
        
        banach_salazar = getImportantPerson("banach_salazar");
		if (banach_salazar == null) return false;
		
		market = getMarket("dpl_research_site_v");
        if (market == null) return false;
        if (!market.getFactionId().equals("dpl_phase_lab")) return false;
        
        system = market.getStarSystem();
        research_site_v = market.getPlanetEntity();
        
        // setting the mission ref allows us to use the Call rulecommand in their dialogues, so that we can make this script do things
        if (!setPersonMissionRef(person, "$dpl_celebration_ref")) {
            return false;
        }

        // set up the pather
        pather = Global.getSector().getFaction(Factions.LUDDIC_PATH).createRandomPerson();
        pather.setRankId(Ranks.SPACE_ADMIRAL);
        pather.setPostId(Ranks.POST_FLEET_COMMANDER);
        pather.getMemoryWithoutUpdate().set("$dpl_celebration_pather", true);

        // pick the target fleet's system
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_banach_fleet();
			}
		});
        
        triggerRunScriptAfterDelay(1, new Script() {
			@Override
			public void run() {
				spawn_pather_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.RETURN_TO_RSV);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_banach_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.COMPLETED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				show_banach();
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
        if (!setGlobalReference("$dpl_celebration_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnMemoryFlag(Stage.RETURN_TO_RSV, person, "$dpl_celebration_killed");
        makeImportant(market, "$dpl_celebration", Stage.RETURN_TO_RSV);
		makeImportant(person, "$dpl_celebration", Stage.RETURN_TO_RSV);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_celebration_completed");
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_RSV);
        // set time limit and credit reward
        setCreditReward(150000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_pather_fleet() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.LUDDIC_PATH,
                null,
                PATROL_LARGE,
                300f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                2f // qualityMod
        );
    	
    	target = FleetFactoryV3.createFleet(params);
        target.setName("Armageddon Fleet");
        target.setNoFactionInName(true);

        target.setCommander(pather);
        target.getFlagship().setCaptain(pather);

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_celebration");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_celebration");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_celebration");
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_celebration_patherfleet", true);
        target.getAI().addAssignment(FleetAssignment.DELIVER_FUEL, research_site_v, 200f, null);
        target.addAssignment(FleetAssignment.HOLD, research_site_v, 0.25f, "Attacking Research Site V", 
        		new Script() {
					public void run() {
						checkRamPlanet(target, market);
					}
				});
        target.addEventListener(this);
        system.addEntity(target);
        Vector2f pos = research_site_v.getLocation();
        target.setLocation(pos.x-3000f, pos.y);
    }
    
    public void spawn_banach_fleet() {
    	CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("dpl_phase_lab", "", true);
		fleet.getFleetData().addFleetMember("dpl_cimbasso_AA");
		FleetMemberAPI member = fleet.getFlagship();
		
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_phase_lab",
                null,
                PATROL_LARGE,
                120f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1.5f // qualityMod
        );
    	
    	banach_fleet = FleetFactoryV3.createFleet(params);
    	banach_fleet.setName(banach_salazar.getNameString() + "'s Defence Armada");
    	banach_fleet.getFleetData().addFleetMember(member);
    	banach_fleet.getFleetData().setFlagship(member);
    	banach_fleet.getFleetData().addFleetMember("dpl_cimbasso_AA");
    	banach_fleet.setNoFactionInName(true);
    	banach_fleet.setCommander(banach_salazar);
    	banach_fleet.getFlagship().setCaptain(banach_salazar);
    	banach_fleet.getFleetData().sort();
    	List<FleetMemberAPI> members = banach_fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeImportant(banach_fleet, "$dpl_celebration");

        banach_fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        //Must be set to true, or some improper tithe check will ruin the story.
        banach_fleet.getMemoryWithoutUpdate().set("$dpl_celebration_banachfleet", true);
        banach_fleet.getAI().addAssignment(FleetAssignment.DEFEND_LOCATION, research_site_v, 200f, null);
        system.addEntity(banach_fleet);
        Vector2f pos = research_site_v.getLocation();
        banach_fleet.setLocation(pos.x, pos.y);
    }
    
    protected void despawn_banach_fleet() {
    	if (banach_fleet != null) {
    		banach_fleet.getAI().clearAssignments();
        	banach_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, research_site_v, 200f, null);
        }
    }
    
    protected void show_banach() {
    	banach_salazar.getMarket().getCommDirectory().getEntryForPerson(banach_salazar.getId()).setHidden(false);
    }

    protected void convertPlanet(PlanetAPI planet) {
    	market.getPlanetEntity().changeType("barren-bombarded", null);
    	market.getPlanetEntity().setCustomDescriptionId("barren-bombarded");
    	market.removeCondition(Conditions.HABITABLE);
    	market.removeCondition(Conditions.MILD_CLIMATE);
    	market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    	market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
    	market.removeCondition(Conditions.ORE_ULTRARICH);
    	market.addCondition(Conditions.HOT);
    	market.addCondition(Conditions.EXTREME_WEATHER);
    	market.addCondition(Conditions.DECIVILIZED);
        // keep the pre-existing ruins
    	market.removeCondition(Conditions.RUINS_VAST);
        market.addCondition(Conditions.RUINS_WIDESPREAD);
    }

    protected void ramIntoPlanet() {
        PlanetAPI planet = research_site_v;
        MarketCMD.addBombardVisual(planet);
        
        for (Object o : market.getMemoryWithoutUpdate().getKeys().toArray()) {
            String memFlag = (String) o;
            if (memFlag.startsWith(MemFlags.STORY_CRITICAL)) {
                market.getMemoryWithoutUpdate().unset(memFlag);
            }
        }
        
        DecivTracker.decivilize(market, true, true);

        // relationship effects
        FactionAPI church = Global.getSector().getFaction("luddic_church");
        FactionAPI path = Global.getSector().getFaction("luddic_path");
        FactionAPI dpl_phase_lab = Global.getSector().getFaction("dpl_phase_lab");
        dpl_phase_lab.setRelationship(church.getId(), -0.5f);
        dpl_phase_lab.setRelationship(path.getId(), -1f);

        convertPlanet(planet);

        sendUpdateIfPlayerHasIntel(null, false);

        if (target != null) {
        	target.getAI().clearAssignments();
        	target.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, research_site_v, 200f, null);
        }
        
        if (banach_fleet != null) {
        	banach_fleet.getAI().clearAssignments();
        	banach_fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, research_site_v, 200f, null);
        }

    }
    
    protected void checkRamPlanet(CampaignFleetAPI fleet, MarketAPI target) {
        if (fleet != null) {
            // if not in actual contact with the target, try again
            if (Misc.getDistance(fleet.getLocation(), target.getPlanetEntity().getLocation()) > 100)
                return;

            // engage any nearby fleets before attempting Destroying the planet
            for (CampaignFleetAPI otherFleet : target.getContainingLocation().getFleets()) {
                if (otherFleet.equals(fleet)) continue;
                if (!otherFleet.isHostileTo(fleet)) continue;
                if (Misc.getDistance(fleet.getLocation(), otherFleet.getLocation()) > 350f) continue;

                // engage station if there's one
                if (otherFleet.equals(Misc.getStationFleet(target))) {
                    Global.getFactory().createBattle(fleet, otherFleet);
                    return;
                }
            }
        }
        ramIntoPlanet();
    }
    
    // during the initial dialogue and in any dialogue where we use "Call $dpl_celebration_ref updateData", these values will be put in memory
    // here, used so we can, say, type $dpl_celebration_patherName and automatically insert the pather's name
    protected void updateInteractionDataImpl() {
        set("$dpl_celebration_barEvent", isBarEvent());
        set("$dpl_celebration_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_celebration_heOrShe", getPerson().getHeOrShe());
        set("$dpl_celebration_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_celebration_personName", getPerson().getNameString());
        set("$dpl_celebration_patherName", pather.getNameString());
        set("$dpl_celebration_systemName", system.getNameWithLowercaseTypeShort());
        set("$dpl_celebration_dist", getDistanceLY(system));
    }

    // used to detect when the pather's fleet is destroyed and complete the mission
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) return;

        //The player's involvement is not important, as long as the flagship gets destroyed, it's OK.
        if (!battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain().equals(pather)) return;
        
        getPerson().getMemoryWithoutUpdate().set("$dpl_celebration_killed", true);

    }

    // description when selected in intel screen
    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Find the agent and eliminate the pather in the " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            info.addPara("Return to Research Site V to release the agent.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: PATHER IS LOCATED IN THE " +
                    system.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Defend Research Site V in " +
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
    	if (currentStage == Stage.KILL_FLEET) {
            return getMapLocationFor(system.getCenter());
        } else if (currentStage == Stage.RETURN_TO_RSV) {
            return getMapLocationFor(system.getCenter());
        }
        return null;
    }

    // mission name
    @Override
    public String getBaseName() {
        return "Celebration";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
