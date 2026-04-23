package data.missions.weekendatphillips;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "SFS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "SFS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The next generation of the Sindrian Fuel Fleet.");
		api.setFleetTagline(FleetSide.ENEMY, "The battle-tested designs of a bygone era.");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Impress the higher ups in the Company by defeating the enemy fleet.");
		api.addBriefingItem("SFS Kween is Researcher's Kween lifelong work. It must survive.");
		api.addBriefingItem("Ensure strong cohesion with your fleet. Don't let your ships be separated to be easy pickings.");
		api.addBriefingItem("The enemy lacks any fighter wings supporting them: Use this to your advantage.");

		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "sfcprometheus_barrage", FleetMemberType.SHIP, "SFS Kween", true);
		api.addToFleet(FleetSide.PLAYER, "sfcphaeton_ballistics", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcphaeton_bombard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcarke_Suppression", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcdrachm_Pressure", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcdram_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcdram_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "sfcdram_missile", FleetMemberType.SHIP, false);

		// Mark player flagship as essential
		api.defeatOnShipLoss("SFS Kween");

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

		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");

		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
				20f, 70f, 50);

	}

}
