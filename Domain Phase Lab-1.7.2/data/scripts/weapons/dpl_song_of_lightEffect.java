package data.scripts.weapons;

import java.awt.Color;
import java.util.Iterator;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatAsteroidAPI;
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
public class dpl_song_of_lightEffect implements OnFireEffectPlugin {

	public static float ARC = 30f;
	public static float DECAY = 0.5f;
	public static float SECDECAY = 0.3f;
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		//ARC = 30f;
		float emp = projectile.getEmpAmount();
		float dam = projectile.getDamageAmount();
	
		CombatEntityAPI target = findTarget(projectile, weapon, engine);
		float thickness = 120f;
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
					   new Color(150,50,200,200)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			arc.setSingleFlickerMode();
			if (target instanceof MissileAPI) {
				Global.getCombatEngine().applyDamage(projectile, target, arc.getTargetLocation(), 
						dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				float MaxHP = target.getMaxHitpoints();
				if (MaxHP >= 5000) {
					Global.getCombatEngine().applyDamage(projectile, target, arc.getTargetLocation(), 
							MaxHP*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				}
			} else if (target instanceof ShipAPI) {
				ShipAPI enemy = (ShipAPI) target;
				if (enemy.isFighter() || enemy.isDrone() || enemy.isShuttlePod()) {
					Global.getCombatEngine().applyDamage(projectile, target, arc.getTargetLocation(), 
							dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				} else if (enemy.isAlive()) {
					EmpArcEntityAPI back_arc = engine.spawnEmpArc(enemy, arc.getTargetLocation(), enemy,
							   weapon.getShip(),
							   DamageType.ENERGY, 
							   dam*3f,
							   emp, // emp 
							   100000f, // max range 
							   "shock_repeater_emp_impact",
							   thickness, // thickness
							   color,
							   new Color(150,50,200,200)
							   );
					back_arc.setCoreWidthOverride(thickness * coreWidthMult);
					back_arc.setSingleFlickerMode();
				}
			}
			allSecTargets(projectile,target,arc.getTargetLocation(),weapon,engine);
			CombatEntityAPI new_target = findNewTarget(target, weapon, engine);
			if (new_target != null) {
				EmpArcEntityAPI new_arc = engine.spawnEmpArc(projectile.getSource(), arc.getTargetLocation(), target,
					   new_target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(150,50,200,200)
					   );
				new_arc.setCoreWidthOverride(thickness * coreWidthMult);
				new_arc.setSingleFlickerMode();
				if (new_target instanceof MissileAPI) {
					Global.getCombatEngine().applyDamage(projectile, new_target, new_arc.getTargetLocation(), 
							dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
					float MaxHP = new_target.getMaxHitpoints();
					if (MaxHP >= 5000) {
						Global.getCombatEngine().applyDamage(projectile, new_target, new_arc.getTargetLocation(), 
								MaxHP*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
					}
				} else if (new_target instanceof ShipAPI) {
					ShipAPI new_enemy = (ShipAPI) new_target;
					if (new_enemy.isFighter() || new_enemy.isDrone()) {
						Global.getCombatEngine().applyDamage(projectile, new_target, new_arc.getTargetLocation(), 
								dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
					}
				}
				allSecTargets(projectile,new_target,new_arc.getTargetLocation(),weapon,engine);
			    CombatEntityAPI new_2target = findNewTarget(new_target, weapon, engine);
				if (new_2target != null) {
					EmpArcEntityAPI new_2arc = engine.spawnEmpArc(projectile.getSource(), new_arc.getTargetLocation(), new_target,
					   	new_2target,
					   	DamageType.ENERGY, 
					   	dam,
					   	emp, // emp 
					   	100000f, // max range 
					   	"shock_repeater_emp_impact",
					   	thickness, // thickness
					   	color,
					   	new Color(150,50,200,200)
					   	);
					new_2arc.setCoreWidthOverride(thickness * coreWidthMult);
					new_2arc.setSingleFlickerMode();
					if (new_2target instanceof MissileAPI) {
						Global.getCombatEngine().applyDamage(projectile, new_2target, new_2arc.getTargetLocation(), 
								dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
						float MaxHP = new_2target.getMaxHitpoints();
						if (MaxHP >= 5000) {
							Global.getCombatEngine().applyDamage(projectile, new_2target, new_2arc.getTargetLocation(), 
									MaxHP*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
						}
					} else if (new_2target instanceof ShipAPI) {
						ShipAPI new_2enemy = (ShipAPI) new_2target;
						if (new_2enemy.isFighter() || new_2enemy.isDrone()) {
							Global.getCombatEngine().applyDamage(projectile, new_2target, new_2arc.getTargetLocation(), 
									dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
						}
					}
					allSecTargets(projectile,new_2target,new_2arc.getTargetLocation(),weapon,engine);
				}
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
		float spread = 18f;
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
					!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
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
			
			float score = dist;
			
			if (score < minScore) {
				minScore = score;
				best = other;
			}
		}
		return best;
	}
	
	public CombatEntityAPI findNewTarget(CombatEntityAPI starting_ship, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange()*DECAY;
		Vector2f from = starting_ship.getLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
																			range * 2f, range * 2f);
		int owner = weapon.getShip().getOwner();
		CombatEntityAPI best = null;
		float minScore = Float.MAX_VALUE;
		float minSizeScore = Float.MAX_VALUE;
		
		ShipAPI ship = weapon.getShip();
		boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue(Stats.PD_IGNORES_FLARES, 0) >= 1;
		ignoreFlares |= weapon.hasAIHint(AIHints.IGNORES_FLARES);
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof MissileAPI) &&
					!(o instanceof CombatAsteroidAPI) &&
					!(o instanceof ShipAPI)) continue;
			CombatEntityAPI other = (CombatEntityAPI) o;
			if (other.getOwner() == owner) continue;
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;
			
			float sizeScore = 1000f;
			
			if (other instanceof ShipAPI) {
				ShipAPI otherShip = (ShipAPI) other;
				if (otherShip.isPhased()) {
					continue;
				} else {
					if (otherShip.isDrone()) {
						sizeScore = 3f;
					} else if (otherShip.isFighter()) {
						sizeScore = 2f;
					} else if (otherShip.isFrigate()) {
						sizeScore = 50f;
					} else if (otherShip.isDestroyer()) {
						sizeScore = 25f;
					} else if (otherShip.isCruiser()) {
						sizeScore = 10f;
					} else if (otherShip.isCapital()) {
						sizeScore = 5f;
					}
				}
			}
			
			if (ignoreFlares && other instanceof MissileAPI) {
				MissileAPI missile = (MissileAPI) other;
				if (missile.isFlare()) {
					continue;
				} else {
					sizeScore = 1f;
				}
			}

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist < 2) continue;
			if (dist > range) continue;
			
			float score = dist;
			
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
	
	public void allSecTargets(DamagingProjectileAPI projectile, CombatEntityAPI starting_ship, Vector2f from, WeaponAPI weapon, CombatEngineAPI engine) {
		float range = weapon.getRange()*SECDECAY ;
		
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
			
			float emp = projectile.getEmpAmount() * 0.5f;
			float dam = projectile.getDamageAmount() * 0.5f;
		
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
						dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				float MaxHP = other.getMaxHitpoints();
				if (MaxHP >= 5000) {
					Global.getCombatEngine().applyDamage(projectile, other, other.getLocation(), 
							MaxHP*10f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				}
			} else if (other instanceof ShipAPI) {
				ShipAPI other_enemy = (ShipAPI) other;
				if (other_enemy.isFighter() || other_enemy.isDrone()) {
					Global.getCombatEngine().applyDamage(projectile, other, other.getLocation(), 
							dam*3f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, projectile.getSource(), true);
				}
			}
		}
	}
}
