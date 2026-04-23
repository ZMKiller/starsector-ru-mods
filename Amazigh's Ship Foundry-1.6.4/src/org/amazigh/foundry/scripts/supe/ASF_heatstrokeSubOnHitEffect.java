package org.amazigh.foundry.scripts.supe;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;

public class ASF_heatstrokeSubOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target, final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        float proj_facing = projectile.getFacing();
        
		engine.addNebulaParticle(point, fxVel,
        		MathUtils.getRandomNumberInRange(30f, 40f),
        		1.9f, //endsizemult
        		0.2f, //rampUpFraction
        		0.3f, //fullBrightnessFraction
        		1.1f, //totalDuration
        		new Color(60,40,30,90),
        		true);
        
    	for (int i=0; i < 5; i++) {
			float angle1 = (i * 72f) + MathUtils.getRandomNumberInRange(-6f, 6f);
			
			Vector2f smokePos1 = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(17f, 28f), angle1);
			Vector2f smokePos2 = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(19f, 35f), angle1);
			
            Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(9f, 31f), angle1);
            Vector2f smokeVel2 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(17f, 35f), angle1);
            
            engine.addSwirlyNebulaParticle(smokePos1, smokeVel,
            		MathUtils.getRandomNumberInRange(23f, 32f),
            		1.9f, //endsizemult
            		0.1f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(0.9f, 1.2f), //totalDuration
            		new Color(45,40,50,110),
            		true);
            
            engine.addSwirlyNebulaParticle(smokePos2, smokeVel2,
            		MathUtils.getRandomNumberInRange(21f, 28f),
            		1.7f, //endsizemult
            		0.2f, //rampUpFraction
            		0.35f, //fullBrightnessFraction
            		MathUtils.getRandomNumberInRange(0.4f, 0.6f), //totalDuration
            		new Color(153,60,35,90),
            		true);
            
            for (int j=0; j < 6; j++) {
            	
            	float arcPoint = MathUtils.getRandomNumberInRange(0f, 360f);
            	
            	float sparkRange = MathUtils.getRandomNumberInRange((j - 1f) * 12f, j * 12f);
            	
            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(point, sparkRange, arcPoint);
            	
                Vector2f sparkVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(5f, 20f), arcPoint);
                
                engine.addSmoothParticle(spawnLocation,
                		sparkVel,
        				MathUtils.getRandomNumberInRange(3f, 8f), //size
        				1f, //brightness
        				MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
        				new Color(255,76,58,255));
          
		
            }
    	}
    	
    	for (int i=0; i < 2; i++) {
    		
    		Vector2f flarePoint = MathUtils.getPointOnCircumference(point, 15f + (i * 11f), MathUtils.getRandomNumberInRange(0f, 360f));
    		
        	MagicLensFlare.createSharpFlare(
    			    engine,
    			    projectile.getSource(),
    			    flarePoint,
    			    4,
    			    160 - (i * 10f),
    			    proj_facing + 90f,
    			    new Color(190,40,30),
    				new Color(255,101,77));
    	}
    	
    	
    	if (target instanceof ShipAPI) {
			if (shieldHit) {
				// nothing, cry about it! ;)
			} else {
				
				// hi! thanks VIC, i stole code again lol!
				engine.addPlugin(new EveryFrameCombatPlugin() {
					
	                final float initialFacing = target.getFacing();
	                final float trueDamage = projectile.getDamageAmount() * 2f;
	                float damageLeft = trueDamage;
	                float damagePerTick = 0.1f;
	            	final Vector2f shipRefHitLoc = new Vector2f(point.x - target.getLocation().x, point.y - target.getLocation().y);
	                final IntervalUtil damageTimer = new IntervalUtil(0.8f, 0.8f);
	                final IntervalUtil FXTimer1 = new IntervalUtil(0.1f, 0.1f);
	                final IntervalUtil FXTimer2 = new IntervalUtil(0.8f, 0.8f);
	                boolean combust = false;
	                float glowSize = 34f;
	                
	                @Override
	                public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
	                	
	                }
	                
	                @Override
	                public void advance(float amount, List<InputEventAPI> events) {
	                    if (engine.isPaused()) return;
	                    damageTimer.advance(amount);
	                    FXTimer1.advance(amount);
	                    FXTimer2.advance(amount);
	                    
//	                    Vector2f hitLoc = new Vector2f();
//	                    if (damageTimer.intervalElapsed() || FXTimer1.intervalElapsed() || FXTimer2.intervalElapsed()) {
//	                        hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
//	                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);
//	                    }
	                    // changed to a less performant method, to allow for playing a looping sound effect
	                    Vector2f hitLoc = VectorUtils.rotate(new Vector2f(shipRefHitLoc), target.getFacing() - initialFacing);
                        hitLoc = new Vector2f(hitLoc.x + target.getLocation().x, hitLoc.y + target.getLocation().y);
	                    
	                    if (((ShipAPI) target).getFluxTracker().isOverloaded()) {
	                    	if (!combust) {
	                    		damageLeft *= 2f;
	                    	}
	                    	combust = true;
	                    	glowSize = 45f;
	                    }

		                Global.getSoundPlayer().playLoop("disintegrator_loop", target, 0.8f, 0.7f + (((ShipAPI) target).getFluxLevel() * 0.25f), hitLoc, target.getVelocity());
		                
		                
	                    if (damageTimer.intervalElapsed()) {
	                    	float fluxMult = 1f + ((ShipAPI) target).getFluxLevel();
	                    	engine.applyDamage(target, hitLoc, trueDamage * damagePerTick * fluxMult, DamageType.HIGH_EXPLOSIVE, 0, true, true, projectile.getSource());
	                    	
	                    	damageLeft -= (trueDamage * damagePerTick);
	                    }
	                    
	                    
	                    if (FXTimer1.intervalElapsed()) {
	                    	float fluxMult = 1f + ((ShipAPI) target).getFluxLevel();
	                    	
	                    	engine.addSwirlyNebulaParticle(hitLoc, target.getVelocity(),
	                        		MathUtils.getRandomNumberInRange(12f, 21f) * fluxMult,
	                        		1.8f, //endsizemult
	                        		0.1f, //rampUpFraction
	                        		0.3f, //fullBrightnessFraction
	                        		MathUtils.getRandomNumberInRange(0.7f, 1.4f), //totalDuration
	                        		new Color(45,40,50,110),
	                        		true);
	                    	
	                    	engine.addNebulaParticle(hitLoc, target.getVelocity(),
	                        		MathUtils.getRandomNumberInRange(14f, 23f) * fluxMult,
	                        		1.7f, //endsizemult
	                        		0.2f, //rampUpFraction
	                        		0.3f, //fullBrightnessFraction
	                        		MathUtils.getRandomNumberInRange(0.6f, 0.8f), //totalDuration
	                        		new Color(40,30,60,90),
	                        		true);
	                    	
	                    	engine.addSwirlyNebulaParticle(hitLoc, target.getVelocity(),
	                        		MathUtils.getRandomNumberInRange(12f, 21f) * fluxMult,
	                        		1.7f, //endsizemult
	                        		0.2f, //rampUpFraction
	                        		0.3f, //fullBrightnessFraction
	                        		MathUtils.getRandomNumberInRange(0.3f, 0.4f), //totalDuration
	                        		new Color(150,60,35,100),
	                        		true);
	                    	
	                    	// the "glow" that might be too questionable to keep....
	                    	engine.addSmoothParticle(hitLoc, target.getVelocity(),
	                    			glowSize * fluxMult, //size
                    				0.9f, //brightness
                    				0.15f, //duration
                    				new Color(255,76,58,175));
	                    	
	                    	if (combust) {
	                    		engine.addNebulaParticle(hitLoc, target.getVelocity(),
		                        		MathUtils.getRandomNumberInRange(22f, 35f) * fluxMult,
		                        		2.0f, //endsizemult
		                        		0.4f, //rampUpFraction
		                        		0.5f, //fullBrightnessFraction
		                        		MathUtils.getRandomNumberInRange(0.7f, 0.9f), //totalDuration
		                        		new Color(200,80,48,95),
		                        		true);
	                    	}
	                    	
	                    	Vector2f spawnLocation = MathUtils.getRandomPointInCircle(hitLoc, 25f);
	                    	Vector2f sparkVel = MathUtils.getRandomPointInCircle(target.getVelocity(), 10f);
	                    	
	                    	engine.addSmoothParticle(spawnLocation,
	                    			sparkVel,
	                    			MathUtils.getRandomNumberInRange(2f, 5f), //size
	                    			0.9f, //brightness
	                    			MathUtils.getRandomNumberInRange(0.3f, 0.45f), //duration
	                    			new Color(255,76,58,225));
	                    	
	                    }
	                    if (FXTimer2.intervalElapsed()) {
	                    	
	                    	float flareAngle = MathUtils.getRandomNumberInRange(0f, 360f);
	                    	Vector2f flarePoint = MathUtils.getPointOnCircumference(hitLoc, MathUtils.getRandomNumberInRange(0f, 19f), flareAngle);
                    		MagicLensFlare.createSharpFlare(
	                			    engine,
	                			    projectile.getSource(),
	                			    flarePoint,
	                			    3.3f,
	                			    110f * (1f + ((ShipAPI) target).getFluxLevel()),
	                			    flareAngle + 90f,
	                			    new Color(85,20,15),
	                				new Color(125,49,37));
	                    	
	                    }
	                    if (damageLeft <= 0f) engine.removePlugin(this);
	                }

	                @Override
	                public void renderInWorldCoords(ViewportAPI viewport) {

	                }

	                @Override
	                public void renderInUICoords(ViewportAPI viewport) {

	                }

	                @Override
	                public void init(CombatEngineAPI engine) {

	                }
	            });
			}
    	}
    	
	}
}
