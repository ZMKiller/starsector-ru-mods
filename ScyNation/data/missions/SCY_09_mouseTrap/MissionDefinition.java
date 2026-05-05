package data.missions.SCY_09_mouseTrap;

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
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
        api.initFleet(FleetSide.ENEMY, "???", FleetGoal.ESCAPE, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "14.1 Intel Division");
        api.setFleetTagline(FleetSide.ENEMY, "TRAITOR");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Catch the traitor!");
        api.addBriefingItem("Beware of the collisions");
        //api.addBriefingItem("Your Atlas must survive.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "SCY_megaera_combat", FleetMemberType.SHIP, "SCY Saucisson", true);
        api.addToFleet(FleetSide.PLAYER, "SCY_lealaps_support", FleetMemberType.SHIP, "SCY Croustade", false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);


        // Mark a ship as essential, if you want
        //api.defeatOnShipLoss("RED Sexual harassment");

        // Set up the enemy fleet

        api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
        
        
        // Set up the map.
        float width = 3000f;
        float height = 35000f;
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
        api.addAsteroidField( 0, 0, -90f, 3000f, 150f, 250f, 750);

        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        //api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "nav_buoy");
        //api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "sensor_array");
        //api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "comm_relay");
        //api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "comm_relay");
        //api.addObjective(minX + width * 0.5f, minY + height * 0.75f, "sensor_array");

        //api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
        
        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
		context.setInitialEscapeRange(9000f);
		api.addPlugin(new EscapeRevealPlugin(context));
	}

}






