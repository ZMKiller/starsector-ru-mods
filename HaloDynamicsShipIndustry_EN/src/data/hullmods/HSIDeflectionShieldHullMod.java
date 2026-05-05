package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSIDeflectionShieldHullMod extends BaseHullMod {

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(!ship.hasListenerOfClass(HSIDeflectionShield.class)){
            ship.addListener(new HSIDeflectionShield(ship));
        }
    }
}
