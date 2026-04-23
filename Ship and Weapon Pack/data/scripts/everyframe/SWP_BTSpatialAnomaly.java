package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.shipsystems.SWP_PhaseShuntDriveStats;
import data.scripts.util.SWP_AnamorphicFlare;
import data.scripts.util.SWP_Util;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_BTSpatialAnomaly extends BaseEveryFrameCombatPlugin {

    private static final Vector2f ZERO = new Vector2f();

    private CombatEngineAPI engine;
    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.2f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (engine.getPlayerShip() != null) {
            String display;
            if (engine.getPlayerShip().getHullLevel() >= 0.9f) {
                display = "nominal";
            } else if (engine.getPlayerShip().getHullLevel() >= 0.5f) {
                display = "moderate";
            } else if (engine.getPlayerShip().getHullLevel() >= 0.1f) {
                display = "severe";
            } else {
                display = "error";
            }
            engine.maintainStatusForPlayerShip(this, "graphics/icons/hullsys/temporal_shell.png", "Spatial Anomaly", display, true);
        }

        interval.advance(amount);
        if (!interval.intervalElapsed()) {
            return;
        }

        List<ShipAPI> allShips = engine.getShips();
        for (ShipAPI ship : allShips) {
            if (ship.isShuttlePod() || ship.isLanding() || ship.isLiftingOff() || !ship.isAlive()) {
                continue;
            }

            double chancePerSec = SWP_Util.lerp(0.01f, 0.2f, 1f - ship.getHullLevel());
            double chancePerInterval = 1.0 - Math.pow(1.0 - chancePerSec, 0.2);
            if (Math.random() < chancePerInterval) {
                Vector2f startLoc = new Vector2f(ship.getLocation());
                Vector2f toLoc = null;
                findCollisions:
                for (int i = 0; i < 5; i++) {
                    toLoc = MathUtils.getPointOnCircumference(startLoc, (100f + ship.getCollisionRadius()) * MathUtils.getRandomNumberInRange(0.5f, 1.5f),
                            MathUtils.getRandomNumberInRange(-180f, 180f));
                    List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(toLoc, ship.getCollisionRadius());
                    for (ShipAPI nearbyShip : nearbyShips) {
                        if (nearbyShip == ship) {
                            continue;
                        }
                        if (nearbyShip.getCollisionClass() != CollisionClass.SHIP) {
                            continue;
                        }
                        toLoc = null;
                        continue findCollisions;
                    }
                    break;
                }
                if (toLoc == null) {
                    continue;
                }
                float level = ship.getMass();
                float sqrtLevel = (float) Math.sqrt(level);
                float sqrtLevel2 = (float) Math.sqrt(sqrtLevel);

                SWP_AnamorphicFlare.createStripFlare(ship, startLoc, engine, sqrtLevel / 20f, 1,
                        sqrtLevel / 20f, sqrtLevel / 5f, (float) Math.random() * 360f, 0f, 1f, SWP_PhaseShuntDriveStats.COLOR2, SWP_PhaseShuntDriveStats.COLOR3, true);

                RippleDistortion ripple = new RippleDistortion(startLoc, ZERO);
                ripple.setSize(sqrtLevel * 15f);
                ripple.setIntensity(sqrtLevel * 1f);
                ripple.setFrameRate(360f / sqrtLevel2);
                ripple.fadeInSize(sqrtLevel2 / 6f);
                ripple.fadeOutIntensity(sqrtLevel2 / 6f);
                DistortionShader.addDistortion(ripple);

                ShipAPI otherShip = allShips.get(MathUtils.getRandomNumberInRange(0, allShips.size() - 1));
                SWP_AnamorphicFlare.createStripFlare(otherShip, toLoc, engine, sqrtLevel / 20f, 1,
                        sqrtLevel / 20f, sqrtLevel / 5f, (float) Math.random() * 360f, 0f, 1f, SWP_PhaseShuntDriveStats.COLOR2, SWP_PhaseShuntDriveStats.COLOR3, true);

                ship.getLocation().set(toLoc);
                ship.setJitter(this, SWP_PhaseShuntDriveStats.COLOR1, 1f, 10, (float) Math.pow(ship.getCollisionRadius(), 0.25));

                float pitch = 0.75f;
                float vol = 0.25f + (sqrtLevel / 30f);
                if (ship.isFrigate()) {
                    pitch = 1f;
                    vol *= 0.75f;
                }
                if (ship.isFighter()) {
                    pitch = 1.25f;
                    vol *= 0.5f;
                }
                Global.getSoundPlayer().playSound("swp_phaseshuntdrive_impact", pitch, vol, startLoc, ZERO);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }
}
