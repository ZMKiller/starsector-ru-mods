package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class XHAN_PlasteenArmor extends BaseHullMod {

    public static final float HIGH_EXPLOSIVE_DAMAGE_REDUCTION = 0.6f;
    public static final float KINETIC_DAMAGE_REDUCTION = 1.2f;


    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHighExplosiveDamageTakenMult().modifyMult(id, HIGH_EXPLOSIVE_DAMAGE_REDUCTION);
        stats.getKineticDamageTakenMult().modifyMult(id, KINETIC_DAMAGE_REDUCTION);
    }


    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) return Math.round((1f - HIGH_EXPLOSIVE_DAMAGE_REDUCTION) * 100f) + "%";
        if (index == 1) return Math.round((1f - KINETIC_DAMAGE_REDUCTION) * 100f) + "%";
        return null;
    }


}
