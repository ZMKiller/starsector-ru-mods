package data.scripts.weapons;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
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
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

/**
 */
public class dpl_arc_blasterEffect implements OnFireEffectPlugin {

	public static float ARC = 30f;
	public static float CHANCE = 0.05f;
	public static int MAXARC = 20;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
	
		ShipAPI target = findTarget(projectile, weapon, engine);
		float thickness = 50f;
		float coreWidthMult = 0.67f;
		Color color = weapon.getSpec().getGlowColor();
		if (target != null) {
			float s = (float) Math.random();
			if (s<=CHANCE) {
				for (int i=0; i <= MAXARC; i++) {
					EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), projectile.getLocation(), weapon.getShip(),
							   target,
							   DamageType.ENERGY, 
							   dam,
							   emp, // emp 
							   100000f, // max range 
							   "shock_repeater_emp_impact",
							   thickness, // thickness
							   new Color(55,55,255,255),
							   color
							   );
					arc.setCoreWidthOverride(thickness * coreWidthMult);
				}
			} else {
				EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), projectile.getLocation(), weapon.getShip(),
						   target,
						   DamageType.ENERGY, 
						   dam,
						   emp, // emp 
						   100000f, // max range 
						   "shock_repeater_emp_impact",
						   thickness, // thickness
						   new Color(55,55,255,255),
						   color
						   );
				arc.setCoreWidthOverride(thickness * coreWidthMult);
			}
			
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
		float spread = 50f;
		float range = weapon.getRange() - spread;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle());
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
	
	public ShipAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange();
		Vector2f from = projectile.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		ShipAPI best = null;
		float minScore = Float.MAX_VALUE;
		float minSizeScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			ShipAPI other = (ShipAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other.isFighter()) continue;
			
			if (other.isDrone()) continue;
			
			if (other.isShuttlePod()) continue;
			
			if (other.isHulk()) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist > range) continue;
			
			if (!Misc.isInArc(weapon.getCurrAngle(), ARC, from, other.getLocation())) continue;
			
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
		return best;
	}

}
