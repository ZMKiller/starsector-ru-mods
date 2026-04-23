package data.missions.dpl_ittakesone;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DSS", FleetGoal.ATTACK, false, 0);
		api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The last experimental warship lead by vice admiral Vassiliev");
		api.setFleetTagline(FleetSide.ENEMY, "Drone Ship Extinction Task Force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("DSS Lyre of Orpheus must survive.");
		api.addBriefingItem("The causal shock emitter can overload enemy ships.");
		api.addBriefingItem("Defeat all enemy forces or make them retreat.");
		api.addBriefingItem("Complete this mission to unlock a unique ship at Lab Security Department.");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_vibraphone_standard", FleetMemberType.SHIP, "DSS Lyre of Orpheus", true);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "radiant_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "radiant_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "radiant_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nova_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nova_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "nova_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brilliant_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Overdriven", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Overdriven", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "apex_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fulgent_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false);
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
		
		for (int i = 0; i < 25; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 1000f + (float) Math.random() * 1000f; 
			api.addNebula(x, y, radius);
		}
		
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.4f, 2000);
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.5f, 2000);
		api.addNebula(minX + width * 0.8f - 2000, minY + height * 0.6f, 2000);
	}

}






