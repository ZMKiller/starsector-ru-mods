package data.scripts.weapons;

import java.awt.Color;
import java.util.Iterator;

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
import com.fs.starfarer.api.util.Misc;

/**
 */
public class dpl_heavy_lightningEffect implements OnFireEffectPlugin {

	public static float ARC = 30f;
	public static float SECDECAY = 0.5f;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		//ARC = 30f;
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
	
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		float thickness = 30f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), projectile.getLocation(), weapon.getShip(),
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(150,150,255,200)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			allSecTargets(projectile,target,arc.getTargetLocation(),weapon,engine);
		} else {
			Vector2f from = new Vector2f(projectile.getLocation());
			Vector2f to = pickNoTargetDest(projectile, weapon, engine);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, weapon.getShip(), to, weapon.getShip(), thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			//Global.getSoundPlayer().playSound("shock_repeater_emp_impact", 1f, 1f, to, new Vector2f());
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
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				//if (!otherShip.isAlive()) continue;
				if (otherShip.isPhased()) continue;
			}
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			if (ignoreFlares && other instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) other;
				if (missile.isFlare()) continue;
			}

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
	
	public void allSecTargets(DamagingProjectileAPI projectile, CombatEntityAPI starting_ship, Vector2f from, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange()*SECDECAY;
		
		if (from == null) {
			from = starting_ship.getLocation();
		}
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					//!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isHulk()) continue;
				//if (!otherShip.isAlive()) continue;
				if (otherShip.isPhased()) continue;
			}
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist < 2) continue;
			if (dist > range) continue;
			
			float emp = projectile.getEmpAmount();
			float dam = projectile.getDamageAmount();
		
			float thickness = 60f;
			float coreWidthMult = 0.67f;
			Color color = weapon.getSpec().getGlowColor();
			EmpArcEntityAPI sec_arc = engine.spawnEmpArc(weapon.getShip(), from, starting_ship,
					   other,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(150,50,200,200)
					   );
			sec_arc.setCoreWidthOverride(thickness * coreWidthMult);
			sec_arc.setSingleFlickerMode();
			Vector2f point = other.getLocation();
			if ((other instanceof MissileAPI)) {
				float damage = dam * 1.5f;
				Global.getCombatEngine().applyDamage(projectile, other, point, 
						damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
			} else if ((other instanceof ShipAPI)) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isFighter() || otherShip.isDrone()) {
					float damage = dam * 1.5f;
					Global.getCombatEngine().applyDamage(projectile, other, point, 
							damage, DamageType.ENERGY, 0f, false, false, projectile.getSource(), true);
				}
			}
		}
	}

}
