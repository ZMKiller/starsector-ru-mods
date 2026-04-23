package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class XHAN_TargetingMast extends BaseHullMod {

//	public static final float BONUS = 100f;
//	
//	public String getDescriptionParam(int index, HullSize hullSize) {
//		if (index == 0) return "" + (int)BONUS + "%";
//		return null;
//	}
//	
//	
//	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//		stats.getBallisticWeaponRangeBonus().modifyPercent(id, BONUS);
//		stats.getEnergyWeaponRangeBonus().modifyPercent(id, BONUS);
//	}


    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    public static float RANGE_BONUS = 20f;

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(RANGE_BONUS) + "%";
        //if (index == 0) return "" + (int)RANGE_THRESHOLD;
        //if (index == 1) return "" + (int)((RANGE_MULT - 1f) * 100f);
        //if (index == 1) return "" + new Float(VISION_BONUS).intValue();
        return null;
    }


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);

    }

}
