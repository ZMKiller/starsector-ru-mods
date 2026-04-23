package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class dpl_Precursor_EngineFix extends BaseHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEngineDamageTakenMult().modifyMult("dpl_precursor_engine_fix", 0f);
		stats.getEngineMalfunctionChance().modifyMult("dpl_precursor_engine_fix", 0f);
		stats.getEmpDamageTakenMult().modifyMult("dpl_precursor_engine_fix", 0f);
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null;
	}

}
