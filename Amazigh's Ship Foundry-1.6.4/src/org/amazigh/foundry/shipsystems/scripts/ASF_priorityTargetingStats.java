package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_priorityTargetingStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 60f;
	public static final float DAMAGE_BONUS_EN = 20f;
	public static final float EN_FLUX_BONUS = 10f;
	public static final float ROF_MULT = 1.35f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = (DAMAGE_BONUS * effectLevel);
		stats.getDamageToTargetShieldsMult().modifyPercent(id, mult);
		stats.getDamageToTargetWeaponsMult().modifyPercent(id, mult);
		stats.getDamageToTargetEnginesMult().modifyPercent(id, mult);
		stats.getHitStrengthBonus().modifyPercent(id, mult);
		float en_mult = (DAMAGE_BONUS_EN * effectLevel);
		stats.getEnergyWeaponDamageMult().modifyPercent(id, en_mult); // haha hidden energy weapon damage buff because lol
		stats.getEnergyRoFMult().modifyMult(id, ROF_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -EN_FLUX_BONUS);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getDamageToTargetShieldsMult().unmodify(id);
		stats.getDamageToTargetWeaponsMult().unmodify(id);
		stats.getDamageToTargetEnginesMult().unmodify(id);
		stats.getHitStrengthBonus().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = (DAMAGE_BONUS * effectLevel);
		float bonusPercent = (int) mult;
		
		if (index == 0) {
			return new StatusData("weapon performance increased by +" + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
