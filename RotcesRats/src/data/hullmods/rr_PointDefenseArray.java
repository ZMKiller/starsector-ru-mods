package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class rr_PointDefenseArray extends BaseHullMod {

	public static final float DAMAGE_BONUS = 50f;
	public static final float RANGE_BONUS = 60f;
	
	public void applyEffectsBeforeShipCreation(MutableShipStatsAPI stats, String id) {

		stats.getDamageToFighters().modifyPercent(id, DAMAGE_BONUS);
		
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(DAMAGE_BONUS) + "%";
		if (index == 1) return "" + (int)Math.round(RANGE_BONUS);
		return null;
	}

}
