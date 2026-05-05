package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_reinforcedhull extends BaseHullMod {
	public static final float HULL_BONUS = -10f;
	public static final float CASUALTY_REDUCTION = 60f;
	public static final float FLUX_RESISTANCE = 60f;	
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getCrewLossMult().modifyMult(id, 1f - CASUALTY_REDUCTION * 0.01f);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f);			
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_BONUS;
		if (index == 1) return "" + (int) FLUX_RESISTANCE + "%";
		if (index == 2) return "" + (int) CASUALTY_REDUCTION + "%";		
		
		return null;
	}
 }
