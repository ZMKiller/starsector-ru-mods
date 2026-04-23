package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_AnomalousSystems extends BaseHullMod {

	public static final float WEP_HEALTH_BONUS = 200f;
	public static final float WEP_REPAIR_RATE = 75f;
	
	public static final float RATE_BONUS = 1.5f;
	public static final float RoF_BONUS = 1.2f;
	public static final float HEALTH_MULT = 5f;	
	public static final float AMMO_BONUS = 100f;
	public static final float FLUX_MULT = 0.6f;
	public static final float VEL_MULT = 1.1f;

	public static final float PD_MULT = 1.25f;
	
	public static final float VENT_BONUS = 100f;
	
	private IntervalUtil clownMode = new IntervalUtil(0.2f,0.4f); // Clowns shall suffer, as they should
	// if you don't get why you shouldn't have this ship, then all i can say is smh.
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoRegenMult().modifyMult(id, RATE_BONUS);
		stats.getMissileHealthBonus().modifyMult(id, HEALTH_MULT);
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
		
		stats.getMissileAccelerationBonus().modifyMult(id, VEL_MULT);
		stats.getMissileMaxSpeedBonus().modifyMult(id, VEL_MULT);
		stats.getMissileTurnAccelerationBonus().modifyMult(id, VEL_MULT);
		stats.getMissileMaxTurnRateBonus().modifyMult(id, VEL_MULT);
		
		stats.getEnergyAmmoRegenMult().modifyMult(id, RATE_BONUS);
		stats.getEnergyAmmoBonus().modifyPercent(id, AMMO_BONUS);
		
		stats.getBallisticRoFMult().modifyMult(id, RoF_BONUS);
		stats.getEnergyRoFMult().modifyMult(id, RoF_BONUS);
		stats.getMissileRoFMult().modifyMult(id, RoF_BONUS);
		
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		stats.getMissileWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		
		stats.getWeaponHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - (WEP_REPAIR_RATE * 0.01f));
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - (WEP_REPAIR_RATE * 0.01f));
		
		stats.getDamageToFighters().modifyMult(id, PD_MULT);
		stats.getDamageToMissiles().modifyMult(id, PD_MULT);
		
		stats.getVentRateMult().modifyPercent(id, VENT_BONUS);
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		if ( !ship.isAlive() || ship.isPiece() ) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();
		
		ship.setJitter(this, new Color(50,100,200,10), 1f, 3, 0, 10f);
		ship.setJitterUnder(this, new Color(100,150,255,30), 1f, 25, 0f, 32f);
		
		
		if (Global.getSector().getPlayerFleet() == null) {
            return;
        }
		
        boolean isPlayerFleet = false;
        for (FleetMemberAPI member: Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.getVariant().getHullVariantId().equals(ship.getVariant().getHullVariantId())) {
                isPlayerFleet = true;
            }
        }
        
        if (isPlayerFleet) {
        	
        	clownMode.advance(amount);
        	
        	if (clownMode.intervalElapsed()) {
				
        		Vector2f arcSource = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
				
        		EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship,
						arcSource,
	        			ship,
	        			ship,
	        			DamageType.ENERGY,
	        			600f,
	        			400f,
	        			1000f,
	        			"tachyon_lance_emp_impact",
						15f,
						new Color(50,100,255,255),
						new Color(0,0,0,255));
				
				Vector2f arcEnd = arc.getTargetLocation();
				
				for (int j1=0; j1 < 2; j1++) {
					
					Vector2f offsetVel0 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(0f, 30f));
		            Vector2f offsetVel01 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(0f, 30f));
					
					engine.addNebulaParticle(arcSource,
							offsetVel0,
							MathUtils.getRandomNumberInRange(25f, 30f),
							MathUtils.getRandomNumberInRange(1.6f, 1.9f),
							0.4f,
							0.3f,
							MathUtils.getRandomNumberInRange(0.4f, 0.6f),
							new Color(40,140,250,225)); 
					engine.addSwirlyNebulaParticle(arcSource,
							offsetVel01,
							MathUtils.getRandomNumberInRange(25f, 30f),
							MathUtils.getRandomNumberInRange(1.6f, 1.9f),
							0.4f,
							0.3f,
							MathUtils.getRandomNumberInRange(0.4f, 0.6f),
							new Color(40,140,250,225), false);
					
					Vector2f offsetVel02 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(0f, 30f));
		            Vector2f offsetVel03 = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(0f, 30f));
					
					engine.addNebulaParticle(arcEnd,
							offsetVel02,
							MathUtils.getRandomNumberInRange(25f, 30f),
							MathUtils.getRandomNumberInRange(1.6f, 1.9f),
							0.4f,
							0.3f,
							MathUtils.getRandomNumberInRange(0.4f, 0.6f),
							new Color(40,140,250,225)); 
					engine.addSwirlyNebulaParticle(arcEnd,
							offsetVel03,
							MathUtils.getRandomNumberInRange(25f, 30f),
							MathUtils.getRandomNumberInRange(1.6f, 1.9f),
							0.4f,
							0.3f,
							MathUtils.getRandomNumberInRange(0.4f, 0.6f),
							new Color(40,140,250,225), false);
				}
        	}
        }
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
