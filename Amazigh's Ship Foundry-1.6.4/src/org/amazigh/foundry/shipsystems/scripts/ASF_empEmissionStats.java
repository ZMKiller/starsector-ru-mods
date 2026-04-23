package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_empEmissionStats extends BaseShipSystemScript {
	
	public static float MAIN_ARC_DAMAGE = 400f;
	public static float MAIN_ARC_EMP_DAMAGE = 1600f;
	
	public static float SUB_ARC_DAMAGE = 60f;
	public static float SUB_ARC_EMP_DAMAGE = 500f;
	
	public static float BLAST_DAMAGE = 300f;
	public static float BLAST_DAMAGE_OUTER = 200f;
	
	protected static float BASE_RANGE = 660f; // 10 range longer than "advertised" to make it more reliable to use or smth idk
	
	public static final Color ARC_COLOR_O = new Color(35,100,155,255);
	public static final Color ARC_COLOR_I = new Color(255,225,255,255);
	
	public static final Color BLAST_COLOR = new Color(35,100,155,120);
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String targetKey = ship.getId() + "_ASF_empEmission_target";
		Object foundTarget = Global.getCombatEngine().getCustomData().get(targetKey); 
		if (state == State.IN) {
			if (foundTarget == null) {
				ShipAPI target = findTarget(ship);
				if (target != null) {
					Global.getCombatEngine().getCustomData().put(targetKey, target);
				}
			}
		} else if (effectLevel >= 1) {
			if (foundTarget instanceof ShipAPI) {
				ShipAPI target = (ShipAPI) foundTarget;
				
				// hullmod universal slot thing checking for ballistic weapons here
				int subArcCount = 4;
				for (String slot : stats.getVariant().getNonBuiltInWeaponSlots() ) {
					if (stats.getVariant().getSlot(slot).getWeaponType() == WeaponType.UNIVERSAL && stats.getVariant().getWeaponSpec(slot).getType() == WeaponType.BALLISTIC) {
						subArcCount++;
					}
				}
				
				for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	        		if (weapon.isSystemSlot()) {
	        			
	        			Vector2f arcStart = weapon.computePosition(ship);
	        			
	        			// "muzzle flash"
	        			for (int i=0; i < 3; i++) {
	        				Vector2f arcPoint = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(30f, 75f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-45f, 45f));
	        				
		        			engine.spawnEmpArcVisual(arcStart, ship, arcPoint, ship, 10f,
		        					ARC_COLOR_O,
		    						ARC_COLOR_I);
		        			
		    				for (int j=0; j < 10; j++) {
		        				
		                        float angle = j * MathUtils.getRandomNumberInRange(0f, 36f);
		                        
		                        Vector2f flashVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 25f), angle);
		                        Vector2f sparkPoint = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(1f, 40f), angle);
		                        
		                        Global.getCombatEngine().addSmoothParticle(sparkPoint,
		                        		flashVel,
		                        		MathUtils.getRandomNumberInRange(3f, 8f), //size
		                        		1.0f, //brightness
		                        		0.5f, //duration
		                        		ARC_COLOR_O);
		        			}
	        			}
	        			
	        			// main arc
            			EmpArcEntityAPI arc = engine.spawnEmpArc(ship, arcStart, ship, target,
        						DamageType.KINETIC,
        						MAIN_ARC_DAMAGE, // damage
        						MAIN_ARC_EMP_DAMAGE, // emp
        						2000f, // max range
        						"system_emp_emitter_impact",
        						21f, // thickness
        						ARC_COLOR_O,
        						ARC_COLOR_I);
            			
            			Global.getSoundPlayer().playSound("system_emp_emitter_impact", 0.75f, 1.5f, arcStart, ship.getVelocity());
            			
            			Vector2f arcEnd = arc.getTargetLocation();
            			
            			// if it's a fighter, then do an explosion, if it's not a fighter, then spawn sub-arcs
            			// explosion for fighters, as that's a "simple" way of hitting other nearby fighters
            			if (target.isFighter()) {
            				
            				// hullmod universal slot thing, +15% blast damage per ballistic weapon
            				float blastAdd = 1f;
            				if (subArcCount > 4) {
            					blastAdd += (subArcCount -4) * 0.15f;  
            				}
            				
            				DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
            		                150f,
            		                85f,
            		                BLAST_DAMAGE * blastAdd,
            		                BLAST_DAMAGE_OUTER * blastAdd,
            		                CollisionClass.PROJECTILE_FF,
            		                CollisionClass.PROJECTILE_FIGHTER,
            		                3f,
            		                5f,
            		                0.5f,
            		                60,
            		                ARC_COLOR_O,
            		                BLAST_COLOR);
            		        blast.setDamageType(DamageType.ENERGY);
            		        blast.setShowGraphic(true);
            		        blast.setUseDetailedExplosion(false);
            		        
            		        engine.spawnDamagingExplosion(blast,ship,arcEnd,false);
            		        
    	        			for (int i=0; i < 8; i++) {
    	        				Vector2f arcPoint = MathUtils.getRandomPointOnCircumference(arcEnd, MathUtils.getRandomNumberInRange(30f, 150f));
    	        	            
    		        			engine.spawnEmpArcVisual(arcEnd, target, arcPoint, target, 12f,
    		        					ARC_COLOR_O,
    		    						ARC_COLOR_I);
    	        			}
            				
            			} else {
    	        			for (int i=0; i < subArcCount; i++) {
    	        				Vector2f arcPoint = MathUtils.getRandomPointOnCircumference(arcEnd, MathUtils.getRandomNumberInRange(30f, 150f));
    	        	            
    		        			engine.spawnEmpArcVisual(arcEnd, target, arcPoint, target, 12f,
    		        					ARC_COLOR_O,
    		    						ARC_COLOR_I);
    		        			
                				// hullmod universal slot thing, +1 arc, and ""+20%"" shield pierce per ballistic weapon
    		        			boolean pierceShield = false;
    		        			
    		        			if (subArcCount > 4) {
    		        				float pierceChance = (((ShipAPI)target).getHardFluxLevel() - 0.1f) * ((subArcCount - 4) * 0.2f);
    		        				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
    		        				pierceShield = (float) Math.random() < pierceChance;
    		        			}
    		        			
    		        			if (pierceShield) {
        		        			engine.spawnEmpArcPierceShields(ship, arcEnd, target, target,
        		        					DamageType.ENERGY,
        		        					SUB_ARC_DAMAGE,
        		        					SUB_ARC_EMP_DAMAGE,
        		        					1000f,
        		        					"tachyon_lance_emp_impact",
        		        					12f,
        		        					ARC_COLOR_O,
        		        					ARC_COLOR_I);
    		        			} else {
        		        			engine.spawnEmpArc(ship, arcEnd, target, target,
        		        					DamageType.ENERGY,
        		        					SUB_ARC_DAMAGE,
        		        					SUB_ARC_EMP_DAMAGE,
        		        					1000f,
        		        					"tachyon_lance_emp_impact",
        		        					12f,
        		        					ARC_COLOR_O,
        		        					ARC_COLOR_I);
    		        			}
    	        			}
    	        			
		        			ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) target);
		        			emitter.location(arcEnd);
		        			emitter.life(0.5f, 0.5f);
		        			emitter.size(3f, 8f);
		        			emitter.velocity(1f, 14f);
		        			emitter.distance(2f, 138f);
		        			emitter.color(35,100,155,255);
		        	        emitter.velDistLinkage(false);
		        	        emitter.burst((int) (15f * subArcCount));
            			}
	        		}
				}
				
			}
		} else if (state == State.OUT && foundTarget != null) {
			Global.getCombatEngine().getCustomData().remove(targetKey);
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = getMaxRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true);
				} else {
					Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
					if (test instanceof ShipAPI) {
						target = (ShipAPI) test;
						float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
						float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
						if (dist > range + radSum) target = null;
						// if (!target.isAlive() || target.isPiece()) target = null; //added this check, because the arcs go wacky when zapping a wreck, but it caused a crash, so disabled.
					}
				}
			}
			if (target == null) {
				target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true);
			}
		}
		if (target == null) target = ship;
		
		return target;
	}
	
	public static float getMaxRange(ShipAPI ship) {
		
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(BASE_RANGE);
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null || target == ship) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		ShipAPI target = findTarget(ship);
		
		if (target != null && target != ship) {
			if (target.isPhased()) {
				return false;
			} else {
				return true;
			}
		} else return false;
		
		// return target != null && target != ship;
	}
	
}
