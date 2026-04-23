package data.missions.titanomachy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "SFS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The Capital Ships of the Grand Fuel Fleet.");
		api.setFleetTagline(FleetSide.ENEMY, "The unusual pirate fleet.");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the hostile enemy fleet.");
		api.addBriefingItem("The SFS Fuel Eternal must survive.");
		api.addBriefingItem("Admiral Ruka Yenni is a highly skilled officer.");
		api.addBriefingItem("The other capital ships are also commanded by skilled, aggressive officers.");

		//test officers
		PersonAPI sfcyenniPerson = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.FEMALE);
		sfcyenniPerson.setFaction(Factions.DIKTAT);
		sfcyenniPerson.setGender(FullName.Gender.FEMALE);
		sfcyenniPerson.setPersonality(Personalities.AGGRESSIVE);
		sfcyenniPerson.getName().setFirst("Ruka");
		sfcyenniPerson.getName().setLast("Yenni");
		sfcyenniPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcyenni"));
		sfcyenniPerson.setFaction("sindrian_diktat");
		sfcyenniPerson.getStats().setLevel(15);
		sfcyenniPerson.getStats().setSkillLevel("sfc_titan", 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
		sfcyenniPerson.getMemoryWithoutUpdate().set("$chatterChar", "sfcyenni");

		PersonAPI person1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
		person1.setId("sfc_generic_officer1");
		person1.setPostId(Ranks.POST_OFFICER);
		person1.setRankId(Ranks.POST_OFFICER);
		person1.setPersonality(Personalities.AGGRESSIVE);
		person1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		person1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		person1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		person1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		person1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person1.getStats().setLevel(7);

		PersonAPI person2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
		person2.setId("sfc_generic_officer2");
		person2.setPostId(Ranks.POST_OFFICER);
		person2.setRankId(Ranks.POST_OFFICER);
		person2.setPersonality(Personalities.AGGRESSIVE);
		person2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		person2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		person2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		person2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		person2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person2.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person2.getStats().setLevel(7);

		PersonAPI person3 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
		person3.setId("sfc_generic_officer3");
		person3.setPostId(Ranks.POST_OFFICER);
		person3.setRankId(Ranks.POST_OFFICER);
		person3.setPersonality(Personalities.AGGRESSIVE);
		person3.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person3.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		person3.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		person3.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		person3.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		person3.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person3.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person3.getStats().setLevel(7);

		PersonAPI reckless1 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
		reckless1.setId("sfc_reckless1");
		reckless1.setPostId(Ranks.POST_OFFICER);
		reckless1.setRankId(Ranks.POST_OFFICER);
		reckless1.setPersonality(Personalities.RECKLESS);
		reckless1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		reckless1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		reckless1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		reckless1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		reckless1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		reckless1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		reckless1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		reckless1.getStats().setLevel(7);

		PersonAPI reckless2 = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.ANY);
		reckless2.setId("sfc_reckless2");
		reckless2.setPostId(Ranks.POST_OFFICER);
		reckless2.setRankId(Ranks.POST_OFFICER);
		reckless2.setPersonality(Personalities.RECKLESS);
		reckless2.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		reckless2.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		reckless2.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		reckless2.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		reckless2.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		reckless2.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		reckless2.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		reckless2.getStats().setLevel(7);

		// Set up the player's fleet
		FleetMemberAPI flagship = api.addToFleet(FleetSide.PLAYER, "sfciapetus_Mixed", FleetMemberType.SHIP,"SFS Fuel Eternal", true);
		flagship.setCaptain(sfcyenniPerson);
		//api.addToFleet(FleetSide.PLAYER, "sfciapetus_Mixed", FleetMemberType.SHIP,"SFS Fuel Eternal", false);
		FleetMemberAPI prometheus = api.addToFleet(FleetSide.PLAYER, "sfcprometheus_barrage", FleetMemberType.SHIP,"SFS Fuel Eagle", false);
		prometheus.setCaptain(person1);
		FleetMemberAPI epimetheus = api.addToFleet(FleetSide.PLAYER, "sfcepimetheus_Experimental", FleetMemberType.SHIP,"SFS Afterfuel", false);
		epimetheus.setCaptain(reckless1);
		FleetMemberAPI menoetius = api.addToFleet(FleetSide.PLAYER, "sfcmenoetius_Defense", FleetMemberType.SHIP,"SFS Fuelrage", false);
		menoetius.setCaptain(person2);
		FleetMemberAPI notos = api.addToFleet(FleetSide.PLAYER, "sfcskyrend_Barrage", FleetMemberType.SHIP,"SFS Not a Skysplitter", false);
		notos.setCaptain(reckless2);
		FleetMemberAPI atlas = api.addToFleet(FleetSide.PLAYER, "sfcatlas_Strike", FleetMemberType.SHIP,"SFS Fuelslinger", false);
		atlas.setCaptain(person3);

		// Mark player flagship as essential
		api.defeatOnShipLoss("SFS Fuel Eternal");

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP,  true);
		api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP,  false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);

		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

		float minX = -width/2;
		float minY = -height/2;

		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f;
			api.addNebula(x, y, radius);
		}

	}

}
