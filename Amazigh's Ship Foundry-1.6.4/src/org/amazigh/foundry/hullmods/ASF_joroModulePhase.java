package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.ShipAPI;

public class ASF_joroModulePhase extends BaseHullMod {
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (!ship.isAlive()) return;
		
		// A "generic" script for a (non-destructible) module that solely exists to give a ship an "extra" drone shipsystem 
		
        ship.setCollisionClass(CollisionClass.NONE);
        ship.setExtraAlphaMult(0f);
        
        if(ship.getParentStation() != null) {
    		if (ship.getParentStation().isPhased()) {
    			ship.setPhased(true);
    		} else {
    			ship.setPhased(false);
    		}        	
        }
        
	}
}