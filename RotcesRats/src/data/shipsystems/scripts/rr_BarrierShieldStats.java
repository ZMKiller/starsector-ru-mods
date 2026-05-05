package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class rr_BarrierShieldStats extends BaseShipSystemScript {

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - (0.8f * effectLevel));
		stats.getShieldUpkeepMult().modifyMult(id, 0f);
		
		if (state == ShipSystemStatsScript.State.OUT) {
			//stats.getMaxSpeed().unmodify(id);
			//stats.getMaxTurnRate().unmodify(id);
			// to return ship to its regular top speed while powering system down
		} else {
			stats.getMaxSpeed().modifyMult(id, 0.7f * effectLevel);
			stats.getAcceleration().modifyMult(id, 0.6f * effectLevel);
			stats.getDeceleration().modifyMult(id, 0.6f * effectLevel);
			stats.getTurnAcceleration().modifyMult(id, 0.6f * effectLevel);
			stats.getMaxTurnRate().modifyMult(id, 0.7f * effectLevel);
			
		}
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
		
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("shield absorbs 5x damage", false);
		} else if (index == 1) {
			return new StatusData("reduced maneuverability", false);
		} else if (index == 2) {
			return new StatusData("-30% top speed", false);
		}
		return null;
	}
}
