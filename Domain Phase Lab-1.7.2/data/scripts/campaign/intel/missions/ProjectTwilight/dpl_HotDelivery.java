package data.scripts.campaign.intel.missions.ProjectTwilight;

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
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Revenge.Stage;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

public class dpl_HotDelivery extends HubMissionWithBarEvent implements FleetEventListener {
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
    protected PersonAPI attackers_leader;
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
        if (!setPersonMissionRef(person, "$dpl_hotdeliv_ref")) {
            return false;
        }

        // set up the pather
        attackers_leader = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
        attackers_leader.setRankId(Ranks.SPECIAL_AGENT);
        attackers_leader.setPostId(Ranks.POST_SPECIAL_AGENT);
        attackers_leader.getMemoryWithoutUpdate().set("$dpl_hotdeliv_leader", true);
        
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

        // pick the target fleet's system

        system2 = Global.getSector().getStarSystem("capella");
        if (system2 == null) return false;
        
        beginStageTrigger(Stage.KILL_FLEET);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_attackers_fleet();
			}
		});
        endTrigger();

        // set a global reference we can use, useful for once-off missions.
        if (!setGlobalReference("$dpl_hotdeliv_ref")) return false;

        // set our starting, success and failure stages
        setStartingStage(Stage.KILL_FLEET);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);

        // set stage transitions when certain global flags are set, and when certain flags are set on the questgiver
        setStageOnGlobalFlag(Stage.RETURN_TO_FACTORY, "$dpl_hotdeliv_killed");
        makeImportant(market, "$dpl_hotdeliv", Stage.RETURN_TO_FACTORY);
		makeImportant(person, "$dpl_hotdeliv", Stage.RETURN_TO_FACTORY);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$dpl_hotdeliv_completed");
        
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.RETURN_TO_FACTORY);
        setCreditReward(300000);

        return true;
    }

    // set up the target fleet. I've done this using the old style, because the trigger-system doesn't support event listeners by default,
    // and we need to know when this fleet dies or despawns. I also need to write it outside of create function, so that this fleet only gets
    // created after the mission is accepted, instead of when the mission is created.
    public void spawn_attackers_fleet() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                Factions.TRITACHYON,
                null,
                PATROL_LARGE,
                0f, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
    	List<String> addShips = params.addShips;
    	for (int i=0; i<10; i++) {
    		addShips.add("dpl_super_hyperion_standard");
    	}
    	addShips.add("dpl_super_hyperion_flagship");
    	addShips.add("dpl_super_hyperion_flagship");
    	
    	params.averageSMods = 3;
    	target = FleetFactoryV3.createFleet(params);
        target.setName("The Attackers");
        target.setNoFactionInName(true);

        target.setCommander(attackers_leader);
        target.getFlagship().setCaptain(attackers_leader);
        
        List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			if (!curr.isFlagship()) {
				PersonAPI person = Global.getSector().getFaction(Factions.TRITACHYON).createRandomPerson();
				person.setPersonality(Personalities.RECKLESS);
				person.getStats().setLevel(6);
				person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
				person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
				person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 2);
				person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
				person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
				person.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2);
				person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
				person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
				person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		        
				curr.setVariant(curr.getVariant().clone(), false, false);
				curr.getVariant().setSource(VariantSource.REFIT);
				curr.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
				curr.setCaptain(person);
			}
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeHostile(target);
        Misc.makeImportant(target, "$dpl_hotdeliv");

        target.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, "$dpl_hotdeliv");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, "$dpl_hotdeliv");
        target.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, "$dpl_hotdeliv");
        //Must be set to true, or some improper tithe check will ruin the story.
        target.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
        target.getMemoryWithoutUpdate().set("$dpl_hotdeliv_attackersfleet", true);
        target.getAI().addAssignment(FleetAssignment.ORBIT_PASSIVE, system2.getStar(), 200f, null);
        target.addEventListener(this);
        system2.addEntity(target);
    }
    
    protected void updateInteractionDataImpl() {
        set("$dpl_hotdeliv_barEvent", isBarEvent());
        set("$dpl_hotdeliv_manOrWoman", getPerson().getManOrWoman());
        set("$dpl_hotdeliv_heOrShe", getPerson().getHeOrShe());
        set("$dpl_hotdeliv_reward", Misc.getWithDGS(getCreditsReward()));

        set("$dpl_hotdeliv_personName", getPerson().getNameString());
        set("$dpl_hotdeliv_patherName", attackers_leader.getNameString());
        set("$dpl_hotdeliv_systemName", system2.getNameWithLowercaseTypeShort());
        set("$dpl_hotdeliv_dist", getDistanceLY(system2));
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
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Eliminate the 'Attackers' special force in the " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        } else if (currentStage == Stage.RETURN_TO_FACTORY) {
            info.addPara("Return to Lab Factory.", opad);
        }
        if (isDevMode()) {
            info.addPara("DEVMODE: THE ATTACKERS ARE LOCATED IN THE " +
                    system2.getNameWithLowercaseTypeShort() + ".", opad);
        }
    }

    // short description in popups and the intel entry
    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        Color h = Misc.getHighlightColor();
        if (currentStage == Stage.KILL_FLEET) {
            info.addPara("Eliminate the 'Attackers' special force in the " +
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
        return "Hot Delivery";
    }

    //I don't know why we need to implement this. If I don't implement this dummy method, things go wrong.
	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		if (isDone() || result != null) return;
	}
}
