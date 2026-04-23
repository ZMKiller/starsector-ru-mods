package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ASF_ammonHullmod extends BaseHullMod {
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("AMMON_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        if(!info.INIT) {
        	info.L_TIMER = MathUtils.getRandomNumberInRange(28f, 30f);
        	    	// timer is "semi-randomised", to save all 3 from a deployment exploding at the same time.
        	info.INIT = true;
        }
        
        ship.getMutableStats().getSightRadiusMod().modifyMult(spec.getId(), 0.4f);
        	// so you don't get (potential) massive map awareness from using these
        
        // the self destruct timer, because we need *some* balance in here.
        info.L_TIMER -= amount;
        
        // do some "sparking" fx as lifetime gets low, with rate increasing as timer gets to zero.
        if (info.L_TIMER <= 5f) {
        	info.SPK_TIMER += amount;
        	if (info.SPK_TIMER > Math.max(info.L_TIMER * 0.35f, 0.25f)) {
            	Global.getSoundPlayer().playSound("disabled_small", MathUtils.getRandomNumberInRange(0.9f, 1.2f), MathUtils.getRandomNumberInRange(0.4f, 0.6f), ship.getLocation(), ship.getVelocity());
            	
            	float distanceRandom1 = MathUtils.getRandomNumberInRange(12f, 26f);
        		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
                Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
                
                float distanceRandom2 = MathUtils.getRandomNumberInRange(12f, 26f);
                float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
                Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
                
                Global.getCombatEngine().spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
        				new Color(25,100,155,45),
        				new Color(175,220,255,50));
            	
                Global.getCombatEngine().addHitParticle(ship.getLocation(), //position
        				ship.getVelocity(), //velocity
            			25f, //size
            			0.8f, //brightness
            			0.1f, //duration
            			new Color(80,170,255,255));
                
    			for (int i=0; i < 6; i++) {
                    Global.getCombatEngine().addSmoothParticle(ship.getLocation(), //position
                			MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(35f, 90f)), //velocity
                			MathUtils.getRandomNumberInRange(4f, 9f), //size
                			0.7f, //brightness
                			MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
                			new Color(90,150,255,255));
    			}
                
                
            	info.SPK_TIMER = 0f;
        	}
        }
        
        // when timer hits zero, explode the drone
        if (info.L_TIMER <= 0f) {
        	ship.setHitpoints(1f);
        	
        	Global.getCombatEngine().spawnEmpArcPierceShields(ship,
        			ship.getLocation(),
        			ship,
        			ship,
        			DamageType.ENERGY,
        			100f,
        			1f,
        			1000f,
        			"disabled_small_crit",
        			6f,
        			new Color(5,10,15,1),
        			new Color(5,5,5,1));
        }
        
		Global.getCombatEngine().getCustomData().put("AMMON_DATA_KEY" + ship.getId(), info);
	}
	

    private class ShipSpecificData {
        private float L_TIMER = 28f;
        private float SPK_TIMER = 0f;
        private boolean INIT = false;
    }
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
