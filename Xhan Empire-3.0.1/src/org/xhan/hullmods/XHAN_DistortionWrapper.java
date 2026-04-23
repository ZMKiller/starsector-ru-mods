/*
code by Xaiier

this class exists because Java will not allow unloaded methods that use other mod's parameters (LightAPI in this case) to exist within a class, even if the logical path would never actually call said method
this allows for optional support of GraphicsLib
infinite thanks to SafariJohn for working out this issue
*/

package org.xhan.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lwjgl.util.vector.Vector2f;

class XHAN_DistortionWrapper {
    static void addDistortion(ShipAPI ship, float DURATION, float FADE_TIME, float ACTUAL_RADIUS) {
        RippleDistortion ripple = new RippleDistortion(ship.getShieldCenterEvenIfNoShield(), new Vector2f());

        ripple.setFrameRate(60f / (DURATION + FADE_TIME));

        ripple.setSize(ship.getHullSpec().getShieldSpec().getRadius() * 2f * ACTUAL_RADIUS / DURATION);
        ripple.fadeInSize((DURATION + FADE_TIME));
        ripple.setSize(0f);


        ripple.setIntensity(10f);
        ripple.fadeInIntensity((DURATION + FADE_TIME));
        ripple.setIntensity(0f);

        DistortionShader.addDistortion(ripple);

        ship.setCustomData("bubbleripple", ripple);
    }
}

