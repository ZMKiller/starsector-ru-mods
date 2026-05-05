package data.missions.RR_testbattle;

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
		api.setFleetTagline(FleetSide.PLAYER, "RotcesRats Fleet, questionable value.");
		api.setFleetTagline(FleetSide.ENEMY, "Standard Fleet, tried and tested.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Try out these questionable vessels, prove their worth.");
		
		// Set up the player's fleet.
		api.addToFleet(FleetSide.PLAYER, "rr_onquest_std", FleetMemberType.SHIP, "Another Source of Ignition", true);
		
		// api.addToFleet(FleetSide.PLAYER, "rr_conslaught_scv", FleetMemberType.SHIP, "This Is A Test", true);
		// api.addToFleet(FleetSide.PLAYER, "rr_phaeton_eq_custom", FleetMemberType.SHIP, "This Is A Test", false);
		
		api.addToFleet(FleetSide.PLAYER, "rr_sojurn_sup", FleetMemberType.SHIP, "Less Than a Couple", false);
		api.addToFleet(FleetSide.PLAYER, "rr_dominatrix_fs", FleetMemberType.SHIP, "This Is My Experience So Far", true);
		api.addToFleet(FleetSide.PLAYER, "rr_undertaking_support", FleetMemberType.SHIP, "Negative Trust", false);
		api.addToFleet(FleetSide.PLAYER, "rr_undertaking_lp_siege", FleetMemberType.SHIP, "Always Trying To Kill", false);
		api.addToFleet(FleetSide.PLAYER, "rr_saga_stk", FleetMemberType.SHIP, "You Have Enough", false);
		api.addToFleet(FleetSide.PLAYER, "rr_gryphon_b_std", FleetMemberType.SHIP, "General Overview", false);
		api.addToFleet(FleetSide.PLAYER, "rr_aeith_attack", FleetMemberType.SHIP, "When Not Provided", true);
		api.addToFleet(FleetSide.PLAYER, "rr_lily_bal", FleetMemberType.SHIP, "Just Feeling", true);
		api.addToFleet(FleetSide.PLAYER, "rr_pteroma_sup", FleetMemberType.SHIP, "Overwhelmed Again", false);
		api.addToFleet(FleetSide.PLAYER, "rr_scut_stk", FleetMemberType.SHIP, "It won't Let You", false);
		
		api.addToFleet(FleetSide.PLAYER, "rr_rednus_cs", FleetMemberType.SHIP, "Look Past My Lips", false);
		api.addToFleet(FleetSide.PLAYER, "rr_cephalofoil_bal", FleetMemberType.SHIP, "Never Being Told", false);
		api.addToFleet(FleetSide.PLAYER, "rr_valkyrie_m2_std", FleetMemberType.SHIP, "Customer Requirement", false);
		api.addToFleet(FleetSide.PLAYER, "rr_portico_sup", FleetMemberType.SHIP, "The Way I See It", false);
		api.addToFleet(FleetSide.PLAYER, "rr_foreman_std", FleetMemberType.SHIP, "More Than You Know", false);
		api.addToFleet(FleetSide.PLAYER, "rr_falconet_l_std", FleetMemberType.SHIP, "On The Other Side", false);
		api.addToFleet(FleetSide.PLAYER, "rr_falconet_r_std", FleetMemberType.SHIP, "On Your Back", false);
		api.addToFleet(FleetSide.PLAYER, "rr_inimeg_sup", FleetMemberType.SHIP, "Know Your Brain", false);
		api.addToFleet(FleetSide.PLAYER, "rr_scric_elite", FleetMemberType.SHIP, "No Double Life", false);
		api.addToFleet(FleetSide.PLAYER, "rr_susrat_stk", FleetMemberType.SHIP, "More Than One", false);
		api.addToFleet(FleetSide.PLAYER, "rr_susrat_slv_std", FleetMemberType.SHIP, "Exchange Of Value", false);
		api.addToFleet(FleetSide.PLAYER, "rr_buffaloeq_support", FleetMemberType.SHIP, "Up Your Street", false);
		api.addToFleet(FleetSide.PLAYER, "rr_buffaloeq_assault", FleetMemberType.SHIP, "My Body Looks Different", false);
		api.addToFleet(FleetSide.PLAYER, "rr_surebrec_hrd", FleetMemberType.SHIP, "Good To Be True", false);
		api.addToFleet(FleetSide.PLAYER, "rr_surebrec_p_cs", FleetMemberType.SHIP, "To Not Be Used for Profit", false);
		
		api.addToFleet(FleetSide.PLAYER, "rr_dram_eq_ass", FleetMemberType.SHIP, "Lucky Reader", false);
		api.addToFleet(FleetSide.PLAYER, "rr_nemo_sup", FleetMemberType.SHIP, "Kind to Others", false);
		api.addToFleet(FleetSide.PLAYER, "rr_tamerlane_ass", FleetMemberType.SHIP, "Forget To Take Me", true);
		api.addToFleet(FleetSide.PLAYER, "rr_vexilla_ass", FleetMemberType.SHIP, "No Mystery Involved", false);
		api.addToFleet(FleetSide.PLAYER, "rr_pilaster_sup", FleetMemberType.SHIP, "Rare Occasion", false);
		api.addToFleet(FleetSide.PLAYER, "rr_shkiper_bar", FleetMemberType.SHIP, "Attention To Quality", false);
		api.addToFleet(FleetSide.PLAYER, "rr_hooligan_ass", FleetMemberType.SHIP, "Lame Conclusion", false);
		api.addToFleet(FleetSide.PLAYER, "rr_concierge_esc", FleetMemberType.SHIP, "Messages Will Appear", false);
		api.addToFleet(FleetSide.PLAYER, "rr_concierge_p_sup", FleetMemberType.SHIP, "On Display For The Duration", false);
		api.addToFleet(FleetSide.PLAYER, "rr_lout_od", FleetMemberType.SHIP, "Lingering Odour", false);
		api.addToFleet(FleetSide.PLAYER, "rr_ox_2_hrd", FleetMemberType.SHIP, "Product Review Process", false);
		
		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, "Drawing Level", false);
		api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, "Sunk Costs", false);
		api.addToFleet(FleetSide.ENEMY, "gryphon_Standard", FleetMemberType.SHIP, "Limited Resources", false);
		api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, "Time In Solitary", false);
		api.addToFleet(FleetSide.ENEMY, "heron_Attack", FleetMemberType.SHIP, "Four Common Types", false);
		api.addToFleet(FleetSide.ENEMY, "falcon_p_Strike", FleetMemberType.SHIP, "Nothing That Actually Happens", false);
		api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, "The Second Reason", false);
		api.addToFleet(FleetSide.ENEMY, "venture_Outdated", FleetMemberType.SHIP, "Any New Features", false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Balanced", FleetMemberType.SHIP, "Waiting For A Diner", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Support", FleetMemberType.SHIP, "The Wrong Place", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, "Does It Feel Good", false);
		api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, "One More Night", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, "Just By Looking", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, "Remember Anything", false);
		api.addToFleet(FleetSide.ENEMY, "buffalo2_Fighter_Support", FleetMemberType.SHIP, "Not Happy About This", false);
		api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, "Wanted To Show", false);
		api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, "Time And Place", false);
		api.addToFleet(FleetSide.ENEMY, "vigilance_Standard", FleetMemberType.SHIP, "Out To Transform", false);
		api.addToFleet(FleetSide.ENEMY, "cerberus_Hardened", FleetMemberType.SHIP, "After The Next Update", false);
		api.addToFleet(FleetSide.ENEMY, "cerberus_d_pirates_Standard", FleetMemberType.SHIP, "Indeterminate Generation", false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, "Moving To another Dimension", false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Shielded", FleetMemberType.SHIP, "A Whole Bunch", false);
		api.addToFleet(FleetSide.ENEMY, "hound_d_pirates_Overdriven", FleetMemberType.SHIP, "Working On The Next Layer", false);
		
		
		// api.defeatOnShipLoss("Another Source of Ignition");
		
		// Set up the map.
		float width = 12000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// Add an asteroid field
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
							 20f, 70f, 100);
		
	}

}
