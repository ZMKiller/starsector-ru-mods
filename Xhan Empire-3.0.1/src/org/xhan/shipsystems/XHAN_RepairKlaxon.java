/*
code by Xaiier

this class exists because Java will not allow unloaded methods that use other mod's parameters (LightAPI in this case) to exist within a class, even if the logical path would never actually call said method
this allows for optional support of GraphicsLib
*/

package org.xhan.shipsystems;

import com.fs.starfarer.api.combat.ShipAPI;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

class XHAN_RepairKlaxon {
    private static final float LIGHT_INTENSITY = 2f;
    private static final float LIGHT_SIZE = 64f;

    private final StandardLight lightA;
    private final StandardLight lightB;
    private float rot = 0f;

    public XHAN_RepairKlaxon(ShipAPI attach, Vector2f offset) {
        lightA = new StandardLight(new Vector2f(),
                new Vector2f(),
                offset,
                attach,
                LIGHT_INTENSITY,
                LIGHT_SIZE
        );
        lightA.setColor(XHAN_EmergencyRepairs.KLAXON_COLOR);
        lightA.setLifetime(XHAN_EmergencyRepairs.KLAXON_LIFETIME);
        lightA.setType(2); //needed for arc to work
        LightShader.addLight(lightA);

        lightB = new StandardLight(new Vector2f(),
                new Vector2f(),
                offset,
                attach,
                LIGHT_INTENSITY,
                LIGHT_SIZE
        );
        lightB.setColor(XHAN_EmergencyRepairs.KLAXON_COLOR);
        lightB.setLifetime(XHAN_EmergencyRepairs.KLAXON_LIFETIME);
        lightB.setType(2); //needed for arc to work
        LightShader.addLight(lightB);

        rot = MathUtils.getRandomNumberInRange(0f, 360f); //random start
    }

    public void Update() {
        rot += XHAN_EmergencyRepairs.KLAXON_ROT_SPEED;
        lightA.setArc(rot - XHAN_EmergencyRepairs.KLAXON_ARC / 2, rot + XHAN_EmergencyRepairs.KLAXON_ARC / 2);
        lightB.setArc((rot + 180) - XHAN_EmergencyRepairs.KLAXON_ARC / 2, (rot + 180) + XHAN_EmergencyRepairs.KLAXON_ARC / 2);
    }
}
