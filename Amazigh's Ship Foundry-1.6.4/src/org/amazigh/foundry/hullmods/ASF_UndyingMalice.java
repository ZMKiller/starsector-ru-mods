package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.skills.NeuralLinkScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import com.fs.starfarer.api.input.InputEventAPI;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_UndyingMalice extends BaseHullMod {
	
	public static final float MAINT_MALUS = 100f;
	public static final float DEGRADE_INCREASE_PERCENT = 50f;
	
	public static final float HIT_MOD = 36f; // +36% hitstrength "base"
	public static final float TIME_MOD = 0.5f; // +50% timescale "base"
	
	public static final float DAMAGE_PER_CHARGE = 100f;
	public static final int DECAY_TIMER = 3;
	
	public static final Color PARTICLE_COLOR = new Color(255,52,84,255);
	public static final Color BLAST_COLOR = new Color(210,55,140,255);
	
	public static final float VENT_BONUS = 75f; // active vent rate "base"
	private IntervalUtil ventInterval1 = new IntervalUtil(0.3f,0.45f);
	private IntervalUtil ventInterval2 = new IntervalUtil(0.3f,0.45f);
	
	private static final float REPAIR_CD = 10f;
	private static final float REPAIR_THRESHOLD = 150f; // amount of charge needed before repairs are available
	private static final float REPAIR_SHOCK_RANGE = 600f;
	private static Map<HullSize, Float> repairImpulseMult = new HashMap<HullSize, Float>();
	static {
		repairImpulseMult.put(HullSize.FIGHTER, 75f);
		repairImpulseMult.put(HullSize.FRIGATE, 150f);
		repairImpulseMult.put(HullSize.DESTROYER, 300f);
		repairImpulseMult.put(HullSize.CRUISER, 700f);
		repairImpulseMult.put(HullSize.CAPITAL_SHIP, 800f);
		repairImpulseMult.put(HullSize.DEFAULT, 450f);
	}
	
	private IntervalUtil chargeSparkInterval = new IntervalUtil(0.05f,0.05f);
	private IntervalUtil chargeCloudInterval = new IntervalUtil(0.12f,0.2f);
	
	// STORM STUFF
	private static final float STORM_RANGE = 1000f; // was 1000 (1500 with sysex) - now has half of current charge added as bonus range (can get close to old "max" with high charge+flux cap)
	private static float ARC_BLAST_SIZE = 50f;
	
	private IntervalUtil arcInterval1 = new IntervalUtil(0.25f,0.5f);
	private IntervalUtil arcInterval2 = new IntervalUtil(0.25f,0.5f);
	private IntervalUtil sparkInterval = new IntervalUtil(0.05f,0.05f);
	
	private IntervalUtil cloudInterval1 = new IntervalUtil(0.25f,0.4f); // 0.2,0.3
	private IntervalUtil cloudInterval2 = new IntervalUtil(0.25f,0.4f); // 0.2,0.3
	
	private boolean arcFired1 = false;
	private boolean arcFired2 = false;

	private static Map<HullSize, Float> arcRateMult = new HashMap<HullSize, Float>();
	static {
		arcRateMult.put(HullSize.FIGHTER, 0.2f); // with how these are set up, arc rate (from the storm) will equal old rate at exactly RATE_THRESHOLD charge
		arcRateMult.put(HullSize.FRIGATE, 0.4f); // above RATE_THRESHOLD charge then you will get *more* arc rate!
		arcRateMult.put(HullSize.DESTROYER, 0.5f);
		arcRateMult.put(HullSize.CRUISER, 0.7f); 	  // but! missile rate at RATE_THRESHOLD charge is halved from the old value!
		arcRateMult.put(HullSize.CAPITAL_SHIP, 0.9f); // although it can scale up when over RATE_THRESHOLD charge, potentially to a higher rate when charge goes up enough!
		arcRateMult.put(HullSize.DEFAULT, 0.3f);
	}
	
	private static float RATE_THRESHOLD = 180f; // the primary scalar on arc rate, higher means lower rate
	
	private static float STORM_ARC_DRAIN = 0.2f; // amount of charge each storm arc consumes
	
	private static float ARC_DAM = 60f; // 50 // slightly stronger than OG storm arcs, to make it a bit more worth using.
	private static float ARC_EMP = 400f;
	private static float PHASE_FLUX_SPIKE = 200f;
	private static float ARC_SLOWDOWN = 0.92f; // removed the "passive" 20% slowdown on targets, but instead this is an 8% drop, rather than a 5%
	
	private static float ARC_RATE_DECAY_MULT = 0.9f;
	// STORM STUFF
	
		
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		
        CombatEngineAPI engine = Global.getCombatEngine();
		ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("UNDYING_MALICE_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        if (info.doOnce) {
        	engine.getListenerManager().addListener(new ASF_maliceDamageListener(ship));
        	
        	Global.getCombatEngine().addPlugin(new chargeBarManager(ship));
        	
        	info.doOnce = false;
        }
        
		// death section - [start]
		if (!ship.isAlive() && !info.dead) {
			info.charge = 0f;
			
			stats.getTimeMult().unmodify(spec.getId());
        	engine.getTimeMult().unmodify(spec.getId());
        	
			for (int i=0; i < 9; i++) {
				
				float distanceRandom1 = MathUtils.getRandomNumberInRange(80f, 300f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(1f, 1.3f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 130);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
						new Color(153,92,103,135),
						new Color(255,216,224,140));
		        
				Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.9f, 0.5f, ship.getLocation(), ship.getVelocity());
				
			}
			
			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.6f,
	                350f,
	                210f,
	                1500f,
	                900f,
	                CollisionClass.PROJECTILE_FF,
	                CollisionClass.PROJECTILE_FIGHTER,
	                2f,
	                6f,
	                0.5f,
	                175,
	                PARTICLE_COLOR,
	                BLAST_COLOR);
	        blast.setDamageType(DamageType.ENERGY);
	        blast.setShowGraphic(true);
	        blast.setDetailedExplosionFlashColorCore(new Color(165,140,160,255));
	        blast.setDetailedExplosionFlashColorFringe(new Color(200,80,140,255));
	        blast.setUseDetailedExplosion(true);
	        blast.setDetailedExplosionRadius(400f);
	        blast.setDetailedExplosionFlashRadius(550f);
	        blast.setDetailedExplosionFlashDuration(0.5f);
	        
	        engine.spawnDamagingExplosion(blast,ship,ship.getLocation(),true);
	        
	        	// background smoke
	        for (int i=0; i < 5; i++) {
	        	engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(ship.getLocation(), 25f),
		        		MathUtils.getRandomPointOnCircumference(ship.getVelocity(), 10f),
		        		210f,
						MathUtils.getRandomNumberInRange(1.7f, 2.1f),
						0.9f,
						0.6f,
						MathUtils.getRandomNumberInRange(2.1f, 2.65f),
						new Color(140,40,120,60),
						false);
	        }
			
	        	// sub-blasts, main smoke
	        for (int i=0; i < 5; i++) {
	        	Vector2f blastPos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(70f, 200f));
	        	
		        engine.spawnExplosion(blastPos, ship.getVelocity(), BLAST_COLOR, 140f, 1.1f);
		        
		        for (int j=0; j < 6; j++) {
		        	float nebAngle = MathUtils.getRandomNumberInRange(0f, 360f);
		        	float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
					
			        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 350f * dist, nebAngle),
			        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), nebAngle),
							50f,
							MathUtils.getRandomNumberInRange(1.6f, 2.0f),
							0.7f,
							0.5f,
							MathUtils.getRandomNumberInRange(1.45f, 1.65f),
							new Color(190,65,150,75),
							false);
		        }
	        }
			
			info.dead = true;
		}
		// death section - [end]
		
		
		if (info.dead || ship.isPiece()) {
			return;
		}
		
        
        // the damage listener, (standard) charge gain/decay, "scaling stat setup", [AND] phased storm - [start]
        Map<String, Object> customCombatData = engine.getCustomData();
        
        float maxCharge = ship.getMaxFlux() * 0.1f; // we limit max charge, for balans
        
        float currDamage = 0f;
        
        if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
            currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
        }
        
        // can only generate charge when CR remains, no 0CR bullshit.
    	while (currDamage >= DAMAGE_PER_CHARGE && ship.getCurrentCR() > 0f) {
            currDamage -= DAMAGE_PER_CHARGE;
            info.charge = Math.min(maxCharge, info.charge + 1f); // increment charge while damageDealt is above the damage value for 1 charge
            
            if (!ship.isPhased()) {
            	info.decay = -(DECAY_TIMER * (1f - (info.charge / maxCharge))); // reset decay if we've dealt damage, but only if not in phase!
        	}
        }
        
        if (info.charge > 0f) {
        	info.decay += amount; // base decay
        	
        	if (ship.getCurrentCR() == 0f) {
        		info.decay += amount; // doubled decay when CR is out!
        	}
        }
        
        if (info.decay > 0f && info.charge > 0f) {
        	info.charge = Math.max(0f, info.charge - (info.decay * amount)); // reduce charge by current decay rate if it's in "decay mode"
        }
        
        float chargeScalarD = 1f; // two separate scalars, so one can scale slower than the other!
        float chargeScalar = 1f;
    	// simply:
    		// when charge is under 100 it's simply:  Scalar = (charge/100) 
    		// otherwise it becomes more complex, and you gain less and less from each point of charge over 100, (with "value thresholds" at each 100 extra charge)
        		// (if my math is right) at base flux cap of 6000, max mults are:
        			// 3.68928 - "chargeScalar"			// 4.1611392 at 8000 flux cap
        			// 2.38336 - "chargeScalarD"		// 2.4580096 at 8000 flux cap
    	if (info.charge > 100f) {
    		float chargeTemp = info.charge - 100f;
    		int tempCount = 1;
    		
    		while (chargeTemp > 0f) {
    			if (chargeTemp > 100f) {
    				chargeScalar += Math.pow(0.8, tempCount);
    				chargeScalarD += Math.pow(0.6, tempCount);
    			} else {
    				chargeScalar += (chargeTemp * 0.01f) * Math.pow(0.8, tempCount);
    				chargeScalarD += (chargeTemp * 0.01f) * Math.pow(0.6, tempCount);
    			}
    			chargeTemp -= 100f;
    			tempCount += 1;
    		}
    		
    	} else {
    		chargeScalar = info.charge * 0.01f;
    		chargeScalarD = info.charge * 0.01f;
    	}
    	
    	
    	// Phased Storm [start]
    	if (ship.isPhased()) {
    		
    		arcInterval1.advance(amount);
    		if (arcInterval1.intervalElapsed()) {
    			
    			float trueRange = STORM_RANGE + (info.charge * 0.5f);
    			arcFired1 = false;
    			float arcMult = 1f; // scaling down arc chance if an arc has already happened, to prevent it being completely brainless op when in a big swarm
    			float arcMultM = 1f; // separate scalar for missiles
    			
    			for (ShipAPI target_ship : engine.getShips()) {
    				// check if the ship is a valid target
    				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
    					continue;
    				}
    				
    				// if the target ship is within range, do an arc
    				if (MathUtils.isWithinRange(ship, target_ship, trueRange)) {
        				// arc chance is determined by charge scaling and then multiplied by:
    						// arcMult - multi-arc reduction
    						// arcRateMult - hullsize based arc rate scaling (bigger hull = more arcs)
    					if (info.charge * arcMult * (arcRateMult.get(target_ship.getHullSize())) > MathUtils.getRandomNumberInRange(0,info.charge+RATE_THRESHOLD) ) {
    						arcFired1 = true;
    						arcMult *= ARC_RATE_DECAY_MULT;
    						info.charge -= STORM_ARC_DRAIN; // decay charge by STORM_ARC_DRAIN for each "arc"
    						
    						if (target_ship.isPhased()) {
    	    					// if the enemy is phased, then we have them eat a chunk of soft flux, less "AI breaking" than hitting them with an arc after all
    	    					target_ship.getFluxTracker().increaseFlux(PHASE_FLUX_SPIKE, false);
    	    					
    	    					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
    	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 5f),
    	    			        		target_ship.getCollisionRadius(),
    	    							1.6f,
    	    							0.5f,
    	    							0.7f,
    	    							0.25f,
    	    							new Color(255,216,224,95),
    	    							false);
    	    	                
    	    	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
    	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 10f),
    	    			        		target_ship.getCollisionRadius(),
    	    							MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    	    							0.7f,
    	    							0.3f,
    	    							0.65f,
    	    							new Color(190,65,150,70),
    	    							false);
    	    	                
    	    	                ASF_RadialEmitter emitterPhaseSpike = new ASF_RadialEmitter((CombatEntityAPI) target_ship);
    	    	                emitterPhaseSpike.location(target_ship.getLocation());
    	    	                emitterPhaseSpike.life(0.35f, 0.5f);
    	    	    			emitterPhaseSpike.size(4f, 8f);
    	    	    			emitterPhaseSpike.velocity(30f, 45f);
    	    	    			emitterPhaseSpike.color(255,52,84,255);
    	    	    			emitterPhaseSpike.coreDispersion(target_ship.getCollisionRadius() * 0.65f);
    	    	    			emitterPhaseSpike.burst((int) (target_ship.getCollisionRadius() * 0.2f));
    	    	                
    	        			} else {
    			                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    			                
    			                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
    			                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
    			                
    			                CombatEntityAPI dummy = new SimpleEntity(loc);
    			                
    			                target_ship.getVelocity().scale(ARC_SLOWDOWN); // slowing the target when they get arced
    			                
    			                engine.spawnEmpArc(
    			                        ship,
    			                        loc,
    			                        dummy,
    			                        target_ship,
    			                        DamageType.ENERGY,
    			                        ARC_DAM,
    			                        ARC_EMP,
    			                        10000f,
    			                        "A_S-F_malice_arc_impact",
    			                        11f,
    			                        new Color(153,92,103,220),
    									new Color(255,216,224,210));
    			                
    			                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
    			                
    			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    					        		MathUtils.getRandomPointInCircle(null, 5f),
    					        		ARC_BLAST_SIZE * 1.2f,
    									1.6f,
    									0.5f,
    									0.7f,
    									0.25f,
    									new Color(255,216,224,95),
    									false);
    			                
    			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    					        		MathUtils.getRandomPointInCircle(null, 10f),
    					        		ARC_BLAST_SIZE * 1.8f,
    									MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    									0.7f,
    									0.3f,
    									0.6f,
    									new Color(190,65,150,70),
    									false);
    			                
    			                for (int i=0; i < 7; i++) {
    			            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 85f));
    			            		engine.addSmoothParticle(loc,
    			    						sparkVel,
    			    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    			    						1.0f, //brightness
    			    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
    			    						new Color(255,52,84,255));
    			            	}
    			                
    			                engine.removeEntity(dummy);
    	        			}
    						
    					}
    				}
    			}
    			
    			for (MissileAPI target_missile : engine.getMissiles()) {
        			// check if the missile is a valid target
            		if (target_missile.getOwner() == ship.getOwner()) {
            			continue;
            		}
            		
    				// if the target missile is within range, do an arc
            		if (MathUtils.isWithinRange(ship, target_missile, trueRange)) {
            			// missiles have a 10% scale on arc rate (while fighters/ships have a 20-90% scale)
            			if (info.charge * arcMultM * 0.1f > MathUtils.getRandomNumberInRange(0,info.charge+RATE_THRESHOLD) ) {
            				arcFired1 = true;
            				arcMultM *= ARC_RATE_DECAY_MULT;
    						info.charge -= STORM_ARC_DRAIN; // decay charge by STORM_ARC_DRAIN for each "arc"
    						
            				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    		                
    		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
    		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
    		                
    		                CombatEntityAPI dummy = new SimpleEntity(loc);
    		                
    		                engine.spawnEmpArc(
    		                        ship,
    		                        loc,
    		                        dummy,
    		                        target_missile,
    		                        DamageType.ENERGY,
    		                        ARC_DAM,
    		                        ARC_EMP,
    		                        10000f,
    		                        "A_S-F_malice_arc_impact",
    		                        10f,
    		                        new Color(153,92,103,220),
    								new Color(255,216,224,210));
    		                
    		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE * 0.9f, 0.45f);
    		                
    		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    				        		MathUtils.getRandomPointInCircle(null, 4f),
    				        		ARC_BLAST_SIZE * 1.1f,
    								1.6f,
    								0.5f,
    								0.7f,
    								0.23f,
    								new Color(255,216,224,95),
    								false);
    		                
    		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    				        		MathUtils.getRandomPointInCircle(null, 8f),
    				        		ARC_BLAST_SIZE * 1.7f,
    								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    								0.7f,
    								0.3f,
    								0.55f,
    								new Color(190,65,150,70),
    								false);
    		                
    		                for (int i=0; i < 6; i++) {
    		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 80f));
    		            		engine.addSmoothParticle(loc,
    		    						sparkVel,
    		    						MathUtils.getRandomNumberInRange(4f, 8f), //size
    		    						1.0f, //brightness
    		    						MathUtils.getRandomNumberInRange(0.35f, 0.45f), //duration
    		    						new Color(255,52,84,255));
    		            	}
    		                
    		                engine.removeEntity(dummy);
            				
            			}
            		}
    			}
    			
    			// if we didn't arc to anything, spawn an extra "angry" cloud vfx
    			if (!arcFired1) {
    				
                    Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), trueRange * 0.8f));
                    
    				engine.addNebulaParticle(loc,
    		        		MathUtils.getRandomPointInCircle(null, 10f),
    						85f,
    						1.6f,
    						0.5f,
    						0.5f,
    						0.66f,
    						new Color(255,104,150,123),
    						false);
    	            
    	            engine.addNebulaParticle(loc,
    		        		MathUtils.getRandomPointInCircle(null, 10f),
    						170f,
    						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    						0.7f,
    						0.6f,
    						1.3f,
    						new Color(190,70,135,70),
    						false);
    	            
    	            engine.addSmoothParticle(loc,
    	            		MathUtils.getRandomPointInCircle(null, 1f),
    						MathUtils.getRandomNumberInRange(35f, 45f), //size
    						1.0f, //brightness
    						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
    						new Color(255,52,84,255));
    	            
    	            for (int i=0; i < 5; i++) {
    	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
    	        		engine.addSmoothParticle(loc,
    							sparkVel,
    							MathUtils.getRandomNumberInRange(4f, 9f), //size
    							1.0f, //brightness
    							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
    							new Color(255,52,84,255));
    	        	}
    			}
    		}
    		
    		arcInterval2.advance(amount);
    		if (arcInterval2.intervalElapsed()) {

    			float trueRange = STORM_RANGE + (info.charge * 0.5f);
    			arcFired2 = false;
    			float arcMult = 1f; // scaling down arc chance if an arc has already happened, to prevent it being completely brainless op when in a big swarm
    			float arcMultM = 1f; // separate scalar for missiles
    			
    			for (ShipAPI target_ship : engine.getShips()) {
    				// check if the ship is a valid target
    				if (target_ship.isHulk() || target_ship.getOwner() == ship.getOwner()) {
    					continue;
    				}
    				
    				// if the target ship is within range, do an arc
    				if (MathUtils.isWithinRange(ship, target_ship, trueRange)) {
        				// arc chance is determined by charge scaling and then multiplied by:
    						// arcMult - multi-arc reduction
    						// arcRateMult - hullsize based arc rate scaling (bigger hull = more arcs)
    					if (info.charge * arcMult * (arcRateMult.get(target_ship.getHullSize())) > MathUtils.getRandomNumberInRange(0,info.charge+RATE_THRESHOLD) ) {
    						arcFired2 = true;
    						arcMult *= ARC_RATE_DECAY_MULT;
    						info.charge -= STORM_ARC_DRAIN; // decay charge by STORM_ARC_DRAIN1 for each "arc"
    						
    						if (target_ship.isPhased()) {
    	    					// if the enemy is phased, then we have them eat a chunk of soft flux, less "AI breaking" than hitting them with an arc after all
    	    					target_ship.getFluxTracker().increaseFlux(PHASE_FLUX_SPIKE, false);
    	    					
    	    					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
    	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 5f),
    	    			        		target_ship.getCollisionRadius(),
    	    							1.6f,
    	    							0.5f,
    	    							0.7f,
    	    							0.25f,
    	    							new Color(255,216,224,95),
    	    							false);
    	    	                
    	    	                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(target_ship.getLocation(), 10f),
    	    			        		MathUtils.getRandomPointInCircle(target_ship.getVelocity(), 10f),
    	    			        		target_ship.getCollisionRadius(),
    	    							MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    	    							0.7f,
    	    							0.3f,
    	    							0.65f,
    	    							new Color(190,65,150,70),
    	    							false);
    	    	                
    	    	                ASF_RadialEmitter emitterPhaseSpike = new ASF_RadialEmitter((CombatEntityAPI) target_ship);
    	    	                emitterPhaseSpike.location(target_ship.getLocation());
    	    	                emitterPhaseSpike.life(0.35f, 0.5f);
    	    	    			emitterPhaseSpike.size(4f, 8f);
    	    	    			emitterPhaseSpike.velocity(30f, 45f);
    	    	    			emitterPhaseSpike.color(255,52,84,255);
    	    	    			emitterPhaseSpike.coreDispersion(target_ship.getCollisionRadius() * 0.65f);
    	    	    			emitterPhaseSpike.burst((int) (target_ship.getCollisionRadius() * 0.2f));
    	    	                
    	        			} else {
    			                float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    			                
    			                float distance = target_ship.getCollisionRadius() + MathUtils.getRandomNumberInRange(30f, 60f);
    			                Vector2f loc = MathUtils.getPointOnCircumference(target_ship.getLocation(), distance, angle);
    			                
    			                CombatEntityAPI dummy = new SimpleEntity(loc);
    			                
    			                target_ship.getVelocity().scale(ARC_SLOWDOWN); // slowing the target when they get arced
    			                
    			                engine.spawnEmpArc(
    			                        ship,
    			                        loc,
    			                        dummy,
    			                        target_ship,
    			                        DamageType.ENERGY,
    			                        ARC_DAM,
    			                        ARC_EMP,
    			                        10000f,
    			                        "A_S-F_malice_arc_impact",
    			                        11f,
    			                        new Color(153,92,103,220),
    									new Color(255,216,224,210));
    			                
    			                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE, 0.5f);
    			                
    			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    					        		MathUtils.getRandomPointInCircle(null, 5f),
    					        		ARC_BLAST_SIZE * 1.2f,
    									1.6f,
    									0.5f,
    									0.7f,
    									0.25f,
    									new Color(255,216,224,95),
    									false);
    			                
    			                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    					        		MathUtils.getRandomPointInCircle(null, 10f),
    					        		ARC_BLAST_SIZE * 1.8f,
    									MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    									0.7f,
    									0.3f,
    									0.6f,
    									new Color(190,65,150,70),
    									false);
    			                
    			                for (int i=0; i < 7; i++) {
    			            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(50f, 85f));
    			            		engine.addSmoothParticle(loc,
    			    						sparkVel,
    			    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    			    						1.0f, //brightness
    			    						MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
    			    						new Color(255,52,84,255));
    			            	}
    			                
    			                engine.removeEntity(dummy);
    	        			}
    						
    					}
    				}
    			}
    			
    			for (MissileAPI target_missile : engine.getMissiles()) {
        			// check if the missile is a valid target
            		if (target_missile.getOwner() == ship.getOwner()) {
            			continue;
            		}
            		
    				// if the target missile is within range, do an arc
            		if (MathUtils.isWithinRange(ship, target_missile, trueRange)) {
            			// missiles have a 10% scale on arc rate (while fighters/ships have a 20-90% scale)
            			if (info.charge * arcMultM * 0.1f > MathUtils.getRandomNumberInRange(0,info.charge+RATE_THRESHOLD) ) {
            				arcFired2 = true;
            				arcMultM *= ARC_RATE_DECAY_MULT;
    						info.charge -= STORM_ARC_DRAIN; // decay charge by STORM_ARC_DRAIN for each "arc"
    						
            				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
    		                
    		                float distance = MathUtils.getRandomNumberInRange(55f, 85f);
    		                Vector2f loc = MathUtils.getPointOnCircumference(target_missile.getLocation(), distance, angle);
    		                
    		                CombatEntityAPI dummy = new SimpleEntity(loc);
    		                
    		                engine.spawnEmpArc(
    		                        ship,
    		                        loc,
    		                        dummy,
    		                        target_missile,
    		                        DamageType.ENERGY,
    		                        ARC_DAM,
    		                        ARC_EMP,
    		                        10000f,
    		                        "A_S-F_malice_arc_impact",
    		                        10f,
    		                        new Color(153,92,103,220),
    								new Color(255,216,224,210));
    		                
    		                engine.spawnExplosion(loc, dummy.getVelocity(), new Color(210,55,140,255), ARC_BLAST_SIZE * 0.9f, 0.45f);
    		                
    		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    				        		MathUtils.getRandomPointInCircle(null, 4f),
    				        		ARC_BLAST_SIZE * 1.1f,
    								1.6f,
    								0.5f,
    								0.7f,
    								0.23f,
    								new Color(255,216,224,95),
    								false);
    		                
    		                engine.addNebulaParticle(MathUtils.getRandomPointInCircle(loc, 10f),
    				        		MathUtils.getRandomPointInCircle(null, 8f),
    				        		ARC_BLAST_SIZE * 1.7f,
    								MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    								0.7f,
    								0.3f,
    								0.55f,
    								new Color(190,65,150,70),
    								false);
    		                
    		                for (int i=0; i < 6; i++) {
    		            		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(45f, 80f));
    		            		engine.addSmoothParticle(loc,
    		    						sparkVel,
    		    						MathUtils.getRandomNumberInRange(4f, 8f), //size
    		    						1.0f, //brightness
    		    						MathUtils.getRandomNumberInRange(0.35f, 0.45f), //duration
    		    						new Color(255,52,84,255));
    		            	}
    		                
    		                engine.removeEntity(dummy);
            				
            			}
            		}
    			}
    			
    			// if we didn't arc to anything, spawn an extra "angry" cloud vfx
    			if (!arcFired2) {
    				
                    Vector2f loc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(ship.getCollisionRadius(), trueRange * 0.8f));
                    
    				engine.addNebulaParticle(loc,
    		        		MathUtils.getRandomPointInCircle(null, 10f),
    						85f,
    						1.6f,
    						0.5f,
    						0.5f,
    						0.66f,
    						new Color(255,104,150,123),
    						false);
    	            
    	            engine.addNebulaParticle(loc,
    		        		MathUtils.getRandomPointInCircle(null, 10f),
    						170f,
    						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
    						0.7f,
    						0.6f,
    						1.3f,
    						new Color(190,70,135,70),
    						false);
    	            
    	            engine.addSmoothParticle(loc,
    	            		MathUtils.getRandomPointInCircle(null, 1f),
    						MathUtils.getRandomNumberInRange(35f, 45f), //size
    						1.0f, //brightness
    						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
    						new Color(255,52,84,255));
    	            
    	            for (int i=0; i < 5; i++) {
    	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
    	        		engine.addSmoothParticle(loc,
    							sparkVel,
    							MathUtils.getRandomNumberInRange(4f, 9f), //size
    							1.0f, //brightness
    							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
    							new Color(255,52,84,255));
    	        	}   
    			}
    		}
    		
    		// "radius marker"
    		sparkInterval.advance(amount);
    		if (sparkInterval.intervalElapsed()) {

    			float trueRange = STORM_RANGE + (info.charge * 0.5f);
    			
                ASF_RadialEmitter emitterRangeRing = new ASF_RadialEmitter((CombatEntityAPI) ship);
                emitterRangeRing.location(ship.getLocation());
                emitterRangeRing.life(0.5f, 0.65f);
                emitterRangeRing.size(6f, 11f);
                emitterRangeRing.velocity(-25f, -30f);
                emitterRangeRing.distance(trueRange, 0f);
                emitterRangeRing.color(255,52,84,225);
                emitterRangeRing.burst(25);
    		}
    		
    		// general area visual stuff
    		// 1 - spawns generic clouds, and the "onship big nebs"
    		// 2 - spawns generic clouds, and smaller "angry" clouds
    		cloudInterval1.advance(amount);
			if (cloudInterval1.intervalElapsed()) {
				
    			float trueRange = STORM_RANGE + (info.charge * 0.5f);
				Vector2f cloudLoc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), trueRange * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				
				for (int i=0; i < 3; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(210f, 290f), // 110-140
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.3f, //0.7
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(160,60,185,60), //70a
							false);
				}
				
				Vector2f cloudLoc2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), trueRange * MathUtils.getRandomNumberInRange(0.5f, 0.9f));
				for (int i=0; i < 2; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc2, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(220f, 300f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.3f, //0.7
							0.6f,
							MathUtils.getRandomNumberInRange(1.8f, 2.5f),
							new Color(160,60,185,60), //70a
							false);
				}
				
				engine.addNebulaParticle(MathUtils.getRandomPointInCircle(ship.getLocation(), 60f),
						MathUtils.getRandomPointInCircle(null, 10f),
						MathUtils.getRandomNumberInRange(360f, 480f),
						MathUtils.getRandomNumberInRange(1.8f, 2.3f),
						0.2f, //0.7
						0.4f, //0.6
						MathUtils.getRandomNumberInRange(1.9f, 2.6f),
						new Color(190,50,125,30), //210,55,140,50 //35a
						false);
			}
			cloudInterval2.advance(amount);
			if (cloudInterval2.intervalElapsed()) {
				
    			float trueRange = STORM_RANGE + (info.charge * 0.5f);
				Vector2f cloudLoc = MathUtils.getRandomPointOnCircumference(ship.getLocation(), trueRange * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				
				for (int i=0; i < 3; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(210f, 290f), // 110-140
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.9f, 2.6f),
							new Color(150,65,190,60), //70a
							false);
				}

				Vector2f cloudLoc2 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), trueRange * MathUtils.getRandomNumberInRange(0.5f, 0.9f));
				for (int i=0; i < 2; i++) {
					engine.addNebulaParticle(MathUtils.getRandomPointInCircle(cloudLoc2, 50f),
							MathUtils.getRandomPointInCircle(null, 10f),
							MathUtils.getRandomNumberInRange(220f, 300f),
							MathUtils.getRandomNumberInRange(1.8f, 2.3f),
							0.7f,
							0.6f,
							MathUtils.getRandomNumberInRange(1.8f, 2.5f),
							new Color(160,60,185,60), //70a
							false);
				}
				
				Vector2f cloudLoc3 = MathUtils.getRandomPointOnCircumference(ship.getLocation(), trueRange * MathUtils.getRandomNumberInRange(0.05f, 0.9f));
				engine.addNebulaParticle(cloudLoc3,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						85f,
						1.6f,
						0.5f,
						0.5f,
						0.66f,
						new Color(255,104,158,123),
						false);
	            
	            engine.addNebulaParticle(cloudLoc3,
		        		MathUtils.getRandomPointInCircle(null, 10f),
						170f,
						MathUtils.getRandomNumberInRange(1.5f, 1.8f),
						0.7f,
						0.6f,
						1.3f,
						new Color(190,65,140,70),
						false);
	            
	            engine.addSmoothParticle(cloudLoc3,
	            		MathUtils.getRandomPointInCircle(null, 1f),
						MathUtils.getRandomNumberInRange(35f, 45f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.2f, 0.3f), //duration
						new Color(255,52,84,255));
	            
	            for (int i=0; i < 5; i++) {
	        		Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(35f, 70f));
	        		engine.addSmoothParticle(cloudLoc3,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							1.0f, //brightness
							MathUtils.getRandomNumberInRange(0.5f, 0.7f), //duration
							new Color(255,52,84,255));
	        	}
			}
    	}
    	// Phased Storm [end]
    	
        
        // a sanity check, because something was causing this to drop to a MASSIVE negative value, for unknown reasons
        if (currDamage < 0f) {
        	currDamage = 0f;
        }
        
        customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
        // the damage listener and (standard) charge gain/decay [AND] system gimmick - [end]
		
		
        // buff section - [start]
    	if (info.charge > 0f) {
        	
        	float TIME_MULT = 1f + (TIME_MOD * chargeScalarD);
        	
        	stats.getHitStrengthBonus().modifyPercent(spec.getId(), HIT_MOD * chargeScalar);
        	
        	boolean player = ship == engine.getPlayerShip();
        	
			stats.getTimeMult().modifyMult(spec.getId(), TIME_MULT);
			
    		if (player) {
    			engine.getTimeMult().modifyMult(spec.getId(), 1f / TIME_MULT);
    		} else {
    			engine.getTimeMult().unmodify(spec.getId());
    		}
    		
        } else {
        	stats.getHitStrengthBonus().unmodify(spec.getId());
        	stats.getHullDamageTakenMult().unmodify(spec.getId());
        	stats.getArmorDamageTakenMult().unmodify(spec.getId());
        	stats.getEmpDamageTakenMult().unmodify(spec.getId());
        	
        	stats.getTimeMult().unmodify(spec.getId());
        	engine.getTimeMult().unmodify(spec.getId());
        }
        // buff section - [end]
        
		// repair section - [start]
		if (info.repairCooldown < REPAIR_CD) {
			if (ship.isPhased()) {
				info.repairCooldown += amount * 0.3f; // repair cools down a lot slower when phased (balancing act!)
			} else {
				info.repairCooldown += amount; // increment the repair cooldown.
			}
		}
		
		// only repair if we have:
			// CR remaining (no zero CR zombie memes)
			// at least 150 charge
			// under 50% hull
			// repair is not cooling down (the delay is to make this at least a *bit* balanced)
		if (ship.getCurrentCR() > 0f && info.charge >= REPAIR_THRESHOLD && ship.getHullLevel() < 0.5f && info.repairCooldown >= REPAIR_CD) {
			
			info.charge *= 0.5f; // you really don't want to be forced into having a repair, as it eats a *lot* of charge, even/especially at higher charge levels.
			info.repairCooldown = 0f;
			
			ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() * 0.5f); // halving current flux, a lil helping hand!
			ship.getFluxTracker().setCurrFlux(ship.getFluxTracker().getCurrFlux() * 0.5f); // we do both both hard+soft, and hard first because of how hard/soft are handled.
			
			float hull = ship.getHitpoints();
			ship.setHitpoints(Math.min(ship.getMaxHitpoints(), hull + (ship.getMaxHitpoints() * 0.5f))); // +50% hull (with a sanity check just in case)
			
			ArmorGridAPI armorGrid = ship.getArmorGrid();
	        final float[][] grid = armorGrid.getGrid();
	        final float max = armorGrid.getMaxArmorInCell();
	        
	        float repairAmount = armorGrid.getMaxArmorInCell();
	        
			for (int x = 0; x < grid.length; x++) {
	            for (int y = 0; y < grid[0].length; y++) {
	                if (grid[x][y] < max) {
	                    float regen = grid[x][y] + repairAmount;
	                    armorGrid.setArmorValue(x, y, regen);
	                }
	            }
	        }
			
			ship.clearDamageDecals();
			ship.syncWithArmorGridState();
	        ship.syncWeaponDecalsWithArmorDamage();
			

            ASF_RadialEmitter emitterRepair1 = new ASF_RadialEmitter((CombatEntityAPI) ship);
            emitterRepair1.location(ship.getLocation());
            emitterRepair1.life(0.6f, 0.75f);
            emitterRepair1.size(4f, 9f);
            emitterRepair1.velocity(25f, 10f);
            emitterRepair1.distance(35f, 25f);
            emitterRepair1.color(50,240,100,255);
            emitterRepair1.velDistLinkage(false);
            emitterRepair1.burst(40);
	        
	        for (int i=0; i < 30; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(15f, 45f), angle);
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.85f, 1.1f), //duration
						new Color(50,240,100,255));
        	}

	        for (int i=0; i < 28; i++) {
	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 1.2f);
	        	engine.addHitParticle(sparkLoc, ship.getVelocity(),
	        			MathUtils.getRandomNumberInRange(5f, 10f), //size
	        			0.8f, //bright
	        			0.4f, //dur
	        			new Color(50, 240, 100));
	        }
	        
	        for (int i=0; i < 3; i++) {
	        	
				float distanceRandom1 = MathUtils.getRandomNumberInRange(65f, 110f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(1f, 1.3f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 130);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship, 8f,
						new Color(92,193,130,135),
						new Color(190,255,220,140));
		        
		        for (int j=0; j < 2; j++) {
		        	
		        	float distanceRandom3 = distanceRandom1 * MathUtils.getRandomNumberInRange(1.9f, 2.1f);
					float angleRandom3 = MathUtils.getRandomNumberInRange(0, 360);
			        Vector2f arcPoint3 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom3, angleRandom3);
			        
			        float distanceRandom4 = distanceRandom3 * MathUtils.getRandomNumberInRange(1f, 1.3f);
			        float angleRandom4 = angleRandom3 + MathUtils.getRandomNumberInRange(45, 75);
			        Vector2f arcPoint4 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom4, angleRandom4);
			        
			        engine.spawnEmpArcVisual(arcPoint3, ship, arcPoint4, ship, 8f,
							new Color(92,193,130,140),
							new Color(190,255,220,145));
		        }
			}
	        
	        for (int i=0; i < 12; i++) {
	        	float angle = (i * 30f) + MathUtils.getRandomNumberInRange(-4f, 4f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius(), angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * dist, angle),
		        		ship.getCollisionRadius(),
						MathUtils.getRandomNumberInRange(1.8f, 2.1f),
						0.5f,
						0.7f,
						1.2f,
						new Color(80,255,175,70),
						false);
	        }
	        
	        
	        engine.addFloatingTextAlways(ship.getLocation(),
					"Emergency repairs!",
					NeuralLinkScript.getFloatySize(ship) * 1.5f, new Color(80,255,175,255), ship,
					15f, // flashFrequency
					3f, // flashDuration
					0.5f, // durInPlace
					1f, // durFloatingUp
					1.5f, // durFadingOut
					1f); // baseAlpha
			
			engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), new Color(80,255,175,70), 400f, 1f);
			
			for (ShipAPI target_ship : engine.getShips()) {
				if (MathUtils.isWithinRange(ship, target_ship, REPAIR_SHOCK_RANGE)) {
					
					float impulseForce = Math.min(repairImpulseMult.get(((ShipAPI) target_ship).getHullSize()) * 3f, (target_ship.getMass() * 0.4f) + repairImpulseMult.get(((ShipAPI) target_ship).getHullSize()));
					// force is whatever is lower of:
						// 3x hullsize scalar
						// hullsize scalar + 40% of target mass
							// so we get a respectable push, but it's not SILLY on stuff like the invictus
					
					CombatUtils.applyForce(target_ship, VectorUtils.getDirectionalVector(ship.getLocation(), target_ship.getLocation()), impulseForce); // knockback!
					
				}
			}
			
			Global.getSoundPlayer().playSound("ui_refit_slot_filled_energy_large", 1.2f, 1.75f, ship.getLocation(), ship.getVelocity());
			
		}
        // repair section - [end]
		
		
        // vent section - [start]
        stats.getVentRateMult().modifyPercent(spec.getId(), (VENT_BONUS * chargeScalarD)); // boost vent rate by an amount proportional to current charge
        
		if (ship.getFluxTracker().isVenting()) {
			info.charge = Math.max(0f, info.charge - ((info.charge * 0.0625f) * chargeScalarD * amount)); // while venting, charge decays by 1/16th of current charge /sec
					// decay is also further mult'd by chargeScalar!
				// you get this significant charge decay, because you get a (potentially) *insane* boost to vent rate.
			
			// vent fx rate scales up as charge goes up, at the same rate as the vent speed bonus goes up.
			ventInterval1.advance(amount * chargeScalar);
            if (ventInterval1.intervalElapsed()) {
            	
            	float mult = 1f + ship.getFluxLevel();
            	
            	for (int i=0; i < (4 * mult); i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * mult);
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				engine.addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.65f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		80f * mult, // 65
						MathUtils.getRandomNumberInRange(1.6f, 2.2f),
						0.8f,
						0.5f,
						0.8f,
						new Color(140,70,130,70),
						false);
            }
            
            ventInterval2.advance(amount *chargeScalar);
            if (ventInterval2.intervalElapsed()) {

            	float mult = 1f + ship.getFluxLevel();
            	
            	for (int i=0; i < (4 * mult); i++) {
                	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * mult);
    				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(40f, 80f));
    				engine.addSmoothParticle(sparkPoint,
    						sparkVel,
    						MathUtils.getRandomNumberInRange(4f, 9f), //size
    						0.6f, //brightness
    						0.65f, //duration
    						new Color(150,70,135,255));
            	}
		        
		        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
		        		80f * mult, // 65
						MathUtils.getRandomNumberInRange(1.6f, 2.2f),
						0.8f,
						0.5f,
						0.8f,
						new Color(140,70,130,70),
						false);
            }
			
		}
		// storing charge here, as we have to do it after the vent decay for that to actually work!
        customCombatData.put("ASF_undyingHullmodCharge" + ship.getId(), info.charge);
        // vent section - [end]
		
		
        // sprite rendering + passive fx section - [start]
		Vector2f spritePos = MathUtils.getPointOnCircumference(ship.getLocation(), 6f, ship.getFacing());
		Vector2f spriteSize = new Vector2f(90f, 116f); // 98, 104
		float alphaMult = 1f;
		
		if (ship.isPhased()) {
			alphaMult = 0.3f;
		}
		
        if (info.charge > 0f) {
        	SpriteAPI GlowTatt1 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow1");
        	int alpha1 = 20 + ((int) (Math.min(info.charge, 170) * alphaMult)); // added flat 20 with new glowsprite, and increased "max scale" from 150 to 170
        	
    		double alphaTemp = alpha1;
    		double timeMult = (double) stats.getTimeMult().modified; // this timeMult stuff is a "well fuck sprite rendering gets screwy with increases to timescale, let's fix it!"
    		alpha1 = Math.min(255, (int) Math.ceil(alphaTemp / timeMult));
    		
        	MagicRender.singleframe(GlowTatt1, spritePos, spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha1), true);
        	
        	if (info.charge > 100f) {
        		SpriteAPI GlowTatt2 = Global.getSettings().getSprite("fx", "A_S-F_persenachia_tatt_glow2");
            	int alpha2 = (int) (Math.min((info.charge - 100f) * 0.8f, 255f) * alphaMult); // changed mult from 0.75 to 0.8 with new glowsprite
            	
        		double alphaTemp2 = alpha2;
        		alpha2 = Math.min(255, (int) Math.ceil(alphaTemp2 / timeMult));
            	
            	MagicRender.singleframe(GlowTatt2, MathUtils.getRandomPointInCircle(spritePos, 1f), spriteSize, ship.getFacing() - 90f, new Color(255,52,84,alpha2), true);
        	}
        }
		
		if (info.repairCooldown < REPAIR_CD) {
			// repair is not ready, lower alpha mult.
			alphaMult *= 0.3f;
		}
		
		// the pipe glow is set up like this, so it fades in/out, rather than popping in instantly (and is done after the tattoo so it gets an extra alphaMult when repairs aren't ready)
        if (info.charge >= REPAIR_THRESHOLD) {
        	if (info.fadeIn < 1f) {
            	info.fadeIn = Math.min(1f, info.fadeIn + (amount * 1.5f));
        	}
        }
    	if (info.fadeIn > 0f) {
    		
    		int pipeAlpha = (int) Math.min(180, Math.max(0, (int) (180 * alphaMult * info.fadeIn)) );
    		
    		double alphaTemp = pipeAlpha;
    		double timeMult = (double) stats.getTimeMult().modified;
    		pipeAlpha = Math.min(255, (int) Math.ceil(alphaTemp / timeMult));
    		
        	SpriteAPI GlowPipe = Global.getSettings().getSprite("fx", "A_S-F_persenachia_pipe_glow");
        	MagicRender.singleframe(GlowPipe, spritePos, spriteSize, ship.getFacing() - 90f, new Color(80,255,175,pipeAlpha), true);
        	
        	if (info.charge < REPAIR_THRESHOLD) {
            	info.fadeIn = Math.max(0f, info.fadeIn - (amount * 1.5f));
        	}
        }
    	
		// "passive particle fx"
    	chargeSparkInterval.advance(amount);
		if (chargeSparkInterval.intervalElapsed()) {
			
			float chargeScale = info.charge / maxCharge; // particle alpha scales with charge level!
			int alpha = 25 + (int) (175 * chargeScale);
			float durAdd = chargeScalarD * 0.1f;
			
			if (ship.getSystem().isActive()) {
				alpha += (int) (ship.getSystem().getEffectLevel() * 35f);
				durAdd = chargeScalar * 0.1f;
			}
			
			for (int i=0; i < 4; i++) {
				float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				Vector2f sparkLoc = MathUtils.getPointOnCircumference(spritePos, MathUtils.getRandomNumberInRange(45f, 60f), angle); //35,45
				
				Vector2f sparkVelTemp = MathUtils.getMidpoint(MathUtils.getRandomPointInCircle(null, 1f), ship.getVelocity());
				Vector2f sparkVel = MathUtils.getPointOnCircumference(sparkVelTemp, MathUtils.getRandomNumberInRange(25f, 35f), angle);
				
				engine.addSmoothParticle(sparkLoc,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						1.0f, //brightness
						MathUtils.getRandomNumberInRange(0.35f, 0.45f) + durAdd, //duration
						new Color(255,52,84,alpha));
        	}
		}
		
		// "passive cloud fx"
		chargeCloudInterval.advance(amount);
		if (chargeCloudInterval.intervalElapsed()) {
			
			float chargeScale = info.charge / maxCharge; // particle alpha scales with charge level!
			int alpha = 10 + (int) (40 * chargeScale);
			
			engine.addNebulaParticle(ship.getLocation(),
					MathUtils.getRandomPointInCircle(null, 10f),
					MathUtils.getRandomNumberInRange(120f, 140f), //110,130
					1.9f,
					0.28f,
					0.69f,
					MathUtils.getRandomNumberInRange(1.15f, 1.69f),
					new Color(185,60,117,alpha), // 160,60,185
					false);
		}
        // sprite rendering + passive fx section - [end]
        
    	
    	
        // debug display section - [start]
        // engine.maintainStatusForPlayerShip("MALICEDEBUG4", "graphics/icons/hullsys/phase_cloak.png",  "chargeScalar: " + chargeScalar, "chargeScalarD: " + chargeScalarD, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG3", "graphics/icons/hullsys/phase_cloak.png",  "Repair CD: " + info.repairCooldown, "Max Charge: " + maxCharge, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG2", "graphics/icons/hullsys/phase_cloak.png", "Decay: " + info.decay, "currDamage: " + currDamage, false);
        // engine.maintainStatusForPlayerShip("MALICEDEBUG1", "graphics/icons/hullsys/phase_cloak.png", "DEBUG INFO", "Charge: " + info.charge, false);
        // debug display section - [end]
        
        
        engine.getCustomData().put("UNDYING_MALICE_DATA_KEY" + ship.getId(), info);
        	
	}
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float dpad = 6f;
		float opad = 10f;
		float tpad = 10f;
		float hpad = 12f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color grey = Misc.getGrayColor();
		
		Color banner = Misc.getDarkHighlightColor().darker();
		Color repBanner = new Color(65,160,100,255);
		Color stormBanner = new Color(128,26,42,255);
		
		// UNDYING \\ MALICE
		
		// A heretic, clad in the husks of the dead. In order to hide her figure, she spreads a thick, dark mist. The black fog conceals her as she hunts her next victim.
		
		LabelAPI label = tooltip.addPara("This vessel features a unique system called the Malice Resonator, this improves the ships performance after dealing damage and can use the accumulated energy to repair itself.", pad);
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The rate of in-combat CR decay after peak performance time runs out is increased by %s.", pad, bad, "" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlight("" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlightColors(bad);
		
		tooltip.addSectionHeading("Malice Resonator", h, banner, Alignment.MID, hpad);
		label = tooltip.addPara("The resonator generates one charge for every %s damage the ship deals.", tpad, h, "" + (int)DAMAGE_PER_CHARGE);
		label.setHighlight("" + (int)DAMAGE_PER_CHARGE);
		label.setHighlightColors(h);
		label = tooltip.addPara("The ship recieves bonuses to %s, %s and %s based on the current charge level.", pad, h, "Weapon Hitstrength", "Timescale", "Active vent rate");
		label.setHighlight("Weapon Hitstrength", "Timescale", "Damage Resistance", "Active vent rate");
		label.setHighlightColors(h, h, h, h);
		label = tooltip.addPara("Charges will %s while actively venting.", pad, bad, "Decay");
		label.setHighlight("Decay");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The resonator can only store a limited charge level, equivalent to %s of the vessels flux capacity.", dpad, h, "10%");
		label.setHighlight("10%");
		label.setHighlightColors(h);
		if (!Global.CODEX_TOOLTIP_MODE) {
			label = tooltip.addPara("%s %s", pad, grey, "Current charge capacity is:", "" + (int) ((ship.getMaxFlux() * 0.1f)));
			label.setHighlight("Current charge capacity is:", "" + (int) ((ship.getMaxFlux() * 0.1f)));
			label.setHighlightColors(grey, h);
		}
		
		label = tooltip.addPara("Resonator charge can only remain stable for a limited duration before %s.", opad, bad, "Decaying");
		label.setHighlight("Decaying");
		label.setHighlightColors(bad);
		label = tooltip.addPara("There is an up to %s pause after generating charge before decay starts.", pad, h, DECAY_TIMER + " second");
		label.setHighlight(DECAY_TIMER + " second");
		label.setHighlightColors(h);
		label = tooltip.addPara("The duration of the pause %s at higher charge levels.", pad, bad, "Decreases");
		label.setHighlight("Decreases");
		label.setHighlightColors(bad);
		
		tooltip.addSectionHeading("Malice Storm", h, stormBanner, Alignment.MID, hpad);
		label = tooltip.addPara("While phased resonator charge causes a p-space disturbance that manifests as a localised %s.", tpad, h, "Malice Storm");
		label.setHighlight("Malice Storm");
		label.setHighlightColors(h);
		label = tooltip.addPara("The storm will discharge energy into all nearby enemies, slowing and disrupting them.", pad);
		label = tooltip.addPara("The number of discharges produced scales with current charge level.", pad);
		label = tooltip.addPara("Maintaining the storm causes charge to %s.", pad, bad, "Decay" );
		label.setHighlight("Decay");
		label.setHighlightColors(bad);
		
//		Cycles energy from the Malice Resonator through the hyperspace drive, producing a storm of energy that discharges into all nearby enemies,
//		slowing and disrupting them. Resonator charge is drained to enhance the power of the storm.
		
		tooltip.addSectionHeading("Emergency Repair System", h, repBanner, Alignment.MID, hpad);
		label = tooltip.addPara("If the ship drops below %s hull and has at least %s charge stored, then the Resonator will consume %s of current charge to trigger an emergency repair.", tpad, bad, "50%", "" + (int) REPAIR_THRESHOLD, "50%");
		label.setHighlight("50%", "" + (int) REPAIR_THRESHOLD, "50%");
		label.setHighlightColors(bad, h, h);
		label = tooltip.addPara("An emergency repair will restore all damaged armour, and replenish %s of the ships maximum hull.", pad, h, "50%");
		label.setHighlight("50%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Emergency repairs can only be triggered once every %s seconds.", pad, h, "" + (int)REPAIR_CD);
		label.setHighlight("" + (int)REPAIR_CD);
		label.setHighlightColors(h);
		
	}
	
	// damage dealt listener [start]
		// "stolen" from the VIC stolas script ;)
    public static class ASF_maliceDamageListener implements DamageListener {
        ShipAPI ship;

        ASF_maliceDamageListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (source instanceof ShipAPI) {
                if (!source.equals(ship)) {
                    return;
                }
                if (!ship.isAlive()) {
                    Global.getCombatEngine().getListenerManager().removeListener(this);
                }
                
                if (target instanceof ShipAPI) {
                    if (!((ShipAPI) target).isAlive()) return;
                }
                
                float totalDamage = 0;
                totalDamage += result.getDamageToHull();
                totalDamage += (result.getDamageToShields());
                totalDamage += result.getTotalDamageToArmor();
                Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
                
                float currDamage = 0f;

                if (customCombatData.get("ASF_undyingHullmodDamage" + ship.getId()) instanceof Float) {
                    currDamage = (float) customCombatData.get("ASF_undyingHullmodDamage" + ship.getId());
                }
                
                if (ship.getSystem().isActive()) {
                	totalDamage *= 1.35f; // boosted charge generation when system is active!
                }
                
                currDamage += totalDamage;
                
                customCombatData.put("ASF_undyingHullmodDamage" + ship.getId(), currDamage);
            }
        }
    }
	// damage dealt listener [end]
    
    //bar rendering everyframe
    private static class chargeBarManager extends BaseEveryFrameCombatPlugin {

        ShipAPI ship;

        private chargeBarManager(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {

            CombatEngineAPI engine = Global.getCombatEngine();
            
            if (!ship.isAlive()) {
                engine.removePlugin(this);
                return;
            }
            
            if (ship == engine.getPlayerShip()) {
            	
            	float maxCharge = ship.getMaxFlux() * 0.1f; // we limit max charge, for balans
            	
            	ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("UNDYING_MALICE_DATA_KEY" + ship.getId());
            	float chargeFill = Math.max(0f, Math.min(1f, info.charge / maxCharge)); // a double sanity check because i'm paranoid lol!
            	
            	if (info.charge >= REPAIR_THRESHOLD) {
            		if (info.repairCooldown < REPAIR_CD) {
                		MagicUI.drawHUDStatusBar(ship,
                				chargeFill,
        						new	Color(205,98,22,255), // old col: 80,205,125,255
        						null,
        						chargeFill * (Math.min(1f, info.repairCooldown * 0.1f)), // the repair "marker" moves up as the repair recharges
        						"CHARGE: " + (int) info.charge,
        						"",
        						false);
            		} else {
            			// when repair is ready the color matches the pipes!
                		MagicUI.drawHUDStatusBar(ship,
                				chargeFill,
        						new	Color(105,255,155,255),
        						null,
        						chargeFill,
        						"CHARGE: " + (int) info.charge,
        						"",
        						false);
            		}
            	} else {
            		MagicUI.drawHUDStatusBar(ship,
            				chargeFill,
            				new	Color(205,42,68,255), // was:  "textFriendColor").darker().darker()
    						null,
    						0,
    						"CHARGE: " + (int) info.charge,
    						"",
    						false);
            	}
            }
            
        }
    }
    //bar rendering everyframe
    
    private class ShipSpecificData {
    	private float charge = 0f;
    	private float decay = -2f;
    	private boolean dead = false;
    	private boolean doOnce = true;
    	private float fadeIn = 0f;
    	private float repairCooldown = 10f;
    }
}