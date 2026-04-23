package data.missions.ASF_testbattle;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

  @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Trial Ships.");
		api.setFleetTagline(FleetSide.ENEMY, "Test Fleet.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Try out these vessels, prove their worth.");
		
		// Set up the player's fleet.
		
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_apologee_anom", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_perytonne_anom", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_chompiron_anom", FleetMemberType.SHIP, "This Is A Test", false);
		
		//api.addToFleet(FleetSide.PLAYER, "falcon_p_Strike", FleetMemberType.SHIP, "xXxXx Nerd Slayer 420 xXxXx", false);
		
//		api.addToFleet(FleetSide.PLAYER, "A_S-F_istina_test", FleetMemberType.SHIP, "SIMP (Sniper Monkey)", false);
		
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_jorogumo_stalker", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_lafiel_custom", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_pneuma_hunter", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_nebel_knecht", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_auditor_mod_custom", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_lanner_p_custom", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_mancatcher_custom", FleetMemberType.SHIP, "This Is A Test", false);
		
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_micare_ass", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_axicon_stk", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_niteo_ass", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_ilgryps_assault", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_lustre_ass", FleetMemberType.SHIP, "This Is A Test", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_gleam_ass", FleetMemberType.SHIP, "This Is A Test", false);

		//api.addToFleet(FleetSide.PLAYER, "A_S-F_estucheon_rkt", FleetMemberType.SHIP, "This Is A Test", false);
		
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_margrave_stk", FleetMemberType.SHIP, "Trial", true);
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_glentoran_ass", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_phobia_ass", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_lanner_elt", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_marlow_stk", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_ecumenist_ass", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_triarii_std", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_alqand_std", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_porroko_sup", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_ocklynge_std", FleetMemberType.SHIP, "Trial", false); //ass
		api.addToFleet(FleetSide.PLAYER, "A_S-F_initone_std", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_initone_lg_elt", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_peryton_fs", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gemogee_strike", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_scrag_od", FleetMemberType.SHIP, "Trial", false);
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_morris_std", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_morris_p_raider", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_henki_std", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_peluda_ass", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_edifice_esc", FleetMemberType.SHIP, "Trial", false); //sup
		api.addToFleet(FleetSide.PLAYER, "A_S-F_bathory_ass", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gaoler_sup", FleetMemberType.SHIP, "Trial", false); //stk
		api.addToFleet(FleetSide.PLAYER, "A_S-F_buraq_std", FleetMemberType.SHIP, "Trial", false); //sup2

		api.addToFleet(FleetSide.PLAYER, "A_S-F_superlasher_supp", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_misside_std", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_auditor_esc", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_rinka_stk", FleetMemberType.SHIP, "Trial", true);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_rinka_p_raider", FleetMemberType.SHIP, "Trial", true);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_exegetes_ass", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_exegetes_lg_elt", FleetMemberType.SHIP, "Trial", false);
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_invigilator_ass", FleetMemberType.SHIP, "Trial", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_velox_ass", FleetMemberType.SHIP, "Trial", false);
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_dunnock_brwl", FleetMemberType.SHIP, "Trial", false); //atk
		
		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "Stand Alone", false);
		api.addToFleet(FleetSide.ENEMY, "doom_Strike", FleetMemberType.SHIP, "Search Dialog", false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, "Explosively Fortunate", false);
		api.addToFleet(FleetSide.ENEMY, "champion_Support", FleetMemberType.SHIP, "Extreme Rendition", false);
		api.addToFleet(FleetSide.ENEMY, "champion_Support", FleetMemberType.SHIP, "Negligent Affection", false);
		api.addToFleet(FleetSide.ENEMY, "eradicator_Assault", FleetMemberType.SHIP, "Terminal Attention", false);
		api.addToFleet(FleetSide.ENEMY, "eradicator_Support", FleetMemberType.SHIP, "Two Years Of Plotting", false);
		api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, "Low Price Point", false);
		api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, "Copies To Be Returned", false);
		api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, "Arterial Roadway", false);
		api.addToFleet(FleetSide.ENEMY, "fury_Attack", FleetMemberType.SHIP, "So Well Received", false);
		api.addToFleet(FleetSide.ENEMY, "falcon_LG_Attack", FleetMemberType.SHIP, "Filling A Blank", false);

		api.addToFleet(FleetSide.ENEMY, "manticore_Assault", FleetMemberType.SHIP, "Strange Feeling", false);
		api.addToFleet(FleetSide.ENEMY, "manticore_Support", FleetMemberType.SHIP, "Proudly Made", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_PD", FleetMemberType.SHIP, "Harsh Words", false);
		api.addToFleet(FleetSide.ENEMY, "medusa_PD", FleetMemberType.SHIP, "Cobbled Together", false);
		api.addToFleet(FleetSide.ENEMY, "shrike_Attack", FleetMemberType.SHIP, "Common Type", false);
		api.addToFleet(FleetSide.ENEMY, "shrike_Attack", FleetMemberType.SHIP, "Only Interested", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, "Please Transfer", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, "Never Commit", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, "Nearly As Filling", false);
		
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, "Your Help", false);
		api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, "My Greed", false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, "Dry Protection", false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, "Wet Offense", false);
		api.addToFleet(FleetSide.ENEMY, "omen_PD", FleetMemberType.SHIP, "Moist Consideration", false);
		api.addToFleet(FleetSide.ENEMY, "brawler_LG_Elite", FleetMemberType.SHIP, "Masterclass In Pain", false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, "For Guidance Only", false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, "Here And Now", false);
		api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, "Coherent But Still", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_luddic_church_Standard", FleetMemberType.SHIP, "The Rest Is History", false);
		
		
		
		// Set up the map.
		float width = 18000f;
		float height = 16000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 10000f,
							 20f, 75f, 120);
		
	}

}
