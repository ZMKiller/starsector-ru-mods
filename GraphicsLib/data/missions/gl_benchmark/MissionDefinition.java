package data.missions.gl_benchmark;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AssignmentTargetAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.ShipRoles;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.RoleEntryAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final List<String> FACTIONS = new ArrayList<>(28);
    public static final List<String> ROLES = new ArrayList<>(29);

    public static final Logger log = Global.getLogger(MissionDefinition.class);

    static {
        FACTIONS.add(Factions.HEGEMONY);
        FACTIONS.add(Factions.DIKTAT);
        FACTIONS.add(Factions.INDEPENDENT);
        FACTIONS.add(Factions.KOL);
        FACTIONS.add(Factions.LIONS_GUARD);
        FACTIONS.add(Factions.LUDDIC_CHURCH);
        FACTIONS.add(Factions.LUDDIC_PATH);
        FACTIONS.add(Factions.PIRATES);
        FACTIONS.add(Factions.TRITACHYON);
        FACTIONS.add(Factions.PERSEAN);
        FACTIONS.add(Factions.DERELICT);
        FACTIONS.add(Factions.REMNANTS);
        FACTIONS.add(Factions.OMEGA);
        FACTIONS.add("cabal");
        FACTIONS.add("interstellarimperium");
        FACTIONS.add("blackrock_driveyards");
        FACTIONS.add("exigency");
        FACTIONS.add("exipirated");
        FACTIONS.add("templars");
        FACTIONS.add("shadow_industry");
        FACTIONS.add("junk_pirates");
        FACTIONS.add("pack");
        FACTIONS.add("syndicate_asp");
        FACTIONS.add("SCY");
        FACTIONS.add("tiandong");
        FACTIONS.add("diableavionics");
        FACTIONS.add("ORA");

        ROLES.add(ShipRoles.COMBAT_SMALL);
        ROLES.add(ShipRoles.COMBAT_MEDIUM);
        ROLES.add(ShipRoles.COMBAT_LARGE);
        ROLES.add(ShipRoles.COMBAT_CAPITAL);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_SMALL);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_MEDIUM);
        ROLES.add(ShipRoles.COMBAT_FREIGHTER_LARGE);
        ROLES.add(ShipRoles.CIV_RANDOM);
        ROLES.add(ShipRoles.CARRIER_SMALL);
        ROLES.add(ShipRoles.CARRIER_MEDIUM);
        ROLES.add(ShipRoles.CARRIER_LARGE);
        ROLES.add(ShipRoles.FREIGHTER_SMALL);
        ROLES.add(ShipRoles.FREIGHTER_MEDIUM);
        ROLES.add(ShipRoles.FREIGHTER_LARGE);
        ROLES.add(ShipRoles.TANKER_SMALL);
        ROLES.add(ShipRoles.TANKER_MEDIUM);
        ROLES.add(ShipRoles.TANKER_LARGE);
        ROLES.add(ShipRoles.PERSONNEL_SMALL);
        ROLES.add(ShipRoles.PERSONNEL_MEDIUM);
        ROLES.add(ShipRoles.PERSONNEL_LARGE);
        ROLES.add(ShipRoles.LINER_SMALL);
        ROLES.add(ShipRoles.LINER_MEDIUM);
        ROLES.add(ShipRoles.LINER_LARGE);
        ROLES.add(ShipRoles.TUG);
        ROLES.add(ShipRoles.CRIG);
        ROLES.add(ShipRoles.UTILITY);
    }

    private final List<String> ships = new ArrayList<>(5000);

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        Set<String> variants = new HashSet<>(5000);
        for (String role : ROLES) {
            for (String faction : FACTIONS) {
                List<RoleEntryAPI> roleEntries;
                try {
                    roleEntries = Global.getSettings().getEntriesForRole(faction, role);
                } catch (Exception e) {
                    continue;
                }
                for (RoleEntryAPI roleEntry : roleEntries) {
                    try {
                        ShipVariantAPI variant = Global.getSettings().getVariant(roleEntry.getVariantId());
                        if (variant.getHullSpec().hasTag(Tags.RESTRICTED) && !Global.getSettings().isDevMode()) {
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                    variants.add(roleEntry.getVariantId());
                }
            }
            List<RoleEntryAPI> roleEntries = Global.getSettings().getDefaultEntriesForRole(role);
            for (RoleEntryAPI roleEntry : roleEntries) {
                try {
                    ShipVariantAPI variant = Global.getSettings().getVariant(roleEntry.getVariantId());
                    if (variant.getHullSpec().hasTag(Tags.RESTRICTED) && !Global.getSettings().isDevMode()) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                variants.add(roleEntry.getVariantId());
            }
        }

        for (String variant : variants) {
            addShip(variant);
        }

        api.initFleet(FleetSide.PLAYER, "Green", FleetGoal.ATTACK, false, 5);
        api.initFleet(FleetSide.ENEMY, "Red", FleetGoal.ATTACK, false, 5);

        api.setFleetTagline(FleetSide.PLAYER, "Green fleet");
        api.setFleetTagline(FleetSide.ENEMY, "Red fleet");

        api.addBriefingItem("This mission automatically enables dev mode");

        generateFleet(FleetSide.PLAYER, ships, api);
        generateFleet(FleetSide.ENEMY, ships, api);

        float width = 8000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addNebula(minX + 3000f, minY + 7000f, 150f);
        api.addNebula(minX + 5000f, minY + 7000f, 150f);
        api.addNebula(minX + 5000f, minY + 9000f, 150f);
        api.addNebula(minX + 3000f, minY + 9000f, 150f);
        api.addNebula(minX + 4000f, minY + 8000f, 150f);

        api.addAsteroidField(minX + width * 0.5f, minY + height * 0.5f, 45f, 500f + height / 2f, 50f, 100f, 100);

        api.addObjective(minX + width * 0.375f, minY + height * 0.45f, "nav_buoy");
        api.addObjective(minX + width * 0.625f, minY + height * 0.45f, "sensor_array");
        api.addObjective(minX + width * 0.625f, minY + height * 0.55f, "nav_buoy");
        api.addObjective(minX + width * 0.375f, minY + height * 0.55f, "sensor_array");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "comm_relay");

        api.addPlugin(new Plugin());
    }

    private void addShip(String variant) {
        ships.add(variant);
    }

    private void generateFleet(FleetSide side, List<String> ships, MissionDefinitionAPI api) {
        Set<String> hulls = new HashSet<>(ships.size());
        for (String ship : ships) {
            try {
                String id = ship;
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, id);
                String hull = member.getHullId();
                if (member.isCivilian() || member.getVariant().isEmptyHullVariant() || member.isStation()) {
                    continue;
                }
                if (hulls.contains(hull)) {
                    continue;
                } else {
                    hulls.add(hull);
                }
                api.addToFleet(side, id, FleetMemberType.SHIP, false);
            } catch (Exception ex) {
            }
        }
    }

    private final static class Plugin extends BaseEveryFrameCombatPlugin {

        private double advanced = 0.0;
        private double advancedShort = 0.0;
        private int battleSize = 600;
        private float clearanceEnemy = 0f;
        private float clearancePlayer = 0f;
        private boolean clearanceRightEnemy = true;
        private boolean clearanceRightPlayer = false;
        private boolean ended;
        private long epoch;
        private long epochInfo;
        private long epochMicro;
        private long epochShort;
        private long epochSize;
        private final List<Double> frameTimes = new LinkedList<>();
        private final List<Double> memLoadsGB = new LinkedList<>();
        private int frames = 0;
        private int framesShort = 0;
        private int framesSize = 0;
        private float height = 4500f;
        private int maxDP = 0;
        private double minimums = 0.0;
        private int orderedStuff = 60;
        private double variances = 0.0;
        private float width = 4500f;

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            List<ShipAPI> ships = Global.getCombatEngine().getShips();
            int playerDP = 0;
            int enemyDP = 0;
            for (ShipAPI ship : ships) {
                if (ship.isFighter()) {
                    continue;
                }
                if (ship.getLocation().x <= -25000f || ship.getLocation().x >= 25000f || ship.getLocation().y <= -40000f || ship.getLocation().y >= 40000f) {
                    Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 10000f, DamageType.OTHER, 0f, true, false, ship);
                }
                if (ship.isAlive()) {
                    if (ship.getOwner() == 0) {
                        DeployedFleetMemberAPI deployed = Global.getCombatEngine().getFleetManager(0).getDeployedFleetMemberEvenIfDisabled(ship);
                        if (deployed != null) {
                            playerDP += deployed.getMember().getDeploymentCostSupplies();
                        }
                    } else if (ship.getOwner() == 1) {
                        DeployedFleetMemberAPI deployed = Global.getCombatEngine().getFleetManager(1).getDeployedFleetMemberEvenIfDisabled(ship);
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
            while (playerDP < battleSize / 2) {
                if (Global.getCombatEngine().getFleetManager(0).getReservesCopy().isEmpty()) {
                    break;
                }
                FleetMemberAPI member = Global.getCombatEngine().getFleetManager(0).getReservesCopy().get(0);
                playerDP += member.getDeploymentCostSupplies();
                log.info(String.format("Spawning %s", member.getSpecId()));
                ShipAPI ship = Global.getCombatEngine().getFleetManager(0).spawnFleetMember(member, new Vector2f(clearancePlayer, -3000f), 90f, 1f);
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
            while (enemyDP < battleSize / 2) {
                if (Global.getCombatEngine().getFleetManager(1).getReservesCopy().isEmpty()) {
                    break;
                }
                FleetMemberAPI member = Global.getCombatEngine().getFleetManager(1).getReservesCopy().get(Global.getCombatEngine().getFleetManager(1).getReservesCopy().size() - 1);
                enemyDP += member.getDeploymentCostSupplies();
                log.info(String.format("Spawning %s", member.getSpecId()));
                ShipAPI ship = Global.getCombatEngine().getFleetManager(1).spawnFleetMember(member, new Vector2f(clearanceEnemy, 3000f), 270f, 1f);
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

            if (Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getTaskManager(false).isInFullRetreat()
                    || Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(false).isInFullRetreat()
                    || Global.getCombatEngine().getFleetManager(0).getReservesCopy().isEmpty()
                    || Global.getCombatEngine().getFleetManager(1).getReservesCopy().isEmpty()) {
                if (!ended) {
                    ended = true;
                    double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                    double fpsAvg = frames / intervalAvg;
                    double minimumFPS = minimums / (intervalAvg / 10.0);
                    double variancesAvg = variances / (intervalAvg / 10.0);
                    double gameSpeed = advanced / intervalAvg;
                    log.info(String.format("********************************"));
                    log.info(String.format("Benchmark Results (%.1f minute run):", (System.currentTimeMillis() - epoch) / 60000.0));
                    log.info(String.format("  Average FPS: %.1f", fpsAvg));
                    log.info(String.format("  Minimum FPS: %.1f", minimumFPS));
                    log.info(String.format("  Average Frame Variance: %.2fms", variancesAvg));
                    log.info(String.format("  Perceived Game Speed: %d%%", Math.round(gameSpeed * 100.0)));
                    log.info(String.format("  Battle Size: %d", battleSize - 200));
                    log.info(String.format("********************************"));
                    Global.getSoundPlayer().playUISound("cr_playership_critical", 1f, 2f);
                }
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 0f), String.format("Recommended Battle Size: %d", battleSize - 200), 200f, Color.yellow, null, 1f, 0f);
                return;
            }

            if (Global.getCombatEngine().isPaused()) {
                epoch += System.currentTimeMillis() - epochMicro;
                epochShort += System.currentTimeMillis() - epochMicro;
                epochInfo += System.currentTimeMillis() - epochMicro;
                epochSize += System.currentTimeMillis() - epochMicro;
                epochMicro = System.currentTimeMillis();
                return;
            }

            double frameTime = (System.currentTimeMillis() - epochMicro) / 1000.0;
            double usedMemGB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1073741824.0);
            advanced += amount;
            advancedShort += amount;
            frameTimes.add(frameTime);
            memLoadsGB.add(usedMemGB);
            epochMicro = System.currentTimeMillis();

            frames++;
            framesShort++;
            framesSize++;
            orderedStuff--;
            if (orderedStuff == 0) {
                for (BattleObjectiveAPI objective : Global.getCombatEngine().getObjectives()) {
                    if (objective.getLocation().x <= 500f && objective.getLocation().y <= 500f) {
                        Global.getCombatEngine().getFleetManager(0).getTaskManager(false).createAssignment(CombatAssignmentType.ASSAULT, (AssignmentTargetAPI) objective, false);
                        Global.getCombatEngine().getFleetManager(1).getTaskManager(false).createAssignment(CombatAssignmentType.ASSAULT, (AssignmentTargetAPI) objective, false);
                    } else {
                        Global.getCombatEngine().getFleetManager(0).getTaskManager(false).createAssignment(CombatAssignmentType.CAPTURE, (AssignmentTargetAPI) objective, false);
                        Global.getCombatEngine().getFleetManager(1).getTaskManager(false).createAssignment(CombatAssignmentType.CAPTURE, (AssignmentTargetAPI) objective, false);
                    }
                }
            }

            if ((System.currentTimeMillis() - epochSize >= 1000 * 30) && (System.currentTimeMillis() - epoch >= 1000 * 60 * 2)) {
                double intervalSize = (System.currentTimeMillis() - epochSize) / 1000.0;
                double currentFPSAvg = framesSize / intervalSize;
                double progress = 1.0 - Math.min(playerTotalDP - battleSize / 2, enemyTotalDP - battleSize / 2) / (double) (maxDP - battleSize / 2);
                double scale = 1.0 - progress * 0.9;

                if (currentFPSAvg > 30.0 + 10.0 * scale) {
                    battleSize += scale * 20.0;
                } else if (currentFPSAvg < 30.0) {
                    battleSize -= scale * 20.0;
                }

                framesSize = 0;
                epochSize = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - epochShort >= 1000) {
                double interval = (System.currentTimeMillis() - epochShort) / 1000.0;
                double fps = framesShort / interval;
                double progress = 1.0 - Math.min(playerTotalDP - battleSize / 2, enemyTotalDP - battleSize / 2) / (double) (maxDP - battleSize / 2);
                double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                double fpsAvg = frames / intervalAvg;
                log.info(String.format("Benchmark [%s] - %.1f FPS - %.2fGB Memory Used", new SimpleDateFormat("HH.mm.ss").format(new Date()), fps, usedMemGB));
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 87.5f), String.format("Progress: %d%%", (int) (progress * 100.0)), 75f, Color.yellow, null, 1f, 0f);
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, 0f), String.format("Average FPS: %.1f", fpsAvg), 100f, Color.yellow, null, 1f, 0f);
                Global.getCombatEngine().addFloatingText(new Vector2f(0f, -75f), String.format("Battle Size: %d", battleSize - 200), 50f, Color.yellow, null, 1f, 0f);
                if (progress > 0.0 && System.currentTimeMillis() - epoch >= 1000 * 60 * 2) {
                    double speed = progress / ((System.currentTimeMillis() - epoch) / 60000.0);
                    double eta = (1.0 - progress) / speed;
                    Global.getCombatEngine().addFloatingText(new Vector2f(0f, -125f), String.format("Time Remaining: %d minutes", Math.round(eta)), 50f, Color.yellow, null, 1f, 0f);
                }

                epochShort = System.currentTimeMillis();
                framesShort = 0;

                for (ShipAPI ship : ships) {
                    if (!ship.isAlive()) {
                        Global.getCombatEngine().applyDamage(ship, ship.getLocation(), 0.025f * ship.getMaxHitpoints(), DamageType.HIGH_EXPLOSIVE, 0f, true, false, ship);
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
                double meanMem = 0.0;
                double minMem;
                if (memLoadsGB.isEmpty()) {
                    minMem = 0.0;
                } else {
                    minMem = memLoadsGB.get(0);
                }
                for (double mem : memLoadsGB) {
                    meanMem += mem;
                    minMem = Math.min(minMem, mem);
                }
                meanMem /= memLoadsGB.size();
                double intervalAvg = (System.currentTimeMillis() - epoch) / 1000.0;
                double fpsAvg = frames / intervalAvg;
                double minimumFPS = minimums / (intervalAvg / 10.0);
                double variancesAvg = variances / (intervalAvg / 10.0);
                double progress = 1.0 - Math.min(playerTotalDP - battleSize / 2, enemyTotalDP - battleSize / 2) / (double) (maxDP - battleSize / 2);
                double intervalInfo = (System.currentTimeMillis() - epochInfo) / 1000.0;
                double gameSpeed = advancedShort / intervalInfo;
                log.info(String.format("Benchmark Stats [%s] (Progress: %d%%):", new SimpleDateFormat("HH:mm:ss").format(new Date()), (int) (progress * 100.0)));
                log.info(String.format("  Average FPS: %.1f", fpsAvg));
                log.info(String.format("  Minimum FPS: %.1f", minimumFPS));
                log.info(String.format("  Frame Variance: %.2fms", stdevMs));
                log.info(String.format("  Average Frame Variance: %.2fms", variancesAvg));
                log.info(String.format("  Average Memory Used: %.2fGB", meanMem));
                log.info(String.format("  Minimum Memory Used: %.2fGB", minMem));
                log.info(String.format("  Game Speed: %.1f%%", gameSpeed * 100.0));
                log.info(String.format("  Battle Size: %d", battleSize - 200));

                frameTimes.clear();
                memLoadsGB.clear();
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
            float ratio = Global.getSettings().getScreenWidthPixels() / Global.getSettings().getScreenHeightPixels();
            if (ratio < 1f) {
                width *= ratio;
            } else {
                height /= ratio;
            }
            Global.getSettings().setDevMode(true);
            engine.getViewport().setExternalControl(true);
            engine.getViewport().set(-width * 0.5f, -height * 0.5f, width, height);
            epoch = System.currentTimeMillis();
            epochShort = System.currentTimeMillis();
            epochMicro = System.currentTimeMillis();
            epochInfo = System.currentTimeMillis();
        }
    }
}
