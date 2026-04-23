package data.missions.ASF_arkDefenders;

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
		api.initFleet(FleetSide.PLAYER, "ARK", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "LP", FleetGoal.ATTACK, true);
		
		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "Ark Defenders.");
		api.setFleetTagline(FleetSide.ENEMY, "Path Crusading Fleet.");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat in Detail: Individual heathens are of minimal threat.");
		api.addBriefingItem("Hold Firm: Desire victory, and it shall come.");
		api.addBriefingItem("Pressure and Persist: Foolish modifications to their vessels mean they will fail given time.");
		
		/*
		 * This was originally going to be from the LP perspective, and the brief was as follows:
		 * 
		 * Our Agents have been tracking a fleet of these Ark Defenders and have finally confirmed their location to this sector.
		 * After refusing to repent and give up the location of their heretical Ark they have taken up positions to resist the will of Ludd, The only mercy they shall find will be in God's holy vacuum.
		 * Through the use of sinful technologies the heretic ships have great power.
		 * But with faithful comrades at your side this matters not.
		 * Though the righteous suffer the lash, our reward will be everlasting grace.
		 */
		
		
		// Set up the player's fleet.
		FleetMemberAPI berzel = api.addToFleet(FleetSide.PLAYER, "A_S-F_grandum_ark", FleetMemberType.SHIP, "Hoka", true);
		PersonAPI zelius = Global.getSector().getFaction("independent").createRandomPerson(FullName.Gender.FEMALE);
		zelius.setId("A_S-F_mission_2");
		zelius.getName().setFirst("Ark");
		zelius.getName().setLast("Defender");
        zelius.getName().setGender(FullName.Gender.FEMALE);
        zelius.setPersonality(Personalities.AGGRESSIVE);
        zelius.setPortraitSprite("graphics/portraits/portrait_berzel.png");
        zelius.setFaction("independent");
        zelius.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        zelius.getStats().setSkillLevel(Skills.POINT_DEFENSE, 1);
        zelius.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
        zelius.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
        zelius.getStats().setLevel(4);
		berzel.setCaptain(zelius);
		
		
		
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_berzelius_desire", FleetMemberType.SHIP, "Don'yoku", false);
		
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_mbishi_def", FleetMemberType.SHIP, "Sirius", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_mlinzi_def", FleetMemberType.SHIP, "Spica", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_mtetezi_def", FleetMemberType.SHIP, "Deneb", false);
		//api.addToFleet(FleetSide.PLAYER, "A_S-F_mtumaji_def", FleetMemberType.SHIP, "Altair", false);
		
		
		
		api.addToFleet(FleetSide.PLAYER, "A_S-F_giganberg_ark", FleetMemberType.SHIP, "Kyokan", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gaderoga_ark", FleetMemberType.SHIP, "Hohei", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gaderoga_ark", FleetMemberType.SHIP, "Heiki", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_genbura_ark", FleetMemberType.SHIP, "Kame", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_genbura_ark", FleetMemberType.SHIP, "Kuro", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gathima_ark", FleetMemberType.SHIP, "Shoju", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gathima_ark", FleetMemberType.SHIP, "Tanju", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gathima_ark", FleetMemberType.SHIP, "Kenju", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_galsteel_ark", FleetMemberType.SHIP, "Hagane", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_galsteel_ark", FleetMemberType.SHIP, "Tetsu", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_galsteel_ark", FleetMemberType.SHIP, "Shin", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gatorbacker_ark", FleetMemberType.SHIP, "Wani", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gatorbacker_ark", FleetMemberType.SHIP, "Kani", false);
		api.addToFleet(FleetSide.PLAYER, "A_S-F_gatorbacker_ark", FleetMemberType.SHIP, "Nani", false);
		
		
		// Set up the enemy fleet.
		api.addToFleet(FleetSide.ENEMY, "A_S-F_prometheus2_crusader", FleetMemberType.SHIP, "Altar Of Submission", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_prometheus2_faithful", FleetMemberType.SHIP, "Altar Of Devotion", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_crusader", FleetMemberType.SHIP, "Lash Of Perdition", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_crusader", FleetMemberType.SHIP, "Divine Judgement", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_crusader", FleetMemberType.SHIP, "Wrath Of God", false);
		
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_undertaking_lp_siege", FleetMemberType.SHIP, "Hammer Of Ludd", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_undertaking_lp_siege", FleetMemberType.SHIP, "Anvil Of Ludd", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_undertaking_lp_siege", FleetMemberType.SHIP, "Apocalypse", false);
		//api.addToFleet(FleetSide.ENEMY, "A_S-F_undertaking_lp_siege", FleetMemberType.SHIP, "Armageddon", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_faithful", FleetMemberType.SHIP, "Hammer Of Ludd", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_faithful", FleetMemberType.SHIP, "Anvil Of Ludd", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_faithful", FleetMemberType.SHIP, "Apocalypse", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_eradicator_faithful", FleetMemberType.SHIP, "Armageddon", false);
			// swapped the undertakings to a funny eradicator loadout because mod splitting
		
		api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, "Pentitence Of Labour", false);
		api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, "Glory In Faith", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, "Crusade", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, "Excommunication", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, "Holy Justice", false);
		api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, "Ambassador", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, "Gehenna", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, "Boramander", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, "Last Prayer", false);
		api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, "Sword Of Damocles", false);
		
		api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, "Jihad", false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, "Sacrifice", false);
		api.addToFleet(FleetSide.ENEMY, "enforcer_Overdriven", FleetMemberType.SHIP, "Salvation In Duty", false);
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_crusader", FleetMemberType.SHIP, "Radok", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_crusader", FleetMemberType.SHIP, "Blessed Muir", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_crusader", FleetMemberType.SHIP, "Bristlecone", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_faithful", FleetMemberType.SHIP, "Mother Sequoia", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_faithful", FleetMemberType.SHIP, "Weeping Willow", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_enforcer_faithful", FleetMemberType.SHIP, "White Pine", false);
			// to "compensate" for adding the giganberg, have more enforcers!
		
		api.addToFleet(FleetSide.ENEMY, "A_S-F_condor_barrage", FleetMemberType.SHIP, "Pitiable Offering", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_condor_barrage", FleetMemberType.SHIP, "Humble Submission", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_condor_barrage", FleetMemberType.SHIP, "Righteous Necessity", false);
		api.addToFleet(FleetSide.ENEMY, "A_S-F_condor_barrage", FleetMemberType.SHIP, "Holy Obsession", false);
		api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, "Purgatory", false);
		api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, "Inferno", false);
		api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, "Chastisement", false);
		api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, "Burnt Offering", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, "Torment", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, "Arkon", false);
		api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, "Zulif", false);
		api.addToFleet(FleetSide.ENEMY, "gremlin_luddic_path_Strike", FleetMemberType.SHIP, "Zealot", false);
		api.addToFleet(FleetSide.ENEMY, "gremlin_luddic_path_Strike", FleetMemberType.SHIP, "Herem", false);
		api.addToFleet(FleetSide.ENEMY, "gremlin_luddic_path_Strike", FleetMemberType.SHIP, "Fanatic", false);
		api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, "Servant", false);
		api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Strike", FleetMemberType.SHIP, "All Is Given", false);
		api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Raider", FleetMemberType.SHIP, "All Is Taken", false);
		api.addToFleet(FleetSide.ENEMY, "kite_luddic_path_Raider", FleetMemberType.SHIP, "Rewards Of Devotion", false);
		
		api.defeatOnShipLoss("Hoka");
		
		// Set up the map.
		float width = 15000f;
		float height = 16000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		for (int i = 0; i < 13; i++) {
			float x = ((float) Math.random() * width) + minX;
			float y = ((float) Math.random() * height) + minY;
			float radius = 300f + ((float) Math.random() * 1300f); 
			api.addNebula(x, y, radius);
		}
		
		api.addAsteroidField(minX, minY + height / 2, 0, 14000f, 10f, 60f, 90);
		api.addAsteroidField(minX, minY + height / 2, 0, 8000f, 35f, 85f, 60);
		
		api.addObjective(minX * 0.5f, 1600f, "sensor_array");
		api.addObjective(width/4, -1600f, "nav_buoy");
		
		api.getContext().aiRetreatAllowed = false;
//        api.getContext().enemyDeployAll = true; // this makes it a lot harder, mass swarm hell!
        api.getContext().fightToTheLast = true;
        
	}
  
}

