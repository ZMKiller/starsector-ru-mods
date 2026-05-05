package data.missions.SCY_03_allin;

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

        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "HNS", FleetGoal.ESCAPE, false);
        api.initFleet(FleetSide.ENEMY, "SKL", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Hegemony Navy heavy recon fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Skoll fleet remains");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Heavy losses have been suffered by both sides");
        api.addBriefingItem("The enemy favour torpedoes");
        api.addBriefingItem("You must permit at least the HNS Optimum to escape");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, "HNS Vigor", true);
        api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "HNS Dominion", false);
        api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP,"HNS Dallas", false);
        api.addToFleet(FleetSide.PLAYER, "valkyrie_Elite", FleetMemberType.SHIP,"HNS Optimum", false);
        api.addToFleet(FleetSide.PLAYER, "tarsus_d_Standard", FleetMemberType.SHIP, false);

        api.defeatOnShipLoss("HNS Optimum");
        // Set up the enemy fleet

        api.addToFleet(FleetSide.ENEMY, "SCY_geryon_bomber", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false);

        api.addToFleet(FleetSide.ENEMY, "SCY_keto_combat", FleetMemberType.SHIP, "SKL Pinacle", false);
        // Set up the map.
        float width = 10000f;
        float height = 32000f;
        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        for (int i = 0; i < 10; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 200f + (float) Math.random() * 600f;
            api.addNebula(x, y, radius);
        }

        // Add objectives
        api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "sensor_array");
        api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "sensor_array");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "nav_buoy");
    }
}
