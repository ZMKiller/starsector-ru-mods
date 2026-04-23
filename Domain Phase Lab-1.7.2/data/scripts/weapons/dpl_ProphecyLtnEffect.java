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
public class dpl_ProphecyLtnEffect implements OnFireEffectPlugin {
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		allTargets(projectile,projectile.getSource(),projectile.getLocation(),weapon,engine);
	}
	
	public void allTargets(DamagingProjectileAPI projectile, CombatEntityAPI starting_ship, Vector2f from, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange();
		
		if (from == null) {
			from = starting_ship.getLocation();
		}
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		
		boolean hasTarget = false;
		
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
			
			hasTarget = true;
			
			float emp = projectile.getEmpAmount();
			float dam = projectile.getDamageAmount();
		
			float thickness = 80f;
			float coreWidthMult = 0.78f;
			Color color = weapon.getSpec().getGlowColor();
			EmpArcEntityAPI sec_arc = engine.spawnEmpArc(weapon.getShip(), from, starting_ship,
					   other,
					   DamageType.ENERGY, 
					   dam*0.5f,
					   emp, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(150,50,200,200)
					   );
			sec_arc.setCoreWidthOverride(thickness * coreWidthMult);
			sec_arc.setSingleFlickerMode();
			if (other instanceof MissileAPI) {
				Global.getCombatEngine().applyDamage(projectile, other, other.getLocation(), 
						other.getMaxHitpoints()*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
			} else if (other instanceof ShipAPI) {
				ShipAPI other_enemy = (ShipAPI) other;
				if (other_enemy.isFighter() || other_enemy.isDrone()) {
					Global.getCombatEngine().applyDamage(projectile, other, other.getLocation(), 
							dam*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				}
			}
		}
		
		if (!hasTarget) {
			float thickness = 25f;
			float coreWidthMult = 0.67f;
			Color color = weapon.getSpec().getGlowColor();
			
			for (int i=1 ; i<=3; i++) {
				Vector2f to = pickNoTargetDest(projectile, weapon, engine);
				EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, weapon.getShip(), to, weapon.getShip(), thickness, color, Color.white);
				arc.setCoreWidthOverride(thickness * coreWidthMult);
				arc.setSingleFlickerMode();
			}
		}
	}
	
	public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float spread = 50f;
		float range = weapon.getRange() - spread;
		Vector2f from = projectile.getLocation();
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float) (360f * Math.random()));
		dir.scale(range);
		Vector2f.add(from, dir, dir);
		dir = Misc.getPointWithinRadius(dir, spread);
		return dir;
	}
}
