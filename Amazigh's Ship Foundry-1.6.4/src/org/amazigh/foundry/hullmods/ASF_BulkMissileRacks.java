package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_BulkMissileRacks extends BaseHullMod {

	public static final float AMMO_BONUS = 100f;
	
	public static final float RATE_BONUS = 20f;
	public static final float DAMAGE_MALUS = 0.25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
		
		stats.getMissileRoFMult().modifyPercent(id, RATE_BONUS);
		stats.getMissileAmmoRegenMult().modifyMult(id, 1f + (RATE_BONUS * 0.01f));
		float mult = (1f - DAMAGE_MALUS);
		stats.getMissileWeaponDamageMult().modifyMult(id, mult);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
	  
	  	float malus = DAMAGE_MALUS * 100f;
	  
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		if (index == 1) return "" + (int) malus + "%";
		if (index == 2) return "" + (int) RATE_BONUS + "%";
		return null;
	}

}
