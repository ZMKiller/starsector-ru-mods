/*
code by Xaiier

this class exists because Java will not allow unloaded methods that use other mod's parameters (LightAPI in this case) to exist within a class, even if the logical path would never actually call said method
this allows for optional support of GraphicsLib
infinite thanks to SafariJohn for working out this issue
*/

package org.xhan.shipsystems;

import com.fs.starfarer.api.combat.ShipAPI;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

class XHAN_EngineLightWrapper {
    static void addLight(ShipAPI ship) {
        StandardLight light = new StandardLight(ship.getLocation(),
                new Vector2f(),
                XHAN_TorchDriveStats.ENGINE_OFFSET,
                ship,
                XHAN_TorchDriveStats.ENGINE_LIGHT_INTENSITY,
                XHAN_TorchDriveStats.ENGINE_LIGHT_SIZE
        );
        light.setColor(XHAN_TorchDriveStats.ENGINE_COLOR);
        light.setLifetime(ship.getSystem().getSpecAPI().getActive());
        light.fadeIn(ship.getSystem().getSpecAPI().getIn());
        light.setAutoFadeOutTime(ship.getSystem().getSpecAPI().getOut());
        LightShader.addLight(light);
    }
}

