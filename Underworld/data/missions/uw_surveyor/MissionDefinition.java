package data.missions.uw_surveyor;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.UnderworldModPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static boolean printedCodexWarning = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Tri-Tachyon Survey Fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Pirate ambush");

        if (!CodexDataV2.hasUnlockedEntryForShip("uw_infernus") && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        if (!UnderworldModPlugin.hasSWP) {
            api.addBriefingItem("SHIP/WEAPON PACK REQUIRED");
            api.addBriefingItem("Download Ship/Weapon Pack to play this mission!");
        }

        if (!UnderworldModPlugin.hasSWP) {
            return;
        }

        api.addBriefingItem("Destroy The Infernal Machine if possible");
        api.addBriefingItem("The TTS Parse must survive");

        api.addToFleet(FleetSide.PLAYER, "swp_chronos_sta", FleetMemberType.SHIP, "TTS Parse", true);
        api.addToFleet(FleetSide.PLAYER, "swp_beholder_sta", FleetMemberType.SHIP, "TTS Vanguard", false);
        api.addToFleet(FleetSide.PLAYER, "swp_hecate_sta", FleetMemberType.SHIP, "TTS Strafe", false);
        api.addToFleet(FleetSide.PLAYER, "swp_hecate_sta", FleetMemberType.SHIP, "TTS Striker", false);

        api.defeatOnShipLoss("TTS Parse");

        api.addToFleet(FleetSide.ENEMY, "uw_infernus_dre", FleetMemberType.SHIP, "The Infernal Machine", false);
        api.addToFleet(FleetSide.ENEMY, "uw_barbarian_sta", FleetMemberType.SHIP, "Hammer", false);
        api.addToFleet(FleetSide.ENEMY, "uw_barbarian_sta", FleetMemberType.SHIP, "Anvil", false);
        api.addToFleet(FleetSide.ENEMY, "uw_stalker_sta", FleetMemberType.SHIP, "Reaper", false);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, "Haven", false);
        api.addToFleet(FleetSide.ENEMY, "uw_boar_sta", FleetMemberType.SHIP, "Blast", false);
        api.addToFleet(FleetSide.ENEMY, "uw_boar_sta", FleetMemberType.SHIP, "Boots", false);
        api.addToFleet(FleetSide.ENEMY, "uw_scythe_sta", FleetMemberType.SHIP, "Deep", false);
        api.addToFleet(FleetSide.ENEMY, "uw_scythe_sta", FleetMemberType.SHIP, "Thought", false);
        api.addToFleet(FleetSide.ENEMY, "uw_venom_sta", FleetMemberType.SHIP, "Viking", false);
        api.addToFleet(FleetSide.ENEMY, "uw_venom_sta", FleetMemberType.SHIP, "Marauder", false);
        api.addToFleet(FleetSide.ENEMY, "uw_sidecar_jun", FleetMemberType.SHIP, "Vega", false);
        api.addToFleet(FleetSide.ENEMY, "uw_sidecar_jun", FleetMemberType.SHIP, "Megaton", false);

        float width = 14000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addAsteroidField(0f, 0f, (float) Math.random() * 360f, width, 20f, 70f, 250);
    }
}
