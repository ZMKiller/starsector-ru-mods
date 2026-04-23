package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ExcelsiorPhaseStats extends BaseShipSystemScript {

    private static final float ARMOR_REPAIR_MULTIPLIER = 300.0f;
    private static final float HULL_REPAIR_MULTIPLIER = 100.0f;
    private static final float MAX_TIME_MULT_MAX = 4f;
    private static final float SHIP_ALPHA_MULT = 0.25f;
    private static final float SPARK_BRIGHTNESS = 0.9f;
    private static final Color SPARK_COLOR = new Color(255, 0, 200);
    private static final Color GLOW_COLOR = new Color(200, 0, 255, 75);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);
    private static final float SPARK_DURATION = 0.5f;
    private static final float SPARK_MAX_RADIUS = 5f;
    private static final Vector2f ZERO = new Vector2f(0f, 0f);

    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;

        return cellLoc;
    }

    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT_MAX - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();
    private final Object STATUSKEY3 = new Object();
    private final Object STATUSKEY4 = new Object();
    private final Object STATUSKEY5 = new Object();
    private final IntervalUtil syncInterval = new IntervalUtil(0.4f, 0.8f);
    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);
    private final IntervalUtil afterimageInterval = new IntervalUtil(0.033f, 0.033f);
    private final Random rand = new Random();

    private float killTimer = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship;
        boolean player;
        String actualId = id;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == engine.getPlayerShip();
            actualId = actualId + "_" + ship.getId();
        } else {
            return;
        }

        if (player) {
            maintainStatus(ship, effectLevel);
        }

        if (engine.isPaused()) {
            return;
        }

        if (state == State.COOLDOWN || state == State.IDLE) {
            unapply(stats, actualId);
            return;
        }

        float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);

        float fluxLevel = ship.getFluxTracker().getCurrFlux() / stats.getFluxCapacity().getBaseValue();

        if (ship == engine.getPlayerShip()) {
            engine.maintainStatusForPlayerShip(STATUSKEY1, "graphics/icons/hullsys/phase_cloak.png",
                    "Phase Drive", "Draining flux", true);

            boolean display3 = false;
            String displayStr3 = "Repairing ";
            ArmorGridAPI armorGrid = ship.getArmorGrid();
            OUT:
            for (int x = armorGrid.getGrid().length - 1; x >= 0; x--) {
                for (int y = armorGrid.getGrid()[x].length - 1; y >= 0; y--) {
                    if (armorGrid.getArmorValue(x, y) < armorGrid.getMaxArmorInCell()) {
                        displayStr3 += "armor";
                        display3 = true;
                        break OUT;
                    }
                }
            }

            if (ship.getHitpoints() < ship.getMaxHitpoints()) {
                if (display3) {
                    displayStr3 += "/";
                }
                displayStr3 += "hull";
                display3 = true;
            }

            if (display3) {
                engine.maintainStatusForPlayerShip(STATUSKEY3, "graphics/icons/hullsys/phase_cloak.png",
                        "Phase Drive", displayStr3, false);
            }
        }

        float levelForAlpha = effectLevel;
        ship.setPhased(true);

        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = Math.min(Math.max(1f + ((getMaxTimeMult(stats) - 1f) * effectLevel * Math.max(0f, fluxLevel)), 1f), getMaxTimeMult(stats));
        stats.getTimeMult().modifyMult(actualId, shipTimeMult);
        if (player) {
            engine.getTimeMult().modifyMult(actualId, 1f / shipTimeMult);
        } else {
            engine.getTimeMult().unmodify(actualId);
        }

        interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
        if (interval.intervalElapsed()) {
            ArmorGridAPI armorGrid = ship.getArmorGrid();
            int x = rand.nextInt(armorGrid.getGrid().length);
            int y = rand.nextInt(armorGrid.getGrid()[0].length);
            float newArmor = armorGrid.getArmorValue(x, y);
            float cellSize = armorGrid.getCellSize();

            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
                newArmor += ARMOR_REPAIR_MULTIPLIER * interval.getIntervalDuration() * fluxLevel;
                armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));

                Vector2f cellLoc = getCellLocation(ship, x, y);
                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
                engine.addHitParticle(cellLoc, ship.getVelocity(), SPARK_MAX_RADIUS * (float) Math.random()
                        + SPARK_MAX_RADIUS, SPARK_BRIGHTNESS, SPARK_DURATION,
                        SPARK_COLOR);
            }

            ship.setHitpoints(Math.min(ship.getHitpoints() + interval.getIntervalDuration() * HULL_REPAIR_MULTIPLIER * fluxLevel, ship.getMaxHitpoints()));
        }

        syncInterval.advance(engine.getElapsedInLastFrame());
        if (syncInterval.intervalElapsed()) {
            ship.syncWithArmorGridState();
            ship.syncWeaponDecalsWithArmorDamage();

            if ((ship.getHitpoints() / ship.getMaxHitpoints()) >= 0.9f) {
                ArmorGridAPI armorGrid = ship.getArmorGrid();
                float totalArmor = 0f;
                float currArmor = 0f;
                for (int x = armorGrid.getGrid().length - 1; x >= 0; x--) {
                    for (int y = armorGrid.getGrid()[x].length - 1; y >= 0; y--) {
                        totalArmor += armorGrid.getMaxArmorInCell();
                        currArmor += armorGrid.getArmorValue(x, y);
                    }
                }
                if ((currArmor / totalArmor) >= 0.9f) {
                    ship.clearDamageDecals();
                }
            }
        }

        killTimer -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();
        if ((ship.getFluxTracker().getCurrFlux() < 100f) && (killTimer <= 0f) && (state == State.ACTIVE)) {
            ship.getFluxTracker().setCurrFlux(100f);
            killTimer = 0.5f;
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(actualId);
            stats.getMaxTurnRate().unmodify(actualId);
        } else {
            float level = (1f + fluxLevel * 0.5f) * effectLevel;
            stats.getMaxSpeed().modifyFlat(actualId, 50f * level);
            stats.getMaxSpeed().modifyPercent(actualId, speedPercentMod * effectLevel);
            stats.getMaxTurnRate().modifyPercent(actualId, 50f * level);
            stats.getAcceleration().modifyFlat(actualId, 200f * level);
            stats.getDeceleration().modifyFlat(actualId, 200f * level);
            stats.getTurnAcceleration().modifyPercent(actualId, 75f * level);
            if (ship == engine.getPlayerShip()) {
                engine.maintainStatusForPlayerShip(STATUSKEY4, "graphics/icons/hullsys/phase_cloak.png",
                        "Phase Drive", "+" + (int) (50f * level) + " speed", false);
                engine.maintainStatusForPlayerShip(STATUSKEY5, "graphics/icons/hullsys/phase_cloak.png",
                        "Phase Drive", "+" + (int) (125f * level) + "% maneuverability",
                        false);
            }
            if (ship.getFluxTracker().getCurrFlux() <= 50f) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
            afterimageInterval.advance(engine.getElapsedInLastFrame() * shipTimeMult);
            if (afterimageInterval.intervalElapsed()) {
                float randRange = 15f * level;
                float randAngle = MathUtils.getRandomNumberInRange(0f, 360f);
                float randRadiusFrac = (float) (Math.random() + Math.random());
                randRadiusFrac = (randRadiusFrac > 1f ? 2f - randRadiusFrac : randRadiusFrac);
                Vector2f randLoc = MathUtils.getPointOnCircumference(ZERO, randRange * randRadiusFrac, randAngle);
                ship.addAfterimage(GLOW_COLOR, randLoc.x, randLoc.y, 0f, 0f,
                        randRange, 0f, 0f, 0.1f, true, false, false);

                Color afterimageColor = SWP_Util.fadeColor255(SPARK_COLOR, fluxLevel);
                ship.addAfterimage(SHADOW_COLOR, 0f, 0f, 0f, 0f,
                        0, 0.033f, 0f, 0.033f, false, false, false);
                ship.addAfterimage(afterimageColor, 0f, 0f, -ship.getVelocity().x, -ship.getVelocity().y,
                        0, 0f, 0f, 0.1f * level, true, false, false);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return (ship.getFluxTracker().getCurrFlux() > 125f) || ship.getPhaseCloak().isActive();
    }

    private void maintainStatus(ShipAPI playerShip, float effectLevel) {
        ShipSystemAPI cloak = playerShip.getPhaseCloak();
        if (cloak == null) {
            cloak = playerShip.getSystem();
        }
        if (cloak == null) {
            return;
        }

        if (effectLevel > 0f) {
            Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
                    cloak.getSpecAPI().getIconSpriteName(),
                    cloak.getDisplayName(), "time flow altered", false);
        }
    }
}
