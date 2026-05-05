package data.missions.SCY_01_trail;

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
		api.initFleet(FleetSide.PLAYER, "HNI", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "SKL", FleetGoal.ESCAPE, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Hegemony Navy Intelligence pursuit fleet");
		api.setFleetTagline(FleetSide.ENEMY, "Skoll pirates convoy");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Catch as many fleeing ships as you can before they reach the wormhole");
		api.addBriefingItem("They are very fast, capture nav points to regain the advantage");
		 
		// Set up the player's fleet
                api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, "HNI Indomitable", true);
                
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP,false);
                api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "condor_Support", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "lasher_Standard", FleetMemberType.SHIP, false);
		
                api.defeatOnShipLoss("HNI Indomitable");
		// Set up the enemy fleet

                api.addToFleet(FleetSide.ENEMY, "SCY_manticore_support", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_corocotta_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_erymanthianboar_bomber", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusF_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusF_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusF_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusF_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusT_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_baliusT_auxiliary", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_xanthus_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_xanthus_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_talos_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_megaera_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_combat", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
                api.addToFleet(FleetSide.ENEMY, "SCY_alecto_advanced", FleetMemberType.SHIP, false).getCaptain().setPersonality("aggressive");
		
		// Set up the map.
		float width = 10000f;
		float height = 24000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
                api.addObjective(minX + width * 0.3f, minY + height * 0.15f, "nav_buoy");
                api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "nav_buoy");
                api.addObjective(minX + width * 0.7f, minY + height * 0.15f, "nav_buoy");
                
                api.addPlanet(0,9000,250,"SCY_wormholeUnder",300,false);
//                api.addPlanet(0,9000,290,"SCY_wormholeMid",0,false);
                api.addPlanet(0,9000,280,"SCY_wormholeA",0,false);
                api.addPlanet(0,9000,270,"SCY_wormholeB",0,false);
                api.addPlanet(0,9000,260,"SCY_wormholeC",0,false);
                
                BattleCreationContext context = new BattleCreationContext(null, null, null, null);
		context.setInitialEscapeRange(8000f);
		api.addPlugin(new EscapeRevealPlugin(context));
	}
}