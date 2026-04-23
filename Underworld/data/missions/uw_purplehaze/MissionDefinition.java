package data.missions.uw_purplehaze;

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
import data.scripts.UnderworldModPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static boolean printedCodexWarning = false;

    private static boolean swp = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "STAR", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Starlight Cabal raiders");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony task force");

        if (!CodexDataV2.hasUnlockedEntryForShip("uw_aurora_cabal") && !CodexDataV2.hasUnlockedEntryForShip("uw_harbinger_cabal")
                && !CodexDataV2.hasUnlockedEntryForShip("uw_medusa_cabal") && !CodexDataV2.hasUnlockedEntryForShip("uw_afflictor_cabal")
                && !CodexDataV2.hasUnlockedEntryForShip("uw_wolf_cabal") && !CodexDataV2.hasUnlockedEntryForShip("uw_tempest_cabal")
                && !CodexDataV2.hasUnlockedEntryForShip("uw_wolf_cabal") && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("The STAR Corset must survive");
        api.addBriefingItem("NOTE: Different enemy ships available with Ship/Weapon Pack (click mission to toggle)");

        if (UnderworldModPlugin.hasSWP) {
            swp = !swp;
        }

        api.addToFleet(FleetSide.PLAYER, "uw_aurora_cabal_cus", FleetMemberType.SHIP, "STAR Corset", true);
        api.addToFleet(FleetSide.PLAYER, "uw_harbinger_cabal_cus", FleetMemberType.SHIP, "STAR Leggings", false);
        api.addToFleet(FleetSide.PLAYER, "uw_medusa_cabal_cus", FleetMemberType.SHIP, "STAR Chemise", false);
        api.addToFleet(FleetSide.PLAYER, "uw_afflictor_cabal_cus", FleetMemberType.SHIP, "STAR Chopine", false);
        api.addToFleet(FleetSide.PLAYER, "uw_wolf_cabal_cus", FleetMemberType.SHIP, "STAR Nightgown", false);
        api.addToFleet(FleetSide.PLAYER, "uw_tempest_cabal_cus", FleetMemberType.SHIP, "STAR Thigh Highs", false);
        api.addToFleet(FleetSide.PLAYER, "uw_wolf_cabal_tri", FleetMemberType.SHIP, "STAR Heels", false);

        api.defeatOnShipLoss("STAR Corset");

        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_vindicator_o_sta" : "dominator_Assault", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_punisher_sta" : "falcon_CS", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_gryphon_xiv_eli" : "gryphon_FS", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_caliber_sta" : "enforcer_Balanced", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_albatross_sta" : "mule_Standard", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, swp ? "swp_hammerhead_xiv_eli" : "hammerhead_Elite", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "condor_Support", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false);
        member.setShipName(hegemony.pickRandomShipName());

        float width = 14000f;
        float height = 16000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        /*
         for (int i = 0; i < 5; i++) {
         float x = (float) Math.random() * width - width / 2;
         float y = (float) Math.random() * height - height / 2;
         float radius = 100f + (float) Math.random() * 400f;
         api.addNebula(x, y, radius);
         }
         */
        float minX = -width / 2;
        float minY = -height / 2;
        api.addObjective(minX + width * 0.29f, minY + height * 0.33f, "nav_buoy");
        //api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "comm_relay");
        api.addObjective(minX + width * 0.71f, minY + height * 0.67f, "sensor_array");

        api.setHyperspaceMode(true);
    }
}
