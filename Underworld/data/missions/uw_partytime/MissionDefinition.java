package data.missions.uw_partytime;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final String ENEMY_FACTION_ID = Factions.PIRATES;
    public static final int ENEMY_FLEET_SIZE = 400;

    public static boolean printedCodexWarning = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "STAR", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Starlight Gala Parade");
        api.setFleetTagline(FleetSide.ENEMY, "Damn You, Dickerson!");

        if (!CodexDataV2.hasUnlockedEntryForShip("uw_palace") && !CodexDataV2.hasUnlockedEntryForShip("uw_climax")
                && !CodexDataV2.hasUnlockedEntryForShip("uw_shadowclaw") && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        api.addBriefingItem("Defeat all enemy forces");
        api.addBriefingItem("The Transcendance must survive");

        api.addToFleet(FleetSide.PLAYER, "uw_palace_gra", FleetMemberType.SHIP, "Transcendance", true);
        api.addToFleet(FleetSide.PLAYER, "uw_climax_sur", FleetMemberType.SHIP, "STAR Birthday", false);
        api.addToFleet(FleetSide.PLAYER, "uw_climax_dis", FleetMemberType.SHIP, "STAR Inferno", false);
        api.addToFleet(FleetSide.PLAYER, "uw_climax_han", FleetMemberType.SHIP, "STAR Part II", false);

        api.addToFleet(FleetSide.ENEMY, "uw_shadowclaw_pro", FleetMemberType.SHIP, "Shadowclaw", false);
        for (int i = 0; i < 5; i++) {
            api.addToFleet(FleetSide.ENEMY, "uw_sidecar_att", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "uw_sidecar_cs", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "uw_sidecar_jun", FleetMemberType.SHIP, false);
            api.addToFleet(FleetSide.ENEMY, "uw_sidecar_ovd", FleetMemberType.SHIP, false);
        }

        FleetParamsV3 params = new FleetParamsV3(null,
                new Vector2f(0, 0),
                ENEMY_FACTION_ID,
                1.25f, // qualityOverride
                "missionFleet",
                ENEMY_FLEET_SIZE, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f); // qualityMod
        params.withOfficers = false;
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
        p.quality = 1.25f;
        p.seed = MathUtils.getRandom().nextLong();
        p.mode = ShipPickMode.PRIORITY_THEN_ALL;

        DefaultFleetInflater inflater = new DefaultFleetInflater(p);
        inflater.inflate(fleet);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.ENEMY, member);
        }

        api.defeatOnShipLoss("Transcendance");

        float width = 12000f;
        float height = 12000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;
        api.addObjective(minX + width * 0.33f, minY + height * 0.33f, "nav_buoy");
        api.addObjective(minX + width * 0.5f, minY + height * 0.33f, "sensor_array");
        api.addObjective(minX + width * 0.67f, minY + height * 0.33f, "nav_buoy");

        api.addPlanet(0, 100, 120f, "toxic", 0f, true);

        api.addPlugin(new BaseEveryFrameCombatPlugin() {
            @Override
            public void init(CombatEngineAPI engine) {
                engine.getContext().aiRetreatAllowed = false;
                engine.getContext().enemyDeployAll = true;
                engine.getContext().fightToTheLast = true;
            }
        });
    }
}
