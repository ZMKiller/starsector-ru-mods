// Based on the MagicMissileAI script By Tartiflette.
// A "flashy" / "advanced" MISSILE_TWO_STAGE_SECOND_UNGUIDED   to sell things short
package org.amazigh.foundry.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.magiclib.util.MagicTargeting;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_AlbatreosMagicMissileAI implements MissileAIPlugin, GuidedMissileAI {

    //////////////////////
    //     SETTINGS     //
    //////////////////////

    // degree of accuracy that is needed for the missile to progress into the "ARMED" state
    private final float LOCK_ANGLE=20;

    // how long to wait before starting up the engine after "locking" on to the target. (reduced to 0.7f with ECCM)
    private float DELAY=1.0f;
    
    // what fraction of turn rate to drop to after "locking" on to the target.
    private final float DECAY_TURN=0.45f;

    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING=0.1f;
    
    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH=true;
    
    //Does the missile find a random target or aways tries to hit the ship's one?    
    /*
     *  NO_RANDOM,
     * If the launching ship has a valid target within arc, the missile will pursue it.
     * If there is no target, it will check for an unselected cursor target within arc.
     * If there is none, it will pursue its closest valid threat within arc.    
     *
     *  LOCAL_RANDOM, 
     * If the ship has a target, the missile will pick a random valid threat around that one. 
     * If the ship has none, the missile will pursue a random valid threat around the cursor, or itself.
     * Can produce strange behavior if used with a limited search cone.
     * 
     *  FULL_RANDOM, 
     * The missile will always seek a random valid threat within arc around itself.
     * 
     *  IGNORE_SOURCE,
     * The missile will pick the closest target of interest. Useful for custom MIRVs.
     * 
    */
    private final MagicTargeting.targetSeeking seeking = MagicTargeting.targetSeeking.NO_RANDOM;
    
    //Target class priorities
    //set to 0 to ignore that class
    private final int fighters=0;
    private final int frigates=1;
    private final int destroyers=2;
    private final int cruisers=3;
    private final int capitals=4;
    
    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE_START=360;
    private final int SEARCH_CONE_ARMED=90;
    
    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 3600;
    
    //should the missile fall back to the closest enemy when no target is found within the search parameters
    //only used with limited search cones
    private final boolean FAILSAFE = false;
    
    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE=500;
    
    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    
    //////////////////////
    //    VARIABLES     //
    //////////////////////
    
    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Random starting offset for the waving.
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private Vector2f targetPoint = new Vector2f();
    private boolean launch=true;
    private float timer=0, check=0f, arming=0f;
    private boolean ARMED = false;
    private boolean FIRED = false;
    
    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ASF_AlbatreosMagicMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
            DELAY = 0.7f;
        }        
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
        	
        	int SEARCH_CONE = SEARCH_CONE_START;
        	if (ARMED) {SEARCH_CONE = SEARCH_CONE_ARMED;}
        	
            setTarget(
                    MagicTargeting.pickTarget(
                        MISSILE,
                        seeking,
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

            //forced deceleration/acceleration by default
            if (ARMED) {
                MISSILE.giveCommand(ShipCommand.ACCELERATE);
            } else {
                MISSILE.giveCommand(ShipCommand.DECELERATE);
            }
            
            return;
        }
        
        timer+=amount;
        //finding lead point to aim to        
        if(launch || timer>=check){
            launch=false;
            timer -=check;
            
            //best intercepting point
            lead = AIUtils.getBestInterceptPoint(
            		MISSILE.getLocation(),
            		MAX_SPEED*ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
            		target.getLocation(),
            		target.getVelocity()
            		);
            //null pointer protection
            if (lead == null) {
            	lead = target.getLocation(); 
            }
            
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(lead, targetPointRotated, lead);
            
        }
        
        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                        MISSILE.getLocation(),
                        lead
                );
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        // if armed, then accelerate/ do the initial boost
        if (ARMED) {
    		MISSILE.giveCommand(ShipCommand.ACCELERATE);
        	if (!FIRED) {
    			FIRED = true;
    			
    			Vector2f loc = (Vector2f) (MISSILE.getLocation());
    			Global.getSoundPlayer().playSound("hurricane_mirv_split", 1.4f, 1.2f, loc, MISSILE.getVelocity());
    			// "hurricane_mirv_split", 1.4f, 1.2f

    			// impulse
    			MISSILE.getVelocity().set(MathUtils.getPointOnCircumference(null, 600f, MISSILE.getFacing()));
    			
    			// CombatUtils.applyForce((CombatEntityAPI) MISSILE, MISSILE.getFacing(), 100f);
    			
				// some core smoke
    			engine.addNebulaSmokeParticle(loc,
    					MathUtils.getRandomPointOnCircumference(null, 1f),
                		18f, //size
                		2.5f, //end mult
                		0.5f, //ramp fraction
                		0.75f, //full bright fraction
                		1.0f, //duration
                		new Color(190,185,180,100));
    			for (int i=0; i < 4; i++) {
                    Vector2f puffRandomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(4f, 18f));
                	engine.addSmokeParticle(loc, puffRandomVel, MathUtils.getRandomNumberInRange(20f, 40f), 0.8f, 0.8f, new Color(110,100,90,100));
                	
                	// side + back sprays
                    for (int j=0; j < 2; j++) {
                    	// left smoke
                    	Vector2f smokeRandomVelL = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(29f, 41f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-120f, -60f));
                    	engine.addNebulaSmokeParticle(loc,
                        		smokeRandomVelL,
                        		MathUtils.getRandomNumberInRange(10f, 20f), //size
                        		2.2f, //end mult
                        		0.5f, //ramp fraction
                        		0.75f, //full bright fraction
                        		MathUtils.getRandomNumberInRange(0.9f, 1.3f), //duration
                        		new Color(190,185,180,120));
                    	// right smoke
                    	Vector2f smokeRandomVelR = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(29f, 41f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(120f, 60f));
                    	engine.addNebulaSmokeParticle(loc,
                        		smokeRandomVelR,
                        		MathUtils.getRandomNumberInRange(10f, 20f), //size
                        		2.2f, //end mult
                        		0.5f, //ramp fraction
                        		0.75f, //full bright fraction
                        		MathUtils.getRandomNumberInRange(0.9f, 1.3f), //duration
                        		new Color(190,185,180,120));
//                    	for (int jj=0; jj < 2; jj++) {
//                        	// left sparks
//                        	Vector2f sparkRandomVelL = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(36f, 55f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-115f, -65f));
//                        	engine.addSmoothParticle(loc,
//                        			sparkRandomVelL,
//                        			MathUtils.getRandomNumberInRange(3f, 6f), //size
//                        			1.0f, //brightness
//                        			1.0f, //duration
//                        			new Color(255,172,69,225));
//                        	// right sparks
//                        	Vector2f sparkRandomVelR = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(36f, 55f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(115f, 65f));
//                        	engine.addSmoothParticle(loc,
//                        			sparkRandomVelR,
//                        			MathUtils.getRandomNumberInRange(3f, 6f), //size
//                        			1.0f, //brightness
//                        			1.0f, //duration
//                        			new Color(255,172,69,225));
//                        	// back sparks
//                        	Vector2f sparkRandomVelB = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(36f, 73f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(170f, 190f));
//                        	engine.addSmoothParticle(loc,
//                        			sparkRandomVelB,
//                        			MathUtils.getRandomNumberInRange(3f, 8f), //size
//                        			1.0f, //brightness
//                        			1.2f, //duration
//                        			new Color(255,172,69,255));
//                    	}
                		// frontal smoke jet/trail
                    	for (int k=0; k < 5; k++) {
                    		float jetDist = (i * 18f) + (k * 3f) + MathUtils.getRandomNumberInRange(0f, 15f); // 21-36 - 87-102
                    		int jetAlpha = 140 - ((int) jetDist);
                    		Vector2f smokeRandomVelJ = MathUtils.getPointOnCircumference(null, jetDist * 2.5f, MISSILE.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f));
                        	engine.addNebulaSmokeParticle(loc,
                            		smokeRandomVelJ,
                            		MathUtils.getRandomNumberInRange(13f, 20f) - (i * 2), //size
                            		2.2f, //end mult
                            		0.5f, //ramp fraction
                            		0.5f, //full bright fraction
                            		0.25f + (jetDist * 0.02f), //duration
                            		new Color(190,185,180,jetAlpha));
                    	}
                    }
    			}

            	// left sparks
    			ASF_RadialEmitter emitterLeft = new ASF_RadialEmitter(null);
    			emitterLeft.location(loc);
    			emitterLeft.angle(MISSILE.getFacing() - 115f, 50f);
    			emitterLeft.life(1f, 1f);
    			emitterLeft.size(3f, 6f);
    			emitterLeft.velocity(30f, 25f);
    			emitterLeft.color(255,172,69,225);
    			emitterLeft.burst(16);
            	// right sparks
    			ASF_RadialEmitter emitterRight = new ASF_RadialEmitter(null);
    			emitterRight.location(loc);
    			emitterRight.angle(MISSILE.getFacing() + 65f, 50f);
    			emitterRight.life(1f, 1f);
    			emitterRight.size(3f, 6f);
    			emitterRight.velocity(30f, 25f);
    			emitterRight.color(255,172,69,225);
    			emitterRight.burst(16);
            	// back sparks
    			ASF_RadialEmitter emitterBack = new ASF_RadialEmitter(null);
    			emitterBack.location(loc);
    			emitterBack.angle(MISSILE.getFacing() + 170f, 20f);
    			emitterBack.life(1.2f, 1.2f);
    			emitterBack.size(3f, 8f);
    			emitterBack.velocity(23f, 50f);
    			emitterBack.color(255,172,69,255);
    			emitterBack.burst(19);
    			
    		}
    		
        } else {
    		MISSILE.giveCommand(ShipCommand.DECELERATE);
    		
            if (Math.abs(aimAngle) < LOCK_ANGLE){
                // if we're aimed at the target then increment the arming timer.
        		if (arming < DELAY) {
        			arming += amount;
        		} else {
            		ARMED = true;
        		}
        	}
        }
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        
        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
        
        // Clamps angular velocity if the missile is in the "locked" state
        if (ARMED) {
        	float currTurnLimit = DECAY_TURN * MISSILE.getMaxTurnRate();
        	if (Math.abs(MISSILE.getAngularVelocity()) > currTurnLimit) {
        		float decayMult = currTurnLimit / Math.abs(MISSILE.getAngularVelocity());
        		MISSILE.setAngularVelocity(MISSILE.getAngularVelocity() * decayMult);
        	}
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