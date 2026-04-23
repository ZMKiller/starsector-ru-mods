package data.missions.ASF_rangda_mission;

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
		api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Prototype Vessel.");
		api.setFleetTagline(FleetSide.ENEMY, "Pirate Raiders and traitor Security Forces.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("You are outnumbered and alone, prioritise high threat targets or be overwhelmed.");
		api.addBriefingItem("Disabling the Ballistic Overdrive grants minor armour repair, this is vital to your survival.");
		api.addBriefingItem("Survival is more important than glory, don't overextend.");
		
		// Set up the player's fleet.
		FleetMemberAPI lemontea = api.addToFleet(FleetSide.PLAYER, "A_S-F_rangda_exp", FleetMemberType.SHIP, "Lemontea", true);
		
		PersonAPI ASF_echo = Global.getSector().getFaction("independent").createRandomPerson(FullName.Gender.MALE);
		ASF_echo.setId("A_S-F_mission_r1");
		ASF_echo.getName().setFirst("Richard");
		ASF_echo.getName().setLast("Osmond");
		ASF_echo.getName().setGender(FullName.Gender.MALE);
		ASF_echo.setPersonality(Personalities.AGGRESSIVE);
		ASF_echo.setPortraitSprite("graphics/portraits/portrait_mercenary08.png");
		ASF_echo.setFaction("independent");
		ASF_echo.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		ASF_echo.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
		ASF_echo.getStats().setSkillLevel(Skills.FIELD_MODULATION, 1);
		ASF_echo.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
		ASF_echo.getStats().setSkillLevel("ordnance_expert", 2);
		ASF_echo.getStats().setLevel(5);
		lemontea.setCaptain(ASF_echo);
		
		// was gonna have the security boys as allies originally, but they make better enemies
			// The only use for them that i found was deploying them at the end to kill the falcon, otherwise they just died without really doing much (funny loadout likely at fault but w/e)
		
		
		// Set up the enemy fleet.
		
		//1x Falcon P (captained)		Disrespect
		//1x Colossus P					Oh See The Fireworks
		//3x Shrike P					Out-The-Airlock / Space Your Captain / Clotilde's Baluties
		//3x Rinka P					Rustbucket / Scabby Old Tub / Bloody Space-can
		//1x Shade P					Hairtrigger
		//3x vanguard P					I'm A Torpedo / Anne Bonny's Rocket / No Retros
		//3x brawler					High Sign / Scrambling Film / Line of Fire
		
		
		FleetMemberAPI disrespecter = api.addToFleet(FleetSide.ENEMY, "A_S-F_falcon_p_ambush", FleetMemberType.SHIP, "Disrespect", false);
		
		PersonAPI ASF_sneak = Global.getSector().getFaction("pirates").createRandomPerson(FullName.Gender.FEMALE);
		ASF_sneak.setId("A_S-F_mission_r2");
		ASF_sneak.getName().setFirst("Crea");
		ASF_sneak.getName().setLast("Rockwell");
		ASF_sneak.getName().setGender(FullName.Gender.FEMALE);
		ASF_sneak.setPersonality(Personalities.AGGRESSIVE);
		ASF_sneak.setPortraitSprite("graphics/portraits/portrait_pirate05.png");
		ASF_sneak.setFaction("pirates");
		ASF_sneak.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		ASF_sneak.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
		ASF_sneak.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
		ASF_sneak.getStats().setLevel(3);
		disrespecter.setCaptain(ASF_sneak);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_colossus3_ambush", FleetMemberType.SHIP, "Oh See The Fireworks", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_shrike_p_ambush", FleetMemberType.SHIP, "Out-The-Airlock", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_shrike_p_ambush", FleetMemberType.SHIP, "Space Your Captain", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_shrike_p_ambush", FleetMemberType.SHIP, "Clotilde's Baluties", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_rinka_p_ambush", FleetMemberType.SHIP, "Rustbucket", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_rinka_p_ambush", FleetMemberType.SHIP, "Scabby Old Tub", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_rinka_p_ambush", FleetMemberType.SHIP, "Bloody Space-can", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_shade_p_ambush", FleetMemberType.SHIP, "Hairtrigger", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_vanguard_p_ambush", FleetMemberType.SHIP, "I'm A Torpedo", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_vanguard_p_ambush", FleetMemberType.SHIP, "Anne Bonny's Rocket", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_vanguard_p_ambush", FleetMemberType.SHIP, "No Retros", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_brawler_sec", FleetMemberType.SHIP, "High Sign", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_brawler_sec", FleetMemberType.SHIP, "Scrambling Film", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_brawler_sec", FleetMemberType.SHIP, "Line of Fire", false);
		
		api.defeatOnShipLoss("Lemontea");
		
		// Set up the map.
		float width = 12000f;
		float height = 14000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		// two clashing asteroid fields, for memes
		api.addAsteroidField(0f, 0f, 90f, 12000f, 35f, 90f, 140);
		api.addAsteroidField(0f, 0f, 270f, 12000f, 35f, 90f, 140);
		
	}
  
}

