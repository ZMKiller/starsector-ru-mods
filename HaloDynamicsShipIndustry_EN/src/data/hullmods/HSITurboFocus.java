package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HSITurboFocus extends BaseHullMod {
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2.getInstance(ship).getShield().setCurrent(0);
            HSITurbulanceShieldListenerV2.getInstance(ship).getShield().setExtra(0);
            //HSITurbulanceShieldListenerV2.getInstance(ship).getStats().getShieldRecoveryRate().modifyMult(id, 0);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + 0+"%";
        return null;
    }

}