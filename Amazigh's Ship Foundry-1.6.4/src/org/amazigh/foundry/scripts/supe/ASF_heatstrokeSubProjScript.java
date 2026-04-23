// Featuring things taken from the [MagicGuidedProjectileScript] by Nicke535
package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import java.awt.Color;
import java.util.List;

public class ASF_heatstrokeSubProjScript extends BaseEveryFrameCombatPlugin {
	
	private DamagingProjectileAPI proj;
	private DamagingProjectileAPI masterProj;
	final IntervalUtil FXTimer1 = new IntervalUtil(0.05f, 0.05f);
	int decay1 = 0;
	int decay2 = 0;
	int decay3= 0;
	
	private float waveCounter1;
	private float waveCounter2;
	private float waveVal1 = 50f;
	private float waveVal2 = 8f;
	private static final float WAVE_TIME_1 = 0.9f;
	private static final float WAVE_TIME_2 = 1.5f;
	private float lifeCounter; // Keeps track of projectile lifetime
	private float estimateMaxLife; // How long we estimate this projectile should be alive
	private float TURN_RATE = 32f; // uh, idk, how well it copes with the parent getting thrown off course (if that somehow happens lol)
	float accelRate = 150f; // we accelerate at this rate/sec, so at longer ranges you will get the sub projs in front of the main one!
	
	public ASF_heatstrokeSubProjScript(@NotNull DamagingProjectileAPI projectile, Vector2f parentVel, DamagingProjectileAPI masterProjectile) {
		this.proj = projectile;
		this.masterProj = masterProjectile;
		
		waveCounter1 = MathUtils.getRandomNumberInRange(0f, 1.8f);
		waveCounter2 = MathUtils.getRandomNumberInRange(0f, 3f);
		
		lifeCounter = 0f;
		estimateMaxLife = projectile.getWeapon().getRange() / new Vector2f(projectile.getVelocity().x - parentVel.x, projectile.getVelocity().y - parentVel.y).length();
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
			return;
		}
		
		//Checks if our script should be removed from the combat engine
		if (proj == null || proj.didDamage() || !engine.isEntityInPlay(proj)) {
			engine.removePlugin(this);
			return;
		}
		
		// backup panic mode for when a subproj outlives the master!
		if (!engine.isEntityInPlay(masterProj) || masterProj.isFading()) {
			masterProj = proj;
			accelRate = -300f; // mega inverted accel if the master is no more / is fading!
		}
		
		if (!proj.isFading()) {
			
			lifeCounter+=amount;
			if (lifeCounter > estimateMaxLife) { lifeCounter = estimateMaxLife; }
			
			waveCounter1 += amount*WAVE_TIME_1;
			waveCounter2 += amount*WAVE_TIME_2;
			
			float swayThisFrame = (float)Math.pow(1f - (lifeCounter / estimateMaxLife), 0.5f) *
					((float)(FastTrig.sin(Math.PI * 2f * waveCounter1) * waveVal1) + (float)(FastTrig.sin(Math.PI * 2f * waveCounter2) * waveVal2));
			
			
			float thisAccel = (amount * accelRate);
			
			// doing the weave!
			float facingSwayless = proj.getFacing() - swayThisFrame;
			float angleToHit = VectorUtils.getAngle(proj.getLocation(), MathUtils.getPointOnCircumference(masterProj.getLocation(), 10000f, masterProj.getFacing()));
			float angleDiffAbsolute = Math.abs(angleToHit - facingSwayless);
			while (angleDiffAbsolute > 180f) { angleDiffAbsolute = Math.abs(angleDiffAbsolute-360f);}
			facingSwayless += Misc.getClosestTurnDirection(facingSwayless, masterProj.getFacing()) * Math.min(angleDiffAbsolute, TURN_RATE*amount);
			proj.setFacing(facingSwayless + swayThisFrame);
			proj.getVelocity().x = MathUtils.getPoint(new Vector2f(Misc.ZERO), Math.max(150f, proj.getVelocity().length()) + thisAccel, facingSwayless+swayThisFrame).x;
			proj.getVelocity().y = MathUtils.getPoint(new Vector2f(Misc.ZERO), Math.max(150f, proj.getVelocity().length()) + thisAccel, facingSwayless+swayThisFrame).y;
			
		}
		
		
		// spawn a fancy trail
		FXTimer1.advance(amount);
        if (FXTimer1.intervalElapsed()) {
        	
        	int alpha1 = Math.max(0,230 - decay1);
        	int alpha2 = Math.max(0,110 - decay2);
        	int alpha3 = Math.max(0,90 - decay3);
        	
        	if (proj.isFading()) {
        		decay1 += 14;
    	        decay2 += 7;
    	        decay3 += 6;
    		}
        	
        	for (int i=0; i < 2; i++) {
        		
                engine.addSmoothParticle(MathUtils.getRandomPointInCircle(proj.getLocation(), 15f),
                		MathUtils.getRandomPointInCircle(null, 5f),
        				MathUtils.getRandomNumberInRange(2f, 6f), //size
        				0.9f, //brightness
        				MathUtils.getRandomNumberInRange(0.35f, 0.6f), //duration
        				new Color(255,76,58,alpha1));
                
			}
			
        	engine.addSwirlyNebulaParticle(proj.getLocation(),
        			MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(0f, 24f), proj.getFacing() + MathUtils.getRandomNumberInRange(-9f, 9f)),
        			MathUtils.getRandomNumberInRange(16f, 19f),
            		1.9f, //endsizemult
            		0.1f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(0.8f, 1.45f), //totalDuration
            		new Color(45,40,50,alpha2),
            		true);
            
            engine.addSwirlyNebulaParticle(proj.getLocation(),
            		MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 31f), proj.getFacing() + MathUtils.getRandomNumberInRange(-10f, 10f)),
            		MathUtils.getRandomNumberInRange(14f, 17f),
            		1.7f, //endsizemult
            		0.2f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(0.6f, 0.95f), //totalDuration
            		new Color(153,60,35,alpha3),
            		true);
        }
        
		
	}
}