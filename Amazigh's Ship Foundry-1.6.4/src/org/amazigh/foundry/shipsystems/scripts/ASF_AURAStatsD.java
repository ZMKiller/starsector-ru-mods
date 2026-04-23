package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ASF_AURAStatsD extends BaseShipSystemScript {

	public static final float DAMAGE_RESIST = 0.2f;
	
	public static final float TIME_MULT = 1.5f;
	public static final float FLUX_BONUS = 10f;
	public static final float RATE_MULT = 0.3f;
	
	private float missileDelay = 0f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		
		float shipTimeMult = 1f + ((TIME_MULT - 1f) * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		float damMult = 1f - (DAMAGE_RESIST * effectLevel);
		stats.getHullDamageTakenMult().modifyMult(id, damMult);
		stats.getArmorDamageTakenMult().modifyMult(id, damMult);
		
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_BONUS);
		stats.getMissileWeaponFluxCostMod().modifyPercent(id, -FLUX_BONUS);
		
		float rateMult = 1f + (RATE_MULT * effectLevel);
		stats.getBallisticRoFMult().modifyMult(id, rateMult);
		stats.getMissileRoFMult().modifyMult(id, rateMult);
		stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + effectLevel);
		stats.getMissileAmmoRegenMult().modifyMult(id, 1f + effectLevel);
		
		float timer = Global.getCombatEngine().getElapsedInLastFrame();
		missileDelay -= timer;
		while (missileDelay <= 0f) {
			missileDelay += 0.7f;
			for (WeaponSlotAPI weapon : ship.getHullSpec().getAllWeaponSlotsCopy()) {
	            if (weapon.isSystemSlot()) {
	              float randomArc = MathUtils.getRandomNumberInRange(-16f, 16f);
	              Vector2f randomVel1 = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(3f, 17f));
                  Vector2f boltVel = ship.getVelocity();
                  randomVel1.x += boltVel.x;
                  randomVel1.y += boltVel.y;
	              Global.getCombatEngine().spawnProjectile(ship,
	                null,
	                "A_S-F_AURA_missile",
	                weapon.computePosition(ship),
	                weapon.getAngle() + ship.getFacing() +  randomArc,
	                randomVel1);
	              for (int i=0; i < 6; i++) {
	                  Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 44f));
	                  Vector2f sparkVel = ship.getVelocity();
	                  randomVel2.x += sparkVel.x;
	                  randomVel2.y += sparkVel.y;
	                  
	                  float randomSize = MathUtils.getRandomNumberInRange(7f, 17f);
	                  Global.getCombatEngine().addSmoothParticle(weapon.computePosition(ship),
	                      randomVel2,
	                      randomSize, //size
	                      1.0f, //brightness
	                      0.65f, //duration
	                      new Color(215,190,75,255));
	              }
	            }
	          }
			Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.8f, ship.getLocation(), ship.getVelocity());
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getTimeMult().unmodify(id);
		Global.getCombatEngine().getTimeMult().unmodify(id);
		
		stats.getHullDamageTakenMult().unmodify();
		stats.getArmorDamageTakenMult().unmodify();
		
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);
		
		missileDelay = 0f;
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = (DAMAGE_RESIST * 100f * effectLevel);
		
		if (index == 0) {
			return new StatusData("time flow altered", false);
		}
		if (index == 1) {
			return new StatusData("hull + armour damage taken reduced by " + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
