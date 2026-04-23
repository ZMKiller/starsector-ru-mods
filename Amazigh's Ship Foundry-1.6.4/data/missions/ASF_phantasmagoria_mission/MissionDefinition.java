package data.missions.ASF_phantasmagoria_mission;

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
		api.initFleet(FleetSide.PLAYER, "INV", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "PUR", FleetGoal.ATTACK, true);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The Dragon.");
		api.setFleetTagline(FleetSide.ENEMY, "The Pursuers.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("The Dragon proceeds as it must, like it had practiced the fight a hundred times before this first.");
		// api.addBriefingItem("The Dragon proceeds exactly where it must, as if it has practiced the fight a hundred times before this first.");
		// too long, fucks up line positioning
		api.addBriefingItem("Strike, succeed, withdraw.");
		api.addBriefingItem("Death from many points, turning like a pulsar, and they cannot find one body to kill.");
		api.addBriefingItem("The enemy quivers to know that she was wholly ignored, might never have existed at all.");
		
		// Set up the player's fleet.
		FleetMemberAPI dragon = api.addToFleet(FleetSide.PLAYER, "A_S-F_phantasmagoria_exp", FleetMemberType.SHIP, "The Olvek'taar", true);
		
		PersonAPI harbinger = Global.getSector().getFaction("independent").createRandomPerson(FullName.Gender.FEMALE);
		harbinger.setId("A_S-F_mission");
		harbinger.getName().setFirst("The");
        harbinger.getName().setLast("Dragon");
        harbinger.getName().setGender(FullName.Gender.FEMALE);
        harbinger.setPersonality(Personalities.AGGRESSIVE);
        harbinger.setPortraitSprite("graphics/portraits/portrait_yumemi.png");		//portrait_mercenary07
        harbinger.setFaction("independent");
        harbinger.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
        harbinger.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
        harbinger.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
        harbinger.getStats().setSkillLevel("ordnance_expert", 2);
        harbinger.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        harbinger.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
        harbinger.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
        harbinger.getStats().setLevel(7);
		dragon.setCaptain(harbinger);
		
		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "A_S-F_apogee_purs", FleetMemberType.SHIP, "Alabaster-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_gemogee_purs", FleetMemberType.SHIP, "Alabaster-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_peryton_purs", FleetMemberType.SHIP, "Malachite-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_peryton_purs", FleetMemberType.SHIP, "Malachite-Two", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_scric_purs", FleetMemberType.SHIP, "Azure-One", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_scric_purs", FleetMemberType.SHIP, "Azure-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_buraq_purs", FleetMemberType.SHIP, "Emerald-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_buraq_purs", FleetMemberType.SHIP, "Emerald-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_vigilance_purs", FleetMemberType.SHIP, "Verdigris-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_vigilance_purs", FleetMemberType.SHIP, "Verdigris-Two", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_tempest_purs", FleetMemberType.SHIP, "Onyx-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_tempest_purs", FleetMemberType.SHIP, "Onyx-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_exegetes_purs", FleetMemberType.SHIP, "Silver-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_exegetes_purs", FleetMemberType.SHIP, "Silver-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_exegetes_purs", FleetMemberType.SHIP, "Silver-Three", false);
		
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_vexilla_purs", FleetMemberType.SHIP, "Emerald-One", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_vexilla_purs", FleetMemberType.SHIP, "Emerald-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_velox_purs", FleetMemberType.SHIP, "Sapphire-One", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_velox_purs", FleetMemberType.SHIP, "Sapphire-Two", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_velox_purs", FleetMemberType.SHIP, "Sapphire-Three", false);
		
		api.defeatOnShipLoss("The Olvek'taar");
		
		// Set up the map.
		float width = 13000f;
		float height = 12000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// some nebulas because ehhh why not
		for (int i = 0; i < 9; i++) {
			float x = ((float) Math.random() * width) + minX;
			float y = ((float) Math.random() * height) + minY;
			float radius = 100f + ((float) Math.random() * 1100f); 
			api.addNebula(x, y, radius);
		}
		
		// Add an asteroid field (only don't)
		/*
		api.addAsteroidField(minX, minY + height / 2, 0, 5000f,
							 10f, 75f, 50);
		*/
	}
  
}

