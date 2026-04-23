package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_ExpansiveMissileRacks extends BaseHullMod {

	public static final float AMMO_BONUS = 300f;
	
	public static final float RATE_BONUS = 50f;
	public static final float REGEN_BONUS = 30f;
	
	public static final float HYBRID_MALUS = 0.3f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
		
		stats.getMissileRoFMult().modifyPercent(id, RATE_BONUS);
		stats.getMissileAmmoRegenMult().modifyMult(id, 1f + (REGEN_BONUS * 0.01f));
		
		float mult = (1f - HYBRID_MALUS);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getEnergyWeaponDamageMult().modifyMult(id, mult);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		float malus = HYBRID_MALUS * 100f;
		
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		if (index == 1) return "" + (int) RATE_BONUS + "%";
		if (index == 2) return "" + (int) malus + "%";
		if (index == 3) return "" + (int) malus + "%";
		return null;
	}

}
