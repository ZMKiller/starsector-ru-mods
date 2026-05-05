package data.missions.SCY_03_exerciseAppleTree;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "BLU", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "RED", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Blue Team");
		api.setFleetTagline(FleetSide.ENEMY, "Red Squadron");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Geryons (M) and Lealapses are extremely efficient at sinking a single target!");
		api.addBriefingItem("They don't have much ammunitions, be sure to use them at the right time.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_beamer", FleetMemberType.SHIP, "BLU Omelette", true);
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "BLU Crêpe", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_geryon_bomber", FleetMemberType.SHIP, "BLU Confit de canard", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_pyraemon_combat", FleetMemberType.SHIP, "BLU Brochette", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "BLU Paté", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "BLU Raclette", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_talos_combat", FleetMemberType.SHIP, "BLU Papayes", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_megaera_support", FleetMemberType.SHIP, "BLU Cacahuettes", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_bomber", FleetMemberType.SHIP, "BLU Vin", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_combat", FleetMemberType.SHIP, "BLU Floc", false);
		
		// Mark a ship as essential, if you want
		//api.defeatOnShipLoss("ISS Black Star");
		
		// Set up the enemy fleet

                api.addToFleet(FleetSide.ENEMY, "dominator_Outdated", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "enforcer_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 10000f;
		float height = 15000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + width * 0.5f, minY, 6000);
                api.addNebula(minX , minY, 6000);
                api.addNebula(width/2 , minY, 6000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 7; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
                
                // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
                api.addAsteroidField(0, minY + ( height*0.25f), 0, 7000f, 40f, 50f, 150);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.25f, minY + height * 0.65f, "nav_buoy");
                api.addObjective(minX + width * 0.75f, minY + height * 0.65f, "sensor_array");
                api.addObjective(minX + width * 0.25f, minY + height * 0.35f, "comm_relay");
                api.addObjective(minX + width * 0.75f, minY + height * 0.35f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






