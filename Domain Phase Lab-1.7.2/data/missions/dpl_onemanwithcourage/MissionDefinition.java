package data.missions.dpl_onemanwithcourage;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 0);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Captain O'Kane's classified frigate");
		api.setFleetTagline(FleetSide.ENEMY, "Unknown Luddic Path Fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("DPLS Silence must survive");
		api.addBriefingItem("Pick your targets wisely");
		api.addBriefingItem("Complete this mission to unlock a unique ship at Research Site V.");
				
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_silence_Hull", FleetMemberType.SHIP, "DPLS Silence", true);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Silence");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "prometheus2_Standard", FleetMemberType.SHIP, "Sacrifice", true);
		api.addToFleet(FleetSide.ENEMY, "prometheus2_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "prometheus2_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "manticore_luddic_path_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "manticore_luddic_path_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "manticore_luddic_path_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "manticore_luddic_path_Strike", FleetMemberType.SHIP, false);
		
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
		
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 1000, minY + height * 0.6f, 2000);
	}

}






