/*
code by Xaiier
*/

package org.xhan.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class XHAN_BubbleShield extends BaseHullMod {

    private static final float FADE_TIME = 0.7f;
    private static final float BIAS = 0.2f;
    private static final float DURATION = 1f;
    private static final int INTENSITY = 20;

    private static final float ACTUAL_RADIUS = 256f / 239f;

    private ShieldState shieldState = null;

    public static boolean GRAPHICSLIB_LOADED = false;

    @Override
    public void init(HullModSpecAPI spec) {
        this.spec = spec;

        GRAPHICSLIB_LOADED = Global.getSettings().getModManager().isModEnabled("shaderLib");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine() == null || Global.getCombatEngine().isPaused()) {
            return;
        }

        shieldState = (ShieldState) ship.getCustomData().get("bubbleshield");

        ShieldAPI shield = ship.getShield();

        if (shield != null) {
            if (shield.isOn()) {
                if (shieldState == null) {
                    shieldState = new ShieldState(DURATION, ship.getHullSpec().getShieldSpec().getRadius());
                    shield.setActiveArc(360f);

                    Vector2f offset = new Vector2f();
                    offset = Vector2f.sub(ship.getShieldCenterEvenIfNoShield(), ship.getLocation(), offset);

                    Color rc = shield.getRingColor();
                    Color c = new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 255);

                    for (int i = 1; i < INTENSITY; i++) {
                        MagicRender.objectspace(Global.getSettings().getSprite("fx", "Xhan_shieldOUT"),
                                ship,
                                offset,
                                new Vector2f(),
                                new Vector2f(),
                                new Vector2f(ship.getHullSpec().getShieldSpec().getRadius() * 2f * ACTUAL_RADIUS / DURATION, ship.getHullSpec().getShieldSpec().getRadius() * 2f * ACTUAL_RADIUS / DURATION),
                                MathUtils.getRandomNumberInRange(0f, 360f),
                                0f,
                                false,
                                shield.getRingColor(),
                                true,
                                0f,
                                DURATION / i + BIAS,
                                FADE_TIME - BIAS,
                                true
                        );
                    }

                    if (GRAPHICSLIB_LOADED) {
                        XHAN_DistortionWrapper.addDistortion(ship, DURATION, FADE_TIME, ACTUAL_RADIUS);
                    }
                }
                if (GRAPHICSLIB_LOADED && ship.getCustomData().get("bubbleripple") != null) {
                    RippleDistortion ripple = (RippleDistortion) ship.getCustomData().get("bubbleripple");
                    ripple.setLocation(ship.getShieldCenterEvenIfNoShield());
                }
                shieldState.advance(amount);
                float radius = shieldState.onlineTimer * shieldState.shieldRadius;
                shield.setRadius(radius);

                ship.setCustomData("bubbleshield", shieldState);
            } else if (shield.isOff()) {
                ship.removeCustomData("bubbleshield");
                if (GRAPHICSLIB_LOADED) {
                    ship.removeCustomData("bubbleripple");
                }
            }
        }
    }

    private static final class ShieldState {

        float onlineTimer;
        float multTime;
        float shieldRadius;

        private ShieldState(float multTime, float shieldRadius) {
            this.onlineTimer = 0f;
            this.multTime = multTime;
            this.shieldRadius = shieldRadius;
        }

        public void advance(float amount) {
            onlineTimer += amount * multTime;
            if (onlineTimer > 1f) {
                onlineTimer = 1f;
            }
        }

    }
}
