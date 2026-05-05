package data.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;

import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSITimeWarpRendererObject;

public class HSITimeWarp extends BaseShipSystemScript {
    protected ShipAPI ship;
    private static final float TIME_WARP = 4f;
    private static final float EX_SHIELD = 0.1f;
    public static final Color JITTER_COLOR = new Color(25, 171, 224, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(25, 171, 224, 55);
    private static final int BLINK_BUFF = 3;
    private static final float BUFF = 100f;
    private boolean once = false;
    private boolean objectAdded = true;
    private IntervalUtil afterImageTest = new IntervalUtil(0.15f, 0.15f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        // if (!ship.isPhased())
        // ship.setPhased(true);
        float jitterLevel = (float) (Math.pow(effectLevel, 2));
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 1, 0, jitterLevel * 3f);
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 4f + jitterLevel * 3f);
        // HSICombatRenderer renderer = HSICombatRenderer.getInstance(ship);
        // renderer.setBlockShieldRender(true);

        if (objectAdded) {
            HSICombatRendererV2 renderer = HSICombatRendererV2.getInstance();
            renderer.addFxObject(new HSITimeWarpRendererObject(ship));
            objectAdded = false;
        }

        // String spriteId = "";
        /*
         * switch (ship.getHullSpec().getBaseHullId()) {
         * case "HSI_Promise":
         * spriteId = "HSI_Promise_Effect_";
         * if (effectLevel * 7 <= 1) {
         * renderer.requestFX("HSI_Decoration", spriteId + "1", 0.5f * jitterLevel * 7);
         * } else if (effectLevel * 7 >= 6) {
         * renderer.requestFX("HSI_Decoration", spriteId + "7", 0.5f * (1 - jitterLevel)
         * * 7);
         * } else {
         * int index = (int) (effectLevel * 7);
         * renderer.requestFX("HSI_Decoration", spriteId + index, 0.5f * (index + 1 -
         * jitterLevel * 7f));
         * renderer.requestFX("HSI_Decoration", spriteId + (index + 1), 0.5f *
         * (jitterLevel * 7f - index));
         * }
         * break;
         * case "HSI_Oath":
         * spriteId = "HSI_Oath_Effect_";
         * if (effectLevel * 8 <= 1) {
         * renderer.requestFX("HSI_Decoration", spriteId + "1", 0.5f * jitterLevel * 7);
         * } else if (effectLevel * 8 >= 7) {
         * renderer.requestFX("HSI_Decoration", spriteId + "8", 0.5f * (1 - jitterLevel)
         * * 7);
         * } else {
         * int index = (int) (effectLevel * 8);
         * renderer.requestFX("HSI_Decoration", spriteId + index, 0.5f * (index + 1 -
         * jitterLevel * 8f));
         * renderer.requestFX("HSI_Decoration", spriteId + (index + 1), 0.5f *
         * (jitterLevel * 8f - index));
         * }
         * break;
         * default:
         * break;
         * }
         */

        ship.getMutableStats().getAcceleration().modifyPercent(id, effectLevel * BUFF);
        ship.getMutableStats().getMaxSpeed().modifyFlat(id, effectLevel * BUFF);
        ship.getMutableStats().getMaxTurnRate().modifyPercent(id, 3 * effectLevel * BUFF);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 3 * effectLevel * BUFF);
        ship.getMutableStats().getDeceleration().modifyPercent(id, effectLevel * BUFF);
        ship.getMutableStats().getTimeMult().modifyMult(id, 1 + effectLevel * TIME_WARP);
        ship.getMutableStats().getPeakCRDuration().modifyFlat(id + Global.getCombatEngine().getTotalElapsedTime(true),
                effectLevel * TIME_WARP * Global.getCombatEngine().getElapsedInLastFrame());
        if (Global.getCombatEngine().getPlayerShip().equals(ship)) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f/(1f + effectLevel * TIME_WARP));
        }
        if (effectLevel >= 1 && !once) {
            /*
             * if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
             * HSITurbulanceShieldListenerV2 shield =
             * ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
             * shield.addExtraShield(shield.getShield().getShieldCap() * EX_SHIELD);
             * }
             */
            if (ship.getHullSize().compareTo(HullSize.FRIGATE) > 0) {
                Vector2f vel = new Vector2f(ship.getVelocity());
                ship.getLocation().set(Vector2f.add(ship.getLocation(), (Vector2f) (vel.scale(BLINK_BUFF)), null));
            }
            ShipAPI closestEnemy = ship.getShipTarget();
            if (closestEnemy == null) {
                closestEnemy = AIUtils.getNearestEnemy(ship);
            }

            // ship.getVelocity().scale(BLINK_BUFF);
            // ship.setFacing(ship.);
            once = true;
        }

        if (effectLevel > 0) {
            afterImageTest.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (afterImageTest.intervalElapsed()) {
                ship.addAfterimage(JITTER_UNDER_COLOR, 0, 0, ship.getVelocity().getX() * (-1),
                        ship.getVelocity().getY() * (-1), 1f, 0.1f, 0.3f, 0.1f, false, true, false);
            }
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        // HSICombatRendererV2 renderer = HSICombatRendererV2.getInstance();
        // if (ship.isPhased())
        // ship.setPhased(false);
        ship.getMutableStats().getAcceleration().unmodify(id);
        ship.getMutableStats().getMaxSpeed().unmodify(id);
        ship.getMutableStats().getMaxTurnRate().unmodify(id);
        ship.getMutableStats().getTurnAcceleration().unmodify(id);
        ship.getMutableStats().getDeceleration().unmodify(id);
        ship.getMutableStats().getTimeMult().unmodify(id);
        once = false;
        objectAdded = true;
    }
}