// Based on the MagicMissileAI script By Tartiflette.
// A funky MIRV script. 
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

public class ASF_PersisMissileAI implements MissileAIPlugin, GuidedMissileAI {
          
    //////////////////////
    //     SETTINGS     //
    //////////////////////

	//Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
	//  Set to a negative value to disable
	private final float OVERSHOT_ANGLE=60;
	
	//Time to complete a wave in seconds.
	private final float WAVE_TIME=1.5f;
	
	//Max angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
	private final float WAVE_AMPLITUDE=13;
	
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
    private final int fighters=1;
    private final int frigates=4;
    private final int destroyers=5;
    private final int cruisers=3;
    private final int capitals=2;
    
    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE=360;
    
    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 2500;
    
    //should the missile fall back to the closest enemy when no target is found within the search parameters
    //only used with limited search cones
    private final boolean FAILSAFE = false;

	//range under which the missile start to get progressively more precise in game units.
	private float PRECISION_RANGE=800;

	//Is the missile lead the target or tailchase it?
	private final boolean LEADING=true;

	//Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
	//   1: perfect leading with and without ECCM
	//   2: half precision without ECCM
	//   3: a third as precise without ECCM. Default
	//   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
	private float ECCM=3;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!
    
    // square of range to split at
    private float SPLIT_RANGE=1000000; //1000^2
    
	private static final Color SMOKE_COLOR = new Color(92,110,103,110); // 65,125,115,110
//	private static final Color SPARK_COLOR = new Color(175,255,220,240);
	
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
    private float timer=0, check=0f, initTime = 0.4f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////
    
    public ASF_PersisMissileAI(MissileAPI missile, ShipAPI launchingShip) {
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
        
        // an initial delay so we don't mirv Instantly at short range
        if (initTime >= 0f) {
        	initTime -= amount;
        }
        
        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation( MISSILE.getFacing(), correctAngle);
        
        if(OVERSHOT_ANGLE<=0 || Math.abs(aimAngle)<OVERSHOT_ANGLE){
        	
        	MISSILE.giveCommand(ShipCommand.ACCELERATE);
        	
        	if (MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) <= (SPLIT_RANGE + (target.getCollisionRadius() * target.getCollisionRadius())) && initTime <= 0f) {

        		Vector2f loc = MISSILE.getLocation();
            	float angle = VectorUtils.getAngle(loc, target.getLocation());
            	
                for (int i=0; i < 4; i++) {
                	
                     float arcPoint1 = angle + MathUtils.getRandomNumberInRange(-90f, -80f);
                     float arcPoint2 = angle + MathUtils.getRandomNumberInRange(-5f, 5f);
                     float arcPoint3 = angle + MathUtils.getRandomNumberInRange(80, 90f);
                     
                     Vector2f randomVel1 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(-30f, 50f), arcPoint1);
                     Vector2f randomVel2 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(-50f, 10f), arcPoint2);
                     Vector2f randomVel3 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(-30f, 50f), arcPoint3);
                     
                     engine.spawnProjectile(MISSILE.getSource(),
                    		 MISSILE.getWeapon(),
                    		 "A_S-F_persis_sub",
                    		 MISSILE.getLocation(),
                    		 arcPoint1,
                    		 randomVel1);
                     engine.spawnProjectile(MISSILE.getSource(),
                    		 MISSILE.getWeapon(),
                    		 "A_S-F_persis_sub",
                    		 MISSILE.getLocation(),
                    		 arcPoint2,
                    		 randomVel2);
                     engine.spawnProjectile(MISSILE.getSource(),
                    		 MISSILE.getWeapon(),
                    		 "A_S-F_persis_sub",
                    		 MISSILE.getLocation(),
                    		 arcPoint3,
                    		 randomVel3);
                     
                }
                
                
                for (int i=0; i < 5; i++) {
            		
            		Vector2f jetLoc1 = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), angle - 85f);
            		Vector2f jetVel1 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 55f), angle - MathUtils.getRandomNumberInRange(84f, 86f));
            		Vector2f jetLoc2 = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), angle + 85f);
            		Vector2f jetVel2 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 55f), angle + MathUtils.getRandomNumberInRange(84f, 86f));
            		Vector2f jetLoc3 = MathUtils.getPointOnCircumference(loc, MathUtils.getRandomNumberInRange(6f, 22f), angle);
            		Vector2f jetVel3 = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(15f, 95f), angle + MathUtils.getRandomNumberInRange(-2f, 2f));
            		
            		engine.addNebulaSmokeParticle(jetLoc1,
                    		jetVel1,
                    		MathUtils.getRandomNumberInRange(10f, 20f), //size
                    		1.8f, //end mult
                    		0.5f, //ramp fraction
                    		0.75f, //full bright fraction
                    		MathUtils.getRandomNumberInRange(0.7f, 0.9f), //duration
                    		SMOKE_COLOR);
            		engine.addNebulaSmokeParticle(jetLoc2,
                    		jetVel2,
                    		MathUtils.getRandomNumberInRange(10f, 20f), //size
                    		1.8f, //end mult
                    		0.5f, //ramp fraction
                    		0.75f, //full bright fraction
                    		MathUtils.getRandomNumberInRange(0.7f, 0.9f), //duration
                    		SMOKE_COLOR);
            		engine.addNebulaSmokeParticle(jetLoc3,
                    		jetVel3,
                    		MathUtils.getRandomNumberInRange(10f, 20f), //size
                    		1.8f, //end mult
                    		0.5f, //ramp fraction
                    		0.75f, //full bright fraction
                    		MathUtils.getRandomNumberInRange(0.8f, 1.0f), //duration
                    		SMOKE_COLOR);
            		
//            		for (int j=0; j < 6; j++) {
//            			
//        				Vector2f sparkVel = MathUtils.getRandomPointInCone(null, 15f, angle -80f, angle +80f);
//        				
//        				engine.addSmoothParticle(MathUtils.getRandomPointInCone(loc, 9f, angle -80f, angle +80f),
//        						sparkVel,
//        						MathUtils.getRandomNumberInRange(3f, 6f),
//        						1f,
//        						MathUtils.getRandomNumberInRange(0.8f, 1.1f),
//        						SPARK_COLOR);
//            			
//            		}
            	}

    			ASF_RadialEmitter emitter = new ASF_RadialEmitter(null);
    			emitter.location(MISSILE.getLocation());
    			emitter.angle(angle - 80f, 160f);
    			emitter.distance(0f, 9f);
    			emitter.life(0.8f, 1.1f);
    			emitter.size(3f, 6f);
    			emitter.velocity(0f, 15f);
    			emitter.color(175,255,220,240); // SPARK_COLOR
    			emitter.velDistLinkage(false);
    			emitter.angleSplit(true);
    			emitter.burst(30);
    			
                for (int i=0; i < 3; i++) {

            		engine.addNebulaSmokeParticle(MathUtils.getRandomPointInCircle(loc, 10f),
            				MathUtils.getRandomPointInCircle(null, 5f),
                    		MathUtils.getRandomNumberInRange(12f, 16f), //size
                    		1.8f, //end mult
                    		0.5f, //ramp fraction
                    		0.65f, //full bright fraction
                    		MathUtils.getRandomNumberInRange(1.0f, 1.2f), //duration
                    		SMOKE_COLOR);
                }
                engine.addSmoothParticle(loc, MathUtils.getRandomPointInCircle(null, 2f), 85f, 1f, 0.2f, new Color(150,240,230,255));
        		engine.addSmokeParticle(loc, MathUtils.getRandomPointInCircle(null, 5f), 30f, 0.9f, 1.1f, new Color(80,120,105,70));
        		
                Global.getSoundPlayer().playSound("hurricane_mirv_split", 1.2f, 0.7f, loc, MISSILE.getVelocity()); // sabot_srm_split
                
        		engine.removeEntity(MISSILE);
        		
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