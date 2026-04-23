package data.missions.dpl_finalexam;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 0);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Phase Lab Cadet Patrol Fleet");
		api.setFleetTagline(FleetSide.ENEMY, "TriTachyon First Strike Fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("DPLS Virtue must survive");
		api.addBriefingItem("You will lose if you try to engage in a direct fight.");
		api.addBriefingItem("Use your command points wisely.");
				
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_sarrusophone_assault", FleetMemberType.SHIP, "DPLS Virtue", true);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_elite", FleetMemberType.SHIP, false);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Virtue");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "paragon_Raider", FleetMemberType.SHIP, "TTS Victory", true);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "aurora_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fury_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "fury_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		
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






