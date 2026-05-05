package data.missions.SCY_02_trap;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

        @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HNI", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "SKL", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony Navy Intelligence pursuit fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Skoll defence fleet");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Be ready for anything, who knows what forces these pirates have.");
		
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, "HNI Indomitable", true);
                api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
		
                
		// Set up the enemy fleet

//                api.addToFleet(FleetSide.ENEMY, "SCY_nemeanlion_brawler", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_energy", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_corocottaA_assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_erymanthianboar_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_erymanthianboar_bomber", FleetMemberType.SHIP, false);                
                api.addToFleet(FleetSide.ENEMY, "SCY_stymphalianbird_combat", FleetMemberType.SHIP, false);               
                api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_tisiphone_beamer", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false);   
		
                api.addToFleet(FleetSide.ENEMY, "SCY_nemeanlion_combat", FleetMemberType.SHIP, "SKL End time", false);
		// Set up the map.
		float width = 15000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		

                
                api.addPlanet(0,-5000,180,"SCY_wormholeUnder",0,false);
                api.addPlanet(0,-5000,180,"SCY_wormholeA",0,false);
                api.addPlanet(0,-5000,180,"SCY_wormholeB",0,false);
                api.addPlanet(0,-5000,180,"SCY_wormholeC",0,false);

        for (int i = 0; i < 50; i++) {
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
