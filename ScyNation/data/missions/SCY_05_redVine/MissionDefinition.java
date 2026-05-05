package data.missions.SCY_05_redVine;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "RED", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "YEL", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Red Squadron");
		api.setFleetTagline(FleetSide.ENEMY, "Yellow Team");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Your ships are heavier and sturdier than the enemy");
		api.addBriefingItem("You have few ships, avoid being isolated or surrounded");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "conquest_Elite", FleetMemberType.SHIP, "RED Under New Ownership", true);
                api.addToFleet(FleetSide.PLAYER, "eagle_Assault", FleetMemberType.SHIP, "RED Feel lucky, punk", false);
                api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "RED Rebellion", false);
                api.addToFleet(FleetSide.PLAYER, "heron_Attack", FleetMemberType.SHIP, "RED Catch me if you can", false);
                api.addToFleet(FleetSide.PLAYER, "monitor_Escort", FleetMemberType.SHIP, "RED Punk is not a crime", false);
                api.addToFleet(FleetSide.PLAYER, "monitor_Escort", FleetMemberType.SHIP, "RED Yo momma is so fat...", false);
		
		// Set up the enemy fleet

                api.addToFleet(FleetSide.ENEMY, "SCY_khalkotauroi_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_siren_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_corocotta_energy", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_geryon_bomber", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_geryon_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_escort", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 10000f;
		float height = 10000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		//api.addNebula(minX + width * 0.5f, minY, 8000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 4; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 200f; 
			api.addNebula(x, y, radius);
		}
                
                // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
                api.addAsteroidField( minX + (height/2), minY + (width/2), 10, 1500f, 30f, 50f, 50);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.4f, minY + height * 0.5f, "nav_buoy");
                api.addObjective(minX + width * 0.6f, minY + height * 0.5f, "sensor_array");
                api.addObjective(minX + width * 0.5f, minY + height * 0.4f, "comm_relay");
                api.addObjective(minX + width * 0.5f, minY + height * 0.6f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






