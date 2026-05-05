package data.missions.SCY_04_exerciseGardenParty;

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
		api.addBriefingItem("The Manticore is your only carrier, keep it out of harms.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "SCY_khalkotauroi_combat", FleetMemberType.SHIP, "BLU Mozzarella", true);
                api.addToFleet(FleetSide.PLAYER, "SCY_corocotta_artillery", FleetMemberType.SHIP, "BLU Gratin Dauphinois", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_dracanae_combat", FleetMemberType.SHIP, "BLU Lasagnes", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_erymanthianboar_bomber", FleetMemberType.SHIP, "BLU Croque-Monsieur", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "BLU Omelette", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_saturation", FleetMemberType.SHIP, "BLU Crêpe", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "BLU Paté", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "BLU Raclette", false);
                
                api.addToFleet(FleetSide.PLAYER, "SCY_manticore_support", FleetMemberType.SHIP, "BLU Biere", false);
		
		// Mark a ship as essential, if you want
		//api.defeatOnShipLoss("ISS Black Star");
		
		// Set up the enemy fleet

                api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "falcon_Attack", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);                
                api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
                
                api.addToFleet(FleetSide.ENEMY, "gemini_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "gemini_Standard", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 15000f;
		float height = 15000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(minX + (width * 0.15f), minY + (height * 0.2f), 3000);
		api.addNebula(minX + (width * 0.15f), minY + (height * 0.4f), 3000);
		api.addNebula(minX + (width * 0.15f), minY + (height * 0.6f), 3000);
		api.addNebula(minX + (width * 0.15f), minY + (height * 0.8f), 3000);
		api.addNebula(minX + (width * 0.15f), minY + (height * 1.0f), 3000);
		api.addNebula(minX + (width * 0.15f), minY + (height * 0.0f), 3000);
		
		api.addNebula(minX + (width * 0.85f), minY + (height * 0.2f), 3000);
		api.addNebula(minX + (width * 0.85f), minY + (height * 0.4f), 3000);
		api.addNebula(minX + (width * 0.85f), minY + (height * 0.6f), 3000);
		api.addNebula(minX + (width * 0.85f), minY + (height * 0.8f), 3000);
		api.addNebula(minX + (width * 0.85f), minY + (height * 1.0f), 3000);
		api.addNebula(minX + (width * 0.85f), minY + (height * 0.0f), 3000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		//for (int i = 0; i < 12; i++) {
		//	float x = (float) Math.random() * width - width/2;
		//	float radius = 100f + (float) Math.random() * 400f; 
		//	api.addNebula(x, y, radius);
		//}
                
        // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
        //api.addAsteroidField( minX + (height*0.25f), 0, 0, 10000f, 15f, 20f, 300);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.5f, minY + height * 0.75f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "comm_relay");
        api.addObjective(minX + width * 0.15f, minY + height * 0.5f, "nav_buoy");
        api.addObjective(minX + width * 0.85f, minY + height * 0.5f, "nav_buoy");
        //api.addObjective(minX + width * 0.25f, minY + height * 0.35f, "comm_relay");
        //api.addObjective(minX + width * 0.75f, minY + height * 0.35f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






