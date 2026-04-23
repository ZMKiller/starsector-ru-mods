package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import java.util.List;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.amazigh.foundry.scripts.ASF_PainterBeamEffect.ASF_artyTargetListener;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_designateStats extends BaseShipSystemScript {
	
	protected static float BASE_RANGE = 1510f; // 10 range longer than "advertised" to make it more reliable to use or smth idk
	
	public static final Color ARC_COLOR_O = new Color(64,70,216,255);
	public static final Color ARC_COLOR_I = new Color(249,243,253,255);
	
	public static final Color GLOW_COLOR = new Color(152,125,228,169);
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String targetKey = ship.getId() + "_ASF_designator_target";
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
				
				for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	        		if (weapon.isSystemSlot()) {
	        			Vector2f arcStart = weapon.computePosition(ship);

    					float dam = 20f;
	        			float emp = 200f;
	        			
	        			// "muzzle flash"
	        			engine.addHitParticle(arcStart, ship.getVelocity(), 69f, 1f, 0.1f, GLOW_COLOR);
//	        			for (int i=0; i < 15; i++) {
//	        				
//	        				float angle = i * 24;
//	        				
//	        				Vector2f flashVel = MathUtils.getPointOnCircumference(target.getVelocity(), MathUtils.getRandomNumberInRange(3f, 12f), angle);
//	        				Vector2f sparkPoint = MathUtils.getPointOnCircumference(arcStart, MathUtils.getRandomNumberInRange(1f, 13f), angle);
//	        				
//	        				Global.getCombatEngine().addSmoothParticle(sparkPoint,
//	        						flashVel,
//	        						MathUtils.getRandomNumberInRange(23f, 38f), //size
//	        						1.0f, //brightness
//	        						MathUtils.getRandomNumberInRange(0.29f, 0.37f), //duration
//	        						GLOW_COLOR);
//	        			}
	        			
	        			ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
	        			emitter.location(arcStart);
	        			emitter.life(0.29f, 0.37f);
	        			emitter.size(23f, 38f);
	        			emitter.velocity(3f, 9f);
	        			emitter.distance(1f, 12f);
	        			emitter.color(152,125,228,169); // GLOW_COLOR
	        			emitter.lifeLinkage(false);
	        			emitter.burst(15);
	        			
	        			// the arc
	        			EmpArcParams params = new EmpArcParams();
	        	        params.segmentLengthMult = 3f; //8
	        	        params.zigZagReductionFactor = 0.8f; //0.15
	        	        params.fadeOutDist = 100f; //50
	        	        params.minFadeOutMult = 10f;
	        	        params.flickerRateMult = 0.3f;
	        	        
	        	        float arcSpeed = 3333f;
						float dist = MathUtils.getDistance(ship, target);
						params.movementDurOverride = Math.max(0.05f, dist / arcSpeed );
						
            			EmpArcEntityAPI arc = engine.spawnEmpArc(ship, arcStart, ship, target,
        						DamageType.ENERGY,
        						dam, // damage
        						emp, // emp
        						3000f, // max range
        						"A_S-F_quiet_emp_impact",
        						35f, // thickness
        						ARC_COLOR_O,
        						ARC_COLOR_I,
	        	                params);
            			
            			arc.setTargetToShipCenter(arcStart, target);
            			
	        	        arc.setCoreWidthOverride(20f);
	        	        arc.setRenderGlowAtStart(false);
	        	        arc.setFadedOutAtStart(true);
	        	        arc.setSingleFlickerMode(true);
	        	        
            			Global.getSoundPlayer().playSound("system_emp_emitter_impact", 0.75f, 0.5f, arcStart, ship.getVelocity());
            			
            			Vector2f arcEnd = arc.getTargetLocation();
            			
            			
            			// doing the charge stuff
            			if (!target.hasListenerOfClass(ASF_artyTargetListener.class)) {
            				target.addListener(new ASF_artyTargetListener(target));
        				}
        				
        				List<ASF_artyTargetListener> listeners = target.getListeners(ASF_artyTargetListener.class);
        				if (listeners.isEmpty()) return; // ??? (idk either alex, but sanity checks are a real one)
        				
        				ASF_artyTargetListener listener = listeners.get(0);
        				
        				for (int ij=0; ij < 8; ij++) {
            				if (listener.recentHits.getItems().size() >= 8) {
            					engine.spawnEmpArc(
            							ship, arcEnd, target, target,
            							DamageType.ENERGY, 
            							dam, // damage
            							emp, // emp 
            							1000f, // max range 
            							"tachyon_lance_emp_impact",
            							20f,
            							ARC_COLOR_O,
            							ARC_COLOR_I);
            					
            				} else {
            					String uniqueHash = (String.valueOf(target.hashCode()) + String.valueOf((int) engine.getTotalElapsedTime(true)) + ij);
            					
            					// generating a unique identifying int for each damage instance caused by the arc
            					listener.notifyHit(uniqueHash);
            				}
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
		
	}
	
}
