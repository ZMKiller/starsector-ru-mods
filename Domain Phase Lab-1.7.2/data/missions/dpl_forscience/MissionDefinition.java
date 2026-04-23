package data.missions.dpl_forscience;

import java.util.List;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		
		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "DPLS", FleetGoal.ATTACK, false, 2);
		api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The DPLS Machina Sacra and her Escorts");
		api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon Special Force");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The DPLS Machina Sacra Must Survive");
		api.addBriefingItem("Defeat all enemy forces or make them retreat.");
		api.addBriefingItem("This mission unlocks a super capital when you get familiar with Ross.");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "dpl_jazzophone_standard", FleetMemberType.SHIP, "DPLS Sacra Machina", true);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "dpl_trombone_standard", FleetMemberType.SHIP, false);
		
		// Mark player flagship as essential
		api.defeatOnShipLoss("DPLS Machina Sacra");
		
		// Set up the enemy fleet

		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "medusa_Starting", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Starting", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "shrike_Support", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "fury_Attack", FleetMemberType.SHIP, false);
		
		//api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "shrike_Support", FleetMemberType.SHIP, false);
		
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
		//api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
		
		
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
		api.setBackgroundSpriteName("graphics/backgrounds/hyperspace1.jpg");
		//api.setBackgroundSpriteName("graphics/backgrounds/background2.jpg");
		
		//system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");
		//api.setBackgroundSpriteName();
		
		// Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
		api.addAsteroidField(0f, 0f, (float) Math.random() * 360f, width,
									20f, 70f, 100);
		
		
		api.addPlugin(new BaseEveryFrameCombatPlugin() {
			public void advance(float amount, List events) {
			}
			public void init(CombatEngineAPI engine) {
				engine.getContext().setStandoffRange(10000f);
			}
		});
	}

}






