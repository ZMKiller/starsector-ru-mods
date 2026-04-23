package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ZeroBeam implements EveryFrameWeaponEffectPlugin {

    private static final Color COLOR1 = new Color(255, 0, 0);
    private static final Color COLOR2 = new Color(255, 255, 255);
    private static final Color COLOR3 = new Color(255, 25, 25);
    private static final Vector2f ZERO = new Vector2f();

    private boolean charging = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());
        ShipAPI ship = weapon.getShip();

        if (firing) {
            if (weapon.getChargeLevel() >= 1f) {
                ShipAPI target = engine.getPlayerShip();
                if (ship != engine.getPlayerShip() && target != null) {
                    faceTarget(ship, target);
                }

                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    engine.addHitParticle(origin, ZERO, (float) Math.random() * 250f + 350f, 0.2f, 0.2f, COLOR1);
                    engine.addHitParticle(origin, ZERO, (float) Math.random() * 150f + 250f, 0.2f, 0.2f, COLOR2);
                    int amountscalar;
                    switch (ship.getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            amountscalar = 3;
                            break;
                        case "swp_arcade_superzero":
                            amountscalar = 2;
                            break;
                        default:
                            amountscalar = 1;
                    }
                    for (int i = 0; i < 1 * amountscalar; i++) {
                        if (Math.random() > 0.25) {
                            float angle = (float) Math.random() * 360f;
                            float distance = (float) Math.random() * 150f + 50f;
                            switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                                case "swp_arcade_hyperzero":
                                    distance *= 2f;
                                    break;
                                case "swp_arcade_superzero":
                                    distance *= 1.5f;
                                    break;
                                default:
                            }
                            Vector2f point1 = MathUtils.getPointOnCircumference(origin, distance, angle);
                            Vector2f point2 = MathUtils.getPointOnCircumference(origin, distance, angle + 45f
                                    * (float) Math.random());
                            engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2),
                                    DamageType.ENERGY, 0f, 0f, 1000f, null, 40f,
                                    COLOR1, COLOR2);
                        }
                    }
                }
            } else if (weapon.getCooldownRemaining() <= 0f) {
                firing = false;
                charging = false;
            }
        } else {
            if (weapon.getChargeLevel() >= 1f) {
                firing = true;
                Global.getSoundPlayer().playSound("swp_arcade_zerobeam_fire", 1f, 0.75f, origin, ZERO);
                engine.addHitParticle(origin, ZERO, 1000f, 5f, 0.5f, COLOR3);
                engine.addHitParticle(origin, ZERO, 500f, 5f, 0.2f, COLOR2);
            } else if (weapon.getChargeLevel() > 0f) {
                if (!charging) {
                    charging = true;
                    Global.getSoundPlayer().playSound("swp_arcade_zerobeam_charge", 1f, 0.6f, origin, ZERO);
                }
                ShipAPI target = engine.getPlayerShip();
                if (ship != engine.getPlayerShip() && target != null) {
                    faceTarget(ship, target);
                }

                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    engine.addHitParticle(origin, ZERO, (float) Math.random() * weapon.getChargeLevel() * 225f + 75f, 5f,
                            0.035f, COLOR3);
                    engine.addHitParticle(origin, ZERO, (float) Math.random() * weapon.getChargeLevel() * 125f + 50f, 1f,
                            0.035f, COLOR2);
                    if (Math.random() > 0.25) {
                        float distmod;
                        switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                            case "swp_arcade_hyperzero":
                                distmod = 2f;
                                break;
                            case "swp_arcade_superzero":
                                distmod = 1.5f;
                                break;
                            default:
                                distmod = 1f;
                        }
                        Vector2f point1 = MathUtils.getRandomPointInCircle(origin, (float) Math.random()
                                * weapon.getChargeLevel() * 250f * distmod);
                        engine.spawnEmpArc(ship, origin, new SimpleEntity(origin), new SimpleEntity(point1),
                                DamageType.ENERGY, 0f, 0f, 1000f, null,
                                weapon.getChargeLevel() * 20f + 10f, COLOR1, COLOR2);
                    }
                }
            }
        }
    }

    private void faceTarget(ShipAPI ship, ShipAPI target) {
        float angle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
        ship.setFacing(angle);
    }
}
