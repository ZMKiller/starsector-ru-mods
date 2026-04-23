/*
code by Xaiier
*/

package org.xhan.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.xhan.shipsystems.XHAN_EmergencyRepairs;
import org.lwjgl.util.vector.Vector2f;

public class XHAN_EmergencyRepairs_ShipSystemAI implements ShipSystemAIScript {

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
            if (ship.getHullLevel() < (1 - XHAN_EmergencyRepairs.HULL_MULT)) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
        }
    }
}
