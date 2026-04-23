package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_exoticStructure extends BaseHullMod {
	
	public static final float SUPPLY_MULT = 0.6f;
	public static final float ENGINE_REPAIR = 40f;
	public static final float KIN_RESIST = 0.2f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyMult(id, 1f + SUPPLY_MULT);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - (ENGINE_REPAIR * 0.01f));
		
		stats.getKineticArmorDamageTakenMult().modifyMult(id, (1f - KIN_RESIST));
		stats.getKineticDamageTakenMult().modifyMult(id, (1f - KIN_RESIST));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		float resist = KIN_RESIST * 100f;
		
		if (index == 0) return "" + (int)((SUPPLY_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) ENGINE_REPAIR + "%";
		if (index == 2) return "" + (int) resist + "%";
		return null;
	}
}