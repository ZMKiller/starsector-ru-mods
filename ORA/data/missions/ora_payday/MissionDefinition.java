package data.missions.ora_payday;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
	public void defineMission(MissionDefinitionAPI api) {

	
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "BAD", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ANS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Ex Camillian Division");
		api.setFleetTagline(FleetSide.ENEMY, "Humanitarian Force scouts");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Destroy all the enemies.");
		api.addBriefingItem("Customize your loadouts to stand a better chance.");
		api.addBriefingItem("The BAD Dragon must survive");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
			
                api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, "BAD Dragon", true);
                
                //api.addToFleet(FleetSide.PLAYER, "hammerhead_Tutorial", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "hammerhead_d_CS", FleetMemberType.SHIP, false);
//                api.addToFleet(FleetSide.PLAYER, "hammerhead_d_CS", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "sunder_d_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_d_CS", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_d_CS", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_d_CS", FleetMemberType.SHIP, false);
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);		
		//api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);
                FleetMemberAPI member1 = api.addToFleet(FleetSide.ENEMY, "ora_harmony_assault", FleetMemberType.SHIP, false);
                member1.getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "ora_harmony_blaster", FleetMemberType.SHIP, false);
                FleetMemberAPI member2 = api.addToFleet(FleetSide.ENEMY, "ora_mirth_pulse", FleetMemberType.SHIP, false);
                member2.getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "ora_mirth_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "ora_mirth_pulse", FleetMemberType.SHIP, false);
//                api.addToFleet(FleetSide.ENEMY, "ora_mirth_pulse", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "ora_bliss_support", FleetMemberType.SHIP, false);
                                
                api.addToFleet(FleetSide.ENEMY, "ora_communion_beamer", FleetMemberType.SHIP, false);
		
                api.defeatOnShipLoss("BAD Dragon");
                
		// Set up the map.
		float width = 15000f;
		float height = 16000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 7; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 800f; 
			api.addNebula(x, y, radius);
		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.

		api.addObjective(minX + width * 0.7f, minY + height * 0.25f, "sensor_array");
		api.addObjective(minX + width * 0.8f, minY + height * 0.75f, "nav_buoy");
		api.addObjective(minX + width * 0.2f, minY + height * 0.25f, "nav_buoy");
		api.addObjective(minX + width * 0.3f, minY + height * 0.65f, "comm_relay");
		
		
	}

}
