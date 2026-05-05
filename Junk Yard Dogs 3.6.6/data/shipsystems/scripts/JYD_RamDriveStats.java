package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.combat.ShipAPI;

public class JYD_RamDriveStats extends BaseShipSystemScript {

    private Float mass = null;
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        	ShipAPI ship = (ShipAPI) stats.getEntity();
        	if (ship == null) {
        	    return;
        	}
        	if (mass == null) {
        	    mass = ship.getMass();
        	}
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); 
		} else {
			float mult = (effectLevel-1);
            		ship.setMass(5*mass -4*mult*mult*mult*mult);
			stats.getMaxSpeed().modifyFlat(id, 400f * effectLevel);
			stats.getAcceleration().modifyFlat(id, 900f * effectLevel);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("Ramming Speed!", false);
		}
		return null;
	}
}
