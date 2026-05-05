package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class rr_ItanoAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check two to five times a second, to make it even more random.
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.5f);
    
    private static final float RANGE = 1000f; // system range
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
        	// target is over 50% flux / has taken hull damage (so they are considered "vulnerable")
        	// we have taken hull damage / are over 60% flux (so we are "in danger" / "panicking")
        	// the threat counter has hit 10
        // if none of the above are valid, then increment the threat counter by 1
        // threat counter is reset if the enemy leaves the systems range
        if (targetLocation == null) {
        	return;
        } else if (MathUtils.isWithinRange(ship, target, RANGE)) {
        	if (target.getFluxLevel() >= 0.5f || target.getHullLevel() < 0.99f || ship.getHullLevel() < 0.99f || ship.getFluxLevel() >= 0.6f || THREAT > 10f) {
        		useMe = true;
        	} else {
        	  THREAT += 1f;
        	}
        } else {
          THREAT = 0f;
        }
        
        if (useMe) {
            ship.useSystem();
        }

    }
}
