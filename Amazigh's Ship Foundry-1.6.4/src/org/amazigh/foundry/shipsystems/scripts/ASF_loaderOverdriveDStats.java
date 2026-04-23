package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_loaderOverdriveDStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 7.0f;
	public static final float FLUX_REDUCTION = 80f;

	public static final float REPAIR_BOOST = 0.5f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + (ROF_BONUS * effectLevel);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
		stats.getMissileRoFMult().modifyMult(id, mult * 0.8f); // missile spam reduction
		stats.getMissileAmmoRegenMult().modifyMult(id, mult * 0.8f);
		
		float multc = FLUX_REDUCTION * effectLevel;
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -multc);
		stats.getMissileWeaponFluxCostMod().modifyPercent(id, -multc);
		
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - (REPAIR_BOOST * effectLevel));
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - (REPAIR_BOOST * effectLevel));
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		
		stats.getCombatWeaponRepairTimeMult().unmodify(id);
		stats.getCombatEngineRepairTimeMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + (ROF_BONUS * effectLevel);
		float bonusPercent = (int) ((mult - 1f) * 100f);
		
		if (index == 0) {
			return new StatusData("ballistic and missile reload rate +" + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
