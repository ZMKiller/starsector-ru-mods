package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class PAMED_armor extends BaseHullMod {

    public static final float MAX_DAMAGE_REDUCTION_BONUS = 1f;
    public static final float FLUX_RESISTANCE = 25f;


    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxArmorDamageReduction().modifyFlat(id, MAX_DAMAGE_REDUCTION_BONUS);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) FLUX_RESISTANCE + "%";
        return null;
    }

}
