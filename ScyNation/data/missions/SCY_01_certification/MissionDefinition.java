package data.missions.SCY_01_certification;

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
		api.setFleetTagline(FleetSide.PLAYER, "Certification frigate");
		api.setFleetTagline(FleetSide.ENEMY, "Red squadron");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Scyan ships flight profile is extremely focussed on hit-and-run tactics.");
		api.addBriefingItem("Mobility is paramount, don't get surrounded");
		api.addBriefingItem("Venting is the only way to dissipate flux efficiently");
                api.addBriefingItem("Don't forget about your shield system");
		
		// Set up the player's fleet
		api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_combat", FleetMemberType.SHIP, "SCY Foie-gras", true);
		
		// Mark a ship as essential, if you want
		//api.defeatOnShipLoss("ISS Black Star");
		
		// Set up the enemy fleet

		api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, true).getCaptain().setPersonality("aggressive");
		
		// Set up the map.
		float width = 10000f;
		float height = 10000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// Add two big nebula clouds
		//api.addNebula(minX + width * 0.66f, minY + height * 0.5f, 2000);
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 2; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 400f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
						 
		//api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_homePlanet", 0f);
	}

}






