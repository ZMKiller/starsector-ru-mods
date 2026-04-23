package data.missions.ASF_persenachia_mission;

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

import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;


public class MissionDefinition implements MissionDefinitionPlugin {

  @Override
	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "MAL", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true); //ESCAPE
		
		api.setHyperspaceMode(true);
		
		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Heretic.");
		api.setFleetTagline(FleetSide.ENEMY, "Savages.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Draw them near, Keep them close.");
		api.addBriefingItem("Feed them fear, Taste their despair.");
		api.addBriefingItem("Punish their weaknesses, Sever their hope.");
			// Strong, beautiful, and invincible necromancer! Her servant is super strong too! She could beat up Misty any time if she really wanted to!!
		
		// Set up the player's fleet.
		FleetMemberAPI malice = api.addToFleet(FleetSide.PLAYER, "A_S-F_persenachia_exp", FleetMemberType.SHIP, "Executioner", true);
		
		PersonAPI ASF_nicole = Global.getSector().getFaction("tritachyon").createRandomPerson(FullName.Gender.FEMALE);
		ASF_nicole.setId("A_S-F_mission_4");
		ASF_nicole.getName().setFirst("Nicole");
		ASF_nicole.getName().setLast("Malice");
		ASF_nicole.getName().setGender(FullName.Gender.FEMALE);
		ASF_nicole.setPersonality(Personalities.AGGRESSIVE);
		ASF_nicole.setPortraitSprite("graphics/portraits/portrait_nicole_malice.png");
		ASF_nicole.setFaction("independent");
		ASF_nicole.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		ASF_nicole.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
        ASF_nicole.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
        ASF_nicole.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
        ASF_nicole.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        ASF_nicole.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        ASF_nicole.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
        ASF_nicole.getStats().setLevel(7);
		malice.setCaptain(ASF_nicole);
		
		// Set up the enemy fleet.
		
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_conquest_savage", FleetMemberType.SHIP, "Skull", false);
			// swapped for the champ+manticores, because it would instantly transition to "enemy defeated" on killing it, making it impossible to get a high score
		api.addToFleet(FleetSide.ENEMY, "A_S-F_champion_savage", FleetMemberType.SHIP, "Skull", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_ocklynge_savage", FleetMemberType.SHIP, "Sternum", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_edifice_savage", FleetMemberType.SHIP, "Scapula", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_edifice_savage", FleetMemberType.SHIP, "Clavicle", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_manticore_savage", FleetMemberType.SHIP, "Mandible", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_manticore_savage", FleetMemberType.SHIP, "Maxillae", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_bathory_savage", FleetMemberType.SHIP, "Ulna", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_bathory_savage", FleetMemberType.SHIP, "Radius", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_superlasher_savage", FleetMemberType.SHIP, "Patella", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_superlasher_savage", FleetMemberType.SHIP, "Tibia", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_superlasher_savage", FleetMemberType.SHIP, "Fibula", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_omen_savage", FleetMemberType.SHIP, "Ilium", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_omen_savage", FleetMemberType.SHIP, "Ischium", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_omen_savage", FleetMemberType.SHIP, "Sacrum", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_misside_savage", FleetMemberType.SHIP, "Carpal", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_misside_savage", FleetMemberType.SHIP, "Metacarpal", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_misside_savage", FleetMemberType.SHIP, "Phalange", false);
		
		api.defeatOnShipLoss("Executioner");
		
		// Set up the map.
		float width = 11000f;
		float height = 14000f; //24000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// a *lot* of nebulas because the theme says so
		for (int i = 0; i < 34; i++) {
			float x = ((float) Math.random() * width) + minX;
			float y = ((float) Math.random() * height) + minY;
			float radius = 600f + ((float) Math.random() * 1000f); 
			api.addNebula(x, y, radius);
		}
		
		
		api.getContext().aiRetreatAllowed = false;
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;
		
		//BattleCreationContext context = new BattleCreationContext(null, null, null, null);
		//context.setInitialEscapeRange(7000f);
		//api.addPlugin(new EscapeRevealPlugin(context));
		
	}
  
}

