package org.xhan.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/*
code by Tomatopaste
*/

public class XHAN_UogekOnHit implements OnHitEffectPlugin {
    private static final float DISTANCE_THRESHOLD = 700f;
    private static final float DISTANCE_NO_FORCE_FALLOFF = 350f;
    private static final float ARC_EMP_DAMAGE = 200f;
    private static final float ARC_DAMAGE = 100f;

    private static final int MIN_ARCS = 3;
    private static final int MAX_ARCS = 6;
    private static final int MAX_ARCS_FIGHTER = 2;
    private static final int MIN_ARCS_FIGHTER = 1;

    private static final Color ARC_FRINGE_COLOUR_SHIP = new Color(182, 22, 235, 251);
    private static final Color ARC_FRINGE_COLOUR_MAGNET = new Color(182, 22, 235, 251);
    private static final Color ARC_CORE_COLOUR = new Color(248, 232, 255, 251);

    private static final Map<ShipAPI.HullSize, Float> BASE_FORCE_PER_HULLSIZE = new HashMap<>();

    static {
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.FIGHTER, 2000f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.FRIGATE, 1000f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.DESTROYER, 500f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.CRUISER, 200f);
        BASE_FORCE_PER_HULLSIZE.put(ShipAPI.HullSize.CAPITAL_SHIP, 55f);
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        ShipAPI ship = projectile.getSource();
        if (engine == null || engine.isPaused() || !(target instanceof ShipAPI) || !((ShipAPI) target).isAlive() || ship == null || !ship.isAlive()) {
            return;
        }

        applyEffectToTarget(projectile.getWeapon().getLocation(), ship, (ShipAPI) target, engine);
    }

    private void applyEffectToTarget(final Vector2f arcStart, final ShipAPI ship, final ShipAPI target, CombatEngineAPI engine) {
        spawnArcs(arcStart, ship, target, 35f, ARC_FRINGE_COLOUR_SHIP, engine);

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
            spawnArcs(target.getLocation(), target, potentialShip, 25f, ARC_FRINGE_COLOUR_MAGNET, engine);

            potentialShip.getVelocity().scale(0.25f);
            CombatUtils.applyForce(potentialShip, direction, force);
        }
    }

    private void spawnArcs(final Vector2f start, final ShipAPI source, final ShipAPI destination, float thickness, Color fringe, CombatEngineAPI engine) {
        int num;
        if (destination.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) {
            num = (int) (Math.random() * (MAX_ARCS_FIGHTER - MIN_ARCS_FIGHTER));
            num += MIN_ARCS_FIGHTER;
        } else {
            num = (int) (Math.random() * (MAX_ARCS - MIN_ARCS));
            num += MIN_ARCS;
        }

        for (int i = 0; i < num; i++) {
            engine.spawnEmpArcPierceShields(source, start, source, destination, DamageType.ENERGY, ARC_DAMAGE, ARC_EMP_DAMAGE, 2000f, "XHAN_UOGEK_IMPACT_SOUND", thickness, fringe, ARC_CORE_COLOUR);
        }
    }
}
