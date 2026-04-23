package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_droneScreenStats extends BaseShipSystemScript {

	public static final float SENSOR_RANGE_PERCENT = 2f;
	public static final int WEAPON_RANGE_FLAT = 2;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
		float weaponRangeFlat = WEAPON_RANGE_FLAT * effectLevel;
		
		stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);
			// this is a secret hidden bonus, mostly because it's quite minor.
		
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, weaponRangeFlat);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, weaponRangeFlat);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getSightRadiusMod().unmodify(id);
		
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float weaponRangeFlat = WEAPON_RANGE_FLAT * effectLevel;
		if (index == 0) {
			return new StatusData("weapon range +" + (int) weaponRangeFlat, false);
		}
		return null;
	}
}
