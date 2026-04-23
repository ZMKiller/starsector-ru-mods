package data.missions.swp_stainedglass;

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

    public static final String PLAYER_FACTION_ID = Factions.HEGEMONY;
    public static final int PLAYER_FLEET_SIZE = 65;
    public static final String ENEMY_FACTION_ID = Factions.REMNANTS;
    public static final int ENEMY_FLEET_SIZE = 110;

    public static boolean printedCodexWarning = false;

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "CGR Notre Dame with Hegemony allies");
        api.setFleetTagline(FleetSide.ENEMY, "AI Battlefleet");

        if (!CodexDataV2.hasUnlockedEntryForShip("swp_cathedral") && !CodexDataV2.hasUnlockedEntryForShip("swp_solar")
                && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        api.addBriefingItem("Destroy the enemy fleet");
        api.addBriefingItem("CGR Notre Dame must survive");

        api.addToFleet(FleetSide.PLAYER, "swp_cathedral_mob", FleetMemberType.SHIP, "CGR Notre Dame", true);

        FleetParamsV3 params = new FleetParamsV3(null,
                new Vector2f(0, 0),
                PLAYER_FACTION_ID,
                1.25f, // qualityOverride
                "missionFleet",
                PLAYER_FLEET_SIZE, // combatPts
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
        params.maxShipSize = 3;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
        p.quality = 1.25f;
        p.seed = MathUtils.getRandom().nextLong();
        p.mode = ShipPickMode.PRIORITY_THEN_ALL;
        p.allWeapons = true;
        p.factionId = PLAYER_FACTION_ID;

        DefaultFleetInflater inflater = new DefaultFleetInflater(p);
        inflater.inflate(fleet);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.PLAYER, member);
        }

        api.addToFleet(FleetSide.ENEMY, "swp_solar_str", FleetMemberType.SHIP, "TTDS New Dawn", false);

        params = new FleetParamsV3(null,
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
        params.maxShipSize = 3;

        fleet = FleetFactoryV3.createFleet(params);

        p = new DefaultFleetInflaterParams();
        p.quality = 1.25f;
        p.seed = MathUtils.getRandom().nextLong();
        p.mode = ShipPickMode.PRIORITY_THEN_ALL;

        inflater = new DefaultFleetInflater(p);
        inflater.inflate(fleet);

        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.ENEMY, member);
        }

        api.defeatOnShipLoss("CGR Notre Dame");

        float width = 20000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        for (int i = 0; i < 10; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(width * -0.3f, 0f, "nav_buoy");
        api.addObjective(0f, 0f, "sensor_array");
        api.addObjective(width * 0.3f, 0f, "nav_buoy");

        api.addRingAsteroids(0f, height * 0.2f, 0f, width * 0.2f, 30f, 40f, 400);
        api.addRingAsteroids(0f, height * -0.2f, 180f, width * 0.2f, 30f, 40f, 400);

        api.addPlanet(-0.25f, -0.25f, 150f, "terran", 0f, true);

        api.getContext().aiRetreatAllowed = false;
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;

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
