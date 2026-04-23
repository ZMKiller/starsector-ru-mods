package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_rangdaSys extends BaseShipSystemScript {
	
	public static final float TIME_MULT = 0.1f; // add one to get the actual timescale multiplier

	public static final Color JITTER_COLOR = new Color(60,210,150,30);
	public static final Color JITTER_UNDER_COLOR = new Color(60,210,150,85);
	
	public static final float ROF_BONUS = 2f;
	public static final float FLUX_REDUCTION = 70f;
	
	public static final float VEL_BONUS = 1.08f;
	
	public static final float MISSILE_REGEN_MALUS = 0.25f;
	
	public static final float EXIT_ROF_MALUS = 0.2f;
	
	private boolean EXIT = false;
	private boolean REPAIR = false;
	
	private IntervalUtil interval1 = new IntervalUtil(0.05f,0.1f); // spark/steam #1
	private IntervalUtil interval2 = new IntervalUtil(0.05f,0.1f); // spark/steam #2
		// done in two with time variance so that there is reduced "clumping" of particles
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		CombatEngineAPI engine = Global.getCombatEngine();
		ShipAPI ship = (ShipAPI)stats.getEntity();
		boolean player = false;
		player = ship == Global.getCombatEngine().getPlayerShip();
		
		float shipTimeMult = 1f + (TIME_MULT * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		// so you get a 10% timescale bonus while system is active, hidden but it's a nice little bonus thing
		
		float amount = engine.getElapsedInLastFrame() * (0.30f + (effectLevel * 0.7f));
		// amount scales with effectLevel, with 30% as the minimum
		
		if (state == ShipSystemStatsScript.State.OUT) {
			if (!EXIT) {
				EXIT = true;
				Global.getSoundPlayer().playSound("vent_flux", 1.5f, 0.75f, ship.getLocation(), ship.getVelocity());
				// play a "vent" sound when ending system use 
			}
			
			float multO = 1f - (EXIT_ROF_MALUS * effectLevel);
			stats.getBallisticRoFMult().modifyMult(id, multO);
				// Smallish RoF penalty when exiting system. Mostly for flavour, it's unlikely you will exit system when in combat after all.
			
			stats.getBallisticAmmoRegenMult().unmodify(id);
			stats.getBallisticWeaponFluxCostMod().unmodify(id);
			stats.getMissileAmmoRegenMult().unmodify(id);
			
			if (!ship.getFluxTracker().isOverloaded()) {
				if (DefenseUtils.hasArmorDamage(ship)) {
					
		        	ArmorGridAPI armorGrid = ship.getArmorGrid();
			        final float[][] grid = armorGrid.getGrid();
			        final float max = armorGrid.getMaxArmorInCell();
			        
			        float baseCell = armorGrid.getMaxArmorInCell() * Math.min(ship.getHullSpec().getArmorRating(), 750f) / armorGrid.getArmorRating();
			        float repairAmount = baseCell * 0.08f * amount;
			        	// 3*8*((0.65*0.7)+(0.3*0.3)) = ~13.08% armour repaired in the 3s OUT time.  Enough to be worth it, but not so much to be overpowering
			        
					for (int x = 0; x < grid.length; x++) {
			            for (int y = 0; y < grid[0].length; y++) {
			                if (grid[x][y] < max) {
			                    float regen = grid[x][y] + repairAmount;
			                    armorGrid.setArmorValue(x, y, regen);
			                }
			            }
			        }
					
					float jitterLevel = ((float) Math.sqrt(effectLevel)) * 0.8f;
					float jitterRangeBonus = effectLevel * 10f;
					
					ship.setJitter(this, JITTER_COLOR, jitterLevel, 2, 0, 0 + jitterRangeBonus);
					ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 10, 0f, 8f + jitterRangeBonus);
					
			        ship.syncWithArmorGridState();
			        ship.syncWeaponDecalsWithArmorDamage();
			        
		        	REPAIR = true;
		        } else {
		        	REPAIR = false;
		        }
			}
				// Some minor armour repair when disabling system, to give you some more reason (other than flux) to disable it and not have it always on.
			
		} else {
			float mult = 1f + (ROF_BONUS * effectLevel);
			stats.getBallisticRoFMult().modifyMult(id, mult);
			stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
			stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
			
			stats.getBallisticProjectileSpeedMult().modifyMult(id, VEL_BONUS);
			// small hidden velocity bonus, to make weapons more easily aimable
			
			stats.getMissileAmmoRegenMult().modifyMult(id, MISSILE_REGEN_MALUS);
			// only get the weapon buffs when IN/ACTIVE
		}
		
		interval1.advance(amount);
		interval2.advance(amount);
        if (interval1.intervalElapsed()) {
        	
        	float intensity = 0.7f + (ship.getFluxLevel() * 0.3f);
        	
        	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
        		if (weapon.isSystemSlot()) {
        			Vector2f posZero = weapon.computePosition(ship);
        			if (EXIT) {
        				float NebAlpha = Math.max(0.25f, effectLevel);
        				Vector2f nebVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 40f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-15f, 15f));
        				float randomSize2 = MathUtils.getRandomNumberInRange(20f, 32f);
        				Color steamColor = new Color(100f/255f,110f/255f,100f/255f,NebAlpha);
        				if (REPAIR) {
        					steamColor = new Color(100f/255f,150f/255f,120f/255f,NebAlpha);
        				}
        				engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), nebVel, randomSize2, 1.8f, 0.6f, 0.7f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), steamColor);
        		        // spawns a "purge" "steam vent" when in the out state
        				// which is greenish if repairing
        			} else {
        				float glowAlpha = effectLevel * 0.5f;
        				engine.addSmoothParticle(posZero, ship.getVelocity(), MathUtils.getRandomNumberInRange(25f, 35f), intensity, 0.1f, new Color(1f,120f/255f,80f/255f,glowAlpha));
        				
        				Vector2f fastParticleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(80f, 256f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-17f, 17f));
        	            float randomSize01 = MathUtils.getRandomNumberInRange(3f, 5f);
        	            engine.addSmoothParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), fastParticleVel, randomSize01, intensity, MathUtils.getRandomNumberInRange(0.2f, 0.25f), new Color(1f,120f/255f,80f/255f,effectLevel));
        				
            			for (int i=0; i < 2; i++) {
            				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(35f, 128f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-20f, 20f));
            	            float randomSize1 = MathUtils.getRandomNumberInRange(3f, 5f);
            	            engine.addSmoothParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), particleVel, randomSize1, intensity, MathUtils.getRandomNumberInRange(0.35f, 0.5f), new Color(1f,120f/255f,80f/255f,effectLevel));
            	            // spawns "spark" particles when active
            			}
        			}
        		}
        	}
        }
        if (interval2.intervalElapsed()) {
        	
        	float intensity = 0.7f + (ship.getFluxLevel() * 0.3f);
        	
        	for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
        		if (weapon.isSystemSlot()) {
        			Vector2f posZero = weapon.computePosition(ship);
        			if (EXIT) {
        				float NebAlpha = Math.max(0.25f, effectLevel);
        				Vector2f nebVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(10f, 40f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-15f, 15f));
        				float randomSize2 = MathUtils.getRandomNumberInRange(20f, 32f);
        				Color steamColor = new Color(100f/255f,110f/255f,100f/255f,NebAlpha);
        				if (REPAIR) {
        					steamColor = new Color(100f/255f,150f/255f,120f/255f,NebAlpha);
        				}
        				engine.addNebulaParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), nebVel, randomSize2, 1.8f, 0.6f, 0.7f, MathUtils.getRandomNumberInRange(0.3f, 0.6f), steamColor);
        		        // spawns a "purge" "steam vent" when in the out state
        				// which is greenish if repairing
        			} else {
        				float glowAlpha = effectLevel * 0.5f;
        				engine.addSmoothParticle(posZero, ship.getVelocity(), MathUtils.getRandomNumberInRange(25f, 35f), intensity, 0.1f, new Color(1f,120f/255f,80f/255f,glowAlpha));
        				
        				Vector2f fastParticleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(80f, 256f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-17f, 17f));
        	            float randomSize01 = MathUtils.getRandomNumberInRange(3f, 5f);
        	            engine.addSmoothParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), fastParticleVel, randomSize01, intensity, MathUtils.getRandomNumberInRange(0.2f, 0.25f), new Color(1f,120f/255f,80f/255f,effectLevel));
        				
            			for (int i=0; i < 2; i++) {
            				Vector2f particleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(35f, 128f), weapon.computeMidArcAngle(ship) + MathUtils.getRandomNumberInRange(-20f, 20f));
            	            float randomSize1 = MathUtils.getRandomNumberInRange(3f, 5f);
            	            engine.addSmoothParticle(MathUtils.getRandomPointOnCircumference(posZero, 4f), particleVel, randomSize1, intensity, MathUtils.getRandomNumberInRange(0.35f, 0.5f), new Color(1f,120f/255f,80f/255f,effectLevel));
            	            // spawns "spark" particles when active
            			}
        			}
        		}
        	}
        }
	}	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		
		stats.getBallisticProjectileSpeedMult().unmodify(id);
		
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		
		EXIT = false;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		
		if (state != ShipSystemStatsScript.State.OUT) {
			float mult = 1f + (ROF_BONUS * effectLevel);
			float bonusPercent = (int) ((mult - 1f) * 100f);
			
			if (index == 0) {
				return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "% and flux usage -" + (int) FLUX_REDUCTION + "%", false);
			}
		} else {
			float multO = 1f - (EXIT_ROF_MALUS * effectLevel);
			float malusPercent = (int) ((multO - 1f) * 100f);
			
			if (index == 0) {
				return new StatusData("ballistic rate of fire " + (int) malusPercent + "%", true);
			}
			if (REPAIR) {
				if (index == 1) {
					return new StatusData("repairing armor", false);
				}
			}
		}
		
		return null;
	}
}