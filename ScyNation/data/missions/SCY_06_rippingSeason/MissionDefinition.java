package data.missions.SCY_06_rippingSeason;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "RED", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "BLU", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Red Squadron");
		api.setFleetTagline(FleetSide.ENEMY, "Blue Team");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Scy ships are susceptible to attacks from multiple directions.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "RED Tickle Maniac", true);
                api.addToFleet(FleetSide.PLAYER, "lasher_Strike", FleetMemberType.SHIP,"RED Dirty Panties", false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP,"RED Turncoat", false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Strike", FleetMemberType.SHIP,"RED Spanker", false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP,"RED Wanker", false);
                api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP,"RED In your face", false);
                api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP,"RED Kitties Butcher", false);
                api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP,"RED Ex Wife", false);
                api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP,"RED Mother-in-law", false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);

		// Mark a ship as essential, if you want
		api.defeatOnShipLoss("RED Sexual harassment");
		
		// Set up the enemy fleet

                FleetMemberAPI member01 = api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member02 = api.addToFleet(FleetSide.ENEMY, "SCY_megaera_support", FleetMemberType.SHIP, false);
                FleetMemberAPI member03 = api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member06 = api.addToFleet(FleetSide.ENEMY, "SCY_talos_beamer", FleetMemberType.SHIP, false);
                FleetMemberAPI member08 = api.addToFleet(FleetSide.ENEMY, "SCY_lealaps_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member11 = api.addToFleet(FleetSide.ENEMY, "SCY_lealaps_support", FleetMemberType.SHIP, false);
                FleetMemberAPI member12 = api.addToFleet(FleetSide.ENEMY, "SCY_alecto_escort", FleetMemberType.SHIP, false);
                FleetMemberAPI member13 = api.addToFleet(FleetSide.ENEMY, "SCY_alecto_escort", FleetMemberType.SHIP, false);
                FleetMemberAPI member14 = api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member15 = api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member16 = api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_assault", FleetMemberType.SHIP, false);
                FleetMemberAPI member17 = api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_assault", FleetMemberType.SHIP, false);
                FleetMemberAPI member18 = api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_combat", FleetMemberType.SHIP, false);
                FleetMemberAPI member19 = api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
		
				//api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
                //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);
				//api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
				//api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false);
		
		// Set up the map.
		float width = 10000f;
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
		for (int i = 0; i < 12; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 200f; 
			api.addNebula(x, y, radius);
		}
                
                // Add an asteroid field going diagonally across the
		// battlefield, 2000 pixels wide, with a maximum of 
		// 100 asteroids in it.
		// 20-70 is the range of asteroid speeds.
                api.addAsteroidField( minX + (width/2), minY + (height/2), 90, 10000f, 0f, 10f, 750);
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.5f, minY + height * 0.75f, "nav_buoy");
                //api.addObjective(minX + width * 0.6f, minY + height * 0.5f, "sensor_array");
                api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "comm_relay");
                //api.addObjective(minX + width * 0.5f, minY + height * 0.6f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






