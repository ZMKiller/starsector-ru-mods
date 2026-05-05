package data.hullmods.Station;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.util.IntervalUtil;

import data.kit.AjimusUtils;

public class HSISentry extends BaseHullMod {
    public static final float REVIVE_TIME = 90f;

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new HSISentryReviveListener(ship));
	}

    public class HSISentryReviveListener implements HullDamageAboutToBeTakenListener, AdvanceableListener {
        protected boolean isReviving = false;
        protected IntervalUtil reviveTimer = new IntervalUtil(REVIVE_TIME, REVIVE_TIME);
        protected ShipAPI ship;

        public HSISentryReviveListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused())
                return;
            if (isReviving) {
                reviveTimer.advance(amount);
                AjimusUtils.repairHP(ship, ship.getMaxHitpoints() * amount / reviveTimer.getIntervalDuration());
                AjimusUtils.repairArmorGenerally(ship,
                        ship.getArmorGrid().getMaxArmorInCell() * amount / reviveTimer.getIntervalDuration());
                ship.setAlphaMult(Math.max(0,reviveTimer.getElapsed()/reviveTimer.getIntervalDuration()) );
                ship.blockCommandForOneFrame(ShipCommand.FIRE);
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
                ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
                ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
            }
            isReviving = !reviveTimer.intervalElapsed();
        }

        @Override
        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            if (damageAmount > ship.getHitpoints()) {
                isReviving = true;
                return true;
            }
            return false;
        }

    }
}
