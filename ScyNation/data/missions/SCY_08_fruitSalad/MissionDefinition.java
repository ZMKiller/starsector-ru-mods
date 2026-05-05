package data.missions.SCY_08_fruitSalad;

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
        api.initFleet(FleetSide.ENEMY, "SCY", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "14th Intel Squadron");
        api.setFleetTagline(FleetSide.ENEMY, "15th Intel Squadron");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Show your worth!");
        //api.addBriefingItem("Scy ships are susceptible to attacks from multiple directions.");
        //api.addBriefingItem("Your Atlas must survive.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "SCY_siren_combat", FleetMemberType.SHIP, "SCY Gratin Dauphinois", true);
        api.addToFleet(FleetSide.PLAYER, "SCY_khalkotauroi_combat", FleetMemberType.SHIP, "SCY Lasagnes", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_erymanthianboar_bomber", FleetMemberType.SHIP, "SCY Reblochon", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_erymanthianboar_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Omelette", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_lamia_combat", FleetMemberType.SHIP, "SCY Crêpe", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "SCY Paté", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_escort", FleetMemberType.SHIP, "SCY Raclette", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Foie-gras", false);
        api.addToFleet(FleetSide.PLAYER, "SCY_alecto_combat", FleetMemberType.SHIP, "SCY Saussice", false);

        // Set up the enemy fleet

        api.addToFleet(FleetSide.ENEMY, "SCY_dracanae_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_dracanae_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_energy", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_corocotta_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_geryon_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_pyraemon_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false);



        // Set up the map.
        float width = 10000f;
        float height = 15000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.

        // Add two big nebula clouds
        api.addNebula(minX + width * 0.5f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.25f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0.75f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 1f, minY + height * 0.5f, 2000);
        api.addNebula(minX + width * 0f, minY + height * 0.5f, 2000);

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 5; i++) {
                float x = (float) Math.random() * width - width/2;
                float y = (float) Math.random() * height - height/2;
                float radius = 100f + (float) Math.random() * 300f; 
                api.addNebula(x, y, radius);
        }

        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of 
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        //api.addAsteroidField( minX + (height/2), minY + (width/2), 90, 15000f, 0f, 10f, 750);

        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        //api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "nav_buoy");
        //api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "sensor_array");
        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.5f, "comm_relay");
        //api.addObjective(minX + width * 0.5f, minY + height * 0.75f, "sensor_array");

        //api.addPlanet(minX + width * 0.5f, minY + height * 0.5f, 200f, "SCY_miningColony", 0f);
	}

}






