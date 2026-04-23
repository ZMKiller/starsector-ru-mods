package data.missions.gl_sectormark;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final Logger log = Global.getLogger(MissionDefinition.class);

    private final List<String> ships = new ArrayList<>(188);

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_victory_ass");
        addShip("onslaught_Standard");

        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_victory_ass");
        addShip("onslaught_Standard");

        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_victory_ass");
        addShip("onslaught_Standard");

        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_tiger_str");
        addShip("wolf_Assault");
        addShip("uw_boar_eli");
        addShip("sunder_Assault");
        addShip("uw_stalker_sta");
        addShip("dominator_Outdated");
        addShip("uw_victory_ass");
        addShip("onslaught_Standard");

        api.initFleet(FleetSide.PLAYER, "Green", FleetGoal.ATTACK, true, 20);
        api.initFleet(FleetSide.ENEMY, "Red", FleetGoal.ATTACK, true, 20);

        api.setFleetTagline(FleetSide.PLAYER, "Green fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Red fleet");

        api.addBriefingItem("Use this mission for official benchmarks!");
        api.addBriefingItem("This mission automatically enables dev mode");

        generateFleet(FleetSide.PLAYER, ships, api);
        generateFleet(FleetSide.ENEMY, ships, api);

        float width = 4000f;
        float height = 4000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + width * 0.25f, minY + height * 0.25f, 150f);
        api.addNebula(minX + width * 0.75f, minY + height * 0.25f, 150f);
        api.addNebula(minX + width * 0.75f, minY + height * 0.75f, 150f);
        api.addNebula(minX + width * 0.25f, minY + height * 0.75f, 150f);
        api.addNebula(minX + width * 0.5f, minY + height * 0.5f, 150f);

        api.addAsteroidField(minX + width * 0.5f, minY + height * 0.5f, 45f, 500f + height / 2f, 50f, 100f, 50);

        api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "comm_relay");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "comm_relay");

        api.addPlugin(new Plugin());
    }

    private void addShip(String variant) {
        ships.add(variant);
    }

    private void generateFleet(FleetSide side, List<String> ships, MissionDefinitionAPI api) {
        for (String ship : ships) {
            try {
                api.addToFleet(side, ship, FleetMemberType.SHIP, false);
            } catch (Exception ex) {
            }
        }
    }

    private final static class Plugin extends BaseEveryFrameCombatPlugin {

        private double advanced = 0f;
        private double advancedShort = 0f;
        private float clearanceEnemy = 0f;
        private float clearancePlayer = 0f;
        private boolean clearanceRightEnemy = true;
        private boolean clearanceRightPlayer = false;
        private boolean ended;
        private long epoch;
        private long epochInfo;
        private long epochMicro;
        private long epochShort;
        private final List<Double> frameTimes = new LinkedList<>();
        private int frames = 0;
        private int framesShort = 0;
        private float height = 16000f;
        private int maxDP = 0;
        private double memsGB = 0.0;
        private double minimums = 0.0;
        private long score;
        private double variances = 0.0;
        private float width = 8000f;

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            List<ShipAPI> ships = Global.getCombatEngine().getShips();
            int playerDP = 0;
            int enemyDP = 0;
            for (ShipAPI ship : ships) {
                if (ship.isFighter()) {
                    continue;
                }
                if (ship.getLocation().x <= -20000f || ship.getLocation().x >= 20000f || ship.getLocation().y <= -40000f ||
                        ship.getLocation().y >= 40000f) {
                    Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 10000f, DamageType.OTHER, 0f, true,
                                                         false, ship);
                }
                if (ship.isAlive()) {
                    if (ship.getOwner() == 0) {
                        DeployedFleetMemberAPI deployed =
                                               Global.getCombatEngine().getFleetManager(0).getDeployedFleetMemberEvenIfDisabled(
                                                       ship);
                        if (deployed != null) {
                            playerDP += deployed.getMember().getDeploymentCostSupplies();
                        }
                    } else if (ship.getOwner() == 1) {
                        DeployedFleetMemberAPI deployed =
                                               Global.getCombatEngine().getFleetManager(1).getDeployedFleetMemberEvenIfDisabled(
                                                       ship);
                        if (deployed != null) {
                            enemyDP += deployed.getMember().getDeploymentCostSupplies();
                        }
                    }
                }
            }
            int playerTotalDP = playerDP;
            int enemyTotalDP = enemyDP;
            for (FleetMemberAPI member : Global.getCombatEngine().getFleetManager(0).getReservesCopy()) {
                playerTotalDP += member.getDeploymentCostSupplies();
            }
            for (FleetMemberAPI member : Global.getCombatEngine().getFleetManager(1).getReservesCopy()) {
                enemyTotalDP += member.getDeploymentCostSupplies();
            }
            maxDP = Math.max(maxDP, Math.max(playerTotalDP, enemyTotalDP));
            while (playerDP < Global.getSettings().getBattleSize() / 2) {
                if (Global.getCombatEngine().getFleetManager(0).getReservesCopy().isEmpty()) {
                    break;
                }
                FleetMemberAPI member = Global.getCombatEngine().getFleetManager(0).getReservesCopy().get(0);
                playerDP += member.getDeploymentCostSupplies();
                log.info(String.format("Spawning %s", member.getSpecId()));
                ShipAPI ship = Global.getCombatEngine().getFleetManager(0).spawnFleetMember(member, new Vector2f(
                                                                                            clearancePlayer, -3000f),
                                                                                            90f, 1f);
                Global.getCombatEngine().getFleetManager(0).removeFromReserves(member);
                if (clearanceRightPlayer) {
                    clearancePlayer += ship.getCollisionRadius() * 3f;
                    if (clearancePlayer >= 2000f) {
                        clearancePlayer -= ship.getCollisionRadius() * 5f;
                        clearanceRightPlayer = false;
                    }
                } else {
                    clearancePlayer -= ship.getCollisionRadius() * 3f;
                    if (clearancePlayer <= -2000f) {
                        clearancePlayer += ship.getCollisionRadius() * 5f;
                        clearanceRightPlayer = true;
                    }
                }
            }
            while (enemyDP < Global.getSettings().getBattleSize() / 2) {
                if (Global.getCombatEngine().getFleetManager(1).getReservesCopy().isEmpty()) {
                    break;
                }
                FleetMemberAPI member = Global.getCombatEngine().getFleetManager(1).getReservesCopy().get(
                               Global.getCombatEngine().getFleetManager(1).getReservesCopy().size() - 1);
                enemyDP += member.getDeploymentCostSupplies();
                log.info(String.format("Spawning %s", member.getSpecId()));
                ShipAPI ship = Global.getCombatEngine().getFleetManager(1).spawnFleetMember(member, new Vector2f(
                                                                                            clearanceEnemy, 3000f), 270f,
                                                                                            1f);
                Global.getCombatEngine().getFleetManager(1).removeFromReserves(member);
                if (clearanceRightEnemy) {
                    clearanceEnemy += ship.getCollisionRadius() * 3f;
                    if (clearanceEnemy >= 2000f) {
                        clearanceEnemy -= ship.getCollisionRadius() * 5f;
                        clearanceRightEnemy = false;
                    }
                } else {
                    clearanceEnemy -= ship.getCollisionRadius() * 3f;
                    if (clearanceEnemy <= -2000f) {
                        clearanceEnemy += ship.getCollisionRadius() * 5f;
                        clearanceRightEnemy = true;
                    }
                }
            }

            if (Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getTaskManager(false).isInFullRetreat() ||
                    Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(false).isInFullRetreat() ||
                    Global.getCombatEngine().getFleetManager(0).getReservesCopy().isEmpty() ||
                    Global.getCombatEngine().getFleetManager(1).getReservesCopy().isEmpty()) {
                if (!ended) {
                    ended = true;
                    double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                    double fpsAvg = frames / intervalAvg;
                    double minimumFPS = minimums / (intervalAvg / 10.0);
                    double variancesAvg = variances / (intervalAvg / 10.0);
                    double gameSpeed = advanced / intervalAvg;
                    double gameSpeedFactor = 1.0 + Math.min(minimumFPS / 30.0, 1.0);
                    double stabilityFactor = (minimumFPS / fpsAvg + (1.0 - variancesAvg / (1000.0 / fpsAvg)) +
                                              (gameSpeed * gameSpeedFactor)) /
                           (2.0 + gameSpeedFactor);
                    score = Math.round((fpsAvg * Math.sqrt(Math.pow(2.0, stabilityFactor) / 2.0)) * 100.0);
                    log.info(String.format("********************************"));
                    log.info(String.format("Benchmark Results (%.1f minute run):",
                                           (System.currentTimeMillis() - epoch) / 60000.0));
                    log.info(String.format("  Average FPS: %.1f", fpsAvg));
                    log.info(String.format("  Minimum FPS: %.1f", minimumFPS));
                    log.info(String.format("  Average Frame Variance: %.2fms", variancesAvg));
                    log.info(String.format("  Perceived Game Speed: %d%%", Math.round(gameSpeed * 100.0)));
                    log.info(String.format("  SectorMarks: %d", score));
                    log.info(String.format("********************************"));
                    Global.getSoundPlayer().playUISound("cr_playership_critical", 1f, 2f);
                }
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 0f),
                                                         String.format("SectorMarks: %d", score),
                                                         200f, Color.yellow, null, 1f, 0f);
                return;
            }

            if (Global.getCombatEngine().isPaused()) {
                epoch += System.currentTimeMillis() - epochMicro;
                epochShort += System.currentTimeMillis() - epochMicro;
                epochInfo += System.currentTimeMillis() - epochMicro;
                epochMicro = System.currentTimeMillis();
                return;
            }

            double frameTime = (System.currentTimeMillis() - epochMicro) / 1000.0;
            advanced += amount;
            advancedShort += amount;
            frameTimes.add(frameTime);
            epochMicro = System.currentTimeMillis();

            frames++;
            framesShort++;

            if (System.currentTimeMillis() - epochShort >= 1000) {
                double interval = (System.currentTimeMillis() - epochShort) / 1000.0;
                double fps = framesShort / interval;
                double progress = 1.0 - Math.min(playerTotalDP - 200, enemyTotalDP - 200) / (double) (maxDP - Math.max(
                                                                                                      200, 200));
                double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                double fpsAvg = frames / intervalAvg;
                double usedMemGB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /
                       (1073741824.0);
                memsGB += usedMemGB;
                log.info(String.format("Benchmark [%s] - %.1f FPS - %.2fGB Memory Used",
                                       new SimpleDateFormat("HH.mm.ss").format(new Date()), fps, usedMemGB));
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 87.5f),
                                                         String.format("Progress: %d%%", (int) (progress * 100.0)),
                                                         75f, Color.yellow, null, 1f, 0f);
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 0f),
                                                         String.format("Average FPS: %.1f", fpsAvg),
                                                         100f, Color.yellow, null, 1f, 0f);
                if (progress > 0.0 && System.currentTimeMillis() - epoch >= 1000 * 60 * 2) {
                    double speed = progress / ((System.currentTimeMillis() - epoch) / 60000.0);
                    double eta = (1.0 - progress) / speed;
                    Global.getCombatEngine().addFloatingText(new Vector2f(0f, -75f),
                                                             String.format("Time Remaining: %d minutes", Math.round(eta)),
                                                             50f, Color.yellow, null, 1f, 0f);
                }

                epochShort = System.currentTimeMillis();
                framesShort = 0;

                for (ShipAPI ship : ships) {
                    if (!ship.isAlive()) {
                        Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 0.025f * ship.getMaxHitpoints(),
                                                             DamageType.HIGH_EXPLOSIVE, 0f, true,
                                                             false, ship);
                    }
                }
            }

            if (System.currentTimeMillis() - epochInfo >= 1000 * 10) {
                double mean = 0.0;
                for (double frame : frameTimes) {
                    mean += frame;
                }
                mean /= frameTimes.size();
                double variance = 0.0;
                for (double frame : frameTimes) {
                    variance += (frame - mean) * (frame - mean);
                }
                variance /= frameTimes.size();
                double stdevMs = Math.sqrt(variance) * 1000.0;
                variances += stdevMs;
                double min = Double.MAX_VALUE;
                for (double frame : frameTimes) {
                    min = Math.min(min, 1.0 / frame);
                }
                minimums += min;
                double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                double fpsAvg = frames / intervalAvg;
                double minimumFPS = minimums / (intervalAvg / 10.0);
                double variancesAvg = variances / (intervalAvg / 10.0);
                double progress = 1.0 - Math.min(playerTotalDP - 200, enemyTotalDP - 200) / (double) (maxDP - Math.max(
                                                                                                      200, 200));
                double memoryAvg = memsGB / intervalAvg;
                double intervalInfo = (System.currentTimeMillis() - epochInfo) / 1000.0;
                double gameSpeed = advancedShort / intervalInfo;
                log.info(String.format("Benchmark Stats [%s] (Progress: %d%%):",
                                       new SimpleDateFormat("HH:mm:ss").format(new Date()), (int) (progress * 100.0)));
                log.info(String.format("  Average FPS: %.1f", fpsAvg));
                log.info(String.format("  Minimum FPS: %.1f", minimumFPS));
                log.info(String.format("  Frame Variance: %.2fms", stdevMs));
                log.info(String.format("  Average Frame Variance: %.2fms", variancesAvg));
                log.info(String.format("  Average Memory Used: %.2fGB", memoryAvg));
                log.info(String.format("  Game Speed: %.1f%%", gameSpeed * 100.0));

                frameTimes.clear();
                advancedShort = 0.0;
                epochInfo = System.currentTimeMillis();
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
            log.setLevel(Level.INFO);
            engine.getContext().setStandoffRange(8000f);
            engine.getContext().setInitialDeploymentBurnDuration(0.5f);
            engine.getContext().setNormalDeploymentBurnDuration(0.5f);
            float ratio = Global.getSettings().getScreenWidth() / Global.getSettings().getScreenHeight();
            if (ratio < 1f) {
                width *= ratio;
            } else {
                height /= ratio;
            }
            Global.getSettings().setDevMode(true);
            engine.getViewport().setExternalControl(true);
            engine.getViewport().set(-width / 3f, -height / 6f, width / 1.5f, height / 3f);
            epoch = System.currentTimeMillis();
            epochShort = System.currentTimeMillis();
            epochMicro = System.currentTimeMillis();
            epochInfo = System.currentTimeMillis();
        }
    }
}
