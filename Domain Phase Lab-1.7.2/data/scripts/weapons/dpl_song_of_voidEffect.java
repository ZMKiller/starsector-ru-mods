package data.scripts.weapons;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.Misc;

/**
 */
public class dpl_song_of_voidEffect implements OnFireEffectPlugin {

	public static float ARC = 45f;
	public static float DECAY = 0.5f;
	public static float GUIDING_BEAM_RANGE = 1000f;
	public static int REAL_DAMAGE = 100;
	public static int HARD_FLUX = 100;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		float thickness = 30f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(projectile.getLocation(),projectile.getSource(),target.getLocation(),target,thickness, // thickness
					   color,
					   new Color(150,150,255,200));
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			
			CombatEntityAPI new_target = findNewTarget(target, weapon, engine);
			if (new_target != null) {
				EmpArcEntityAPI new_arc = engine.spawnEmpArcPierceShields(projectile.getSource(), target.getLocation(), target,
					   new_target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   new Color(150,25,255,200),
					   new Color(100,5,200,200)
					   );
				new_arc.setCoreWidthOverride(thickness * coreWidthMult);
				new_arc.setSingleFlickerMode();
				
				if (new_target instanceof ShipAPI) {
					ShipAPI s = (ShipAPI) new_target;
					s.setHitpoints(Math.max(s.getHitpoints() - REAL_DAMAGE, 1));
					engine.addFloatingDamageText(new_arc.getTargetLocation(), REAL_DAMAGE, Misc.FLOATY_HULL_DAMAGE_COLOR, s, projectile.getSource());	
					Vector2f loc = new_arc.getTargetLocation();
					engine.applyDamage(new_target, loc, dam, DamageType.ENERGY, emp, true, false, weapon.getShip(), true);
					if (s.getHitpoints() <= 10 && !s.getVariant().hasHullMod("vastbulk")) {
						//Just kill it if its hull point is too low.
						s.getMutableStats().getHullDamageTakenMult().unmodify();
						s.getMutableStats().getArmorDamageTakenMult().unmodify();
						s.setHitpoints(1f);
				        int[] cell = s.getArmorGrid().getCellAtLocation(new_arc.getTargetLocation());
				        s.getArmorGrid().setArmorValue(cell[0], cell[1], 0f);
						engine.applyDamage(s, new_arc.getTargetLocation(), 50000f, DamageType.OTHER, 0f, true, false, null);
					}
				}
		    }
		} else {
			ShipAPI ownShip = weapon.getShip();
			ownShip.getFluxTracker().increaseFlux(HARD_FLUX, true);
		}
	}
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 15f;
		float range = weapon.getRange() - spread;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange();
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		CombatEntityAPI best = null;
		
		float minScore = Float.MAX_VALUE;
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI)) continue;
			MissileAPI other = (MissileAPI) o;
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (other.getWeaponSpec() != null) {
				if (other.getWeaponSpec().getWeaponId() != null) {
					if (!(other.getWeaponSpec().getWeaponId().equals("dpl_anchor"))) continue;
				} else continue;
			} else continue;
			

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			if (!Misc.isInArc(weapon.getCurrAngle(), ARC, from, other.getLocation())) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
	public CombatEntityAPI findNewTarget(CombatEntityAPI starting_ship, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = GUIDING_BEAM_RANGE;
		Vector2f from = starting_ship.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		float minSizeScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			ShipAPI other = (ShipAPI) o;
			if (other.getOwner() == owner) continue;
			if (other.isHulk()) continue;
			//if (other.isPhased()) continue;
			//if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			//float angleTo = Misc.getAngleInDegrees(from, other.getLocation());
			//float score = Misc.getAngleDiff(weapon.getCurrAngle(), angleTo);
			float score = dist;
			float sizeScore = 1000f;
			if (other.isDrone()) {
				sizeScore = 100f;
			} else if (other.isFighter()) {
				sizeScore = 95f;
			} else if (other.isFrigate()) {
				sizeScore = 10f;
			} else if (other.isDestroyer()) {
				sizeScore = 5f;
			} else if (other.isCruiser()) {
				sizeScore = 2f;
			} else if (other.isCapital()) {
				sizeScore = 1f;
			}
			
			if (sizeScore < minSizeScore) {
				minSizeScore = sizeScore;
				minScore = score;
				best = other;
			} else if (sizeScore == minSizeScore) {
				if (score < minScore) {
					minSizeScore = sizeScore;
					minScore = score;
					best = other;
				}
			}
		}
		
		if (best == null) {
			best = ship;
		}
		
		return best;
	}

}
