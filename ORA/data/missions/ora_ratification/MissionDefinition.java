package data.missions.ora_ratification;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
//import com.fs.starfarer.api.fleet.FleetMemberAPI;
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
		api.initFleet(FleetSide.PLAYER, "ANS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "CLA", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "Underpowered Alliance Humanitarian Force");
		api.setFleetTagline(FleetSide.ENEMY, "Desperate Camillian Liberation Forces");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Destroy the all the enemies.");
		api.addBriefingItem("Customize your loadouts to stand a better chance.");
		api.addBriefingItem("The ANS Illumination must survive!");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
                
                api.addToFleet(FleetSide.PLAYER, "ora_enlightenment_artillery", FleetMemberType.SHIP, "ANS Illumination", true);
                
                api.addToFleet(FleetSide.PLAYER, "ora_beatitude_artillery", FleetMemberType.SHIP, false); 
                api.addToFleet(FleetSide.PLAYER, "ora_beatitude_artillery", FleetMemberType.SHIP, false); 
                api.addToFleet(FleetSide.PLAYER, "ora_beatitude_artillery", FleetMemberType.SHIP, false);   
                api.addToFleet(FleetSide.PLAYER, "ora_revelation_artillery", FleetMemberType.SHIP, false);              
                
                api.addToFleet(FleetSide.PLAYER, "ora_harmony_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_harmony_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_sanctuary_blaster", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_sanctuary_blaster", FleetMemberType.SHIP, false);
                
                api.addToFleet(FleetSide.PLAYER, "ora_mirth_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_mirth_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_bliss_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_bliss_support", FleetMemberType.SHIP, false);                
                
//                api.addToFleet(FleetSide.PLAYER, "ora_felicity_support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "ora_communion_beamer", FleetMemberType.SHIP, false); 
                
                
                api.defeatOnShipLoss("ANS Illumination");
			
		// Set up the enemy fleet.
                
		api.addToFleet(FleetSide.ENEMY, "onslaught_Elite", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);   
		api.addToFleet(FleetSide.ENEMY, "falcon_Attack", FleetMemberType.SHIP, false);   
		api.addToFleet(FleetSide.ENEMY, "falcon_d_CS", FleetMemberType.SHIP, false);   
                                         
		api.addToFleet(FleetSide.ENEMY, "gryphon_FS", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "gryphon_FS", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "sunder_d_Assault", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_d_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Escort", FleetMemberType.SHIP, false);
                
		api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brawler_Starting", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "monitor_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "monitor_Escort", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_AP", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "lasher_d_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "cerberus_d_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_Standard", FleetMemberType.SHIP, false);
                
                
		api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
                
		
                
		// Set up the map.
		float width = 16000f;
		float height = 20000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
		for (int i = 0; i < 20; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 300f; 
			api.addNebula(x, y, radius);
		}
		
//                api.addAsteroidField(0, 0, -90, 18000, 20, 25, 300);
                
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.
                
		api.addObjective(minX + width * 0.45f, minY + height * 0.25f, "nav_buoy");
		api.addObjective(minX + width * 0.55f, minY + height * 0.75f, "nav_buoy");

		api.addObjective(minX + width * 0.25f, minY + height * 0.55f, "sensor_array");
		api.addObjective(minX + width * 0.75f, minY + height * 0.45f, "comm_relay");
		
		api.addPlanet(0, 0, 150, "desert", 0, true);
	}

}
