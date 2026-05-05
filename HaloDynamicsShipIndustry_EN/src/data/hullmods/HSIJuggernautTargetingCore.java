package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSIJuggernautTargetingCore extends BaseHullMod {

    protected static final float RANGE_BUFF = 140f;
    protected static final float PD_BUFF = 75f;
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponRangeBonus().modifyPercent(id,RANGE_BUFF);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id,RANGE_BUFF);
        stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id,PD_BUFF-RANGE_BUFF);
        stats.getBeamPDWeaponRangeBonus().modifyPercent(id,PD_BUFF-RANGE_BUFF);
    }


    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)RANGE_BUFF+"%";
        if(index == 1) return (int)PD_BUFF+"%";
        return null;
    }


    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getHullSpec().hasTag("HSI_Juggernaut");
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().hasTag("HSI_Juggernaut");
    }
}
