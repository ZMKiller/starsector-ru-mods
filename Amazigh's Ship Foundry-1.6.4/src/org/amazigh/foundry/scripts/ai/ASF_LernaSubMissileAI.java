// Based on the MagicMissileAI script By Tartiflette.
package org.amazigh.foundry.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;

import org.magiclib.util.MagicTargeting;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_LernaSubMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
	//////////////////////
	//     SETTINGS     //
	//////////////////////
	
	//Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
	//  Set to a negative value to disable
	private final float OVERSHOT_ANGLE=50;
	
	//Damping of the turn speed when closing on the desired aim. The smaller the snappier.
	private final float DAMPING=0.1f;
	
	//Does the missile switch its target if it has been destroyed?
	private final boolean TARGET_SWITCH=true;
	
	//Target class priorities
	//set to 0 to ignore that class
	private final int fighters=1;
	private final int frigates=2;
	private final int destroyers=3;
	private final int cruisers=4;
	private final int capitals=5;

	//Arc to look for targets into
	//set to 360 or more to ignore
	private final int SEARCH_CONE=360;

	//range in which the missile seek a target in game units.
	private final int MAX_SEARCH_RANGE = 1500;

	//should the missile fall back to the closest enemy when no target is found within the search parameters
	//only used with limited search cones
	private final boolean FAILSAFE = false;

	//range under which the missile start to get progressively more precise in game units.
	private float PRECISION_RANGE=850;

	//Is the missile lead the target or tailchase it?
	private final boolean LEADING=true;
	
	// checks for whether the missile is: "armed" / within range and firing
	private boolean ARMED = false;
	private boolean RANGE = false;
	
	
	// square of range to switch to "attack mode"
	private float DETECT_RANGE=490000; //700^2
	
	// velocity of the shots fired, for target leading purposes
	private float SHOT_VEL=600;
	
	// timers for:  initial delay, "shooting windup" and vfx
	private IntervalUtil delayInterval = new IntervalUtil(0.75f, 0.75f);
	private IntervalUtil startInterval = new IntervalUtil(0.75f, 0.75f);
	private IntervalUtil vfxInterval = new IntervalUtil(0.2f, 0.2f);
	private IntervalUtil jetInterval = new IntervalUtil(0.05f, 0.05f);
	
	//////////////////////
	//    VARIABLES     //
	//////////////////////
	
	//max speed of the missile after modifiers.
	private CombatEngineAPI engine;
	private final MissileAPI MISSILE;
	private CombatEntityAPI target;
	private Vector2f lead = new Vector2f();
	private boolean launch=true;
	private float timer=0, check=0f;
	
	//////////////////////
	//  DATA COLLECTING //
	//////////////////////
	
    public ASF_LernaSubMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
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
        
        //assigning a target if there is none or it got destroyed
        if (target == null
                || (TARGET_SWITCH 
                        && ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                                  || !engine.isEntityInPlay(target) || (target instanceof ShipAPI && ((ShipAPI) target).isPhased()))
                   )
                ){
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        MagicTargeting.targetSeeking.NO_RANDOM,
                        MAX_SEARCH_RANGE,
                        SEARCH_CONE,
                        fighters,
                        frigates, 
                        destroyers,
                        cruisers,
                        capitals, 
                        FAILSAFE
                )
            );
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation())/PRECISION_RANGE)
            );
            if(LEADING){
                //best intercepting point
            	
            	lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        SHOT_VEL,
                        target.getLocation(),
                        target.getVelocity()
                );
                //null pointer protection
                if (lead == null) {
                    lead = target.getLocation(); 
                }
            } else {
                lead = target.getLocation();
            }
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        delayInterval.advance(amount);
		if (delayInterval.intervalElapsed()) {
	        ARMED = true;
		}
        
    	if (!RANGE) {
    		if (ARMED) {
    			if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
        			
        			if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) <= (DETECT_RANGE + target.getCollisionRadius())) {
        				RANGE = true;
        				
        				Vector2f vel = MISSILE.getVelocity();
        				Vector2f loc = MISSILE.getLocation();
        				
        				for (int i=0; i < 9; i++) {
        					Vector2f muzzleLoc = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 21f), MISSILE.getFacing());
        					Vector2f muzzleRandomVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 130f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f));
        					
        					engine.addSmoothParticle(muzzleLoc, muzzleRandomVel, MathUtils.getRandomNumberInRange(3f, 6f), 1f, MathUtils.getRandomNumberInRange(0.2f, 0.25f),  new Color(25,130,130,160));
        				}
        			}
        		}
    		}
    	} else {
        	
        	vfxInterval.advance(amount);
        	jetInterval.advance(amount);
        	
        	if (vfxInterval.intervalElapsed()) {
        		
        		Vector2f vel = MISSILE.getVelocity();
            	Vector2f loc = MISSILE.getLocation();
            	
        		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
                float distanceRandom1 = MathUtils.getRandomNumberInRange(12f, 15f);
                Vector2f arcPoint1 = MathUtils.getPointOnCircumference(loc, distanceRandom1, angleRandom1);
                
                float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(50, 100);
                float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
                Vector2f arcPoint2 = MathUtils.getPointOnCircumference(loc, distanceRandom2, angleRandom2);
                
                engine.spawnEmpArcVisual(arcPoint1, MISSILE, arcPoint2, MISSILE, 11f,
                		new Color(25,130,135,100),
    					new Color(225,255,255,110));
                
        		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1.0f, 0.4f, loc, vel);
        	}
        	
        	if (jetInterval.intervalElapsed()) {

//        		Vector2f vel = MISSILE.getVelocity();
            	Vector2f loc = MISSILE.getLocation();
            	
//        		for (int i=0; i < 9; i++) {
//        			Vector2f muzzleLoc = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), MISSILE.getFacing());
//            		Vector2f muzzleRandomVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 145f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-1f, 1f));
//            		
//            		engine.addSmoothParticle(muzzleLoc, muzzleRandomVel, MathUtils.getRandomNumberInRange(3f, 6f), 1f, MathUtils.getRandomNumberInRange(0.2f, 0.25f),  new Color(25,130,130,160));
//            	}
            	ASF_RadialEmitter emitterJet = new ASF_RadialEmitter((CombatEntityAPI) MISSILE);
        		emitterJet.location(loc);
        		emitterJet.angle(MISSILE.getFacing() -1f, 2f);
        		emitterJet.life(0.2f, 0.25f);
        		emitterJet.size(3f, 6f);
        		emitterJet.velocity(15f, 130f);
        		emitterJet.distance(6f, 14f);
        		emitterJet.color(25,130,130,160);
        		emitterJet.velDistLinkage(false);
        		emitterJet.burst(9);
        		
                Vector2f dampedVel = MISSILE.getVelocity();
                dampedVel.x = dampedVel.x * 0.85f;
                dampedVel.y = dampedVel.y * 0.85f;
                
                MISSILE.getVelocity().set(dampedVel);
        	}
        	
            startInterval.advance(amount);
            
        	if (startInterval.intervalElapsed()) {
        		
        		Vector2f vel = MISSILE.getVelocity();
            	Vector2f loc = MISSILE.getLocation();
            	
        		for (int i=0; i < 3; i++) {
        			
        			engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_lerna_bolt",
        					loc,
        					MISSILE.getFacing() + ((i * 6f) - 6f) + MathUtils.getRandomNumberInRange(-3f, 3f),
        					MathUtils.getRandomPointInCircle(vel, 40f));
    				
        			Vector2f meltLoc = MathUtils.getRandomPointInCircle(loc, 15f);
        			Vector2f meltRandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 35f));
            		
            		engine.addSmoothParticle(meltLoc, meltRandomVel, MathUtils.getRandomNumberInRange(9f, 18f), 1f,
            				MathUtils.getRandomNumberInRange(0.6f, 0.85f),
            				new Color(200,160,50,150));
        			
//        			for (int j=0; j < 5; j++) {
//                		Vector2f muzzleLoc = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(9f, 35f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-18f, 18f));
//                		Vector2f muzzleRandomVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 50f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-18f, 18f));
//                		
//                		engine.addSmoothParticle(muzzleLoc, muzzleRandomVel, MathUtils.getRandomNumberInRange(6f, 14f), 1f, MathUtils.getRandomNumberInRange(0.25f, 0.35f), new Color(100,225,230,255));
//                		
//                		for (int k=0; k < 2; k++) {
//                			Vector2f sparkLoc = MathUtils.getRandomPointInCircle(loc, 15f);
//                			Vector2f sparkRandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 35f));
//                    		
//                    		engine.addSmoothParticle(sparkLoc, sparkRandomVel, MathUtils.getRandomNumberInRange(4f, 10f), 1f,
//                    				MathUtils.getRandomNumberInRange(0.3f, 0.4f),
//                    				new Color(50,180,180,200));
//                		}
//                	}
            	}

        		ASF_RadialEmitter emitter1 = new ASF_RadialEmitter((CombatEntityAPI) MISSILE);
        		emitter1.location(loc);
        		emitter1.angle(MISSILE.getFacing() - 18f, 36f);
        		emitter1.life(0.25f, 0.35f);
        		emitter1.size(6f, 14f);
        		emitter1.velocity(15f, 35f);
        		emitter1.distance(9f, 26f);
        		emitter1.color(100,225,230,255);
        		emitter1.velDistLinkage(false);
        		emitter1.burst(15);
        		
        		ASF_RadialEmitter emitter2 = new ASF_RadialEmitter((CombatEntityAPI) MISSILE);
        		emitter2.location(loc);
        		emitter2.life(0.3f, 0.4f);
        		emitter2.size(4f, 10f);
        		emitter2.velocity(15f, 35f);
        		emitter2.color(50,180,180,200);
        		emitter2.coreDispersion(15f);
        		emitter2.burst(30);
        		
        		engine.addSmoothParticle(loc, vel, 69f, 1f, 0.1f, new Color(100,230,235,255));
        		engine.addSmokeParticle(loc, vel, 30f, 0.9f, 0.7f, new Color(100,95,90,70));
        		Global.getSoundPlayer().playSound("heavy_blaster_fire", 1.2f, 0.9f, loc, vel);
        		
        		for (int j = 0; j < 3; j++) {
                	engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(loc, 12f),
                    		vel,
                    		MathUtils.getRandomNumberInRange(30f, 33f), //size
                    		2.4f, //end mult
                    		0.65f, //ramp fraction
                    		0.7f, //full bright fraction
                    		MathUtils.getRandomNumberInRange(0.65f, 0.9f), //duration
                    		new Color(90,95,105,70));
                }
        		
        		engine.removeEntity(MISSILE);
        	}
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }
        
        MISSILE.giveCommand(ShipCommand.DECELERATE);
        MISSILE.giveCommand(ShipCommand.ACCELERATE);
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }
    
    //////////////////////
    //    TARGETING     //
    //////////////////////
    
    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
    
    public void init(CombatEngineAPI engine) {}
}