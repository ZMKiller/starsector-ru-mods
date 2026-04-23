package data.missions.dpl_ashestoashes;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 0);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ESCAPE, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Fleet Commander O'Kane's special force");
		api.setFleetTagline(FleetSide.ENEMY, "TriTachyon Phase Fleet with landing forces");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat all enemy forces");
		api.addBriefingItem("DPLS Assasin must survive");
		api.addBriefingItem("Slow down enemy ships before they could get away");
				
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_oboe_standard", FleetMemberType.SHIP, "DPLS Assasin", true);
		api.addToFleet(FleetSide.PLAYER, "dpl_bagpipe_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_bagpipe_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_flumpet_AA", FleetMemberType.SHIP, false);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Assasin");
		
		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, "TTS Silent Hunter", true);
		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "harbinger_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "harbinger_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "harbinger_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "phantom_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "phantom_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "phantom_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "phantom_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "revenant_Elite", FleetMemberType.SHIP, false);
		
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






