package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ZeroAnnihilator implements EveryFrameWeaponEffectPlugin {

    private static final Color COLOR1 = new Color(100, 150, 255);
    private static final Color COLOR2 = new Color(150, 200, 255);
    private static final Color HYPER_COLOR1 = new Color(150, 150, 150);
    private static final Color HYPER_COLOR2 = new Color(255, 255, 255);
    private static final Color SUPER_COLOR1 = new Color(255, 100, 100);
    private static final Color SUPER_COLOR2 = new Color(255, 200, 200);
    private static final Color SUPER_COLOR3 = new Color(255, 50, 50);
    private static final Vector2f ZERO = new Vector2f();

    private boolean charging = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.04f, 0.04f);
    private final IntervalUtil interval2 = new IntervalUtil(0.015f, 0.015f);
    private Vector2f novaLocation = null;
    private float novaTime = -1f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());

        if (firing) {
            if (weapon.getChargeLevel() >= 1f) {
                interval2.advance(amount);
                if (interval2.intervalElapsed()) {
                    switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 100f + 200f, 0.2f, 0.2f,
                                    HYPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 50f + 150f, 0.2f, 0.2f,
                                    HYPER_COLOR2);
                            break;
                        case "swp_arcade_superzero":
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 100f + 200f, 0.2f, 0.2f,
                                    SUPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 50f + 150f, 0.2f, 0.2f,
                                    SUPER_COLOR2);
                            break;
                        default:
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 100f + 200f, 0.2f, 0.2f, COLOR1);
                            engine.addHitParticle(origin, ZERO, (float) Math.random() * 50f + 150f, 0.2f, 0.2f, COLOR2);
                    }
                }
            } else if (weapon.getCooldownRemaining() <= 0f) {
                firing = false;
            } else if (charging) {
                charging = false;
                if (weapon.getShip().getHullLevel() <= 0.33f) {
                    novaLocation = origin;
                    novaTime = 0f;
                    switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            engine.addHitParticle(origin, ZERO, 500f, 1f, 0.3f, HYPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, 300f, 1f, 0.3f, HYPER_COLOR2);
                            engine.spawnExplosion(origin, ZERO, HYPER_COLOR2, 1000f, 0.15f);
                            break;
                        case "swp_arcade_superzero":
                            engine.addHitParticle(origin, ZERO, 500f, 1f, 0.3f, SUPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, 300f, 1f, 0.3f, SUPER_COLOR2);
                            engine.spawnExplosion(origin, ZERO, SUPER_COLOR2, 1000f, 0.15f);
                            break;
                        default:
                            engine.addHitParticle(origin, ZERO, 500f, 1f, 0.3f, COLOR1);
                            engine.addHitParticle(origin, ZERO, 300f, 1f, 0.3f, COLOR2);
                            engine.spawnExplosion(origin, ZERO, COLOR2, 1000f, 0.15f);
                    }
                    Global.getSoundPlayer().playSound("swp_arcade_zeroannihilator_blast", 1f, 2f, origin, ZERO);
                }
            }
        } else {
            if (weapon.getChargeLevel() >= 1f) {
                firing = true;
                Global.getSoundPlayer().playSound("swp_arcade_zeroannihilator_fire", 1f, 2f, origin, ZERO);
                switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                    case "swp_arcade_hyperzero":
                        engine.addHitParticle(origin, ZERO, 750f, 5f, 0.5f, HYPER_COLOR1);
                        engine.addHitParticle(origin, ZERO, 200f, 5f, 0.25f, HYPER_COLOR2);
                        break;
                    case "swp_arcade_superzero":
                        engine.addHitParticle(origin, ZERO, 750f, 5f, 0.5f, SUPER_COLOR1);
                        engine.addHitParticle(origin, ZERO, 200f, 5f, 0.25f, SUPER_COLOR2);
                        break;
                    default:
                        engine.addHitParticle(origin, ZERO, 750f, 5f, 0.5f, COLOR1);
                        engine.addHitParticle(origin, ZERO, 200f, 5f, 0.25f, COLOR2);
                }
            } else if (weapon.getChargeLevel() > 0f) {
                if (!charging) {
                    charging = true;
                    Global.getSoundPlayer().playSound("swp_arcade_zeroannihilator_charge", 1f, 2f, origin, ZERO);
                }

                interval2.advance(amount);
                if (interval2.intervalElapsed()) {
                    switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 150f + 50f, 5f, 0.035f,
                                    HYPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 100f + 25f, 1f, 0.035f,
                                    HYPER_COLOR2);
                            break;
                        case "swp_arcade_superzero":
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 150f + 50f, 5f, 0.035f,
                                    SUPER_COLOR1);
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 100f + 25f, 1f, 0.035f,
                                    SUPER_COLOR2);
                            break;
                        default:
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 150f + 50f, 5f, 0.035f,
                                    COLOR1);
                            engine.addHitParticle(origin, ZERO, weapon.getChargeLevel() * 100f + 25f, 1f, 0.035f,
                                    COLOR2);
                    }
                }
            }
        }

        if (novaTime >= 0f) {
            novaTime += amount;
            interval.advance(amount);

            float boost;
            switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                case "swp_arcade_hyperzero":
                    boost = 2f;
                    break;
                case "swp_arcade_superzero":
                    boost = 1.5f;
                    break;
                default:
                    boost = 1f;
            }
            if (interval.intervalElapsed()) {
                float offset = (float) Math.random() * 360f;
                for (int i = 0; i < (int) (novaTime * 5f * boost) + 4; i++) {
                    float angle = i / ((novaTime * 5f * boost) + 4f) * 360f + offset;
                    if (angle >= 360f) {
                        angle -= 360f;
                    }
                    float distance = (float) Math.random() * 50f + novaTime * 750f * boost;
                    Vector2f point1 = MathUtils.getPointOnCircumference(novaLocation, distance, angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(novaLocation, distance, angle + 360f
                            / ((novaTime * 5f * boost) + 4f)
                            * ((float) Math.random() + 1f));
                    switch (weapon.getShip().getHullSpec().getBaseHullId()) {
                        case "swp_arcade_superzero":
                            engine.spawnEmpArc(weapon.getShip(), point1, new SimpleEntity(point1), new SimpleEntity(
                                    point2), DamageType.ENERGY, 0f, 0f, 10000f,
                                    null, 40f, SUPER_COLOR3, SUPER_COLOR1);
                            break;
                        case "swp_arcade_hyperzero":
                            engine.spawnEmpArc(weapon.getShip(), point1, new SimpleEntity(point1), new SimpleEntity(
                                    point2), DamageType.ENERGY, 0f, 0f, 10000f,
                                    null, 40f, HYPER_COLOR1, HYPER_COLOR2);
                            break;
                        default:
                            engine.spawnEmpArc(weapon.getShip(), point1, new SimpleEntity(point1), new SimpleEntity(
                                    point2), DamageType.ENERGY, 0f, 0f, 10000f,
                                    null, 40f, COLOR1, COLOR2);
                    }
                }

                List<ShipAPI> targets = SWP_Util.getShipsWithinRange(novaLocation, novaTime * 750f * boost + 25f);
                for (ShipAPI target : targets) {
                    if (target == weapon.getShip()) {
                        continue;
                    }

                    float dist = MathUtils.getDistance(novaLocation, target.getLocation());
                    float dist2 = novaTime * 750f * boost + 25f;
                    if (dist - (target.getCollisionRadius() + 50f * boost) <= dist2 && (target.getCollisionRadius()
                            + 50f * boost) >= dist2) {
                        if (target.getOwner() == weapon.getShip().getOwner()) {
                            engine.applyDamage(target, MathUtils.getRandomPointInCircle(target.getLocation(),
                                    target.getCollisionRadius()),
                                    400f * boost * boost,
                                    DamageType.ENERGY, 200f, false, false, weapon.getShip(), false);
                        } else {
                            engine.applyDamage(target, MathUtils.getRandomPointInCircle(target.getLocation(),
                                    target.getCollisionRadius()),
                                    8000f * boost * boost,
                                    DamageType.ENERGY, 4000f, false, false, weapon.getShip(), false);
                        }
                    }
                }
            }

            if (novaTime >= 2.5f) {
                novaTime = -1f;
            }
        }
    }
}
