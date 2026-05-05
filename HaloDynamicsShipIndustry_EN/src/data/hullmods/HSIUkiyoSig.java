package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSIUkiyoSig extends BaseHullMod {

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getHullSpec().getBaseHullId().startsWith("HSI_Ukiyo")&&super.isApplicableToShip(ship);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isApplicableToShip(ship);
    }
}
