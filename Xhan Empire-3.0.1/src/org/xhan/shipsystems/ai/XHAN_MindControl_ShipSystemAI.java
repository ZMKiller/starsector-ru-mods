/*
code by Xaiier
*/

package org.xhan.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.xhan.shipsystems.XHAN_MindControl;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class XHAN_MindControl_ShipSystemAI implements ShipSystemAIScript {

    private final boolean DEBUG = false;

    private final IntervalUtil tracker = new IntervalUtil(1f, 2f);
    private ShipAPI ship;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }

            ship.setShipTarget(findTarget(ship));

            if (ship.getShipTarget() == null) return;

            boolean shouldUseSystem = ship.getShipTarget() != null;

            //shouldUseSystem = shouldUseSystem && MathUtils.getRandomNumberInRange(0, 2) == 0;

            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }

    protected ShipAPI findTarget(ShipAPI ship) {

        float range = XHAN_MindControl.getMaxRange(ship);
        List<ShipAPI> nearbyEnemies = AIUtils.getNearbyEnemies(ship, range);

        //add mind controlled "allies" to list
        for (ShipAPI ally : AIUtils.getNearbyAllies(ship, range)) {
            if (ally.getCustomData().get("XHAN_MindControl") != null) {
                nearbyEnemies.add(ally);
            }
        }

        if (nearbyEnemies.size() > 0) {
            ShipAPI bestTarget = nearbyEnemies.get(0);
            float strength = determineStrength(bestTarget);
            for (ShipAPI potentialTarget : nearbyEnemies) {
                float newStrength = determineStrength(potentialTarget);
                if (newStrength > strength) {
                    bestTarget = potentialTarget;
                    strength = newStrength;
                }
            }

            return bestTarget;
        } else {
            return null;
        }
    }

    protected float determineStrength(ShipAPI ship) {
        //base strength is FP
        float strengthValue = ship.getHullSpec().getFleetPoints();

        //subtract remaining time from our strength value so we only prefer strong "ally" ships that are nearly out of time
        if (ship.getCustomData().get("XHAN_MindControl") != null) {
            XHAN_MindControl.MindControlData mindControlData = (XHAN_MindControl.MindControlData) ship.getCustomData().get("XHAN_MindControl");
            strengthValue -= (mindControlData.durationEnd - mindControlData.elapsedAfterInState) / 2f;
        }

        //weight by remaining hull
        //for ships with modules, average hull level is used as an approximation
        ArrayList<Float> hullLevel = new ArrayList<Float>();
        hullLevel.add(ship.getHullLevel());
        for (ShipAPI child : ship.getChildModulesCopy()) {
            hullLevel.add(child.getHullLevel());
        }
        float sum = 0f;
        for (float f : hullLevel) {
            sum += f;
        }
        strengthValue *= sum / hullLevel.size();

        //weight by distance
        float distanceFactor = MathUtils.getDistance(this.ship, ship) / XHAN_MindControl.getMaxRange(this.ship);
        distanceFactor = -2 * distanceFactor + 2; //bias towards closer enemies weighted appropriately for typical vanilla FP
        strengthValue *= distanceFactor;

        if (DEBUG) {
            Vector2f offset = new Vector2f(0f, 100f);
            Vector2f.add(offset, ship.getLocation(), offset);
            Global.getCombatEngine().addFloatingText(offset, "Strength: " + strengthValue, 50f, Color.white, ship, 0f, 2f);
        }

        return strengthValue;
    }
}
