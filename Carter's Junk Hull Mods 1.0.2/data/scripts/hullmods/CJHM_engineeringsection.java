package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_engineeringsection extends BaseHullMod {
	public static final float REPAIR_BONUS = 35f;	
	public static final float HEALTH_BONUS = 15f;
	private static final float SUPPLY_USE_MULT = -5f;	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
		stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getSuppliesPerMonth().modifyMult(id, 1f - SUPPLY_USE_MULT* 0.01f);		
		}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) REPAIR_BONUS + "%";	
		if (index == 1) return "" + (int) HEALTH_BONUS;	
		if (index == 2) return "" + (int) SUPPLY_USE_MULT + "%";		
		return null;
	}
}
