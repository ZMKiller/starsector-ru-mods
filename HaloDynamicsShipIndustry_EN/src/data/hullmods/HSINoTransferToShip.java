package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSINoTransferToShip extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(!ship.getVariant().getWings().contains("HSI_TianDeng_wing")){
            ship.getVariant().removeMod(spec.getId());
        }
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return ship.getVariant().getWings().contains("HSI_TianDeng_wing");
    }
}
