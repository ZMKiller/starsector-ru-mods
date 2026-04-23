package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class dpl_RelativityTargetingStats extends BaseShipSystemScript {

	public static final float RANGE_BONUS = 0.8f;
	public static final float SPEED_REDUCTION = 80f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = RANGE_BONUS * effectLevel;
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, 100*mult);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, 100*mult);
		stats.getMaxSpeed().modifyMult(id, 1f - (SPEED_REDUCTION * 0.01f));
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + RANGE_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("non-missile weapon range +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ship max speed -" + (int) SPEED_REDUCTION + "%", false);
		}
		return null;
	}
}
