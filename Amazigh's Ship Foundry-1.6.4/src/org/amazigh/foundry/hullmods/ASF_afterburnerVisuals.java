package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_afterburnerVisuals extends BaseHullMod {
	
	private IntervalUtil interval1 = new IntervalUtil(0.05f,0.12f); // spark/smoke #1
	private IntervalUtil interval2 = new IntervalUtil(0.05f,0.12f); // spark/smoke #2
		// done in two with time variance so that there is reduced "clumping" of particles

	public static final float DAMAGE_REDUCTION_SCALE = 0.3f; // strength scalar for the damper effect
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		// doing the vfx in a hullmod as usual because of the classic vanilla AI usage meme
        
        if (ship.getSystem().isActive()) {
        	
        	float resist = ship.getSystem().getEffectLevel() * DAMAGE_REDUCTION_SCALE;
    			
        	ship.getMutableStats().getHullDamageTakenMult().modifyMult(spec.getId(), 1f - (resist));
        	ship.getMutableStats().getArmorDamageTakenMult().modifyMult(spec.getId(), 1f - (resist));
        	ship.getMutableStats().getEmpDamageTakenMult().modifyMult(spec.getId(), 1f - (resist));
    			// secret hidden damage resist while system is active
    		
			interval1.advance(amount);
			interval2.advance(amount);
			
            if (ship.getSystem().isChargedown()) {
            	
                if (interval1.intervalElapsed()) {
                	
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.SMALL)) {
                			Vector2f jetPos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 4f);
                			Vector2f jetVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(20f, 80f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-8f, 8f));
                			
                			engine.addNebulaParticle(jetPos,
                					jetVel,
                					MathUtils.getRandomNumberInRange(8f, 12f),
                					1.8f,
                					0.6f,
                					0.7f,
                					MathUtils.getRandomNumberInRange(0.15f, 0.3f),
                					new Color(120,110,95,255));
                			
                			for (int i=0; i < 2; i++) {
                				Vector2f particlePos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 4f);
                				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(75f, 240f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-12f, 12f));
                	            engine.addSmoothParticle(particlePos,
                	            		particleVel,
                	            		MathUtils.getRandomNumberInRange(3f, 5f),
                	            		1f,
                	            		MathUtils.getRandomNumberInRange(0.15f, 0.25f),
                	            		new Color(255,135,80,255));
                			}
                		}
                	}
                }
                
                if (interval2.intervalElapsed()) {
                	
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.SMALL)) {
                			Vector2f jetPos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 4f);
                			Vector2f jetVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(20f, 80f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-8f, 8f));
                			
                			engine.addNebulaParticle(jetPos,
                					jetVel,
                					MathUtils.getRandomNumberInRange(8f, 12f),
                					1.8f,
                					0.6f,
                					0.7f,
                					MathUtils.getRandomNumberInRange(0.15f, 0.3f),
                					new Color(120,110,95,255));
                			
                			for (int i=0; i < 2; i++) {
                				Vector2f particlePos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 4f);
                				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(75f, 240f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-12f, 12f));
                	            engine.addSmoothParticle(particlePos,
                	            		particleVel,
                	            		MathUtils.getRandomNumberInRange(3f, 5f),
                	            		1f,
                	            		MathUtils.getRandomNumberInRange(0.15f, 0.25f),
                	            		new Color(255,135,80,255));
                			}
                		}
                	}
                }
            	
            } else {
            	
                if (interval1.intervalElapsed()) {
                	
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.MEDIUM)) {
                			
                			for (int i=0; i < 2; i++) {
                    			Vector2f jetPosInit = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 10f);
                    			Vector2f jetPosFinal = MathUtils.getPointOnCircumference(jetPosInit, MathUtils.getRandomNumberInRange(1f, 80f * ship.getSystem().getEffectLevel()), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-8f, 8f));
                    			
                    			Vector2f jetVelBase = new Vector2f();
                    			jetVelBase.x += ship.getVelocity().x;
                    			jetVelBase.y += ship.getVelocity().y;
                    			jetVelBase.x *= 0.7f;
                    			jetVelBase.y *= 0.7f;
                    			
                    			Vector2f jetVel = MathUtils.getPointOnCircumference(jetVelBase, MathUtils.getRandomNumberInRange(100f, 200f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-16f, 16f));
                    			
                    			engine.addNebulaParticle(jetPosFinal,
                    					jetVel,
                    					MathUtils.getRandomNumberInRange(18f, 30f),
                    					1.8f,
                    					0.6f,
                    					0.5f,
                    					MathUtils.getRandomNumberInRange(0.5f, 0.7f),
                    					new Color(145,110,95,220));
                    			
                    			for (int j=0; j < 2; j++) {
                    				Vector2f particlePos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 10f);
                    				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(90f, 360f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-20f, 20f));
                    	            engine.addSmoothParticle(particlePos,
                    	            		particleVel,
                    	            		MathUtils.getRandomNumberInRange(4f, 8f),
                    	            		1f,
                    	            		MathUtils.getRandomNumberInRange(0.45f, 0.65f),
                    	            		new Color(255,130,80,255));
                    			}
                			}
                		}
                	}
                }
                
                if (interval2.intervalElapsed()) {
                	
                	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                		if (weapon.isSystemSlot() && (weapon.getSlotSize() == WeaponSize.MEDIUM)) {
                			
                			for (int i=0; i < 2; i++) {
                    			Vector2f jetPosInit = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 10f);
                    			Vector2f jetPosFinal = MathUtils.getPointOnCircumference(jetPosInit, MathUtils.getRandomNumberInRange(1f, 80f * ship.getSystem().getEffectLevel()), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-8f, 8f));

                    			Vector2f jetVelBase = new Vector2f();
                    			jetVelBase.x += ship.getVelocity().x;
                    			jetVelBase.y += ship.getVelocity().y;
                    			jetVelBase.x *= 0.7f;
                    			jetVelBase.y *= 0.7f;
                    			
                    			Vector2f jetVel = MathUtils.getPointOnCircumference(jetVelBase, MathUtils.getRandomNumberInRange(100f, 200f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-16f, 16f));
                    			
                    			engine.addNebulaParticle(jetPosFinal,
                    					jetVel,
                    					MathUtils.getRandomNumberInRange(18f, 30f),
                    					1.8f,
                    					0.6f,
                    					0.5f,
                    					MathUtils.getRandomNumberInRange(0.5f, 0.7f),
                    					new Color(145,110,95,220));
                    			
                    			for (int j=0; j < 2; j++) {
                    				Vector2f particlePos = MathUtils.getRandomPointInCircle(weapon.computePosition(ship), 10f);
                    				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(90f, 360f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-20f, 20f));
                    	            engine.addSmoothParticle(particlePos,
                    	            		particleVel,
                    	            		MathUtils.getRandomNumberInRange(4f, 8f),
                    	            		1f,
                    	            		MathUtils.getRandomNumberInRange(0.45f, 0.65f),
                    	            		new Color(255,130,80,255));
                    			}
                			}
                		}
                	}
                }
            	
            }
        } else {
    		ship.getMutableStats().getHullDamageTakenMult().unmodify(spec.getId());
    		ship.getMutableStats().getArmorDamageTakenMult().unmodify(spec.getId());
    		ship.getMutableStats().getEmpDamageTakenMult().unmodify(spec.getId());
        }
        
	}
}
