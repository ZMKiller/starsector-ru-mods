package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_HypershotStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 3.0f;
	public static final float REGEN_BONUS = 1.0f;
	public static final float REGEN_BONUS_M = 0.5f;
	public static final float FLUX_REDUCTION = 75f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + (ROF_BONUS * effectLevel);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		float mult_2 = 1f + (REGEN_BONUS * effectLevel);
		stats.getBallisticAmmoRegenMult().modifyMult(id, mult_2);
		float mult_3 = 1f + (REGEN_BONUS_M * effectLevel);
		stats.getMissileAmmoRegenMult().modifyMult(id, mult_3);
		
		float multc = FLUX_REDUCTION * effectLevel;
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -multc);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
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
