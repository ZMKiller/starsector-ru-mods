package data.scripts.campaign.intel.missions.cb;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.cb.BaseCustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.FleetSize;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class dpl_CBHelheimSingularity extends BaseCustomBountyCreator {

	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	
	@Override
	public float getBountyDays() {
		return CBStats.REMNANT_PLUS_DAYS;
	}
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		boolean CompletedPPrometheus = Global.getSector().getMemoryWithoutUpdate().getBoolean("$dpl_PrometheusCompleted");
		if (!CompletedPPrometheus) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.REMNANT_FREQ;
	}

	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Helheim Singularity Nodes";
	}
	
	@Override
	public String getIconName() {
		return Global.getSettings().getSpriteName("campaignMissions", "remnant_bounty");
	}
	
	@Override
	public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
		CustomBountyData data = new CustomBountyData();
		data.difficulty = difficulty;
		
		mission.requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_CORE);
		mission.preferSystemUnexplored();
		mission.preferSystemInteresting();
		mission.requireSystemNotHasPulsar();		
		mission.preferSystemBlackHoleOrNebula();
		mission.preferSystemOnFringeOfSector();
		
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.LARGE;
		FleetQuality quality = FleetQuality.VERY_HIGH;
		OfficerQuality oQuality = OfficerQuality.UNUSUALLY_HIGH;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		String type = FleetTypes.PATROL_LARGE;
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, "dpl_helheim_singularity", type, data.system);
		mission.triggerSetFleetCombatFleetPoints(240f);
		mission.triggerSetFleetOfficers(oNum, oQuality);
		mission.triggerAutoAdjustFleetSize(size, size.next());
		mission.triggerSetRemnantConfigActive();
		mission.triggerSetFleetNoCommanderSkills();
		mission.triggerFleetAddCommanderSkill(Skills.ELECTRONIC_WARFARE, 1);
		mission.triggerFleetAddCommanderSkill(Skills.COORDINATED_MANEUVERS, 1);
		mission.triggerFleetAddCommanderSkill(Skills.NAVIGATION, 1);
		mission.triggerFleetSetAllWeapons();
		mission.triggerMakeHostileAndAggressive();
		mission.triggerFleetAllowLongPursuit();
		mission.triggerPickLocationAtInSystemJumpPoint(data.system);
		mission.triggerSpawnFleetAtPickedLocation(null, null);
		mission.triggerFleetSetPatrolActionText("sending hyperwave signals");
		mission.triggerOrderFleetPatrol(data.system, true, Tags.JUMP_POINT, Tags.NEUTRINO, Tags.NEUTRINO_HIGH, Tags.STATION,
									    Tags.SALVAGEABLE, Tags.GAS_GIANT);
		
		data.fleet = createFleet(mission, data);
		if (data.fleet == null) return null;
		
		// otherwise, remnant dialog which isn't appropriate with an Omega in charge
		data.fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.REMNANT_PLUS_MULT, mission);
		
		return data;
	}
	

	@Override
	public int getMaxDifficulty() {
		return super.getMaxDifficulty();
	}

	@Override
	public int getMinDifficulty() {
		return super.getMaxDifficulty();
	}

}






