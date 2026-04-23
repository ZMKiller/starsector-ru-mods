package data.missions.uw_blunder;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.UnderworldModPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static boolean printedCodexWarning = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "CGR", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "The Infernal Machine Horde");
        api.setFleetTagline(FleetSide.ENEMY, "Church of Galactic Redemption Purification Fleet");

        if (!UnderworldModPlugin.hasSWP) {
            api.addBriefingItem("SHIP/WEAPON PACK REQUIRED");
            api.addBriefingItem("Download Ship/Weapon Pack to play this mission!");
            return;
        }

        if (!CodexDataV2.hasUnlockedEntryForShip("uw_infernus") && !CodexDataV2.hasUnlockedEntryForShip("swp_cathedral")
                && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("The Infernal Machine must survive");

        FactionAPI pirates = Global.getSettings().createBaseFaction(Factions.PIRATES);
        FleetMemberAPI member;
        api.addToFleet(FleetSide.PLAYER, "uw_infernus_dre", FleetMemberType.SHIP, "The Infernal Machine", true);
        member = api.addToFleet(FleetSide.PLAYER, "uw_barbarian_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_stalker_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_boar_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_shark_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "buffalo2_FS", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "condor_Strike", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "condor_Strike", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_tiger_att", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_tiger_att", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_torch_att", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_torch_att", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_venom_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_venom_sta", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "uw_sidecar_att", FleetMemberType.SHIP, false);
        member.setShipName(pirates.pickRandomShipName());

        api.defeatOnShipLoss("The Infernal Machine");

        FactionAPI luddicChurch = Global.getSettings().createBaseFaction(Factions.LUDDIC_CHURCH);
        api.addToFleet(FleetSide.ENEMY, "swp_cathedral_gra", FleetMemberType.SHIP, "CGR Inquisition", true);
        member = api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Outdated", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "enforcer_Outdated", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_caliber_sta", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "swp_archer_sta", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.AGGRESSIVE);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "vigilance_Strike", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.CAUTIOUS);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.CAUTIOUS);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_luddic_church_Standard", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.CAUTIOUS);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_luddic_church_Standard", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.CAUTIOUS);
        member.setShipName(luddicChurch.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "hound_luddic_church_Standard", FleetMemberType.SHIP, false);
        member.getCaptain().setPersonality(Personalities.CAUTIOUS);
        member.setShipName(luddicChurch.pickRandomShipName());

        float width = 14000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(minX + width * 0.50f, minY + height * 0.50f, "nav_buoy");
    }
}
