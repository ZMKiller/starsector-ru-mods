package data.missions.sfcflight;

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
		api.initFleet(FleetSide.ENEMY, "SFS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The Iapetus IS-20000.");
		api.setFleetTagline(FleetSide.ENEMY, "The Sindrian Fuel Company OpFor.");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the opposing Company fleet with only the Iapetus IS-20000.");
		api.addBriefingItem("The SFS Kween's Gambit must survive.");
		api.addBriefingItem("Captain Yunifer Runi's skills will push the IS-20000 to its limits.");

		//test officers
		PersonAPI sfcruniPerson = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.FEMALE);
		sfcruniPerson.setFaction(Factions.DIKTAT);
		sfcruniPerson.setGender(FullName.Gender.FEMALE);
		sfcruniPerson.setPersonality(Personalities.RECKLESS);
		sfcruniPerson.getName().setFirst("Yunifer");
		sfcruniPerson.getName().setLast("Runi");
		sfcruniPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcruni"));
		sfcruniPerson.setFaction("sindrian_diktat");
		sfcruniPerson.getStats().setLevel(10);
		sfcruniPerson.getStats().setSkillLevel("sfc_iapetus", 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.POINT_DEFENSE, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
		sfcruniPerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
		sfcruniPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
		sfcruniPerson.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 1);
		sfcruniPerson.getMemoryWithoutUpdate().set("$chatterChar", "sfcruni3");


		// Set up the player's fleet
		FleetMemberAPI flagship = api.addToFleet(FleetSide.PLAYER, "sfcsuperiapetus_Mixed", FleetMemberType.SHIP,"SFS Kween's Gambit", true);
		flagship.setCaptain(sfcruniPerson);

		// Mark player flagship as essential
		api.defeatOnShipLoss("SFS Kween's Gambit");

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, "SFS Manager", true);
		api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "champion_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, false);

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
/*
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");
*/
		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
				20f, 70f, 50);

	}

}
