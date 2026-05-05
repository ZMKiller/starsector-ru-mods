package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;

public class CJHM_sensorcloak extends BaseHullMod {
	private static final float SENSOR_INCREASE = -60f;
	public static final float PROFILE_MULT = 0.4f;	
	
	public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorStrength().modifyPercent(id, SENSOR_INCREASE);		
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
		stats.getSightRadiusMod().modifyPercent(id, SENSOR_INCREASE);		
	}
 public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) SENSOR_INCREASE + "%";
		if (index == 1) return "" + (int) Math.round((1f - PROFILE_MULT) * 100f) + "%";	
 
    return null;  
  }
}