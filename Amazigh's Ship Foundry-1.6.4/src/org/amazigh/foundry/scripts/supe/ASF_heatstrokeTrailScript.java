package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.Color;
import java.util.List;

public class ASF_heatstrokeTrailScript extends BaseEveryFrameCombatPlugin {
	
	private DamagingProjectileAPI projectile;
	final IntervalUtil FXTimer1 = new IntervalUtil(0.05f, 0.05f);
    final IntervalUtil FXTimer2 = new IntervalUtil(0.1f, 0.25f); // 0.15,0.3
	int decay1 = 0;
	int decay2 = 0;
	int decay3 = 0;
	int projCount = 10;
	private Vector2f parentVel;
	
	public ASF_heatstrokeTrailScript(@NotNull DamagingProjectileAPI projectile) {
		this.projectile = projectile;
		parentVel = new Vector2f(projectile.getSource().getVelocity());
		
	}
	
	//Main advance method
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		//Sanity checks
		if (Global.getCombatEngine() == null) {
			return;
		}
		CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused()) {
			amount = 0f;
		}
		
		//Checks if our script should be removed from the combat engine
		if (projectile == null || projectile.didDamage() || !engine.isEntityInPlay(projectile)) {
			engine.removePlugin(this);
			return;
		}
		
		// spawn a fancy trail
		FXTimer1.advance(amount);
		if (!projectile.isFading()) {
	        FXTimer2.advance(amount);	
		}
        
        if (FXTimer1.intervalElapsed()) {
        	
        	int alpha1 = Math.max(0,250 - decay1);
        	int alpha2 = Math.max(0,110 - decay2);
        	int alpha3 = Math.max(0,90 - decay3);
        	
        	if (projectile.isFading()) {
        		decay1 += 15;
    	        decay2 += 7;
    	        decay3 += 6;
    		}
        	
        	for (int i=0; i < 2; i++) {
        		
                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 20f),
                		MathUtils.getRandomPointInCircle(null, 5f),
        				MathUtils.getRandomNumberInRange(2f, 6f), //size
        				0.9f, //brightness
        				MathUtils.getRandomNumberInRange(0.4f, 0.8f), //duration
        				new Color(255,76,58,alpha1));
                
			}
			
        	engine.addSwirlyNebulaParticle(projectile.getLocation(),
        			MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(0f, 18f), projectile.getFacing() + MathUtils.getRandomNumberInRange(-9f, 9f)),
        			MathUtils.getRandomNumberInRange(32f, 39f),
            		1.9f, //endsizemult
            		0.1f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.35f, 2.1f), //totalDuration
            		new Color(45,40,50,alpha2),
            		true);
            
            engine.addSwirlyNebulaParticle(projectile.getLocation(),
            		MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(7f, 23f), projectile.getFacing() + MathUtils.getRandomNumberInRange(-10f, 10f)),
            		MathUtils.getRandomNumberInRange(29f, 34f),
            		1.7f, //endsizemult
            		0.2f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(1.0f, 1.5f), //totalDuration
            		new Color(153,60,35,alpha3),
            		true);
        }
        
        if (FXTimer2.intervalElapsed()) {

    		Vector2f point = MathUtils.getRandomPointInCircle(projectile.getLocation(), 9f);
    		
        	if (projCount > 0) {
        		projCount --;
        		
        		MagicLensFlare.createSharpFlare(
        			    engine,
        			    projectile.getSource(),
        			    point,
        			    4f,
        			    MathUtils.getRandomNumberInRange(130f, 160f), // 150f
        			    projectile.getFacing() + 90f,
        			    new Color(64,13,10), //190,40,30
        				new Color(85,34,26)); //255,101,77
        		
        		float randAngle = projectile.getFacing();
        		if (Math.random() > 0.5) {
        			randAngle += MathUtils.getRandomNumberInRange(3f, 12f);
        		} else {
        			randAngle -= MathUtils.getRandomNumberInRange(3f, 12f);
        		}
        		
        		CombatEntityAPI subProjectile = engine.spawnProjectile(projectile.getWeapon().getShip(), projectile.getWeapon(), "A_S-F_heatstroke_sub", point, randAngle, parentVel);
        		engine.addPlugin(new ASF_heatstrokeSubProjScript((DamagingProjectileAPI) subProjectile, parentVel, projectile));
        		
        		} else {
        			engine.addHitParticle(point, projectile.getVelocity(), 21f, 1f, 0.1f, new Color(255,101,77,225));
        		}
        	
        }
        
		
	}
}