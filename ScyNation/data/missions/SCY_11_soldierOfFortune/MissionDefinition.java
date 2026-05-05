package data.missions.SCY_11_soldierOfFortune;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        
        @Override
	public void defineMission(MissionDefinitionAPI api) {
            
                api.setHyperspaceMode(true);

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "SCY", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "RRF", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Advanced Recon Force");
		api.setFleetTagline(FleetSide.ENEMY, "Rapid Responce Fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Protect your carriers.");
		api.addBriefingItem("The area of operation is large, and the enemy slower than you.");
		//api.addBriefingItem("Lamias fight better when the target is on their starboard side");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "SCY_keto_combat", FleetMemberType.SHIP, "SCY Cèpe", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_corocotta_combat", FleetMemberType.SHIP, "SCY Fondue", true);
		api.addToFleet(FleetSide.PLAYER, "SCY_erymanthianboar_advanced", FleetMemberType.SHIP, "SCY Gratin Dauphinois", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_manticore_artillery", FleetMemberType.SHIP, "SCY Jambon", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_dracanae_blaster", FleetMemberType.SHIP, "SCY Reblochon", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Omelette", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Crêpe", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_geryon_combat", FleetMemberType.SHIP, "SCY Confit de canard", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_pyraemon_combat", FleetMemberType.SHIP, "SCY Roti", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_combat", FleetMemberType.SHIP, "SCY Foie-gras", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_megaera_combat", FleetMemberType.SHIP, "SCY Saucisson", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "SCY Paté", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Raclette", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "SCY Purée", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Aligot", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_combat", FleetMemberType.SHIP, "SCY Floc", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_combat", FleetMemberType.SHIP, "SCY Vin", false);                
                api.addToFleet(FleetSide.PLAYER, "SCY_talos_combat", FleetMemberType.SHIP, "BLU Papayes", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_talos_saturation", FleetMemberType.SHIP, "BLU Cacahuettes", false);
                
//                FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "SCY_deepSpaceOutpost_standard", FleetMemberType.SHIP, "SCY DSOP-045", false);
//                SCY_station.anchorShip(member, new Vector2f(0, -200), 90); 
		
		// Mark a ship as essential, if you want
//		api.defeatOnShipLoss("SCY DSOP-045");
		
		// Set up the enemy fleet

		api.addToFleet(FleetSide.ENEMY, "paragon_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "aurora_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 20000f;
		float height = 20000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(0, -200, 2000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		//for (int i = 0; i < 7; i++) {
		//	float x = (float) Math.random() * width - width/2;
		//	float y = (float) Math.random() * height - height/2;
		//	float radius = 100f + (float) Math.random() * 400f; 
		//	api.addNebula(x, y, radius);
		//}
                
                // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
                //api.addAsteroidField(0, -6000f, 0, 8000f, 40f, 50f, 350);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.1f, minY + height * 0.9f, "nav_buoy");
                api.addObjective(minX + width * 0.9f, minY + height * 0.9f, "comm_relay");
                api.addObjective(minX + width * 0.1f, minY + height * 0.1f, "comm_relay");
                api.addObjective(minX + width * 0.9f, minY + height * 0.1f, "nav_buoy");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}