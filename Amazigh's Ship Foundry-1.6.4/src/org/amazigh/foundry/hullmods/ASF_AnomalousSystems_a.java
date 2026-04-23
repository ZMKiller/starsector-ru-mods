package org.amazigh.foundry.hullmods;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

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

import org.magiclib.util.MagicLensFlare;

public class ASF_AnomalousSystems_a extends BaseHullMod {

	public static final float WEP_HEALTH_BONUS = 200f;
	public static final float WEP_REPAIR_RATE = 75f;
	
	public static final float RATE_BONUS = 1.6f;
	public static final float HEALTH_MULT = 2f;
	
	public static final float RoF_BONUS_E = 1.4f;
	public static final float FLUX_MULT = 0.5f;
	
	public static final float RoF_BONUS_B = 1.6f;
	public static final float VEL_BONUS_B = 1.4f;
	public static final float RECOIL_BONUS = 50f;
	
	public static final float PD_MULT = 1.5f;
	
	public static final float VENT_BONUS = 100f;
	
	private IntervalUtil clownMode = new IntervalUtil(0.2f,0.4f); // Clowns shall suffer, as they should
		// if you don't get why you shouldn't have this ship, then all i can say is smh.
	private static final Color COLOR_P = new Color(190,255,125,240);
	private static final Color COLOR_X = new Color(200,235,150,255);
	private static final Color COLOR_X_2 = new Color(220,230,140,200);
	private static final Color COLOR_D_C = new Color(155,155,155,100);
	private static final Color COLOR_D_F = new Color(170,200,80,70);
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileRoFMult().modifyMult(id, RATE_BONUS);
		stats.getMissileHealthBonus().modifyMult(id, HEALTH_MULT);
		
		stats.getEnergyRoFMult().modifyMult(id, RoF_BONUS_E);
		
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, FLUX_MULT);
		
		stats.getBallisticRoFMult().modifyMult(id, RoF_BONUS_B);
		stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getBallisticProjectileSpeedMult().modifyMult(id, VEL_BONUS_B);
		
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
		
		ship.setJitter(this, new Color(140,215,50,15), 1f, 3, 0, 12f);
		ship.setJitterUnder(this, new Color(190,255,125,25), 1f, 20, 0f, 32f);
		ship.getEngineController().fadeToOtherColor(this, new Color(160,255,75,225), new Color(140,215,50,20), 1f, 0.8f);
		
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
        		
        		EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(ship,
						ship.getLocation(),
	        			ship,
	        			ship,
	        			DamageType.ENERGY,
	        			600f,
	        			0f,
	        			1000f,
	        			"",
						15f,
						new Color(0,0,0,0),
						new Color(0,0,0,0));
				
				Vector2f arcEnd = arc.getTargetLocation();
        		
    			DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
    	                160f,
    	                90f,
    	                600f,
    	                600f * 0.6f,
    	                CollisionClass.PROJECTILE_FF,
    	                CollisionClass.PROJECTILE_FIGHTER,
    	                4f,
    	                4f,
    	                0.6f,
    	                40,
    	                COLOR_P,
    	                COLOR_X);
    	        blast.setDamageType(DamageType.ENERGY);
    	        blast.setShowGraphic(true);
    	        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
    	        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
    	        blast.setUseDetailedExplosion(true);
    	        blast.setDetailedExplosionRadius(140f);
    	        blast.setDetailedExplosionFlashRadius(190f);
    	        blast.setDetailedExplosionFlashDuration(0.4f);
    	        
		        engine.spawnDamagingExplosion(blast, ship, arcEnd, false);
		        
		        engine.addNebulaParticle(arcEnd,
						ship.getVelocity(),
						MathUtils.getRandomNumberInRange(100f, 120f),
						MathUtils.getRandomNumberInRange(1.5f, 2.0f),
						0.8f,
						0.4f,
						MathUtils.getRandomNumberInRange(0.8f, 1.1f),
						COLOR_X_2,
						false);
		        
				for (int j=0; j < 2; j++) {
			        MagicLensFlare.createSharpFlare(
						    engine,
						    ship,
						    MathUtils.getRandomPointInCircle(arcEnd, 40f),
						    5,
						    290,
						    MathUtils.getRandomNumberInRange(0f, 180f),
						    new Color(90,105,50),
							new Color(190,200,160));
				}
				
				Global.getSoundPlayer().playSound("explosion_from_damage", 1.3f, 0.6f, arcEnd, ship.getVelocity());
		        Global.getSoundPlayer().playSound("hit_heavy_energy", 1.25f, 0.6f, arcEnd, ship.getVelocity());
        	}
        }
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

}
