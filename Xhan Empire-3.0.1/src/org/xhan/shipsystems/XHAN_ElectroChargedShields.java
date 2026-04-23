package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_ElectroChargedShields extends BaseShipSystemScript {
    private static final float SHIELD_BONUS_PERCENT = 25f;
    private static final float SHIELD_UNFOLD_SPEED_MULT = 2f;
    private static final float SHIELD_ARC_LENGTH_MULT = 1f;
    private static final Color SHIELD_CHARGED_COLOUR = new Color(157, 0, 255, 150);
    private static final Color SHIELD_CHARGED_RING_COLOUR = new Color(255, 212, 235, 255);
    private static final float ARC_MIN_INTERVAL = 0.1f;
    private static final float ARC_MAX_INTERVAL = 0.3f;
    private static final Color ARC_INNER_COLOUR = new Color(255, 218, 223, 255);
    private static final Color ARC_FRINGE_COLOUR = new Color(157, 0, 255, 135);
    private static final float ARC_BEAM_THICKNESS = 15f;
    private static final float ARC_MAX_RANGE = 550f;
    private static final float ARC_DAMAGE = 100f;

    private final IntervalUtil arcTracker = new IntervalUtil(ARC_MIN_INTERVAL, ARC_MAX_INTERVAL);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getShieldDamageTakenMult().modifyMult(id, effectLevel * (1f - SHIELD_BONUS_PERCENT * 0.01f));
        stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_UNFOLD_SPEED_MULT);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) {
            return;
        }
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null || !ship.isAlive()) {
            return;
        }
        if (ship.getShield() == null) {
            return;
        }
        ShieldAPI shield = ship.getShield();
        shield.toggleOn();
        if (shield.isOff()) {
            ship.getSystem().deactivate();
        }

        shield.setInnerColor(SHIELD_CHARGED_COLOUR);
        shield.setRingColor(SHIELD_CHARGED_RING_COLOUR);
        shield.setArc(SHIELD_ARC_LENGTH_MULT * ship.getHullSpec().getShieldSpec().getArc());

        arcTracker.advance(engine.getElapsedInLastFrame() * effectLevel);
        if (arcTracker.intervalElapsed()) {
            boolean arced = false;

            for (MissileAPI missile : engine.getMissiles()) {
                if (MathUtils.getDistance(missile, ship) < ARC_MAX_RANGE) {
                    if (Misc.isInArc(shield.getFacing(), shield.getActiveArc(), ship.getLocation(), missile.getLocation())) {
                        engine.spawnEmpArc(
                                ship,
                                MathUtils.getPointOnCircumference(ship.getLocation(), ship.getShieldRadiusEvenIfNoShield(), VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), missile.getLocation()))),
                                ship,
                                missile,
                                DamageType.ENERGY,
                                ARC_DAMAGE,
                                200f,
                                ARC_MAX_RANGE,
                                "tachyon_lance_emp_impact",
                                ARC_BEAM_THICKNESS,
                                ARC_FRINGE_COLOUR,
                                ARC_INNER_COLOUR
                        );

                        arced = true;
                        break;
                    }
                }
            }
            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, ARC_MAX_RANGE)) {
                if (Misc.isInArc(shield.getFacing(), shield.getActiveArc(), ship.getLocation(), enemy.getLocation())) {
                    engine.spawnEmpArc(
                            ship,
                            MathUtils.getPointOnCircumference(ship.getLocation(), ship.getShieldRadiusEvenIfNoShield(), VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), enemy.getLocation()))),
                            ship,
                            enemy,
                            DamageType.ENERGY,
                            ARC_DAMAGE,
                            200f,
                            ARC_MAX_RANGE,
                            "tachyon_lance_emp_impact",
                            ARC_BEAM_THICKNESS,
                            ARC_FRINGE_COLOUR,
                            ARC_INNER_COLOUR
                    );

                    arced = true;
                    break;
                }
            }

            if (!arced) {
                float angle = (float) (((Math.random() * shield.getActiveArc()) - (shield.getActiveArc() * 0.5f)) + shield.getFacing());
                float distance = (float) (ship.getShieldRadiusEvenIfNoShield() + (Math.random() * ARC_MAX_RANGE));
                Vector2f loc = MathUtils.getPointOnCircumference(ship.getLocation(), distance, angle);
                CombatEntityAPI dummy = engine.spawnAsteroid(0, loc.x, loc.y, ship.getVelocity().x, ship.getVelocity().y);

                engine.spawnEmpArc(
                        ship,
                        MathUtils.getPointOnCircumference(ship.getLocation(), ship.getShieldRadiusEvenIfNoShield(), VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(), loc))),
                        ship,
                        dummy,
                        DamageType.ENERGY,
                        ARC_DAMAGE,
                        200f,
                        ARC_MAX_RANGE,
                        "tachyon_lance_emp_impact",
                        ARC_BEAM_THICKNESS,
                        ARC_FRINGE_COLOUR,
                        ARC_INNER_COLOUR
                );

                engine.removeEntity(dummy);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getShieldUnfoldRateMult().unmodify(id);

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) return;

        ShieldAPI shield = ship.getShield();
        if (shield == null) return;

        shield.setRingColor(ship.getHullSpec().getShieldSpec().getRingColor());
        shield.setInnerColor(ship.getHullSpec().getShieldSpec().getInnerColor());
        shield.setArc(ship.getHullSpec().getShieldSpec().getArc());
    }
}
