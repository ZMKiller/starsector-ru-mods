package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_clavataDummy extends BaseHullMod {
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		// just an empty script, for purely informational hullmods.
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
