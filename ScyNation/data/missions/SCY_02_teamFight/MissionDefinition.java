package data.missions.SCY_02_teamFight;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "BLU", FleetGoal.ATTACK, true);
		api.initFleet(FleetSide.ENEMY, "YEL", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Blue Team");
		api.setFleetTagline(FleetSide.ENEMY, "Yellow Team");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("With multiple ships, specialization is advised");
		api.addBriefingItem("Tisiphone frigates are extremely offensive");
		api.addBriefingItem("Alecto frigates are great PD boats");
		api.addBriefingItem("You are NOT in command of the fleet for this battle");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_beamer", FleetMemberType.SHIP, "BLU Foie-gras", true);
                api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_combat", FleetMemberType.SHIP, "BLU Tarte", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_harasser", FleetMemberType.SHIP, "BLU Pat�", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "BLU Raclette", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_support", FleetMemberType.SHIP, "BLU Vin", false);
		api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_combat", FleetMemberType.SHIP, "BLU Floc", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_talos_combat", FleetMemberType.SHIP, "BLU Papayes", false);
                api.addToFleet(FleetSide.PLAYER, "SCY_megaera_saturation", FleetMemberType.SHIP, "BLU Cacahuettes", false);
		
		// Mark a ship as essential, if you want
		//api.defeatOnShipLoss("ISS Black Star");
		
		// Set up the enemy fleet

		api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_brawler", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "SCY_lealaps_bomber", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "SCY_lealaps_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_talos_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false);
		
		// Set up the map.
		float width = 15000f;
		float height = 15000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		api.addNebula(0, -200, 700);
		api.addNebula(200, 500, 300);
		api.addNebula(600, 100, 200);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		//for (int i = 0; i < 2; i++) {
		//	float x = (float) Math.random() * width - width/2;
		//	float y = (float) Math.random() * height - height/2;
		//	float radius = 100f + (float) Math.random() * 400f; 
		//	api.addNebula(x, y, radius);
		//}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "nav_buoy");
                api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "nav_buoy");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 150f, "SCY_homePlanet", 0f);
	}

}






