package data.missions.SCY_10_whackAMole;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "SCY", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "IST", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "14th Intel Division");
        api.setFleetTagline(FleetSide.ENEMY, "Independant Convoy");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("No one must escape to report the position of the outpost");
        api.addBriefingItem("Keep your cruiser alive, or the base will be defenceless");
        //api.addBriefingItem("Your Atlas must survive.");

        // Set up the player's fleet
	api.addToFleet(FleetSide.PLAYER, "SCY_corocotta_combat", FleetMemberType.SHIP, "SCY Fondue", true);
	api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Omelette", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Crêpe", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Paté", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Raclette", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_combat", FleetMemberType.SHIP, "SCY Foie-gras", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_tisiphone_combat", FleetMemberType.SHIP, "SCY Saussice", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_megaera_combat", FleetMemberType.SHIP, "SCY Saucisson", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_megaera_combat", FleetMemberType.SHIP, "SCY Croustade", false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);


        // Mark a ship as essential, if you want
        api.defeatOnShipLoss("SCY Fondue");
		
        // Set up the enemy fleet

//        FleetMemberAPI member01 = api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
		FleetMemberAPI member02= api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
//		FleetMemberAPI member03 = api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, false);
		FleetMemberAPI member04 = api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member05 = api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member06 = api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
		FleetMemberAPI member07 = api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
        
        
        // Set up the map.
        float width = 15000f;
        float height = 25000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.

        // Add two big nebula clouds
//        api.addNebula(minX + width * 0.5f, minY + height * 0.5f, 2000);
//        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 2000);
//        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2000);
//        api.addNebula(minX + width * 1f, minY + height * 0.5f, 2000);
//        api.addNebula(minX + width * 0f, minY + height * 0.5f, 2000);

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 15; i++) {
                float x = (float) Math.random() * width - width/2;
                float y = (float) Math.random() * height - height/2;
                float radius = 300f + (float) Math.random() * 600f; 
                api.addNebula(x, y, radius);
        }

        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        //api.addAsteroidField( 0, 0, -90f, 3000f, 150f, 250f, 750);

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






