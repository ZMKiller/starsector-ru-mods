// Based on the MagicMissileAI script By Tartiflette.
// A wacky "drunk" missile AI, that can also randomly detonate in flight
package org.amazigh.foundry.scripts.ai;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_WeaverDrunkRocketAI implements MissileAIPlugin {
	
    //////////////////////
    //     SETTINGS     //
    //////////////////////
    
	// random start timer, detonation timer waits for this before starting.
	private IntervalUtil startInterval = new IntervalUtil(0.5f,0.6f);
	
    // random Self destruct timer.
    private IntervalUtil destructInterval = new IntervalUtil(0.05f,0.15f);
    
    // Weave timer
    private IntervalUtil weaveInterval = new IntervalUtil(0.04f,0.06f);
    
    // how far off of the launch angle the rocket can get before it transitions into "back and forth" mode
    private float sweepAngle = 23f;
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //Random starting offset for the waving.
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private boolean start = false, init = false, turned = false, side = false;
    private float startAngle = 0f;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ASF_WeaverDrunkRocketAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
    }
    
    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////
    
    @Override
    public void advance(float amount) {
        
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        
        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {return;}
        
        MISSILE.giveCommand(ShipCommand.ACCELERATE);
        //forced acceleration always
        
        // add an initial startup delay!
        
        if (!init) {
        	init = true;
        	startAngle = MISSILE.getWeapon().getCurrAngle();
        }
        
        startInterval.advance(amount);
		if (startInterval.intervalElapsed()) {
			start = true;
		}
        
        if (start) {
            destructInterval.advance(amount);
    		if (destructInterval.intervalElapsed()) {
    			
    			if (Math.random() > 0.96f) {
    				
    				//do the explosion
    				Vector2f blastVel = MathUtils.getRandomPointInCircle(null, 5f);

    				Vector2f loc = (Vector2f) (MISSILE.getLocation());
    				
    				Global.getSoundPlayer().playSound("hit_hull_light", 0.9f, 1.2f, loc, blastVel);
    				
    				float timeMult = (MISSILE.getMaxFlightTime() - MISSILE.getElapsed())  / MISSILE.getMaxFlightTime();
    				
        			for (int i=0; i < 2; i++) {
        				engine.addNebulaSmokeParticle(loc,
        						blastVel,
        						35f + (25f * timeMult), //size
                        		MathUtils.getRandomNumberInRange(1.5f, 1.8f), //end mult
                        		0.6f, //ramp fraction
                        		0.7f, //full bright fraction
                        		MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
                        		new Color(90,75,70,120));
        			}
    				
    				DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
    		                35f + (25f * timeMult),
    		                25f + (25f * timeMult),
    		                MISSILE.getDamageAmount() * Math.max(0.2f, timeMult),
    		                MISSILE.getDamageAmount() * 0.5f * Math.max(0.2f, timeMult),
    		                CollisionClass.PROJECTILE_FF,
    		                CollisionClass.PROJECTILE_FIGHTER,
    		                2f,
    		                5f,
    		                0.5f,
    		                (int) (30 + (15 * timeMult)),
    		                new Color(255,100,20,255),
    		                new Color(255,160,20,200));
    		        blast.setDamageType(DamageType.HIGH_EXPLOSIVE);
    		        blast.setShowGraphic(true);
    		        blast.setUseDetailedExplosion(false);
    		        
    		        engine.spawnDamagingExplosion(blast,MISSILE.getSource(),loc,false);
    				
    				engine.removeEntity(MISSILE);
    				
    			}
    		}
        }
        
        weaveInterval.advance(amount);
		if (weaveInterval.intervalElapsed()) {
			
			Vector2f loc = (Vector2f) (MISSILE.getLocation());
			
			Vector2f vel = new Vector2f();
			vel.x += MISSILE.getVelocity().x;
			vel.y += MISSILE.getVelocity().y;
			
			Vector2f puffRandomVel1 = MathUtils.getRandomPointOnCircumference((Vector2f) vel.scale(MathUtils.getRandomNumberInRange(0.4f, 0.6f)), MathUtils.getRandomNumberInRange(4f, 20f));
			Vector2f puffRandomVel2 = MathUtils.getRandomPointOnCircumference((Vector2f) vel.scale(MathUtils.getRandomNumberInRange(0.4f, 0.6f)), MathUtils.getRandomNumberInRange(4f, 20f));
			
			engine.addSmokeParticle(loc, puffRandomVel1, MathUtils.getRandomNumberInRange(7f, 13f), 0.7f, 0.3f, new Color(80,75,75,90));
            engine.addNebulaSmokeParticle(loc,
            		puffRandomVel2,
            		MathUtils.getRandomNumberInRange(8f, 15f), //size
            		1.9f, //end mult
            		0.5f, //ramp fraction
            		0.5f, //full bright fraction
            		0.33f, //duration
            		new Color(80,75,75,90));
			
			float facing = MISSILE.getFacing();
			facing += MathUtils.getRandomNumberInRange(-2f, 2f);
			MISSILE.setFacing(facing);
		}
        
		// if the rocket has weaved "really far" to one side, then transition into a "back and forth" sweep, otherwise just do some extra random weaving.
		float aimAngle = MathUtils.getShortestRotation(MISSILE.getFacing(), startAngle);
		if (aimAngle < -sweepAngle) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
            turned = true;
            side = true;
        }
		if (aimAngle > sweepAngle) {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
            turned = true;
            side = false;
        }
		if (!turned) {
	        if (Math.random() < 0.5f) {
	            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
	        } else {
	            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
	        }
		} else {
			if (side) {
	            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
	        } else {
	            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
	        }
		}
		
        
    }
   
    public void init(CombatEngineAPI engine) {}
}