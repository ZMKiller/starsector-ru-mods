package org.amazigh.foundry.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicUI;

public class ASF_PhantasmagoriaRegulator_off extends BaseHullMod {
	
	public static final float MAINT_MALUS = 150f;
	public static final float DEGRADE_INCREASE_PERCENT = 30f;
	public static final float DP_MALUS = 100f;
	public static final float VENT_MALUS = 0.25f; // this is hidden, and there to make venting "better" at what you want it for, by being a bit slower
	
	public static final float ENERGY_DAMAGE_BONUS = 80f;
	public static final float DAMAGE_RESIST = 30f;
	
	public static final float TP_STAB_COST = 10f;
	public static final float CORE_STAB_COST = 25f;
	
	public static final float ARC_RANGE = 700f;
	public static final float V_ARC_RANGE = 600f; // vent arcs have slightly shorter range!
	
    private final IntervalUtil overloadInterval = new IntervalUtil(0.2f, 0.35f);
    private final IntervalUtil overloadInterval2 = new IntervalUtil(0.6f, 1.25f);
    
    private final IntervalUtil ventInterval = new IntervalUtil(0.2f, 0.35f);
    
    private final IntervalUtil smokeInterval = new IntervalUtil(0.05f, 0.08f);
    
    private static Map<HullSize, Integer> arcCount = new HashMap<HullSize, Integer>();
	static {
		arcCount.put(HullSize.FIGHTER, 1);
		arcCount.put(HullSize.FRIGATE, 2);
		arcCount.put(HullSize.DESTROYER, 3);
		arcCount.put(HullSize.CRUISER, 4);
		arcCount.put(HullSize.CAPITAL_SHIP, 5);
		arcCount.put(HullSize.DEFAULT, 3);
	}
    
	private static final float HULL_REPAIR_MULTIPLIER = 100.0f;
    private static final float SPARK_MAX_RADIUS = 5f;
    private static final float SPARK_BRIGHTNESS = 0.8f;
    private static final float SPARK_DURATION = 0.4f;
    private static final Color SPARK_COLOR = new Color(50, 240, 100);
    private final IntervalUtil repairSparkInterval = new IntervalUtil(0.033f, 0.033f);
	
	@Override
    public int getDisplaySortOrder() {
        return 2221;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		stats.getSuppliesToRecover().modifyPercent(id, DP_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, DP_MALUS);
		
		stats.getVentRateMult().modifyMult(id, 1f - VENT_MALUS);
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		
		if (!ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("PHANTASMAGORIA_REGULATOR_OFF_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        
        CombatEngineAPI engine = Global.getCombatEngine();
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        if (info.doOnce) {
        	Global.getCombatEngine().addPlugin(new chargeBarManager(ship));
        	
        	info.doOnce = false;
        }
        
		if (ship.getPhaseCloak().isActive()) {
			if (!info.coreActive) {
				info.stability -= CORE_STAB_COST;
				info.coreActive = true;
			}
		} else {
			info.coreActive = false;
		}
		
		if (ship.getSystem().isActive()) {
			if (!info.sysActive) {
				info.stability -= TP_STAB_COST;
				info.sysActive = true;
			}
		} else {
			info.sysActive = false;
		}
		
		
		int redA = Math.min(255, 110 + (int) (info.stability * 0.8f));
		int blueA = Math.max(160 - (int) (info.stability), 0);
		Color arcColor = new Color(redA,45,blueA,255);
		// base color		- 110,45,160
		// min stab color	- 190,45,60
		
		// if stab drops to/below 0, then overload (for 6-12s, time scaling on flux lvl, longer with more flux)
		if (info.stability < 0f) {
			info.stability = 0f;
			ship.getFluxTracker().beginOverloadWithTotalBaseDuration(6f + (ship.getFluxLevel() * 6f));
			
			// A "combined" 60% slow, mostly to make you more vulnerable if you are flamed out (and to help with the appearance of any visual arcs)
			ship.getVelocity().scale(0.4f);
			ship.setAngularVelocity(ship.getAngularVelocity() * 0.4f);
			
			for (int i=0; i < 4; i++) {
				// spawn invisible EMP arcs to zap the ship, self damage!
				engine.spawnEmpArcPierceShields(ship,
						MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius()),
	        			ship,
	        			ship,
	        			DamageType.HIGH_EXPLOSIVE,
	        			200f, // oof ow 200 (800) HE!
	        			2000f, // oh no that's a lot of emp!
	        			1000f,
	        			"A_S-F_quiet_emp_impact",
						15f,
						new Color(15,5,10,15),
						new Color(25,20,25,25));
			}
			
			Global.getSoundPlayer().playSound("disabled_large_crit", 0.9f, 1.8f, ship.getLocation(), ship.getVelocity());
			// do a funny and play the big scary sound!
			
			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.4f,
	                500f,
	                350f,
	                500f,
	                250f,
	                CollisionClass.PROJECTILE_FF,
	                CollisionClass.PROJECTILE_FIGHTER,
	                2f,
	                6f,
	                1.4f,
	                80,
	                new Color(210,235,255, 155),
	                new Color(145,40,105, 255));
	        blast.setDamageType(DamageType.FRAGMENTATION);
	        blast.setShowGraphic(true);
	        blast.setDetailedExplosionFlashColorCore(new Color(145,40,105, 255));
	        blast.setDetailedExplosionFlashColorFringe(new Color(40,80,180, 255));
	        blast.setUseDetailedExplosion(true);
	        blast.setDetailedExplosionRadius(400f);
	        blast.setDetailedExplosionFlashRadius(500f);
	        blast.setDetailedExplosionFlashDuration(0.4f);
	        
	        engine.spawnDamagingExplosion(blast, ship, ship.getLocation(), true);
	        // also spawn an explosion, for visuals and to do some extra damage to anything close to you.
			
	        // and some general vfx
	        engine.addNebulaParticle(ship.getLocation(),
	        		MathUtils.getRandomPointInCircle(null, 3f),
					150f,
					2.3f, // end scale
					0.8f, // ramp
					0.6f, // full bright
					2.2f,
					new Color(165,45,80,100),
					false);
	        
	        // some "ring" visual arcs
	        for (int i=0; i < 4; i++) {
				float angleRandom1 = (i * 90f) + MathUtils.getRandomNumberInRange(0, 90);
	            float distanceRandom1 = MathUtils.getRandomNumberInRange(60f, 150f);
	            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
	            
	            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
	            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
	            
	            engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship,
	            		14f,
                        arcColor,
                        new Color(190,225,255, 255));
			}
	        
	        for (int i=0; i < 72; i++) {
				float dist = MathUtils.getRandomNumberInRange(0.1f, 3.0f);
    			float angle = i * 5f;
				
    			int colVarR = MathUtils.getRandomNumberInRange(0, 80);
    			int colVarB = MathUtils.getRandomNumberInRange(0, 30);
    			
    			engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(null, ship.getCollisionRadius() * (dist * 0.2f), angle),
						50f,
						MathUtils.getRandomNumberInRange(1.8f, 2.2f),
						0.7f,
						0.3f,
						MathUtils.getRandomNumberInRange(1.9f, 2.7f) - (dist * 0.2f),
						new Color(215 - colVarR,40,105 + colVarB,120),
						false);
    		}
	        
			// and some sparks
			ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
			emitter.location(ship.getLocation());
			emitter.life(1.2f, 1.7f);
			emitter.size(4f, 9f);
			emitter.velocity(ship.getCollisionRadius() * 2.7f, - (ship.getCollisionRadius() * 1.8f));
			emitter.distance(ship.getCollisionRadius() * 0.4f, ship.getCollisionRadius() * 2.4f);
			emitter.color(redA,45,blueA,255);
			emitter.burst(144);
			
	        
			
			int arcsFired = 0;
			List<ShipAPI> targets = AIUtils.getNearbyEnemies(ship, ARC_RANGE);
			List<ShipAPI> targets2 = AIUtils.getNearbyAllies(ship, ARC_RANGE);
			
            if (targets.size() > 0) {
            	for (int i=0; i < targets.size(); i++) {
                    ShipAPI target = targets.get(i);
                    
                    int arcNum = MathUtils.getRandomNumberInRange(1, arcCount.get(target.getHullSize()));
                    
                	for (int j=0; j < arcNum; j ++) {
                		
                		Vector2f arcStart = MathUtils.getRandomPointInCircle(MathUtils.getPointOnCircumference(ship.getLocation(), 9f, ship.getFacing() + 180f), 35f);
                		EmpArcEntityAPI arc = engine.spawnEmpArc(ship,
                    			arcStart,
                                ship,
                                target,
                                DamageType.ENERGY,
                                100f,
                                400f,
                                ARC_RANGE + 1000f,
                                null,
                                14f,
                                arcColor,
                                new Color(190,225,255, 255));
                    	
                		
                    	Vector2f arcEnd = arc.getTargetLocation();
                    	
                    	// lil bit of knockback
                    	CombatUtils.applyForce(target, VectorUtils.getAngle(arcStart, arcEnd), 50f);
                        
                	}
                	Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, target.getLocation(), target.getVelocity());
                	arcsFired ++;
            	}
            }
            if (targets2.size() > 0) {
            	for (int i=0; i < targets2.size(); i++) {
                    ShipAPI target = targets2.get(i);
                    
                    int arcNum = MathUtils.getRandomNumberInRange(1, arcCount.get(target.getHullSize()));
                    
                	for (int j=0; j < arcNum; j ++) {
                		
                		Vector2f arcStart = MathUtils.getRandomPointInCircle(MathUtils.getPointOnCircumference(ship.getLocation(), 9f, ship.getFacing() + 180f), 35f);
                		EmpArcEntityAPI arc = engine.spawnEmpArc(ship,
                    			arcStart,
                                ship,
                                target,
                                DamageType.ENERGY,
                                100f,
                                400f,
                                ARC_RANGE + 1000f,
                                null,
                                14f,
                                arcColor,
                                new Color(190,225,255, 255));
                    	
                    	Vector2f arcEnd = arc.getTargetLocation();
                    	
                    	// lil bit of knockback
                    	CombatUtils.applyForce(target, VectorUtils.getAngle(arcStart, arcEnd), 50f);
                	}
                	Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, target.getLocation(), target.getVelocity());
                	arcsFired ++;
            	}
            }
			
            if (arcsFired < 12) {
            	
            	for (int i=12; i > arcsFired; i--) {
            		Vector2f arcStart = MathUtils.getRandomPointInCircle(MathUtils.getPointOnCircumference(ship.getLocation(), 9f, ship.getFacing() + 180f), 35f);
            		
            		engine.spawnEmpArcVisual(arcStart, ship,
            				MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(150f, ARC_RANGE)), ship,
            				13f,
            				arcColor,
                            new Color(190,225,255, 255));
            		
        			Global.getSoundPlayer().playSound("A_S-F_quiet_emp_impact", 1f, 1f, ship.getLocation(), ship.getVelocity());
            	}
            }
            
		}
		
		// so the overload/vent stuff happens more intensely at lower stability levels
		float intervalMult = amount * (1f - (0.4f * info.stability * 0.01f));
		
		// fire an arc at a nearby ship every so often while overloaded
		if (ship.getFluxTracker().isOverloaded()) {
			
			// "damp" turn rate when overloaded, (to assist with the appearance of the visual arcs)
			ship.setAngularVelocity(ship.getAngularVelocity() * (1f - (0.6f * amount)));
			stats.getTurnAcceleration().modifyMult(spec.getId(), 0.4f);
			stats.getMaxTurnRate().modifyMult(spec.getId(), 0.5f);
			
			
			overloadInterval.advance(intervalMult);
	        if (overloadInterval.intervalElapsed()) {
	        	
                List<ShipAPI> targets = AIUtils.getNearbyEnemies(ship, ARC_RANGE);
    			List<ShipAPI> targets2 = AIUtils.getNearbyAllies(ship, ARC_RANGE);
    			
    			Vector2f arcStart = MathUtils.getRandomPointInCircle(MathUtils.getPointOnCircumference(ship.getLocation(), 9f, ship.getFacing() + 180f), 35f);
                
                if (targets.size() > 0) {
                	ShipAPI target = targets.get(MathUtils.getRandom().nextInt(targets.size()));
                	engine.spawnEmpArc(ship,
                			arcStart,
                            ship,
                            target,
                            DamageType.ENERGY,
                            100f,
                            400f,
                            ARC_RANGE + 1000f,
                            "tachyon_lance_emp_impact",
                            14f,
                            arcColor,
                            new Color(190,225,255, 255));
                	
                } else if (targets2.size() > 0) {
                    ShipAPI target = targets2.get(MathUtils.getRandom().nextInt(targets2.size()));
                    engine.spawnEmpArc(ship,
                			arcStart,
                            ship,
                            target,
                            DamageType.ENERGY,
                            100f,
                            400f,
                            ARC_RANGE + 1000f,
                            "tachyon_lance_emp_impact",
                            14f,
                            arcColor,
                            new Color(190,225,255, 255));
    			} else {
            		engine.spawnEmpArcVisual(arcStart, ship,
            				MathUtils.getRandomPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(150f, ARC_RANGE)), ship,
            				13f,
            				arcColor,
                            new Color(190,225,255, 255));
            		
        			Global.getSoundPlayer().playSound("A_S-F_quiet_emp_impact", 1f, 1f, ship.getLocation(), ship.getVelocity());
                }
                
                // some constant nebula vfx
            	for (int i=0; i < 5; i ++) {
                    float angleRandom = MathUtils.getRandomNumberInRange(0, 360);
    	            float distanceRandom = MathUtils.getRandomNumberInRange(35f, 110f);
    	            
                    engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom, angleRandom),
    	            		MathUtils.getPointOnCircumference(null, distanceRandom * 0.1f, angleRandom),
    						40f,
    						MathUtils.getRandomNumberInRange(1.8f, 2.2f),
    						0.7f,
    						0.3f,
    						MathUtils.getRandomNumberInRange(0.6f, 1.0f),
    						new Color(215 - MathUtils.getRandomNumberInRange(0, 80),40,105 + MathUtils.getRandomNumberInRange(0, 30),110),
    						false);
            	}
                
	        }
	        overloadInterval2.advance(intervalMult);
	        if (overloadInterval2.intervalElapsed()) {

                // spawn a "ring" visual arc along with some nebulas
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	            float distanceRandom1 = MathUtils.getRandomNumberInRange(40f, 125f);
	            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom1, angleRandom1);
	            
	            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
	            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(ship.getLocation(), distanceRandom2, angleRandom2);
	            
	            engine.spawnEmpArcVisual(arcPoint1, ship, arcPoint2, ship,
	            		12f,
                        arcColor,
                        new Color(190,225,255, 255));

	            engine.addNebulaParticle(arcPoint1,
	            		MathUtils.getPointOnCircumference(null, distanceRandom1 * 0.1f, angleRandom1),
						40f,
						MathUtils.getRandomNumberInRange(1.8f, 2.2f),
						0.7f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.9f, 1.3f),
						new Color(215 - MathUtils.getRandomNumberInRange(0, 80),40,105 + MathUtils.getRandomNumberInRange(0, 30),120),
						false);
	            
	            float angleMid = (angleRandom1 + angleRandom2) * 0.5f;
	            float distanceMid = (distanceRandom1 + distanceRandom2) * 0.5f;
	            Vector2f nebPoint = MathUtils.getPointOnCircumference(ship.getLocation(), distanceMid,  angleMid);
	            engine.addNebulaParticle(nebPoint,
	            		MathUtils.getPointOnCircumference(null, distanceMid * 0.1f, angleMid),
						40f,
						MathUtils.getRandomNumberInRange(1.8f, 2.2f),
						0.7f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.9f, 1.3f),
						new Color(215 - MathUtils.getRandomNumberInRange(0, 80),40,105 + MathUtils.getRandomNumberInRange(0, 30),120),
						false);
	            
	            engine.addNebulaParticle(arcPoint2,
	            		MathUtils.getPointOnCircumference(null, distanceRandom1 * 0.1f, angleRandom1),
						40f,
						MathUtils.getRandomNumberInRange(1.8f, 2.2f),
						0.7f,
						0.3f,
						MathUtils.getRandomNumberInRange(0.9f, 1.3f),
						new Color(215 - MathUtils.getRandomNumberInRange(0, 80),40,105 + MathUtils.getRandomNumberInRange(0, 30),120),
						false);
	            
			
	        }
		} else {
			stats.getTurnAcceleration().unmodify(spec.getId());
			stats.getMaxTurnRate().unmodify(spec.getId());
		}
		
		
		// rendering the core glow
		SpriteAPI Glow = Global.getSettings().getSprite("fx", "A_S-F_phantasmagoria_glow");
    	Vector2f glowSize = new Vector2f(60f, 34f);
    	Vector2f glowLocInit = MathUtils.getPointOnCircumference(ship.getLocation(), -20f, ship.getFacing());
    	
    	int alpha = 225; // 155
    	double timeMult = (double) ship.getMutableStats().getTimeMult().modified;
		alpha = (int) Math.ceil(225 / timeMult);
    	alpha = Math.min(alpha, 255);
    	
		// setting up the scaling vfx colors
		int blue = Math.min(105 + (int) (info.stability * 1.3f), 255);
    	Color effectColor = new Color(255,100,blue,alpha);
    	// base color		- 255, 100, 240
    	// min stab color	- 255, 100, 110
    	//					- 0,   0,   -130
    	
    	MagicRender.singleframe(Glow, glowLocInit, glowSize, ship.getFacing() - 90f, effectColor, true);
		
		
    	// fire arcs at nearby enemies when venting
		if (ship.getFluxTracker().isVenting()) {
			
			ventInterval.advance(intervalMult);
	        if (ventInterval.intervalElapsed()) {
	        	
                List<ShipAPI> targets = AIUtils.getNearbyEnemies(ship, V_ARC_RANGE);
    			
                Vector2f arcStart = ship.getLocation();
                
                // having the arcs randomly spawn from one of the two system slots, this is a "controlled" emission, so arcs come from a specific spot, not at random
                if (Math.random() > 0.5) {
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.SMALL)) {
                			arcStart = weapon.computePosition(ship);
                		}
                	}
        		} else {
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.MEDIUM)) {
                			arcStart = weapon.computePosition(ship);
                		}
                	}
        		}
                
                if (targets.size() > 0) {
                    ShipAPI target = targets.get(MathUtils.getRandom().nextInt(targets.size()));
                    
                    float arcDamage = 80f - (Math.max(1f, info.stability - 10f) * 0.666f);
                    // arc damage scales with stability, so active venting when at low stability is good not just for stab regain, but also some damage!
                    // scales from 20 damage at max stab, to (just under) 80 damage at 10 or lower stab
                    
                    EmpArcEntityAPI arc = engine.spawnEmpArc(ship,
                			arcStart,
                            ship,
                            target,
                            DamageType.ENERGY,
                            arcDamage,
                            80f + (arcDamage * 3f), // so we always do a "noticeable amount" of EMP, but it ramps up as stability drops
                            V_ARC_RANGE + 1000f,
                            "tachyon_lance_emp_impact",
                            12f,
                            arcColor,
                            new Color(210,235,255, 255));
                	
                	info.stability = Math.min(info.stability + 1.6f, 100f); // a bit of stability regeneration here, we're "offloading" it into the enemy
                	target.getFluxTracker().increaseFlux(100f, true); // dump some (flat) flux into the target, representing "offloading" the instability
                	
                	// a sneaky tiny little bit of hull repair, just as some more "combat vent reward"
                	ship.setHitpoints(Math.min(ship.getHitpoints() + 10f, ship.getMaxHitpoints()));
    	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f);
    	        	engine.addHitParticle(sparkLoc, ship.getVelocity(), SPARK_MAX_RADIUS * (float) Math.random()
    	        			+ SPARK_MAX_RADIUS, SPARK_BRIGHTNESS, SPARK_DURATION,
    	        			SPARK_COLOR);
                	
                	Vector2f arcEnd = arc.getTargetLocation();
                	
                	// do some vfx at the arcs impact point
                	for (int i=0; i < 2; i++) {
                    	engine.addNebulaParticle(arcEnd,
        		        		target.getVelocity(),
        						32f,
        						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
        						0.7f,
        						0.5f,
        						0.55f,
        						arcColor,
        						false);
                    	for (int j=0; j < 8; j++) {
                        	Global.getCombatEngine().addSmoothParticle(arcEnd,
            						MathUtils.getRandomPointInCircle(target.getVelocity(), 90f),
            						MathUtils.getRandomNumberInRange(4f, 9f), //size
            						0.65f, //brightness
            						MathUtils.getRandomNumberInRange(0.4f, 0.55f), //duration
            						arcColor);
                    	}
                	}
                	
                }
                
                // and we spawn some nebula particles
                for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            		if (weapon.isSystemSlot()) {

        				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
            			
            			engine.addNebulaParticle(weapon.computePosition(ship),
        		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-10f, 10f)),
        						32f,
        						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
        						0.7f,
        						0.3f,
        						0.5f,
        						new Color(140,70,130,120),
        						false);
            			
            			// and some sparks
            			for (int i=0; i < 3; i++) {
                        	Global.getCombatEngine().addSmoothParticle(weapon.computePosition(ship),
            						MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-10f, 10f)),
            						MathUtils.getRandomNumberInRange(4f, 9f), //size
            						0.6f, //brightness
            						0.6f, //duration
            						arcColor);
                    	}
            		}
            	}
                
	        }
			
		}
				
		
		// stability regeneration
		float stabGain = amount;
		if (ship.getFluxTracker().isVenting() || ship.getFluxTracker().isEngineBoostActive()) {
        	stabGain *= 2f;
        	
        	float spoolDecay = 1f * amount;
            
            if (ship.getFluxTracker().isVenting()) {
            	stabGain *= 1.5f; // some hidden extra stability gain when venting!
            	spoolDecay *= 2f; // hidden BOOSTED spool rate when venting, so vent repairs are more "responsive"
            }
            
            info.spool = Math.max(0f, info.spool -= spoolDecay); // start spooling!
        	
        } else {
        	info.spool = Math.min(2f, info.spool + amount);
        	// when not "valid" have the spool decay back to being reset
        	// this is to allow for swapping from vent to zero flux, and any other breaks in uptime not completely shutting down regen
        }
		
		// so we regen as long as spool is "valid" a cute little trick that ties into the decay of spool ;)
		if (info.spool < 2f) {
			
        	// repair stuff
        	boolean repairing = false;
        	
            float regenMult = amount * (0.2f + (1.0f * info.stability * 0.01f)); // "base" of 20% power, scaling up to 120% power at max stability
            // you *really* don't want to be on low stab when repairing, but can't gain stab rapidly without "combat venting" so it's a "swings/roundabouts" thing
            // so if you're on low hull+stab, then you *have* to either run and wait a long time, or go balls to the wall and do a combat vent. 
            
            float spoolMult = Math.min(Math.max(0.5f * (2f - info.spool), 0f), 1f); // clamping spool mult just to be safe (this is a variable saying how much we're spooled up by)
            regenMult *= spoolMult; // scaling regeneration rate on current spool power
            
        	if (DefenseUtils.hasArmorDamage(ship)) {
    			info.pulse = true;
        		repairing = true;
        		
	        	ArmorGridAPI armorGrid = ship.getArmorGrid();
		        final float[][] grid = armorGrid.getGrid();
		        final float max = armorGrid.getMaxArmorInCell();
		        
		        float baseCell = armorGrid.getMaxArmorInCell() * (Math.min(ship.getHullSpec().getArmorRating(), 250f) / armorGrid.getArmorRating()); // clamping regen to max out 250 armour, so going sicko with stacking HA/etc doesn't give massive regen as well
		        float repairAmount = baseCell * 0.1f * regenMult;
		        	// at 0 stability, then you regen 2% armour/sec
		        	// at full stability you regen 12% armour/sec
		        
				for (int x = 0; x < grid.length; x++) {
		            for (int y = 0; y < grid[0].length; y++) {
		                if (grid[x][y] < max) {
		                    float regen = grid[x][y] + repairAmount;
		                    armorGrid.setArmorValue(x, y, regen);
		                }
		            }
		        }
				
		        ship.syncWithArmorGridState();
		        ship.syncWeaponDecalsWithArmorDamage();
        	} else {
        		
        		// a visual "pulse" to hide the clearing of damage decals
        		if (info.pulse) {
        			info.pulse = false;
            		ship.clearDamageDecals();
            		
            		engine.addHitParticle(ship.getLocation(),
            				ship.getVelocity(),
            				135f,
            				0.95f,
            				0.25f,
    	        			SPARK_COLOR);
            		
            		for (int i = 0; i < 8; i++) {
            			float angle = MathUtils.getRandomNumberInRange(0f, 45f) + (i * 45f);
        	            engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), 30f,  angle),
        	            		MathUtils.getPointOnCircumference(ship.getVelocity(), 20f, angle),
        						90f,
        						MathUtils.getRandomNumberInRange(0.5f, 0.6f),
        						0.7f,
        						0.3f,
        						0.45f,
        						new Color(60,240,120, 150),
        						false);
        	            for (int j = 0; j < 3; j++) {
            	        	engine.addHitParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * 0.8f, angle),
            	        			MathUtils.getPointOnCircumference(ship.getVelocity(), 20f, angle),
            	        			SPARK_MAX_RADIUS * (float) Math.random()
            	        			+ SPARK_MAX_RADIUS,
            	        			SPARK_BRIGHTNESS * spoolMult,
            	        			SPARK_DURATION,
            	        			SPARK_COLOR);
        	            }
            		}
            		
        		}
        	}
        	
        	if (ship.getHitpoints() < ship.getMaxHitpoints()) {
        		repairing = true;
        		ship.setHitpoints(Math.min(ship.getHitpoints() + (HULL_REPAIR_MULTIPLIER * regenMult), ship.getMaxHitpoints()));
	        	// at 0 stability, then you regen 20 hull/sec
	        	// at full stability you regen 120 hull/sec
        	}
        	
        	// regen spark vfx (opacity of sparks scales up with how spooled up the regen is)
        	if (repairing) {
        		if (spoolMult > 0f) {
        			repairSparkInterval.advance(amount);
        	        if (repairSparkInterval.intervalElapsed()) {
        	        	Vector2f sparkLoc = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * 0.8f);
        	        	engine.addHitParticle(sparkLoc, ship.getVelocity(), SPARK_MAX_RADIUS * (float) Math.random()
        	        			+ SPARK_MAX_RADIUS, SPARK_BRIGHTNESS * spoolMult, SPARK_DURATION,
        	        			SPARK_COLOR);
        	        }
        		}	
        	}
        	
		}
		
		
		// stability regeneration
		info.stability = Math.min(info.stability + stabGain, 100f);
		
		
		// stability stat mods
		float stabPerc = (100f - (Math.max(0f, (info.stability - 10f)) / 0.9f)) * 0.01f;
		
		stats.getEnergyWeaponDamageMult().modifyMult(spec.getId(), 1f + (ENERGY_DAMAGE_BONUS * 0.01f * stabPerc));
		stats.getHullDamageTakenMult().modifyMult(spec.getId(), 1f - (DAMAGE_RESIST * 0.01f * stabPerc));
		stats.getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - (DAMAGE_RESIST * 0.01f * stabPerc));
		stats.getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - (DAMAGE_RESIST * 0.01f * stabPerc));
		
		
		// spawn smoke when at "low" stability
		if (info.stability < 50) {
			smokeInterval.advance(amount);
	        if (smokeInterval.intervalElapsed()) {
	        	
	        	int smokeAlpha = 15 + (int) Math.min(120f, Math.max(0f, (2f * (50f - info.stability))));
	        	
	        	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	        		if (weapon.isSystemSlot()) {
	        			Vector2f posZero = weapon.computePosition(ship);
	        			Vector2f smokeInit = new Vector2f(ship.getVelocity().x * 0.4f,ship.getVelocity().y * 0.4f);
	        			Vector2f smokeVel = MathUtils.getPointOnCircumference(smokeInit, MathUtils.getRandomNumberInRange(3f, 18f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-15f, 15f));
	    	            engine.addNebulaSmokeParticle(posZero,
	    	            		smokeVel,
	    	            		MathUtils.getRandomNumberInRange(12f, 16f),
	    	            		2.4f, // size mult
	    	            		0.7f, // ramp
	    	            		0.6f, // bright frac
	    	            		MathUtils.getRandomNumberInRange(0.5f, 1.0f),
	    	            		new Color(110,110,100,smokeAlpha));
	        			for (int i=0; i < 2; i++) {
	        				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 45f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-18f, 18f));
	        	            engine.addSmoothParticle(posZero,
	        	            		particleVel,
	        	            		MathUtils.getRandomNumberInRange(2f, 8f),
	        	            		0.8f,
	        	            		MathUtils.getRandomNumberInRange(0.3f, 0.9f),
	        	            		new Color(255,120,80,smokeAlpha));
	        			}
	        		}
	        	}
	        }
			
		}
		
		
//		// UI display of stability level
//		if (ship == Global.getCombatEngine().getPlayerShip()) {
//			
//			// color changes when at low stability levels, it's a warning!
//			if (info.stability > 25) {
//				MagicUI.drawHUDStatusBar(ship,
//						(info.stability * 0.01f),
//						null,
//						null,
//						0.25f,
//						"STABILITY",
//						"CORE",
//						false);				
//			} else if (info.stability > 10) {
//				MagicUI.drawHUDStatusBar(ship,
//						(info.stability * 0.01f),
//						Color.ORANGE,
//						null,
//						0.1f,
//						"STABILITY",
//						"CORE",
//						false);
//			} else {
//				MagicUI.drawHUDStatusBar(ship,
//						(info.stability * 0.01f),
//						Color.RED,
//						null,
//						0,
//						"STABILITY",
//						"CORE",
//						false);
//			}
//			
//        }

		engine.getCustomData().put("PHANTASMAGORIA_REGULATOR_OFF_DATA_KEY" + ship.getId(), info);
		
		
		// little thing to make this mode "usable" by the AI
		if (Global.getCombatEngine().isPaused() || ship.getShipAI() == null) {
			return;
		}
		
		// so that the AI knows to never overload the Temporal Core (truly i am giving it access to too much power here)
		if (info.stability < 25f) {
			ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
		}
		if (info.stability < 10f) {
			ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
		}
		
        if (!ship.getFluxTracker().isOverloadedOrVenting()) {
        	if (ship.getFluxTracker().getFluxLevel() > 0.9f && info.stability < 50f) {
        		// if flux is over 90%, and stability is below 50: VENT (for stab)
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
            	return;
                // it's generally good to vent when at high flux, and the AI is likely to be in range for the arcs when it hits high flux (by firing a weapon)
        	}
        	
        	if (ship.getFluxTracker().getFluxLevel() > 0.4f && info.stability < 30f) {
        		// if flux is over 40%, and stability is below 30: VENT (for stab)
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
            	return;
                // if at low stab, then vent more aggressively than at high stab, we want to try and have the AI preserve stability.
        	}
        	
        	if (ship.getFluxTracker().getFluxLevel() > 0.7f && ship.getHullLevel() < 0.95f) {
        		// if flux is over 70%, and we've taken more than 5% hull damage: VENT (for regen)
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
            	return;
                // just a "safety" check to make sure the AI will vent (for some regen) if it's taken even a small amount of damage
        	}
        	
        	if (ship.getFluxTracker().getFluxLevel() > 0.3f && ship.getHullLevel() < 0.5f) {
        		// if flux is over 30%, and we've taken more than 50% hull damage: VENT (for regen)
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
                // hull is low? VENT YOU FOOL.
        	}
        	
        }
		
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
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color grey = Misc.getGrayColor();
		Color flavor = new Color(165,90,255,180);
		Color banner = new Color(64,21,77);
		
		LabelAPI label = tooltip.addPara("The Temporal Core currently has its regulator disabled, this results in increased combat performance, in exchange for a less stable operation of systems.", opad);
		
		//  "Deep insight gives way to Spectacle, and Spectacle to Violence and Gore, until in the end little is left but the Gore."
		
		/*
Emperor, your sword won't help you out
Sceptre and crown are worthless here
I've taken you by the hand
For you must come to my dance
		 */
		
		label = tooltip.addPara("The monthly maintenance supply cost is increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("The rate of in-combat CR decay after peak performance time runs out is increased by %s.", pad, bad, "" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlight("" + (int)DEGRADE_INCREASE_PERCENT + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Deployment and recovery costs increased by %s.", opad, bad, "" + (int)DP_MALUS + "%");
		label.setHighlight("" + (int)DP_MALUS + "%");
		label.setHighlightColors(bad);
		
		tooltip.addSectionHeading("Core Stability", h, banner, Alignment.MID, opad);
		label = tooltip.addPara("Due to the regulator being disabled, the Temporal Core is unstable.", opad);
		label = tooltip.addPara("%s decays stability by %s.", pad, h, "Cycling the Temporal Core", (int) CORE_STAB_COST + "%");
		label.setHighlight("Cycling the Temporal Core", (int) CORE_STAB_COST + "%");
		label.setHighlightColors(h, bad);
		label = tooltip.addPara("Using the %s decays stability by %s.", pad, h, "Dimensional Teleporter", (int) TP_STAB_COST + "%");
		label.setHighlight("Dimensional Teleporter", (int) TP_STAB_COST + "%");
		label.setHighlightColors(h, bad);
		label = tooltip.addPara("%s passively regenerates at a rate of %s per second.", pad, h, "Core Stability", "1%");
		label.setHighlight("Core Stability", "1%");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("Stability regenerates at %s the normal rate if the ship has the %s, or is %s.", pad, h, "Double", "Zero-Flux Speed boost", "Actively Venting");
		label.setHighlight("Double", "Zero-Flux Speed boost", "Actively Venting");
		label.setHighlightColors(h, h, h);
		label = tooltip.addPara("While Active Venting the Temporal Core will arc energy to any nearby hostile vessels, dealing damage while further regenerating stability on any arc impacts.", pad);
		
		label = tooltip.addPara("As stability drops, unstable energy feeds into the ships systems.", opad);
		label = tooltip.addPara("Energy weapon damage is increased by up to %s.", pad, h, "" + (int) ENERGY_DAMAGE_BONUS +"%");
		label.setHighlight("" + (int) ENERGY_DAMAGE_BONUS +"%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Damage taken is reduced by up to %s.", pad, h, "" + (int) DAMAGE_RESIST +"%");
		label.setHighlight("" + (int) DAMAGE_RESIST +"%");
		label.setHighlightColors(h);
		label = tooltip.addPara("%s", pad, grey, "These modifiers reach full potency at 10% or lower Stability.");
		
		label = tooltip.addPara("If stability drops to %s then the Temporal Core will %s.", opad, bad, "0", "Overload");
		label.setHighlight("0", "Overload");
		label.setHighlightColors(bad, bad);
		label = tooltip.addPara("When overloaded the Temporal Core will emit arcs of energy, damaging the Phantasmagoria and all nearby ships.", pad);
		
		tooltip.addSectionHeading("Dimensional Reconstructor", h, banner, Alignment.MID, opad);
		label = tooltip.addPara("This ship features an automated hull and armour repair system that operates most effectively at higher core stability levels.", opad);
		label = tooltip.addPara("While the ship has the %s, or is %s the reconstructor will activate and begin repairing the ship.", pad, h, "Zero-Flux Speed boost", "Actively Venting");
		label.setHighlight("Zero-Flux Speed boost", "Actively Venting");
		label.setHighlightColors(h, h);
		
		label = tooltip.addPara("%s", opad, grey, "Remove hullmod to enable core regulator.");
		label.setHighlight("Remove hullmod to enable core regulator.");
		label.setHighlightColors(grey);
		
		 tooltip.addPara("%s", 6f, flavor, new String[] { "\"I had to work very much and very hard" });
		 tooltip.addPara("%s", 2f, flavor, new String[] { "The sweat was running down my skin" });
		 tooltip.addPara("%s", 2f, flavor, new String[] { "I'd like to escape death nonetheless" });
		 tooltip.addPara("%s", 2f, flavor, new String[] { "But here I won't have any luck\"" });
	}

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
            	
            	ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("PHANTASMAGORIA_REGULATOR_OFF_DATA_KEY" + ship.getId());
            	
        			// color changes when at low stability levels, it's a warning!
        			if (info.stability > 25) {
        				MagicUI.drawHUDStatusBar(ship,
        						(info.stability * 0.01f),
        						Global.getSettings().getColor("textFriendColor").darker(),
        						null,
        						0.25f,
        						"STABILITY",
        						"CORE    ",
        						false);				
        			} else if (info.stability > 10) {
        				MagicUI.drawHUDStatusBar(ship,
        						(info.stability * 0.01f),
        						Color.ORANGE,
        						null,
        						0.1f,
        						"STABILITY",
        						"CORE    ",
        						false);
        			} else {
        				MagicUI.drawHUDStatusBar(ship,
        						(info.stability * 0.01f),
        						Color.RED,
        						null,
        						0,
        						"STABILITY",
        						"CORE    ",
        						false);
        			}
        		
            }
            
        }
    }
    //bar rendering everyframe
	
    private class ShipSpecificData {
        private float stability = 100f;
        private boolean sysActive = false;
        private boolean coreActive = false;
        private float spool = 2f;
        private boolean pulse = false;
    	private boolean doOnce = true;
    }

}
