package data.missions.SCY_07_goldenApple;

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
		api.initFleet(FleetSide.ENEMY, "GRN", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Red Squadron");
		api.setFleetTagline(FleetSide.ENEMY, "Green Team");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The Hyperion is one of the few ships that can outmanoeuver Scy designs");
		api.addBriefingItem("The enemy is using a lot of missiles boats.");
		//api.addBriefingItem("Your Atlas must survive.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "hyperion_Attack", FleetMemberType.SHIP, "RED 12 inches", true);
                api.addToFleet(FleetSide.PLAYER, "aurora_Balanced", FleetMemberType.SHIP,"RED Smelly one", false);
                api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP,"RED Incestuous Granny", false);
                api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP,"RED Backstab", false);
//                api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP,"RED Kneecap", false);
                api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP,"RED Bad Dog", false);
                api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP,"RED Gold Digger", false);
                api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP,"RED White Collar", false);
				
		
		// Mark a ship as essential, if you want
		//api.defeatOnShipLoss("RED Sexual harassment");
		
		// Set up the enemy fleet

                api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_erymanthianboar_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_dracanae_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_lamia_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_escort", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_escort", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_harasser", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_harasser", FleetMemberType.SHIP, false);
		
				
		
		// Set up the map.
		float width = 15000f;
		float height = 15000f;
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
		for (int i = 0; i < 5; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 200f + (float) Math.random() * 600f; 
			api.addNebula(x, y, radius);
		}
                
                // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
                //api.addAsteroidField( minX + (height/2), minY + (width/2), 90, 15000f, 0f, 10f, 750);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
                api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "nav_buoy");
                api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "sensor_array");
                api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "comm_relay");
                api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "comm_relay");
                api.addObjective(minX + width * 0.5f, minY + height * 0.75f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






