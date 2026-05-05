package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIDarkTideCore extends BaseHullMod{

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.getSlot().isBuiltIn()){
                weapon.setAmmo(weapon.getSpec().getMaxAmmo());
            }
        }
    }
    
}
