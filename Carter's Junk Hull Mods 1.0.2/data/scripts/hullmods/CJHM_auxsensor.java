package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_auxsensor extends BaseHullMod {
	private static final float SENSOR_INCREASE = 10f;
	
	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorStrength().modifyFlat(id, SENSOR_INCREASE);
	}
 public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0)
      return "10.0";
    return null;  
  }
}