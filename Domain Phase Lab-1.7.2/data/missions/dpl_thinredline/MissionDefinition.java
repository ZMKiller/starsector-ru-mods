package data.missions.dpl_thinredline;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Domain Phase Lab experimental platforms");
		api.setFleetTagline(FleetSide.ENEMY, "Drone Ship Ellimination Task Force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces, DPLS Curtain Call must survive.");
		api.addBriefingItem("Gather your forces together, your flagship can keep enemies away.");
		api.addBriefingItem("Reminder: Ready! Aim! Fire!");
		api.addBriefingItem("Complete this mission to unlock a unique ship at [SUPER REDACTED] planet.");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_clarinet_elite", FleetMemberType.SHIP, "DPLS Curtain Call", true);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_hydraulis_special", FleetMemberType.SHIP, false);
				
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Curtain Call");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "radiant_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nova_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nova_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Overdriven", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Overdriven", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false);
		
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
				
		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
								20f, 70f, 50);
		
		// Add some planets.  These are defined in data/config/planets.json.
		api.addPlanet(0, 0, 200f, "tundra", 350f, true);
	}

}






