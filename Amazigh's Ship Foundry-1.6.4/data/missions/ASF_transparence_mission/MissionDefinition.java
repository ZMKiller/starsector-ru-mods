package data.missions.ASF_transparence_mission;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

  @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);
		
		api.setHyperspaceMode(true);
		
		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Prototype Vessel.");
		api.setFleetTagline(FleetSide.ENEMY, "Rogue Fragment.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Photon Swell allows you to operate your weapons at maximum potential.");
		api.addBriefingItem("Keep on the move to maintain Photon Swell Levels.");
		api.addBriefingItem("Be wary of flanking maneuvers, they will easily spell your doom.");
			// Good Night, Sleep Tight, Young Lovers.
		
		// Set up the player's fleet.
		FleetMemberAPI gekkou = api.addToFleet(FleetSide.PLAYER, "A_S-F_transparence_exp", FleetMemberType.SHIP, "Lifting Girl", true);
		
		PersonAPI ASF_lifter = Global.getSector().getFaction("tritachyon").createRandomPerson(FullName.Gender.MALE);
		ASF_lifter.setId("A_S-F_mission_3");
		ASF_lifter.getName().setFirst("Sumner");
		ASF_lifter.getName().setLast("Sturgeon");
		ASF_lifter.getName().setGender(FullName.Gender.MALE);
		ASF_lifter.setPersonality(Personalities.AGGRESSIVE);
		ASF_lifter.setPortraitSprite("graphics/portraits/portrait_corporate06.png");
		ASF_lifter.setFaction("tritachyon");
		ASF_lifter.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		ASF_lifter.getStats().setSkillLevel(Skills.FIELD_MODULATION, 1);
		ASF_lifter.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
		ASF_lifter.getStats().setSkillLevel("ordnance_expert", 1);
		ASF_lifter.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		ASF_lifter.getStats().setLevel(5);
		gekkou.setCaptain(ASF_lifter);
		
		// Set up the enemy fleet.
		
		// 2 ilgryps			Samech-Shower / Tsadi-Tower
		// 1 lustre				Chet-Crown
		// 3 Fulgent			Derivative Function / Tangent Velocity / Confirmed Alignment
		// 2 scintilla			White Hole / Daymare
		// 2 gleam				Resh-Roamer / Lamed-Locator 
		// 2 auspice			Hyperbola / Second Exponent
		// 3/3 glimmer/lumen	Real Identity / Projective Function / Combined Evaluation / False Argument / Compound Decay / Fractional Category
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_ilgryps_rogue", FleetMemberType.SHIP, "Samech-Shower", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_ilgryps_rogue", FleetMemberType.SHIP, "Tsadi-Tower", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_lustre_rogue", FleetMemberType.SHIP, "Chet-Crown", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_fulgent_rogue", FleetMemberType.SHIP, "Derivative Function", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_fulgent_rogue", FleetMemberType.SHIP, "Tangent Velocity", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_fulgent_rogue", FleetMemberType.SHIP, "Confirmed Alignment", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_scintilla_rogue_1", FleetMemberType.SHIP, "White Hole", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_scintilla_rogue_2", FleetMemberType.SHIP, "Daymare", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_gleam_rogue", FleetMemberType.SHIP, "Resh-Roamer", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_gleam_rogue", FleetMemberType.SHIP, "Lamed-Locator", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_auspice_rogue", FleetMemberType.SHIP, "Hyperbola", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_auspice_rogue", FleetMemberType.SHIP, "Second Exponent", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_glimmer_rogue", FleetMemberType.SHIP, "Real Identity", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_glimmer_rogue", FleetMemberType.SHIP, "Projective Function", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_glimmer_rogue", FleetMemberType.SHIP, "Combined Evaluation", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_lumen_rogue", FleetMemberType.SHIP, "False Argument", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_lumen_rogue", FleetMemberType.SHIP, "Compound Decay", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_lumen_rogue", FleetMemberType.SHIP, "Fractional Category", false);
		
		api.defeatOnShipLoss("Lifting Girl");
		
		// Set up the map.
		float width = 16000f;
		float height = 14000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// some nebulas because ehhh why not
		for (int i = 0; i < 10; i++) {
			float x = ((float) Math.random() * width) + minX;
			float y = ((float) Math.random() * height) + minY;
			float radius = 200f + ((float) Math.random() * 1200f); 
			api.addNebula(x, y, radius);
		}
		
		api.addObjective(0f, 0f, "sensor_array");
		// so we put a sensor array in the dead center of the map
		// this basically gives the opposition a free +5% ECM rating, because it's going to be somewhere in the realm of impossible to take/hold it while fighting off all the enemies
	}
  
}

