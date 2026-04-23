package org.xhan.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class XHAN_EnergyOptiScript extends BaseShipSystemScript {

    public static final float FLUX_REDUCTION = 50f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 1) {
            return new StatusData("energy flux use -" + (int) FLUX_REDUCTION + "%", false);
        }
        return null;
    }
}