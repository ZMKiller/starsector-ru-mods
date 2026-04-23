package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/*
code by Tomatopaste
*/

public class XHAN_MagneticImpulse extends BaseShipSystemScript {
    private static final float DISTANCE_THRESHOLD = 800f;
    private static final float DISTANCE_NO_FORCE_FALLOFF = 350f;
    private static final float ARC_EMP_DAMAGE = 450f;
    private static final float ARC_DAMAGE = 200f;

    private static final int MIN_ARCS = 3;
    private static final int MAX_ARCS = 6;
    private static final int MAX_ARCS_FIGHTER = 2;
    private static final int MIN_ARCS_FIGHTER = 1;

    private static final Map<ShipAPI.HullSize, Float> BASE_FORCE_PER_HULLSIZE = new HashMap<>();

    static {
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.FIGHTER, 8000f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.FRIGATE, 4000f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.DESTROYER, 2000f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.CRUISER, 900f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.CAPITAL_SHIP, 250f);
    }

    private static final Color ARC_FRINGE_COLOUR_SHIP = new Color(145, 242, 255, 251);
    private static final Color ARC_FRINGE_COLOUR_MAGNET = new Color(145, 242, 255, 251);
    private static final Color ARC_CORE_COLOUR = new Color(247, 255, 230, 251);
    private static final Color SHIP_JITTER_COLOUR = new Color(145, 242, 255, 134);


    private ShipAPI target;
    private CombatEngineAPI engine;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        engine = Global.getCombatEngine();
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (engine == null || engine.isPaused() || ship == null || !ship.isAlive()) {
            return;
        }

        target = findTarget(ship);
        if (effectLevel >= 1f) {
            applyEffectToTarget(ship, target);
        }

        ship.setJitter(this, SHIP_JITTER_COLOUR, 0.8f * effectLevel, 8, 50f * effectLevel);
        target.setJitterUnder(this, SHIP_JITTER_COLOUR, effectLevel, 6, 25f * effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, java.lang.String id) {
    }

    private void applyEffectToTarget(final ShipAPI ship, final ShipAPI target) {
        spawnArcs(ship, target, 35f, ARC_FRINGE_COLOUR_SHIP);

        for (ShipAPI potentialShip : engine.getShips()) {
            float distance = MathUtils.getDistance(potentialShip, target);

            float force;
            Vector2f direction = new Vector2f();
            if (ship.getOwner() == potentialShip.getOwner() || distance > DISTANCE_THRESHOLD || potentialShip == target) {
                continue;
            } else {
                Vector2f.sub(target.getLocation(), potentialShip.getLocation(), direction);

                if (distance > DISTANCE_NO_FORCE_FALLOFF) {
                    force = ((distance - DISTANCE_NO_FORCE_FALLOFF) / (DISTANCE_NO_FORCE_FALLOFF)) * BASE_FORCE_PER_HULLSIZE.get(potentialShip.getHullSize());
                } else {
                    force = BASE_FORCE_PER_HULLSIZE.get(potentialShip.getHullSize());
                }
            }

            //potentialShip.getEngineController().forceFlameout();
            spawnArcs(target, potentialShip, 25f, ARC_FRINGE_COLOUR_MAGNET);

            potentialShip.getVelocity().scale(0.25f);
            CombatUtils.applyForce(potentialShip, direction, force);
        }
    }

    private void spawnArcs(final ShipAPI source, final ShipAPI destination, float thickness, Color fringe) {
        int num;
        if (destination.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) {
            num = (int) (Math.random() * (MAX_ARCS_FIGHTER - MIN_ARCS_FIGHTER));
            num += MIN_ARCS_FIGHTER;
        } else {
            num = (int) (Math.random() * (MAX_ARCS - MIN_ARCS));
            num += MIN_ARCS;
        }

        for (int i = 0; i < num; i++) {
            engine.spawnEmpArcPierceShields(source, source.getLocation(), source, destination, DamageType.ENERGY, ARC_DAMAGE, ARC_EMP_DAMAGE, 2000f, "tachyon_lance_emp_impact", thickness, fringe, ARC_CORE_COLOUR);
        }
    }

    //copied this method from AcausalDisruptorStats.java in starsector api


    private ShipAPI findTarget(ShipAPI ship) {

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();

        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();

            if (dist > DISTANCE_THRESHOLD + radSum) {
                target = null;
            }
        } else {
            if (player) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FIGHTER, DISTANCE_THRESHOLD, true);
            } else {
                Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
                if (test instanceof ShipAPI) {
                    target = (ShipAPI) test;
                    float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                    float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                    if (dist > DISTANCE_THRESHOLD + radSum) target = null;
                }
            }
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FIGHTER, DISTANCE_THRESHOLD, true);
            }
        }
        if (target == null || target.getFluxTracker().isOverloadedOrVenting()) target = ship;

        return target;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        target = findTarget(ship);
        if (target != ship) {
            return "READY";
        }
        if (ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        target = findTarget(ship);
        return target != ship;
    }
}
