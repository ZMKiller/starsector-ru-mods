package data.scripts.campaign.intel.missions.ProjectPrometheus;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_LARGE;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.missions.academy.GABaseMission;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.campaign.intel.missions.dpl_Artworks.Stage;

public class dpl_CurtainCall extends GABaseMission {

	public static enum Stage {
		GO_TO_CTAP,
		CHECK_HELA,
		GO_TO_ACADEMY,
		RETURN_TO_RSV,
		COMPLETED,
		FAILED,
	}
	
	protected PersonAPI baird;
	protected PersonAPI gargoyle;
	protected PersonAPI banach_salazar;
	protected PersonAPI cantor_aleph_null;
	protected StarSystemAPI system2;
	protected SectorEntityToken dpl_coronal_tap;
	protected SectorEntityToken dpl_HELA_device;
	protected MarketAPI kazeron;
	public static float MISSION_DAYS = 60f;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$dpl_CurtainCall_ref")) {
			return false;
		}
		
		kazeron = getMarket("kazeron");
		if (kazeron == null) return false;
		baird = getImportantPerson(People.BAIRD);
		if (baird == null) return false;
		gargoyle = getImportantPerson(People.GARGOYLE);
		if (gargoyle == null) return false;
		banach_salazar = getImportantPerson("banach_salazar");
		if (banach_salazar == null) return false;
		cantor_aleph_null = getImportantPerson("cantor_aleph_null");
		if (cantor_aleph_null == null) return false;
		system2 = Global.getSector().getStarSystem("helheim");
        if (system2 == null) return false;
        dpl_coronal_tap = system2.getEntityById("dpl_helheim_damaged_tap");
        if (dpl_coronal_tap == null) return false;
        dpl_HELA_device = system2.getEntityById("dpl_HELA_Device");
        if (dpl_HELA_device == null) return false;
		
		setStartingStage(Stage.GO_TO_CTAP);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();
		
		beginStageTrigger(Stage.GO_TO_CTAP);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				spawn_singularity_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.CHECK_HELA);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				despawn_singularity_fleet();
			}
		});
        endTrigger();
        
        beginStageTrigger(Stage.FAILED);
        triggerRunScriptAfterDelay(0, new Script() {
			@Override
			public void run() {
				destroy_thule();
			}
		});
        endTrigger();
		
		makeImportant(dpl_coronal_tap, null, Stage.GO_TO_CTAP);
		setStageOnGlobalFlag(Stage.CHECK_HELA, "$dpl_CurtainCall_GotSensor");
		makeImportant(dpl_HELA_device, null, Stage.CHECK_HELA);
		setStageOnGlobalFlag(Stage.GO_TO_ACADEMY, "$dpl_CurtainCall_DefeatedHELA");
		makeImportant(baird.getMarket(), null, Stage.GO_TO_ACADEMY);
		setStageOnGlobalFlag(Stage.RETURN_TO_RSV, "$dpl_CurtainCall_GADone");
		makeImportant(banach_salazar.getMarket(), null, Stage.RETURN_TO_RSV);
		setStageOnGlobalFlag(Stage.COMPLETED, "$dpl_CurtainCall_Completed");
		
		setFailureStage(Stage.FAILED);
        // set time limit and credit reward
        setTimeLimit(Stage.FAILED, MISSION_DAYS, null, Stage.GO_TO_ACADEMY);
		
		setRepFactionChangesNone();
		setRepPersonChangesNone();
		
		return true;
	}
	
	public void spawn_singularity_fleet() {
		CampaignFleetAPI defenders = CreateDefenders();
		dpl_coronal_tap.getMemory().set("$hasDefenders",true);
		dpl_coronal_tap.getMemory().set("$defenderFleet", defenders);
		dpl_coronal_tap.getMemory().unset("$defenderFleetDefeated");
		
		cantor_aleph_null.setPortraitSprite(Global.getSettings().getSpriteName("characters", "dpl_singularity_cantor"));
	}
	
	public void despawn_singularity_fleet() {
		dpl_coronal_tap.getMemory().unset("$hasDefenders");
		dpl_coronal_tap.getMemory().unset("$defenderFleet");
		dpl_coronal_tap.getMemory().set("$defenderFleetDefeated",true);
	}
	
	public CampaignFleetAPI CreateDefenders() {
    	FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                "dpl_helheim_singularity",
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
    	
    	CampaignFleetAPI target;
    	target = FleetFactoryV3.createFleet(params);
        target.setName("Automatic Defense");
        target.setNoFactionInName(true);
        
		target.getFleetData().addFleetMember("dpl_node_large_elite");
		target.getFleetData().addFleetMember("dpl_node_large_elite");
		target.getFleetData().addFleetMember("dpl_node_type_I_elite");
		target.getFleetData().addFleetMember("dpl_node_type_I_elite");
		target.getFleetData().addFleetMember("dpl_node_type_II_elite");
		target.getFleetData().addFleetMember("dpl_node_type_II_elite");
		
		List<FleetMemberAPI> members = target.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			PersonAPI person = Global.getSector().getFaction("dpl_helheim_singularity").createRandomPerson();
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
			person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
	        
			curr.setVariant(curr.getVariant().clone(), false, false);
			curr.getVariant().setSource(VariantSource.REFIT);
			curr.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
			curr.setCaptain(person);
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}

        Misc.makeHostile(target);
        return target;
    }
	
	public void destroy_thule() {
		Global.getSector().getMemoryWithoutUpdate().set("$dpl_Prometheus_Failed", true);
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$dpl_Prometheus_Failed_CanAskGg", true);
		Global.getSector().getPlayerMemoryWithoutUpdate().set("$dpl_Prometheus_Failed_CanAskBanach", true);
		Global.getSector().getMemoryWithoutUpdate().unset("$dpl_DD_Completed");
		Global.getSector().getMemoryWithoutUpdate().unset("$dpl_RepairCTap_Complete");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_ElizaPPAskedAlviss");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_ElizaPPAskedGg");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_GgPPAskedEliza");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_RossPPAskedEliza");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_PPrometheusKnowMissions");
		Global.getSector().getPlayerMemoryWithoutUpdate().unset("$dpl_PrometheusStarted");
		
		MessageIntel msg = new MessageIntel();
		msg.addLine("HELA Device self destructed!", Misc.getBasePlayerColor());
		msg.addLine(BaseIntelPlugin.BULLET + 
				"Eliza and Gargoyle had left your fleet in a shuttle.");
		msg.setIcon(Global.getSector().getFaction("dpl_phase_lab").getCrest());
		msg.setSound(Sounds.REP_LOSS);
		Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
		
		gargoyle.getMarket().getCommDirectory().getEntryForPerson(gargoyle.getId()).setHidden(false);
		
		StarSystemAPI thule = kazeron.getStarSystem();
		for (PlanetAPI planet: thule.getPlanets()) {
			if (!planet.isStar()) {
				ramIntoPlanet(planet);
			}
		}
	}
	
	protected void convertPlanet(PlanetAPI planet) {
		MarketAPI market = planet.getMarket();
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

    protected void ramIntoPlanet(PlanetAPI planet) {
        MarketCMD.addBombardVisual(planet);
        MarketAPI market = planet.getMarket();
        
        for (Object o : market.getMemoryWithoutUpdate().getKeys().toArray()) {
            String memFlag = (String) o;
            if (memFlag.startsWith(MemFlags.STORY_CRITICAL)) {
                market.getMemoryWithoutUpdate().unset(memFlag);
            }
        }
        
        DecivTracker.decivilize(market, true, true);

        // relationship effects
        FactionAPI persean = Global.getSector().getFaction("persean");
        FactionAPI independent = Global.getSector().getFaction("independent");
        FactionAPI dpl_phase_lab = Global.getSector().getFaction("dpl_phase_lab");
        persean.setRelationship(dpl_phase_lab.getId(), -1f);
        persean.setRelationship(independent.getId(), -1f);

        convertPlanet(planet);

        sendUpdateIfPlayerHasIntel(null, false);
    }
	
	protected void updateInteractionDataImpl() {
	
	}
	
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_CTAP) {
			info.addPara("Go to the Helheim System and check the damaged coronal tap.", opad);
		} else if (currentStage == Stage.CHECK_HELA) {
			info.addPara("Check the HELA Device.", opad);
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to Galactia Academy to make a final report for this project.", opad);
		} else if (currentStage == Stage.RETURN_TO_RSV) {
			info.addPara("Go to Research Site V to meet with Vice Admiral Banach Salazar.", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_CTAP) {
			info.addPara("Go to the Helheim System", tc, pad);
			return true;
		} else if (currentStage == Stage.CHECK_HELA) {
			info.addPara("Go to the Helheim System", tc, pad);
			return true;
		} else if (currentStage == Stage.GO_TO_ACADEMY) {
			info.addPara("Go to Galatia Academy", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_RSV) {
			info.addPara("Go to the Muspelheim System", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "Curtain Call";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}

	
}





