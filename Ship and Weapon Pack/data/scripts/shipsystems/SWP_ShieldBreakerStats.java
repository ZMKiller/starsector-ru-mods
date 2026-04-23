package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ShieldBreakerStats extends BaseShipSystemScript {

    private static final Color COLOR1 = new Color(225, 200, 255);
    private static final Color COLOR2 = new Color(150, 100, 200);
    private static final Vector2f ZERO = new Vector2f();

    public boolean activated = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (state == State.IN) {
            if (activated) {
                Global.getSoundPlayer().playSound("swp_arcade_shieldbreaker_charge", 1f, 2f, ship.getLocation(), ship.getVelocity());
                activated = false;
            }
        }

        if (state == State.OUT) {
            if (!activated) {
                StandardLight light = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
                light.setIntensity(3f);
                light.setSize(2500f);
                light.setColor(COLOR1);
                light.fadeOut(2f);
                LightShader.addLight(light);

                float shipRadius = SWP_Util.effectiveRadius(ship);

                boolean didAnything = false;
                List<ShipAPI> targets = SWP_Util.getShipsWithinRange(ship.getLocation(), 1500f);
                for (ShipAPI target : targets) {
                    if (!target.isAlive() || (target.getShield() == null) || (target == ship)
                            || ((target.getOwner() == ship.getOwner()) && (target.isFighter() || target.isDrone()))) {
                        continue;
                    }

                    didAnything = true;
                    for (int i = 0; i < 7; i++) {
                        Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius),
                                ship, target, DamageType.ENERGY, 500f, 4000f, 100000f, null, 20f, COLOR2, COLOR1);
                    }

                    if (target.getShield().isOn()) {
                        target.getFluxTracker().increaseFlux((2000f - MathUtils.getDistance(target, ship)) * 10f, true);
                    }

                    SWP_Util.applyForce(target, VectorUtils.getDirectionalVector(target.getLocation(), ship.getLocation()), 1500f);
                }

                if (didAnything) {
                    Global.getSoundPlayer().playSound("swp_arcade_shieldbreaker_blast", 1f, 2f, ship.getLocation(), ship.getVelocity());
                }
            }
            activated = true;
        }
    }
}
