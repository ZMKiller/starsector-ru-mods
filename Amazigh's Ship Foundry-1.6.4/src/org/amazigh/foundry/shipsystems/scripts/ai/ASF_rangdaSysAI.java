package org.amazigh.foundry.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_rangdaSysAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check four to five times a second, to make it slightly random, but also RESPONSIVE
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.25f);
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        
    	// don't check if paused / can't use the system
    	if (engine.isPaused() || !AIUtils.canUseSystemThisFrame(ship)) {
            return;
        }
        // don't check if timer not up
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }
        
        // setup variables
        boolean useMe = false;
        Vector2f targetLocation = null;

        // assign our target location to whatever ship we are attacking
        if (target != null && target.getOwner() != ship.getOwner()) {
            targetLocation = target.getLocation();
        }

        float range = 0f;
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getType() == WeaponType.BALLISTIC) {
				float curr = w.getRange();
				if (curr > range) range = curr;
			}
		}
		
        float TrueRange = ship.getMutableStats().getBallisticWeaponRangeBonus().computeEffective(range);
		
        // if an enemyis within weapons range and flux is lowish, activate
        // if we have no enemies in range / flux is high, deactivate
        if (targetLocation == null) {
        	return;
        } else if (MathUtils.isWithinRange(ship, target, TrueRange)) {
        	if (!ship.getSystem().isActive()) {
        		if (ship.getFluxLevel() <= 0.5f) {
            		useMe = true;
            	}        		
        	} else {
        		if (ship.getFluxLevel() >= 0.9f) {
            		useMe = true;
            	} 
        	}
        } else if (ship.getSystem().isActive()) {
    		useMe = true;
        }
        
        if (useMe) {
            ship.useSystem();
        }

    }
}
