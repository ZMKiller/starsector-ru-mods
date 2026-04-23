package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

public class ASF_spellbindWeaponScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	
    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.05f);
    private final IntervalUtil interval2 = new IntervalUtil(0.1f, 0.1f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        // if the weapon is charging, then we spawn some visual effects
        if ((weapon.getChargeLevel() > 0f) && (weapon.getCooldownRemaining() == 0f)) {
        	interval.advance(amount);
        	interval2.advance(amount);
	        if (interval.intervalElapsed()) {
	        	
	        	Vector2f point = weapon.getFirePoint(0);
	        	float projAngle = weapon.getCurrAngle();
	        	
	        	for (int i=0; i < 7; i++) {
	        		
	    			engine.addSmoothParticle(MathUtils.getRandomPointInCircle(point, 40f),
	    					MathUtils.getRandomPointInCircle(null, 20f),
	    					MathUtils.getRandomNumberInRange(5f, 15f), //size
	    					1.0f, //brightness
	    					0.45f, //duration
	    					new Color(190,255,125,80));
	    			
	    			
	    			engine.addSmoothParticle(MathUtils.getRandomPointInCircle(point, 5f),
	    					MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 150f), projAngle + MathUtils.getRandomNumberInRange(-5f, 5f)),
	    					MathUtils.getRandomNumberInRange(4f, 14f), //size
	    					1.0f, //brightness
	    					0.5f, //duration
	    					new Color(MathUtils.getRandomNumberInRange(190, 220),MathUtils.getRandomNumberInRange(220, 255),125,80));
	    			
	        		/*
	        		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
	    			engine.addSmoothParticle(MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(45f, 60f), angle + projAngle),
	    					MathUtils.getPointOnCircumference(null, 125f, angle + (360f - projAngle)),
	    					MathUtils.getRandomNumberInRange(5f, 15f), //size
	    					1.0f, //brightness
	    					0.45f, //duration
	    					new Color(190,255,125,80));
	    			*/
	    		}
	        }
	        if (interval2.intervalElapsed()) {
	        	Vector2f point = weapon.getFirePoint(0);
	        	
	        	engine.addNebulaParticle(MathUtils.getRandomPointInCircle(point, 10f),
	        			MathUtils.getRandomPointInCircle(null, 3f),
						50f,
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.7f,
						0.2f,
						MathUtils.getRandomNumberInRange(0.35f, 0.5f),
						new Color(215,255,120,100),
						false);
	        }
        }
        
    }
    

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	Vector2f point = projectile.getLocation();
    	float projAngle = projectile.getFacing();
    	
    	engine.addPlugin(new ASF_spellbindProjScript(projectile));
    	
    	for (int i=0; i < 28; i++) {
			float angle = MathUtils.getRandomNumberInRange(0f, 360f);
			engine.addSmoothParticle(MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(55f, 75f), angle + projAngle),
					MathUtils.getPointOnCircumference(null, 150f, angle + (360f - projAngle)),
					MathUtils.getRandomNumberInRange(5f, 15f), //size
					1.0f, //brightness
					0.45f, //duration
					new Color(190,255,125,80));
		}
    	
        MagicLensFlare.createSharpFlare(
			    engine,
			    weapon.getShip(),
			    point,
			    5,
			    290,
			    projAngle + 90f,
			    new Color(90,105,50),
				new Color(190,200,160));
        
    }
}
