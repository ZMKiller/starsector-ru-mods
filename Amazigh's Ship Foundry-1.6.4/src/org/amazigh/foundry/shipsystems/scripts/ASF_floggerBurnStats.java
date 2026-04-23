package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ASF_floggerBurnStats extends BaseShipSystemScript {

	public static final float SPEED_MULT = 2f; // plus one
	public static final float SPEED_FLAT = 180f;
	public static final float ACCEL_MULT = 4f; // plus one
	public static final float ACCEL_FLAT = 200f;
	public static final float TURN_MALUS = 0.5f;
	
	public static final float ACCEL_MALUS = 0.75f;
	public static final float TURN_BONUS = 2.5f; // plus one
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		if (state == ShipSystemStatsScript.State.OUT) {
			 // these stat mods are so we ""drift"" when in the OUT state
			stats.getAcceleration().unmodify(id);
			stats.getDeceleration().modifyFlat(id, 1f - (ACCEL_MALUS * effectLevel));
			
			 // and here we boost turn rate, to tie in with the "drift" feel
			stats.getTurnAcceleration().modifyMult(id, 1f + (TURN_BONUS * effectLevel));
			stats.getMaxTurnRate().modifyMult(id, 1f + (TURN_BONUS * effectLevel));
			
		} else {
			stats.getAcceleration().modifyMult(id, 1f + (ACCEL_MULT * effectLevel));
			stats.getAcceleration().modifyFlat(id, ACCEL_FLAT * effectLevel);
			
			stats.getTurnAcceleration().modifyMult(id, 1f - (TURN_MALUS * effectLevel));
			stats.getMaxTurnRate().modifyMult(id, 1f - (TURN_MALUS * effectLevel));
			
		}
		
		// top speed is always boosted, but scaled with effectLevel
		stats.getMaxSpeed().modifyMult(id, 1f + (SPEED_MULT * effectLevel));
		stats.getMaxSpeed().modifyFlat(id, SPEED_FLAT * effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		return null;
	}
}
