package data.missions.siegeofnachiketa;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

import static com.fs.starfarer.api.fleet.FleetMemberType.SHIP;

public class MissionDefinition implements MissionDefinitionPlugin {

	public void defineMission(MissionDefinitionAPI api) {

		// Set up the fleets
		api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false, 10);
		api.initFleet(FleetSide.ENEMY, "SFS", FleetGoal.ATTACK, true, 10);

		// Set a blurb for each fleet
		api.setFleetTagline(FleetSide.PLAYER, "The 98th Nachiketa Flying Detachment.");
		api.setFleetTagline(FleetSide.ENEMY, "Sindrian Fuel Grand Synchronotron Core Inspection Fleet.");

		// These show up as items in the bulleted list under
		// "Tactical Objectives" on the mission detail screen
		api.addBriefingItem("Defeat the Sindrian Fuel Fleet at all costs.");
		api.addBriefingItem("Work with the remaining Nachiketa defenders to keep your capital ships safe.");
		api.addBriefingItem("Beware the Talaria TA-50s, they can easily outmaneuver your ships.");
		api.addBriefingItem("The Iapetus IS-15000 was built to stand toe-to-toe against an Onslaught. Don't fight it alone.");

		// Set up the player's fleet
		FleetMemberAPI flagship = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, "HSS Remember Samar", true);
		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.SUPPORT_DOCTRINE, 1);
		//cameo character
		PersonAPI nftstephens = Global.getSector().getFaction("hegemony").createRandomPerson(FullName.Gender.FEMALE);
		nftstephens.setId("nftstephens_mission");
		nftstephens.getName().setFirst("Dawn");
		nftstephens.getName().setLast("Stephens");
		nftstephens.getName().setGender((FullName.Gender.FEMALE));
		nftstephens.setPersonality("aggressive");
		nftstephens.setPortraitSprite("graphics/portraits/characters/nftstephens.png");
		nftstephens.setFaction("hegemony");
		nftstephens.getStats().setSkillLevel(Skills.HELMSMANSHIP, 1);
		nftstephens.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		nftstephens.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		nftstephens.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		nftstephens.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
		nftstephens.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
		nftstephens.getStats().setLevel(7);
		flagship.setCaptain(nftstephens);
/*		FleetMemberAPI ship1 = api.addToFleet(FleetSide.PLAYER, "onslaught_xiv_Elite"), FleetMemberType.SHIP, "HSS Samar Redeemed", false);
		FleetMemberAPI ship2 = api.addToFleet(FleetSide.PLAYER, "onslaught_Standard"), FleetMemberType.SHIP, "HSS Measured Response", false);
		FleetMemberAPI ship3 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship4 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship5 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship6 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship7 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship8 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship9 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship10 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship11 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship12 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship13 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship14 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship15 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship16 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship17 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship18 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship19 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship20 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
		FleetMemberAPI ship21 = api.addToFleet(FleetSide.PLAYER, "onslaught_Elite"), FleetMemberType.SHIP, "HSS Remember Samar", false);
*/
//		api.addToFleet(FleetSide.PLAYER, "onslaught_Elite", FleetMemberType.SHIP, "HSS Remember Samar", true);
		api.addToFleet(FleetSide.PLAYER, "onslaught_xiv_Elite", FleetMemberType.SHIP, "HSS Samar Redeemed", false);
		api.addToFleet(FleetSide.PLAYER, "onslaught_Standard", FleetMemberType.SHIP, "HSS Measured Response", false);
		api.addToFleet(FleetSide.PLAYER, "dominator_Assault", FleetMemberType.SHIP, "HSS Visby", false);
		api.addToFleet(FleetSide.PLAYER, "dominator_XIV_Elite", FleetMemberType.SHIP, "HSS Spirit of the 14th", false);
		api.addToFleet(FleetSide.PLAYER, "mora_Assault", FleetMemberType.SHIP, "HSS Stingray", false);
		api.addToFleet(FleetSide.PLAYER, "mora_Strike", FleetMemberType.SHIP, "HSS Alberta", false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "HSS Memory of the Domain", false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "HSS Ghesite", false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Escort", FleetMemberType.SHIP, "HSS Lynx", false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Escort", FleetMemberType.SHIP, "HSS Bobcat", false);
		api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "HSS Laputa", false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, "HSS Admiral Edward Wong Hau Pepelu Tivruski IV", false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Assault", FleetMemberType.SHIP, "HSS Just Cause", false);
		api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, "HSS Castle Doctrine", false);
		api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, "HSS Vulture", false);
		api.addToFleet(FleetSide.PLAYER, "lasher_Strike", FleetMemberType.SHIP, "HSS Aniki", false);
		api.addToFleet(FleetSide.PLAYER, "centurion_Assault", FleetMemberType.SHIP, "HSS Courier", false);
		api.addToFleet(FleetSide.PLAYER, "centurion_Assault", FleetMemberType.SHIP, "HSS Feather of the Phoenix", false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, "HSS Admiral Erreth-Aknbe", false);
		api.addToFleet(FleetSide.PLAYER, "brawler_Elite", FleetMemberType.SHIP, "HSS Dernhelm", false);
		api.addToFleet(FleetSide.PLAYER, "mule_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "mule_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);


		// Set up the enemy fleet
		api.addToFleet(FleetSide.ENEMY, "sfciapetus_Mixed", FleetMemberType.SHIP, "SFS Sindria Reborn", true);
		api.addToFleet(FleetSide.ENEMY, "sfcprometheus_barrage", FleetMemberType.SHIP,false);
		api.addToFleet(FleetSide.ENEMY, "sfcskyrend_Barrage", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfccrius_Blaster", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfccrius_Pressure", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcarke_Supporter", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcarke_Supporter", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdrachm_Pressure", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdrachm_Pressure", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdram_assault", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdram_missile", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcdram_missile", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfctahlanbento_Beamer", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfctahlanbento_Beamer", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcpolus_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfcpolus_Torpedo", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfctalaria_Strike", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sfctalaria_Overdriven", FleetMemberType.SHIP, false);

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

		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.4f, "nav_buoy");
		api.addObjective(minX + width * 0.8f - 1000, minY + height * 0.6f, "nav_buoy");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.3f, "comm_relay");
		api.addObjective(minX + width * 0.3f + 1000, minY + height * 0.7f, "comm_relay");
		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
		api.addObjective(minX + width * 0.2f + 1000, minY + height * 0.5f, "sensor_array");

		// Add an asteroid field
		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
				20f, 70f, 50);

		//nachiketa
		api.addPlanet(-320, -140, 200f, "barren", 250f, true);

	}

}
