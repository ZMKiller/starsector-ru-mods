package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_AnomalousSystems_c extends BaseHullMod {

	public static final float WEP_HEALTH_BONUS = 200f;
	public static final float WEP_REPAIR_RATE = 75f;
	
	public static final float RATE_BONUS_M = 2f;
	public static final float HEALTH_MULT = 3f;
	public static final float VEL_MULT = 1.5f;
	
	public static final float RANGE_BONUS_E = 450f;
	public static final float RoF_BONUS_E = 1.25f;
	public static final float SHIELD_BONUS = 1.2f;
	
	public static final float FLUX_MULT = 0.6f;
	
	public static final float PD_MULT = 1.5f;
	
	public static final float VENT_BONUS = 100f;
	
	private IntervalUtil clownMode = new IntervalUtil(0.2f,0.4f); // Clowns shall suffer, as they should
	// if you don't get why you shouldn't have this ship, then all i can say is smh.
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - (WEP_REPAIR_RATE * 0.01f));
		stats.getCombatEngineRepairTimeMult().modifyMult(id, 1f - (WEP_REPAIR_RATE * 0.01f));
		
		stats.getMissileAmmoRegenMult().modifyMult(id, RATE_BONUS_M);
		stats.getMissileRoFMult().modifyMult(id, RATE_BONUS_M);
		stats.getMissileHealthBonus().modifyMult(id, HEALTH_MULT);
		stats.getMissileAccelerationBonus().modifyMult(id, VEL_MULT);
		stats.getMissileMaxSpeedBonus().modifyMult(id, VEL_MULT);
		stats.getMissileTurnAccelerationBonus().modifyMult(id, VEL_MULT);
		stats.getMissileMaxTurnRateBonus().modifyMult(id, VEL_MULT);

		stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS_E);
		stats.getEnergyRoFMult().modifyMult(id, RoF_BONUS_E);
		stats.getDamageToTargetShieldsMult().modifyMult(id, SHIELD_BONUS);

		stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		stats.getMissileWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		
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
		
		ship.setJitter(this, new Color(240,100,70,10), 1f, 3, 0, 10f);
		ship.setJitterUnder(this, new Color(255,120,100,30), 1f, 25, 0f, 32f);
		ship.getEngineController().fadeToOtherColor(this, new Color(255,101,77,225), new Color(215,110,70,20), 1f, 0.8f);
		
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
        		
        		Vector2f fxVel = new Vector2f();
        		fxVel.set(ship.getVelocity());
        		
        		EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship,
						ship.getLocation(),
	        			ship,
	        			ship,
	        			DamageType.HIGH_EXPLOSIVE,
	        			400f,
	        			0f,
	        			1000f,
	        			"",
						15f,
						new Color(0,0,0,0),
						new Color(0,0,0,0));
				
				Vector2f arcEnd = arc.getTargetLocation();
        		
				
    	        engine.addNebulaParticle(arcEnd, fxVel,
    	        		MathUtils.getRandomNumberInRange(30f, 40f),
    	        		1.9f, //endsizemult
    	        		0.2f, //rampUpFraction
    	        		0.3f, //fullBrightnessFraction
    	        		1.1f, //totalDuration
    	        		new Color(60,40,30,90),
    	        		true);
    	        
    	    	for (int i=0; i < 5; i++) {
    				float angle1 = (i * 72f) + MathUtils.getRandomNumberInRange(-6f, 6f);
    				
    				Vector2f smokePos1 = MathUtils.getPointOnCircumference(arcEnd, MathUtils.getRandomNumberInRange(17f, 28f), angle1);
    				Vector2f smokePos2 = MathUtils.getPointOnCircumference(arcEnd, MathUtils.getRandomNumberInRange(19f, 35f), angle1);
    				
    	            Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(9f, 31f), angle1);
    	            Vector2f smokeVel2 = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(17f, 35f), angle1);
    	            
    	            engine.addSwirlyNebulaParticle(smokePos1, smokeVel,
    	            		MathUtils.getRandomNumberInRange(23f, 32f),
    	            		1.9f, //endsizemult
    	            		0.1f, //rampUpFraction
    	            		0.3f, //fullBrightnessFraction
    	            		MathUtils.getRandomNumberInRange(0.9f, 1.2f), //totalDuration
    	            		new Color(45,40,50,110),
    	            		true);
    	            
    	            engine.addSwirlyNebulaParticle(smokePos2, smokeVel2,
    	            		MathUtils.getRandomNumberInRange(21f, 28f),
    	            		1.7f, //endsizemult
    	            		0.2f, //rampUpFraction
    	            		0.35f, //fullBrightnessFraction
    	            		MathUtils.getRandomNumberInRange(0.4f, 0.6f), //totalDuration
    	            		new Color(153,60,35,90),
    	            		true);
    	            
    	            for (int j=0; j < 6; j++) {
    	            	
    	            	float arcPoint = MathUtils.getRandomNumberInRange(0f, 360f);
    	            	
    	            	float sparkRange = MathUtils.getRandomNumberInRange((j - 1f) * 12f, j * 12f);
    	            	
    	            	Vector2f spawnLocation = MathUtils.getPointOnCircumference(arcEnd, sparkRange, arcPoint);
    	            	
    	                Vector2f sparkVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(5f, 20f), arcPoint);
    	                
    	                engine.addSmoothParticle(spawnLocation,
    	                		sparkVel,
    	        				MathUtils.getRandomNumberInRange(3f, 8f), //size
    	        				1f, //brightness
    	        				MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
    	        				new Color(255,76,58,255));
    	                
    	            }
    	    	}
    	    	
    	    	for (int i=0; i < 2; i++) {
    	    		
    	    		Vector2f flarePoint = MathUtils.getPointOnCircumference(arcEnd, 15f + (i * 11f), MathUtils.getRandomNumberInRange(0f, 360f));
    	    		
    	        	MagicLensFlare.createSharpFlare(
    	    			    engine,
    	    			    ship,
    	    			    flarePoint,
    	    			    4,
    	    			    160 - (i * 10f),
    	    			    MathUtils.getRandomNumberInRange(0f, 360f),
    	    			    new Color(190,40,30),
    	    				new Color(255,101,77));
    	    	}
    	        
    			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
    	                110f,
    	                70f,
    	                300f,
    	                180f,
    	                CollisionClass.PROJECTILE_FF,
    	                CollisionClass.PROJECTILE_FIGHTER,
    	                3f,
    	                4f,
    	                0.6f,
    	                25,
    	                new Color(255,76,58,255),
    	                new Color(153,60,35,90));
    	        blast.setDamageType(DamageType.ENERGY);
    	        blast.setShowGraphic(true);
    	        blast.setDetailedExplosionFlashColorCore(new Color(120,140,160,95));
    	        blast.setDetailedExplosionFlashColorFringe(new Color(180,150,70,70));
    	        blast.setUseDetailedExplosion(true);
    	        blast.setDetailedExplosionRadius(90f);
    	        blast.setDetailedExplosionFlashRadius(110f);
    	        blast.setDetailedExplosionFlashDuration(0.25f);
    	        
		        engine.spawnDamagingExplosion(blast, ship, arcEnd, false);
		        
				Global.getSoundPlayer().playSound("explosion_from_damage", 0.9f, 0.6f, arcEnd, ship.getVelocity());
		        Global.getSoundPlayer().playSound("hit_hull_heavy", 0.8f, 0.75f, arcEnd, ship.getVelocity());
        	}
        }
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
