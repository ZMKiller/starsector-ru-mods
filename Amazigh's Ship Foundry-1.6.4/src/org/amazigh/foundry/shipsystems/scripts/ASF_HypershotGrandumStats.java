package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_HypershotGrandumStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 2.0f;
	public static final float REGEN_BONUS = 0.75f;
	public static final float FLUX_REDUCTION = 40f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + (ROF_BONUS * effectLevel);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getMissileRoFMult().modifyMult(id, mult);
		float mult_2 = 1f + (REGEN_BONUS * effectLevel);
		stats.getBallisticAmmoRegenMult().modifyMult(id, mult_2);
		stats.getMissileAmmoRegenMult().modifyMult(id, mult_2);
		
		float multc = FLUX_REDUCTION * effectLevel;
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -multc);
		stats.getMissileWeaponFluxCostMod().modifyPercent(id, -(multc));
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float multc = FLUX_REDUCTION * effectLevel;
		
		float bonusPercent = (int) ((mult - 1f) * 100f);
		
		float coolPercent = (int) (multc);
		
		if (index == 0) {
			return new StatusData("missile and ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistic flux use -" + (int) coolPercent + "%", false);
		}
		return null;
	}
}
