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

public class dpl_CBRemnantPlus extends BaseCustomBountyCreator {

	public static String ACCEPTED_KEY = "$dpl_CBRemnantPlus_accepted";
	public static float PROB_IN_SYSTEM_WITH_BASE = 0.5f;
	
	@Override
	public float getBountyDays() {
		return CBStats.REMNANT_PLUS_DAYS;
	}
	
	@Override
	public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
		boolean wasEverAccepted = Global.getSector().getMemoryWithoutUpdate().getBoolean(ACCEPTED_KEY);
		if (wasEverAccepted) return 0f;
		return super.getFrequency(mission, difficulty) * CBStats.REMNANT_PLUS_FREQ;
	}
	
	@Override
	public void notifyAccepted(MarketAPI createdAt, HubMissionWithBarEvent mission, CustomBountyData data) {
		//mission.setNoAbandon();
		Global.getSector().getMemoryWithoutUpdate().set(ACCEPTED_KEY, true);
	}
	
	@Override
	protected boolean isRepeatableGlobally() {
		return false;
	}

	public String getBountyNamePostfix(HubMissionWithBarEvent mission, CustomBountyData data) {
		return " - Exotic Remnant Fleet";
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
		mission.preferSystemTags(ReqMode.ANY, Tags.HAS_CORONAL_TAP);
		mission.preferSystemUnexplored();
		mission.preferSystemInteresting();
		mission.requireSystemNotHasPulsar();		
		mission.preferSystemBlackHoleOrNebula();
		mission.preferSystemOnFringeOfSector();
		
		StarSystemAPI system = mission.pickSystem();
		data.system = system;
	
		FleetSize size = FleetSize.HUGE;
		FleetQuality quality = FleetQuality.VERY_HIGH;
		OfficerQuality oQuality = OfficerQuality.AI_ALPHA;
		OfficerNum oNum = OfficerNum.ALL_SHIPS;
		String type = FleetTypes.PATROL_LARGE;
		
		beginFleet(mission, data);
		mission.triggerCreateFleet(size, quality, Factions.REMNANTS, type, data.system);
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
		
		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.OMEGA, "f1", true);
		fleet.getFleetData().addFleetMember("tesseract_Strike");
		FleetMemberAPI member = fleet.getFlagship();
		
		AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person = plugin.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, mission.getGenRandom());
		member.setCaptain(person);
		
		data.fleet.setCommander(person);
		data.fleet.getFleetData().addFleetMember(member);

		CampaignFleetAPI fleet2 = Global.getFactory().createEmptyFleet(Factions.OMEGA, "f2", true);
		fleet2.getFleetData().addFleetMember("tesseract_Attack");
		FleetMemberAPI member2 = fleet2.getFlagship();
		
		AICoreOfficerPlugin plugin2 = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE);
		PersonAPI person2 = plugin2.createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, mission.getGenRandom());
		member2.setCaptain(person2);
		
		data.fleet.getFleetData().addFleetMember(member2);
		
		data.fleet.getFleetData().sort();
		List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
		for (FleetMemberAPI curr : members) {
			curr.getRepairTracker().setCR(curr.getRepairTracker().getMaxCR());
		}
		
		member.setVariant(member.getVariant().clone(), false, false);
		member.getVariant().setSource(VariantSource.REFIT);
		member.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		
		member2.setVariant(member2.getVariant().clone(), false, false);
		member2.getVariant().setSource(VariantSource.REFIT);
		member2.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);
		member2.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
		
		// otherwise, remnant dialog which isn't appropriate with an Omega in charge
		data.fleet.getMemoryWithoutUpdate().set("$ignorePlayerCommRequests", true);
		
		setRepChangesBasedOnDifficulty(data, difficulty);
		data.baseReward = 2*CBStats.getBaseBounty(difficulty, CBStats.REMNANT_PLUS_MULT, mission);
		
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






