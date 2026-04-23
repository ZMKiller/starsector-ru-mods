package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ASF_rinkaJetsStats extends BaseShipSystemScript {

	public static final float SPEED_BOOST = 120f;
	public static final float ACCEL_BOOST = 320f;
	public static final float TURN_A_BONUS = 60f;
	public static final float TURN_BONUS = 40f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST);
		}
		stats.getAcceleration().modifyPercent(id, ACCEL_BOOST * effectLevel);
		stats.getDeceleration().modifyPercent(id, ACCEL_BOOST * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, TURN_A_BONUS * effectLevel);
		stats.getMaxTurnRate().modifyPercent(id, TURN_BONUS * effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);		
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+" + (int)SPEED_BOOST + " top speed", false);
		}
		return null;
	}
}