package org.xhan.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class PAMED_engines extends BaseLogisticsHullMod {

    public static final float HEALTH_BONUS = 100f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) HEALTH_BONUS + "%";
        return null;
    }


}
