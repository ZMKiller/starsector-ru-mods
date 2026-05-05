package data.missions.ed_showcasewurg;

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
		api.initFleet(FleetSide.PLAYER, "EDS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "PIR", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Bounty Hunters");
		api.setFleetTagline(FleetSide.ENEMY, "Pirate Fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Protect the Wurgandal, use the Tyrant Maw to destroy enemy Station.");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters

				api.addToFleet(FleetSide.PLAYER, "edshipyard_wurgandal_worldeater", FleetMemberType.SHIP, true);
				api.addToFleet(FleetSide.PLAYER, "edshipyard_newfoundland_warfreighter", FleetMemberType.SHIP, true);
				api.addToFleet(FleetSide.PLAYER, "edshipyard_leonberger_assault", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "edshipyard_bullmastiff_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_groenendael_dust", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_dobermann_variant", FleetMemberType.SHIP, false); 
                api.addToFleet(FleetSide.PLAYER, "edshipyard_rottweiler_assault", FleetMemberType.SHIP, false); 
                api.addToFleet(FleetSide.PLAYER, "edshipyard_rottweiler_ranged", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_retriever_rescue", FleetMemberType.SHIP, false);	
                api.addToFleet(FleetSide.PLAYER, "edshipyard_carolina_gunner", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_saluki_assault", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "edshipyard_basset_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_beauceron_rescue", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_beagle_assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_beagle_assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_shiba_assault", FleetMemberType.SHIP, false);                 
                api.addToFleet(FleetSide.PLAYER, "edshipyard_pomeranian_dust", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_pomeranian_dust", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_collie_rescue", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_collie_rescue", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "edshipyard_bischon_assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_corgi_dust", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_corgi_dust", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "edshipyard_wolfhound_assault", FleetMemberType.SHIP, false);   
                api.addToFleet(FleetSide.PLAYER, "edshipyard_wolfhound_assault", FleetMemberType.SHIP, false); 
                api.addToFleet(FleetSide.PLAYER, "edshipyard_chihuahua_assault", FleetMemberType.SHIP, false);   
                api.addToFleet(FleetSide.PLAYER, "edshipyard_chihuahua_assault", FleetMemberType.SHIP, false);
		
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);		
		//api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);

                
        api.addToFleet(FleetSide.ENEMY, "station1_Standard", FleetMemberType.SHIP, "Pirate Base", false);
                
		api.addToFleet(FleetSide.ENEMY, "edshipyard_retriever_pirate", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "edshipyard_retriever_pirate", FleetMemberType.SHIP, false);	
		api.addToFleet(FleetSide.ENEMY, "edshipyard_retriever_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "atlas2_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "atlas2_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "atlas2_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_dobermann_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_dobermann_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_beauceron_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_beauceron_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_beauceron_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_d_pirates_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_dachshund_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_dachshund_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_dachshund_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_carolina_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_carolina_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_carolina_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_wolfhound_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_wolfhound_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_wolfhound_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "edshipyard_wolfhound_pirate", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "shade_d_pirates_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "shade_d_pirates_Assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "afflictor_d_pirates_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "afflictor_d_pirates_Strike", FleetMemberType.SHIP, false);
                
                
		// Set up the map.
		float width = 14000f;
		float height = 14000f;
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

		api.addObjective(minX + width * 0.3f, minY + height * 0.3f, "sensor_array");
		api.addObjective(minX + width * 0.7f, minY + height * 0.3f, "nav_buoy");
		api.addObjective(minX + width * 0.3f, minY + height * 0.7f, "nav_buoy");
		api.addObjective(minX + width * 0.7f, minY + height * 0.7f, "sensor_array");
		
		
	}

}
