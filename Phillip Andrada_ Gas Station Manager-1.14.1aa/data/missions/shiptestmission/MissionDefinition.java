package data.missions.shiptestmission;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.campaign.fleet.FleetMember;

public class MissionDefinition implements MissionDefinitionPlugin {

	public static boolean isArmaA = Global.getSettings().getModManager().isModEnabled("armaa");

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "SFS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "SFS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The Sindrian Fuel Fleet.");
		api.setFleetTagline(FleetSide.ENEMY, "The Unfortunate Test Target.");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Test out the various designs the Sindrian Fuel Fleet has on offer.");
		api.addBriefingItem("Don't disappoint the Board of Research and Development.");
		api.addBriefingItem("Some of these ships may be easier to get than others.");
		api.addBriefingItem("And don't forget to sign up for the monthly company lottery!");

		//test officers
		PersonAPI sfcyenniPerson = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.FEMALE);
		sfcyenniPerson.setFaction(Factions.DIKTAT);
		sfcyenniPerson.setGender(FullName.Gender.FEMALE);
		sfcyenniPerson.setPersonality(Personalities.AGGRESSIVE);
		sfcyenniPerson.getName().setFirst("Ruka");
		sfcyenniPerson.getName().setLast("Yenni");
		sfcyenniPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcyenni"));
		sfcyenniPerson.getStats().setLevel(15);
		sfcyenniPerson.getStats().setSkillLevel("sfc_titan", 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
		sfcyenniPerson.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.OFFICER_TRAINING, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
		sfcyenniPerson.getStats().setSkillLevel(Skills.OFFICER_MANAGEMENT, 1);
		sfcyenniPerson.getMemoryWithoutUpdate().set("$chatterChar", "sfcyenni");

		PersonAPI sfcruniPerson = Global.getSector().getFaction("sindrian_diktat").createRandomPerson(FullName.Gender.FEMALE);
		sfcruniPerson.setFaction(Factions.DIKTAT);
		sfcruniPerson.setGender(FullName.Gender.FEMALE);
		sfcruniPerson.setPersonality(Personalities.TIMID);
		sfcruniPerson.getName().setFirst("Yunifer");
		sfcruniPerson.getName().setLast("Runi");
		sfcruniPerson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcruni"));
		sfcruniPerson.getStats().setSkillLevel("sfc_titan", 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.FIELD_MODULATION, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.HELMSMANSHIP, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 3);
		sfcruniPerson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 3);
		sfcruniPerson.getStats().setLevel(10);
		sfcruniPerson.getMemoryWithoutUpdate().set("$chatterChar", "sfcruni");

		PersonAPI sfcarthurperson = Global.getSector().getFaction("luddic_path").createRandomPerson(FullName.Gender.MALE);
		sfcarthurperson.setFaction(Factions.LUDDIC_PATH);
		sfcarthurperson.setPersonality(Personalities.RECKLESS);
		sfcarthurperson.getName().setFirst("Beligar");
		sfcarthurperson.getName().setLast("Arthur");
		sfcarthurperson.setPortraitSprite(Global.getSettings().getSpriteName("characters", "sfcarthur"));
		sfcarthurperson.getStats().setLevel(8);
		sfcarthurperson.getStats().setSkillLevel("sfc_luddskill", 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		sfcarthurperson.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		sfcarthurperson.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
		sfcarthurperson.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);
		sfcarthurperson.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
		sfcarthurperson.getMemoryWithoutUpdate().set("$chatterChar", "sfcarthur");

		// Set up the player's fleet
		//FleetMemberAPI supercapital = api.addToFleet(FleetSide.PLAYER, "sfcsuperiapetus_Mixed", FleetMemberType.SHIP,"SFS Kween's Gambit", false);
		api.addToFleet(FleetSide.PLAYER, "sfcsuperiapetus_Mixed", FleetMemberType.SHIP,"SFS Kween's Gambit", true);
		FleetMemberAPI yunifership = api.addToFleet(FleetSide.PLAYER, "sfcsuperiapetus_Mixed", FleetMemberType.SHIP,"SFS Kween's Gambit", false);
		yunifership.setCaptain(sfcruniPerson);
		//supercapital.setCaptain(sfcruniPerson);
		//FleetMemberAPI flagship = api.addToFleet(FleetSide.PLAYER, "sfciapetus_Mixed", FleetMemberType.SHIP,"SFS Fuel Eternal", true);
		//flagship.setCaptain(sfcyenniPerson);
		api.addToFleet(FleetSide.PLAYER, "sfciapetus_Mixed", FleetMemberType.SHIP,"SFS Fuel Eternal", false);
		FleetMemberAPI rukaship = api.addToFleet(FleetSide.PLAYER, "sfciapetus_Mixed", FleetMemberType.SHIP,"SFS Fuel Eternal", false);
		rukaship.setCaptain(sfcyenniPerson);
		api.addToFleet(FleetSide.PLAYER, "sfcpathapetus_Zealous", FleetMemberType.SHIP,"Infernium Wrath", false);
		FleetMemberAPI arthurship = api.addToFleet(FleetSide.PLAYER, "sfcpathapetus_Zealous", FleetMemberType.SHIP,"Infernium Wrath", false);
		arthurship.setCaptain(sfcarthurperson);
		api.addToFleet(FleetSide.PLAYER, "sfcprometheus_barrage", FleetMemberType.SHIP,"SFS Fuel Eagle", false);
		api.addToFleet(FleetSide.PLAYER, "sfcepimetheus_Experimental", FleetMemberType.SHIP,"SFS Afterfuel", false);
		api.addToFleet(FleetSide.PLAYER, "sfcpatherepimetheus_Assault", FleetMemberType.SHIP, "Hyperlane Rage of Ludd", false);
		api.addToFleet(FleetSide.PLAYER, "sfcmenoetius_Defense", FleetMemberType.SHIP,"SFS Fuelrage", false);
		api.addToFleet(FleetSide.PLAYER, "sfcskyrend_Barrage", FleetMemberType.SHIP,"SFS Not a Skysplitter", false);
		api.addToFleet(FleetSide.PLAYER, "sfcatlas_Strike", FleetMemberType.SHIP,"SFS Fuelslinger", false);
		api.addToFleet(FleetSide.PLAYER, "sfccrius_Blaster", FleetMemberType.SHIP, "SFS Fuel Emitter", false);
		api.addToFleet(FleetSide.PLAYER,"sfcpathercrius_Holy", FleetMemberType.SHIP, "Moloch's End", false);
		api.addToFleet(FleetSide.PLAYER, "sfcarke_Supporter", FleetMemberType.SHIP, "SFS Your Fuel Ad Here", false);
		api.addToFleet(FleetSide.PLAYER, "sfcclepsydra_Standard", FleetMemberType.SHIP, "SFS Junkers Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "sfcphaeton_ballistics", FleetMemberType.SHIP, "SFS Fuelbard", false);
		api.addToFleet(FleetSide.PLAYER, "sfcstymphalia_Support",FleetMemberType.SHIP, "SFS Fueled Missiles", false);
		api.addToFleet(FleetSide.PLAYER, "sfcaetos_Suppression",FleetMemberType.SHIP, "SFS Fuel Streamer", false);
		api.addToFleet(FleetSide.PLAYER, "sfcdrachm_Pressure", FleetMemberType.SHIP, "SFS Macross Fuelacre", false);
		api.addToFleet(FleetSide.PLAYER, "sfcpatherdrachm_Hammerer", FleetMemberType.SHIP, "Ludd's Holy Hammer", false);
		api.addToFleet(FleetSide.PLAYER, "sfcdram_assault", FleetMemberType.SHIP, "SFS Tough as Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "sfctahlanbento_Pulser", FleetMemberType.SHIP, "SFS Tahlan Fuelworks", false);
		api.addToFleet(FleetSide.PLAYER, "sfclelantus_Vanguard", FleetMemberType.SHIP, "SFS Fuel Rammer", false);
		api.addToFleet(FleetSide.PLAYER, "sfclelantus_Holy", FleetMemberType.SHIP,"Ludd's Warstick", false);
		api.addToFleet(FleetSide.PLAYER, "sfcbia_Hunter", FleetMemberType.SHIP, "SFS Fuelborne", false);
		api.addToFleet(FleetSide.PLAYER, "sfckander_Support", FleetMemberType.SHIP, "SFS Fuelstick", false);
		api.addToFleet(FleetSide.PLAYER, "sfcphoronis_Suppression", FleetMemberType.SHIP, "SFS Fuelworks", false);
		api.addToFleet(FleetSide.PLAYER, "sfcdelos_Support", FleetMemberType.SHIP, "SFS Cargo Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "sfchydria_Suppressor", FleetMemberType.SHIP, "SFS Kween's Surprise", false);
		api.addToFleet(FleetSide.PLAYER, "sfckobalos_Suppression", FleetMemberType.SHIP, "SFS Mannfred's Gremlin", false);
		api.addToFleet(FleetSide.PLAYER, "sfctalaria_Strike", FleetMemberType.SHIP, "SFS Hyperfuel", false);
		api.addToFleet(FleetSide.PLAYER, "sfcporpax_Patrol", FleetMemberType.SHIP, "SFS Fuelmortal", false);
		api.addToFleet(FleetSide.PLAYER, "sfcslent_Offense", FleetMemberType.SHIP, "SFS Fueler's Day Out", false);
		api.addToFleet(FleetSide.PLAYER, "sfcpolus_Strike", FleetMemberType.SHIP, "SFS Above Fuelspicion", false);
		api.addToFleet(FleetSide.PLAYER, "sfccoeus_Patrol", FleetMemberType.SHIP, "SFS Fuel Box",false);
		api.addToFleet(FleetSide.PLAYER, "sfcpolybolos_Standard", FleetMemberType.SHIP, "SFS Fuel Ranger",false);
		api.addToFleet(FleetSide.PLAYER, "sfcxyston_Strike", FleetMemberType.SHIP, "The Last Chance", false);
		if (isArmaA){
			api.addToFleet(FleetSide.PLAYER, "sfcmurmidonlg_pride", FleetMemberType.SHIP, "LGS New Age", false);
			api.addToFleet(FleetSide.PLAYER, "sfcmurmexlg_strikecraft_Prowler", FleetMemberType.SHIP, "LGS Future Perfect", false);
			api.addToFleet(FleetSide.PLAYER, "sfcfuelmech_strikecraft_Escort", FleetMemberType.SHIP, "Fuel Buddy", false);
		}
		api.addToFleet(FleetSide.PLAYER, "pinkdram_Light", FleetMemberType.SHIP, "SFS Little Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "pinkphaeton_Standard", FleetMemberType.SHIP, "SFS Medium Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "pinkprometheus_Super", FleetMemberType.SHIP, "SFS Big Fuel", false);
		api.addToFleet(FleetSide.PLAYER, "pinkmarathon_Standard", FleetMemberType.SHIP, "SFS Hot Dog", false);

		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "conquest_Standard", FleetMemberType.SHIP, "SFS Manager", true);

		// Set up the map.
		float width = 24000f;
		float height = 18000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

		float minX = -width/2;
		float minY = -height/2;

		for (int i = 0; i < 15; i++) {
			float x = (float) Math.random() * width - width/2;
			float y = (float) Math.random() * height - height/2;
			float radius = 100f + (float) Math.random() * 900f;
			api.addNebula(x, y, radius);
		}

	}

}
