package org.amazigh.foundry.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_AdaptiveBurstAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check four to five times a second, to make it slightly random, but also RESPONSIVE
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.25f);
    
    private static final float RANGE = 1600f; // system range
    private float THREAT = 0f;
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }

    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        
    	// don't check if paused / can't use the system
    	if (engine.isPaused() || !AIUtils.canUseSystemThisFrame(ship)) {
            THREAT = 0f;
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
        
        // if we have a target that is within range we then check if:
        	// we are over 80% flux (so at max system power)
        	// the threat counter is over 12
        // if none of the above are valid, then increment the threat counter by a value from 1 to 4 that scales with current flux level AND current hard flux level
        	// This means you will see it activate more readily when more flux is in use, and even MORE readily when taking fire (hardflux)
        // threat counter is reset if the enemy leaves the systems range
        if (targetLocation == null) {
        	return;
        } else if (MathUtils.isWithinRange(ship, target, RANGE)) {
        	if (ship.getFluxLevel() >= 0.8f || THREAT > 12f) {
        		useMe = true;
        	} else {
        		float THREAT_BOOST_S = 1f + ship.getFluxLevel();
        		float THREAT_BOOST_H = 1f + ship.getHardFluxLevel();
          	  	THREAT += (0.5f * ((THREAT_BOOST_S * THREAT_BOOST_S) + (THREAT_BOOST_H * THREAT_BOOST_H)));
        	}
        } else {
        	THREAT = 0f;
        }
        
        if (useMe) {
            ship.useSystem();
        }

    }
}
