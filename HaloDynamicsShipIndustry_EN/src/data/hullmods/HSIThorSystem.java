package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class HSIThorSystem extends BaseHullMod{
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive())
            return;
        if(ship.isFighter()){
            if(ship.getHitpoints()<ship.getMaxHitpoints()*0.8f){
                if(ship.getWing()!=null){
                    if(!ship.getWing().isReturning(ship)){
                        ship.getWing().orderReturn(ship);
                    }
                }
            }
        }
    }
}
