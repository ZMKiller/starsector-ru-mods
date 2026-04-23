package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ASF_AdaptiveBurstStats extends BaseShipSystemScript {

	// This is less cursed than the lustres hullmod, and also less super secret, feel free to look at this mess.

private boolean FLUX_CHECK = true;
private float shotDelay = 0f;
private float strength = 0.2f;
private float burstSize = 1f;
private float burstTime = 0.2f;

public static final float B_ROF_BONUS = 1f;
public static final float B_FLUX_REDUCTION = 50f;

public static final float E_DAMAGE_BONUS_PERCENT = 50f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
	  
	  ShipAPI ship = (ShipAPI)stats.getEntity();
	  
	  CombatEngineAPI engine = Global.getCombatEngine();
	  float timer = engine.getElapsedInLastFrame();
	  
	  // strength setup
	  if (FLUX_CHECK) {
		  if (ship.getFluxLevel() > 0.8f) {
			  strength = 1f;
			  burstSize = 9f;
			  burstTime = 0.2f;
		  } else {
			  strength = ship.getFluxLevel() + 0.2f;
			  burstSize = (ship.getFluxLevel() * 10f) + 1f;
			  burstTime = 0.5f - (ship.getFluxLevel() * 0.375f);
		  }
		  FLUX_CHECK = false;
      }
	  // strength setup
	  
	  // Weapon buffs		
	  float SCALED_B_ROF = 1f + (B_ROF_BONUS * effectLevel * strength);
	  float SCALED_B_FLUX = B_FLUX_REDUCTION * effectLevel * strength;
	  float SCALED_E_DAMAGE = E_DAMAGE_BONUS_PERCENT * effectLevel * strength;
	  
	  stats.getBallisticRoFMult().modifyMult(id, SCALED_B_ROF);
	  stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (SCALED_B_FLUX * 0.01f));
	  stats.getEnergyWeaponDamageMult().modifyPercent(id, SCALED_E_DAMAGE);
	  // Weapon buffs
	  	  
	  // projectile fire section
	  if (effectLevel >= 1f) {
		  shotDelay -= timer;
		  while (shotDelay <= 0f && burstSize > 0) {
			  shotDelay += burstTime;
			  burstSize -= 1f;
			  
			  for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
				  if (weapon.isSystemSlot()) {
					  float randomArc = MathUtils.getRandomNumberInRange(-20f, 20f);
					  DamagingProjectileAPI bolt = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship,
							  null,
							  "A_S-F_formia_sys",
							  weapon.computePosition(ship),
							  weapon.getAngle() + ship.getFacing() + randomArc,
							  ship.getVelocity());
					  
					  // engine.spawnEmpArcVisual(weapon.computePosition(ship), ship, MathUtils.getRandomPointOnCircumference(weapon.computePosition(ship), 3f), bolt, 7f,
					  //	  new Color(70,110,200,80),
					  //	  new Color(200,225,255,100));
					    
			    		// scripted muzzle vfx
			    		for (int i=0; i < 8; i++) {
			    			// core "muzzle flash"
			                float angle1 = weapon.getAngle() + ship.getFacing() + MathUtils.getRandomNumberInRange(-5f, 5f);
			                Vector2f flashVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 15f), angle1);
			                
			                Vector2f point1 = MathUtils.getPointOnCircumference(weapon.computePosition(ship), MathUtils.getRandomNumberInRange(2f, 18f), angle1);
			                
			                Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point1, 3f),
			                		flashVel,
			                		MathUtils.getRandomNumberInRange(7f, 11f), //size
			                		1.0f, //brightness
			                		MathUtils.getRandomNumberInRange(0.15f, 0.25f), //duration
			                		new Color(75,150,240,225));
			                
			                for (int j=0; j < 2; j++) {
			                	// sparkly "particle flash"
			                    Vector2f sparkleVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(3f, 12f), weapon.getAngle() + ship.getFacing());
			                    
			                    Vector2f point2 = MathUtils.getPointOnCircumference(weapon.computePosition(ship), MathUtils.getRandomNumberInRange(10f, 34f), weapon.getAngle() + ship.getFacing());
			                    
			                    Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point2, 13f),
			                    		sparkleVel,
			                    		MathUtils.getRandomNumberInRange(2f, 5f), //size
			                    		1.0f, //brightness
			                    		MathUtils.getRandomNumberInRange(0.55f, 1.05f), //duration
			                    		new Color(100,200,255,255));
			                }
			            }
			            
				  }
			  }
			  
			  Global.getSoundPlayer().playSound("amsrm_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
			  Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.5f, 0.5f, ship.getLocation(), ship.getVelocity());
		  }  
	  }
	  // projectile fire section
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponDamageMult().unmodify(id);
		
		FLUX_CHECK = true;
		shotDelay = 0f;
		strength = 0.2f;
		burstSize = 1f;
		burstTime = 0.2f;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		
		float displayBonus = (int) (strength * 100f);
		
		if (index == 0) {
			return new StatusData("Increased Weapon Performance", false);
		}
		
		if (index == 1) {
			return new StatusData("Performance Increase Strength " + (int) displayBonus + "%", false);
		}
		return null;
	}
}


