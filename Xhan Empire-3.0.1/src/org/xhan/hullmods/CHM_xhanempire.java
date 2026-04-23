package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;

public class CHM_xhanempire extends BaseHullMod {

    private static final Map mag = new HashMap();

    static {
        mag.put(HullSize.FRIGATE, 20f);
        mag.put(HullSize.DESTROYER, 30f);
        mag.put(HullSize.CRUISER, 40f);
        mag.put(HullSize.CAPITAL_SHIP, 50f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEffectiveArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return String.valueOf(((Float) mag.get(HullSize.FRIGATE)).intValue());
        if (index == 1) return String.valueOf(((Float) mag.get(HullSize.DESTROYER)).intValue());
        if (index == 2) return String.valueOf(((Float) mag.get(HullSize.CRUISER)).intValue());
        if (index == 3) return String.valueOf(((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue());
        return null;
    }
}
