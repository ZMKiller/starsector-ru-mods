/*
code by Xaiier
*/

package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class XHAN_TorchDriveStats extends BaseShipSystemScript {

    private static final float SPEED_BOOST = 220f;
    private static final float ACCEL_BOOST = 100f;

    public static final Color ENGINE_COLOR = new Color(255, 63, 0);
    public static final Vector2f ENGINE_OFFSET = new Vector2f(-300f, 0f);
    public static final float ENGINE_LIGHT_INTENSITY = 2f;
    public static final float ENGINE_LIGHT_SIZE = 100f;

    private static final Vector2f EXTRA_FLARE_SIZE = new Vector2f(150f, 150f);
    private static final Color EXTRA_FLARE_COLOR = Color.white; //alpha scaled based on effect level

    private static final int EXTRA_SMOKE_COUNT = 10; //particles per frame
    private static final Vector2f PARTICLE_START_SIZE = new Vector2f(10f, 10f);
    private static final float PARTICLE_MIN_GROWTH = 50f;
    private static final float PARTICLE_MAX_GROWTH = 100f;
    private static final float PARTICLE_MIN_VEL = 25f;
    private static final float PARTICLE_MAX_VEL = 50f;
    private static final Color PARTICLE_COLOR = new Color(50, 50, 50);
    private static final int PARTICLE_MIN_ALPHA = 5;
    private static final int PARTICLE_MAX_ALPHA = 15;
    private static final float PARTICLE_FADE_IN = 0.3f;
    private static final float PARTICLE_MIN_TIME = 1f;
    private static final float PARTICLE_MAX_TIME = 3f;

    private boolean lightNeedsToBeSpawned = true;

    public static boolean GRAPHICSLIB_LOADED = false;

    public XHAN_TorchDriveStats() {
        GRAPHICSLIB_LOADED = Global.getSettings().getModManager().isModEnabled("shaderLib");
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //not sure why we need to check for the ship this way, but this is how vanilla does it
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        //engine glow
        if (GRAPHICSLIB_LOADED && state == State.IN && lightNeedsToBeSpawned) {
            lightNeedsToBeSpawned = false;
            XHAN_EngineLightWrapper.addLight(ship);
        }

        ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(0, 0, 0, 0), effectLevel, 1f);

        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {
            //draw the extra flare
            for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                if (!engine.isDisabled()) {
                    SpriteAPI flare = Global.getSettings().getSprite("fx", "Xhan_flare");
                    MagicRender.singleframe(flare, engine.getLocation(), EXTRA_FLARE_SIZE, ship.getFacing(), new Color(EXTRA_FLARE_COLOR.getRed(), EXTRA_FLARE_COLOR.getGreen(), EXTRA_FLARE_COLOR.getBlue(), (int) (255 * effectLevel)), true, CombatEngineLayers.ABOVE_SHIPS_LAYER);
                }
            }

            //draw extra smoke
            for (ShipEngineControllerAPI.ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
                for (int x = 0; x < (Math.round(EXTRA_SMOKE_COUNT * effectLevel)); x++) {
                    SpriteAPI sprite = Global.getSettings().getSprite("misc", "nebula_particles");
                    float growth = MathUtils.getRandomNumberInRange(PARTICLE_MIN_GROWTH, PARTICLE_MAX_GROWTH);
                    MagicRender.battlespace(sprite,
                            engine.getLocation(),
                            VectorUtils.rotate(new Vector2f(MathUtils.getRandomNumberInRange(PARTICLE_MIN_VEL, PARTICLE_MAX_VEL), 0f), ship.getFacing() + 180f),
                            PARTICLE_START_SIZE,
                            new Vector2f(growth, growth),
                            MathUtils.getRandomNumberInRange(0f, 360f),
                            0f,
                            new Color(PARTICLE_COLOR.getRed(), PARTICLE_COLOR.getGreen(), PARTICLE_COLOR.getBlue(), MathUtils.getRandomNumberInRange(PARTICLE_MIN_ALPHA, PARTICLE_MAX_ALPHA)),
                            true,
                            PARTICLE_FADE_IN,
                            0f,
                            MathUtils.getRandomNumberInRange(PARTICLE_MIN_TIME, PARTICLE_MAX_TIME)
                    );
                }
            }
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            lightNeedsToBeSpawned = true;
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
            stats.getAcceleration().modifyFlat(id, ACCEL_BOOST * effectLevel);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }
}
