package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIReturnFighter extends BaseHullMod {
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.getWing()!=null){
            if(ship.getWing().isReturning(ship)) return;
            boolean shouldReturn = true;
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(weapon.usesAmmo()&&weapon.getAmmo()>0){
                    shouldReturn = false;
                }
            }
            if(shouldReturn){
                ship.getWing().orderReturn(ship);
            }
        }
    }
}
