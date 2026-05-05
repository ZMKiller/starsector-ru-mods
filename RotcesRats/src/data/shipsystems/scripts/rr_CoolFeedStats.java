package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class rr_CoolFeedStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 0.2f;
	public static final float FLUX_REDUCTION = 50f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
		
		float multc = FLUX_REDUCTION * effectLevel;
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -multc);
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float multc = FLUX_REDUCTION * effectLevel;
		
		float bonusPercent = (int) ((mult - 1f) * 100f);
		
		float coolPercent = (int) (multc);
		
		if (index == 0) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) coolPercent + "%", false);
		}
		return null;
	}
}
