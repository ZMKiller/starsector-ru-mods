package data.hullmods;

import org.lazywizard.lazylib.combat.AIUtils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.Misc;

public class HSIFlankerStrategy extends BaseHullMod{

    public void advanceInCombat(ShipAPI ship, float amount) {   
        if(ship.getWing()!=null&&ship.getWing().getSourceShip()!=null){
            ShipAPI source = ship.getWing().getSourceShip();
            if(Math.random()>0.15f*amount*60f){

            }
        }
    }
}
