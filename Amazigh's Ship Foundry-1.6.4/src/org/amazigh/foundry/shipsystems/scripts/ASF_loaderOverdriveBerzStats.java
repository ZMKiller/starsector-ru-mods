package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ASF_loaderOverdriveBerzStats extends BaseShipSystemScript {

	public static final float ROF_BONUS = 7.0f; // 8x
	public static final float ROF_BONUS_M = 5.0f; // 6x (the missile spam was a bit much)
	public static final float FLUX_REDUCTION = 85f;
	
	public static final float ENERGY_DAMAGE = 1f; // 2x
	
	public static final float REPAIR_BOOST = 0.8f;
	public static final float TURRET_TURN_BOOST = 75f;
	
	public static final float SPEED_BOOST = 25f; // [CUSTOM CARTRIDGE: MOVING BURST]
	public static final float ACCEL_BOOST = 80f;
	public static final float TURN_BONUS_P = 40f;
	public static final float TURN_BONUS_F = 4f;
	public static final float TURN_A_BONUS_F = 6;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + (ROF_BONUS * effectLevel);
		float mult_m = 1f + (ROF_BONUS_M * effectLevel);
		
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getBallisticRoFMult().modifyMult(id, mult);
			stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
			stats.getMissileRoFMult().modifyMult(id, mult_m);
			stats.getMissileAmmoRegenMult().modifyMult(id, mult_m);
			stats.getEnergyRoFMult().modifyMult(id, mult);
			stats.getEnergyAmmoRegenMult().modifyMult(id, mult);
			
			float multE = 1f + (ENERGY_DAMAGE * effectLevel);
			stats.getEnergyWeaponDamageMult().modifyMult(id, multE);
			
			float multc = FLUX_REDUCTION * effectLevel;
			stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -multc);
			stats.getMissileWeaponFluxCostMod().modifyPercent(id, -multc);
			stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -multc);
		} else {
			stats.getBallisticRoFMult().modifyMult(id, mult * 2f);
			stats.getBallisticAmmoRegenMult().modifyMult(id, mult * 2f);
			stats.getMissileRoFMult().modifyMult(id, mult_m * 1.5f); // we really are lowering the missile boost, less spam.
			stats.getMissileAmmoRegenMult().modifyMult(id, mult_m * 1.5f);
			stats.getEnergyRoFMult().modifyMult(id, mult * 2f);
			stats.getEnergyAmmoRegenMult().modifyMult(id, mult * 2f);
		}
		
		stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
		
		stats.getAcceleration().modifyPercent(id, ACCEL_BOOST * effectLevel);
		stats.getDeceleration().modifyPercent(id, ACCEL_BOOST * effectLevel);

		stats.getTurnAcceleration().modifyFlat(id, TURN_A_BONUS_F * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, ACCEL_BOOST * effectLevel);
		stats.getMaxTurnRate().modifyFlat(id, TURN_BONUS_F);
		stats.getMaxTurnRate().modifyPercent(id, TURN_BONUS_P);
		
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - (REPAIR_BOOST * effectLevel));
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - (REPAIR_BOOST * effectLevel));
		
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_TURN_BOOST);
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getCombatWeaponRepairTimeMult().unmodify(id);
		stats.getCombatEngineRepairTimeMult().unmodify(id);
		
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getEnergyAmmoRegenMult().unmodify(id);
		
		stats.getEnergyWeaponDamageMult().unmodify(id);
		
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + (ROF_BONUS * effectLevel);
		float bonusPercent = (int) ((mult - 1f) * 100f);
		
		if (index == 0) {
			return new StatusData("weapon reload rate +" + (int) bonusPercent + "%", false);
		}else if (index == 1) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+" + (int)SPEED_BOOST + " top speed", false);
		}
		return null;
	}
}
