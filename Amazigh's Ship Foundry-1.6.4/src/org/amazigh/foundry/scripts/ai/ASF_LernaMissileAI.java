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
import org.magiclib.util.MagicTargeting;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class ASF_LernaMissileAI implements MissileAIPlugin, GuidedMissileAI {
    
	//////////////////////
	//     SETTINGS     //
	//////////////////////
	
	//Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
	//  Set to a negative value to disable
	private final float OVERSHOT_ANGLE=60;
	
	//Time to complete a wave in seconds.
	private final float WAVE_TIME=1.9f;
	
	//Max angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
	private final float WAVE_AMPLITUDE=11;
	
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

	//Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
	//   1: perfect leading with and without ECCM
	//   2: half precision without ECCM
	//   3: a third as precise without ECCM. Default
	//   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
	private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
	
	
	
	// checks for whether the missile is:  engine use allowed / within range and firing
	private boolean ENGINE = true;
	private boolean RANGE = false;
	
	// square of range to split at
	private float DETECT_RANGE=250000; //500^2
	
	//////////////////////
	//    VARIABLES     //
	//////////////////////
	
	//max speed of the missile after modifiers.
	private final float MAX_SPEED;
	//Random starting offset for the waving.
	private final float OFFSET;
	private CombatEngineAPI engine;
	private final MissileAPI MISSILE;
	private CombatEntityAPI target;
	private Vector2f lead = new Vector2f();
	private boolean launch=true;
	private float timer=0, check=0f;
	
	//////////////////////
	//  DATA COLLECTING //
	//////////////////////
	
    public ASF_LernaMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")){
            ECCM=1;
        }
        
        //calculate the precision range factor
        PRECISION_RANGE=(float)Math.pow((2*PRECISION_RANGE),2);
        OFFSET=(float)(Math.random()*MathUtils.FPI*2);
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
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
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
            	
        		float leadVel =  MAX_SPEED*ECCM;
        		
            	lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        leadVel,
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
        
        
        if(WAVE_AMPLITUDE>0){            
            //waving
            float multiplier=1;
            if(ECCM<=1){
                multiplier=0.3f;
            }
            correctAngle+=multiplier*WAVE_AMPLITUDE*check*Math.cos(OFFSET+MISSILE.getElapsed()*(2*MathUtils.FPI/WAVE_TIME));
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
        	if (ENGINE) {
        		MISSILE.giveCommand(ShipCommand.ACCELERATE);
        		
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
        
        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }  
        
        
        if (RANGE) {
        	
        	// SPLIT!
    		Vector2f vel = MISSILE.getVelocity();
        	Vector2f loc = MISSILE.getLocation();

        	float angle = MISSILE.getFacing() - 70f;
        	float angle2 = MISSILE.getFacing() + 70f;
        	
        	Vector2f sideMirvVel = MathUtils.getPointOnCircumference(vel, 660f, angle);
        	Vector2f sideMirvVel2 = MathUtils.getPointOnCircumference(vel, 660f, angle2);
        	
        	engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_lerna_sub",
        			loc,
        			MISSILE.getFacing(),
        			sideMirvVel);
        	engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_lerna_sub",
        			loc,
        			MISSILE.getFacing(),
        			sideMirvVel2);
        	
        	for (int j=0; j < 5; j++) {
        		
        		Vector2f jetLoc1 = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), angle);
        		Vector2f jetVel1 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 55f), angle + MathUtils.getRandomNumberInRange(-1f, 1f));
        		Vector2f jetLoc2 = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), angle2);
        		Vector2f jetVel2 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 55f), angle2 + MathUtils.getRandomNumberInRange(-1f, 1f));
        		
        		engine.addNebulaSmokeParticle(jetLoc1,
                		jetVel1,
                		MathUtils.getRandomNumberInRange(10f, 20f), //size
                		1.8f, //end mult
                		0.5f, //ramp fraction
                		0.75f, //full bright fraction
                		0.9f, //duration
                		new Color(65,115,120,100));
        		engine.addNebulaSmokeParticle(jetLoc2,
                		jetVel2,
                		MathUtils.getRandomNumberInRange(10f, 20f), //size
                		1.8f, //end mult
                		0.5f, //ramp fraction
                		0.75f, //full bright fraction
                		0.9f, //duration
                		new Color(65,115,120,100));
        	}
    		
    		engine.spawnProjectile(MISSILE.getSource(), MISSILE.getWeapon(), "A_S-F_lerna_sub",
					loc,
					MISSILE.getFacing(),
					MathUtils.getPointOnCircumference(vel, 200f, MISSILE.getFacing() + 180f));
    		
    		for (int j=0; j < 5; j++) {
				Vector2f jetLoc = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(3f, 12f), MISSILE.getFacing() + 180f);
				Vector2f jetVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(7f, 75f), MISSILE.getFacing() + MathUtils.getRandomNumberInRange(179f, 181f));
				
				engine.addSmoothParticle(jetLoc, jetVel, MathUtils.getRandomNumberInRange(3f, 6f), 1f, MathUtils.getRandomNumberInRange(0.2f, 0.25f),  new Color(25,130,130,160));    				
			}
    		
    		
    		// all vfx from here down
    		engine.addSmoothParticle(loc, MathUtils.getRandomPointInCircle(null, 5f), 75f, 1f, 0.1f, new Color(100,230,235,255));
    		engine.addSmokeParticle(loc, MathUtils.getRandomPointInCircle(null, 5f), 30f, 0.9f, 0.7f, new Color(100,95,90,70));
    		
    		for (int i=0; i < 5; i++) {
    			
                Vector2f puffRandomVel = MathUtils.getRandomPointInCircle(null, 20f);
            	engine.addSmokeParticle(loc, puffRandomVel, MathUtils.getRandomNumberInRange(20f, 40f), 0.8f, 0.8f, new Color(65,115,120,100));
                
                for (int j=0; j < 2; j++) {
                	
        			Vector2f meltLoc = MathUtils.getRandomPointInCircle(loc, 15f);
        			Vector2f meltRandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(15f, 35f));
            		
            		engine.addSmoothParticle(meltLoc, meltRandomVel, MathUtils.getRandomNumberInRange(9f, 18f), 1f,
            				MathUtils.getRandomNumberInRange(0.6f, 0.85f),
            				new Color(200,160,50,150));
            		
            		
                    float randomSize = MathUtils.getRandomNumberInRange(20f, 40f);
                	Vector2f smokeRandomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(4f, 20f));
                	
                	engine.addNebulaSmokeParticle(loc,
                    		smokeRandomVel,
                    		randomSize, //size
                    		2.2f, //end mult
                    		0.5f, //ramp fraction
                    		0.75f, //full bright fraction
                    		0.9f, //duration
                    		new Color(65,115,120,100));
                	
                	smokeRandomVel.x += (vel.x * MathUtils.getRandomNumberInRange(0.4f, 0.9f));
                	smokeRandomVel.y += (vel.y * MathUtils.getRandomNumberInRange(0.4f, 0.9f));
                	
                    engine.addNebulaSmokeParticle(loc,
                    		smokeRandomVel,
                    		randomSize, //size
                    		2.2f, //end mult
                    		0.5f, //ramp fraction
                    		0.75f, //full bright fraction
                    		0.9f, //duration
                    		new Color(75,110,115,100));
                    
                    for (int k=0; k < 2; k++) {
                    	
                    	float randomSize2 = MathUtils.getRandomNumberInRange(3f, 11f);
                    	
                    	Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 61f));
                    	
                    	engine.addSmoothParticle(loc,
                    			randomVel,
                    			randomSize2, //size
                    			1.0f, //brightness
                    			0.75f, //duration
                    			new Color(80,120,255,255));
                    	
                    	randomVel.x += (vel.x * MathUtils.getRandomNumberInRange(0.4f, 0.9f));
                    	randomVel.y += (vel.y * MathUtils.getRandomNumberInRange(0.4f, 0.9f));
                
                    	engine.addSmoothParticle(loc,
                    			randomVel,
                    			randomSize2, //size
                    			1.0f, //brightness
                    			0.75f, //duration
                    			new Color(80,120,255,255));
                    }
                }
            }
    		
        	Global.getSoundPlayer().playSound("hurricane_mirv_split", 1.0f, 1.0f, loc, vel);
        	engine.removeEntity(MISSILE);
        }
        
		
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