package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_inertialBracing extends BaseHullMod {
	
	public static final float FRAG_RESIST = 0.5f;
	
	public static final float HEALTH_BONUS = 75f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFragmentationDamageTakenMult().modifyMult(id, FRAG_RESIST);
		stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		float resist = FRAG_RESIST * 100f;
		
		if (index == 0) return "" + (int) resist + "%";
		if (index == 1) return "" + (int) HEALTH_BONUS + "%";
		return null;
	}
	
}