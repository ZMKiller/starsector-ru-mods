package org.amazigh.foundry.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import org.lwjgl.util.vector.Vector2f;

public class ASF_MicroVentAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;

    // check four-to-five times a second, we want this to be pretty responsive.
    private IntervalUtil timer = new IntervalUtil(0.2f, 0.25f);
    
    private int VALUE = 0;
    
    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.engine = engine;
    }
    
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
    	// don't check if paused / can't use the system
    	if (engine.isPaused()) {
            return;
        }
    	
    	if (!ship.getPhaseCloak().isActive()) {
    		if (ship.getPhaseCloak().getCooldownRemaining() > 0f) {
    			return;
    		}
    	}
    	    	
        // don't check if timer not up
        timer.advance(amount);
        if (!timer.intervalElapsed()) {
            return;
        }
        
        // alter VALUE based on current flux level
        	// if flux is under 50%, then: remove 1 (down to a min of 0)
        	// if flux is over 50%, then: add 1
        
        if (ship.getFluxLevel() < 0.5f) {
        	VALUE = Math.max(0, VALUE - 1);
        } else {
            VALUE += 1;
        }
        
        // if VALUE is over 5, use the system
        
        if (VALUE > 5) {
        	ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null,0);
        	VALUE = 0;
        }
        
        

    }
}
