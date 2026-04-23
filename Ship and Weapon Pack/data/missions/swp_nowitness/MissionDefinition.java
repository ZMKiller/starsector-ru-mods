package data.missions.swp_nowitness;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static boolean printedCodexWarning = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "<DATA EXPUNGED>");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Advance Scouts");

        if (!CodexDataV2.hasUnlockedEntryForShip("swp_solar") && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        api.addBriefingItem("Eradicate the enemy fleet");
        api.addBriefingItem("TTDS Automata must survive");

        api.addToFleet(FleetSide.PLAYER, "swp_solar_sta", FleetMemberType.SHIP, "TTDS Automata", true);
        api.addToFleet(FleetSide.PLAYER, "swp_nightwalker_att", FleetMemberType.SHIP, "TTS <REDACTED>", false);
        api.addToFleet(FleetSide.PLAYER, "swp_arachne_cs", FleetMemberType.SHIP, "TTS <REDACTED>", false);
        api.addToFleet(FleetSide.PLAYER, "swp_beholder_cs", FleetMemberType.SHIP, "TTS <REDACTED>", false);
        api.addToFleet(FleetSide.PLAYER, "swp_hecate_cs", FleetMemberType.SHIP, "TTS <REDACTED>", false);
        api.addToFleet(FleetSide.PLAYER, "swp_hecate_cs", FleetMemberType.SHIP, "TTS <REDACTED>", false);

        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        member = api.addToFleet(FleetSide.ENEMY, "swp_vindicator_sta", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "mora_Strike", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_punisher_sta", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_punisher_sta", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_sunder_xiv_eli", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_caliber_ass", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_caliber_ass", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_striker_str", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_striker_str", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_brawler_hegemony_ass", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_PD", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_PD", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());

        api.defeatOnShipLoss("TTDS Automata");

        float width = 16000f;
        float height = 16000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        for (int i = 0; i < 6; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(width * 0.35f, -height * 0.1f, "nav_buoy");
        api.addObjective(-width * 0.35f, -height * 0.1f, "nav_buoy");
        api.addObjective(0f, -height * 0.3f, "sensor_array");
        api.addObjective(width * 0.2f, height * 0.35f, "comm_relay");
        api.addObjective(-width * 0.2f, height * 0.35f, "comm_relay");

        api.addNebula(0f, -height * 0.3f, 1000f);
        api.addNebula(width * 0.15f, -height * 0.05f, 2000f);
        api.addNebula(-width * 0.15f, -height * 0.05f, 2000f);

        api.addRingAsteroids(0f, 0f, 40f, width, 30f, 40f, 400);

        api.addPlanet(0, 0, 350f, "ice_giant", 0f, true);
    }
}
