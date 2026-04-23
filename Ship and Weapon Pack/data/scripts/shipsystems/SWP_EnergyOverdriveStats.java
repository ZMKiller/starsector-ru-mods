package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SWP_EnergyOverdriveStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getFluxDissipation().modifyPercent(id, effectLevel * 50f);
        stats.getEnergyRoFMult().modifyPercent(id, effectLevel * 50f);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (effectLevel * 0.25f));
        stats.getEnergyWeaponDamageMult().modifyPercent(id, effectLevel * 25f);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        switch (index) {
            case 0:
                return new StatusData("flux dissipation +" + (int) (effectLevel * 50f) + "%", false);
            case 1:
                return new StatusData("energy rate of fire +" + (int) (effectLevel * 50f) + "%", false);
            case 2:
                return new StatusData("energy flux cost -" + (int) (effectLevel * 25f) + "%", false);
            case 3:
                return new StatusData("energy damage +" + (int) (effectLevel * 25f) + "%", false);
            default:
                break;
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getFluxDissipation().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponDamageMult().unmodify(id);
    }
}
