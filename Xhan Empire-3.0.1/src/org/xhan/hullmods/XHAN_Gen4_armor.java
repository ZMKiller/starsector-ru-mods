package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class XHAN_Gen4_armor extends BaseHullMod {

    public static final float HIGH_EXPLOSIVE_DAMAGE_REDUCTION = 0.5f;
    public static final float ENERGY_DAMAGE_REDUCTION = 0.75f;
    public static final float FLUX_RESISTANCE = 50f;
    public static final float FRAGMENTATION_DAMAGE_REDUCTION = 0.40f;
    public static final float BEAM_DAMAGE_REDUCTION = 0.6f;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, HIGH_EXPLOSIVE_DAMAGE_REDUCTION);
        stats.getEnergyDamageTakenMult().modifyMult(id, ENERGY_DAMAGE_REDUCTION);
        stats.getFragmentationDamageTakenMult().modifyMult(id, FRAGMENTATION_DAMAGE_REDUCTION);
        stats.getBeamDamageTakenMult().modifyMult(id, BEAM_DAMAGE_REDUCTION);
        stats.getEmpDamageTakenMult().modifyMult(id, 1f - FLUX_RESISTANCE * 0.01f);
    }


    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) return Math.round((1f - HIGH_EXPLOSIVE_DAMAGE_REDUCTION) * 100f) + "%";
        if (index == 1) return Math.round((1f - ENERGY_DAMAGE_REDUCTION) * 100f) + "%";
        if (index == 2) return "" + (int) FLUX_RESISTANCE + "%";
        return null;
    }


}
