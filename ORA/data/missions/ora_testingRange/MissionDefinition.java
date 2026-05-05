package data.missions.ora_testingRange;

import com.fs.starfarer.api.fleet.FleetGoal;
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
		api.initFleet(FleetSide.PLAYER, "ORS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Testing grounds");
		api.setFleetTagline(FleetSide.ENEMY, "Targets");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
//		api.addBriefingItem("Launch missiles for maximum PD saturation.");
//		api.addBriefingItem("Use your shield sparingly at high flux, your shielding is merely average.");
//		api.addBriefingItem("USN HeadOn must survive");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
			
                api.addToFleet(FleetSide.PLAYER, "ora_enlightenment_artillery", FleetMemberType.SHIP, true);
             	                
                api.addToFleet(FleetSide.PLAYER, "ora_discernment_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_beatitude_artillery", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_revelation_artillery", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_elevation_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_felicity_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_harmony_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_grace_pulse", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_sanctuary_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_communion_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_ascension_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_mirth_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_bliss_pulse", FleetMemberType.SHIP, false);
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);		
		//api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
                            	
		
		// Set up the map.
		float width = 15000f;
		float height = 11000f;
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
		
		
	}

}
