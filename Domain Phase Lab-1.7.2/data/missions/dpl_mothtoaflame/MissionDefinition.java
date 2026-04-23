package data.missions.dpl_mothtoaflame;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "PLS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Phase Lab Navy task force");
		api.setFleetTagline(FleetSide.ENEMY, "Flagship Escort in Persean League Ellimination Armada");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces, DPLS Road to Sector must survive.");
		api.addBriefingItem("Gather your forces together, use the range advantage to kite enemy forces.");
		api.addBriefingItem("Use phase lightning wings to defend fighters and missiles.");
		api.addBriefingItem("Complete this mission to unlock a unique ship at Lab Factory.");
				
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_aulochrome_flagship", FleetMemberType.SHIP, "DPLS Road to Sector", true);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Road to Sector");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "pegasus_Strike", FleetMemberType.SHIP, "PLS Nullifier", true);
		api.addToFleet(FleetSide.ENEMY, "pegasus_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "pegasus_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
		
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






