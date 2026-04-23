package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_GlisterEngines extends BaseHullMod {

	public static final float HEALTH_BONUS = 100f;
	public static final float REPAIR_BONUS = 50f;
	
	public void applyEffectsBeforeShipCreation(MutableShipStatsAPI stats, String id) {

		stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - REPAIR_BONUS * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
