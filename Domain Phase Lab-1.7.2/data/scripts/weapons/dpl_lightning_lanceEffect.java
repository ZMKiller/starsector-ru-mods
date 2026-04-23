package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_lightning_lanceEffect implements BeamEffectPlugin {
	
	public static int MAX_MOTES = 8;
	public static float UNUSED_RANGE_PER_SPAWN = 200f;
	public static float SPAWN_SPACING = 175f;
	public static float SPAWN_INTERVAL = 0.1f;
	
	
	
	protected Vector2f arcFrom = null;
	protected Vector2f prevMoteLoc = null;
	
	protected boolean doneSpawningMotes = false;
	protected float spawned = 0;
	protected int numToSpawn = 0;
	protected float untilNextSpawn = 0;
	protected float spawnDir = 0;
	
	protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
	
	public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
		tracker.advance(amount);
		
		if (beam.getBrightness() < 1f) return;
		
		
		if (doneSpawningMotes) return;
		
		if (numToSpawn <= 0 && beam.getDamageTarget() != null) {
			float range = beam.getWeapon().getRange();
			float length = beam.getLengthPrevFrame();
			//float perSpawn = range / NUM_SPAWNS;
			numToSpawn = (int) ((range - length) / UNUSED_RANGE_PER_SPAWN) + 1;
			if (numToSpawn > MAX_MOTES) {
				numToSpawn = MAX_MOTES;
			}
			untilNextSpawn = 0f;
		}
		//numToSpawn = 5;
		
//		if (beam.getBrightness() >= 1f) {
//			canSpawn = true;
//		}
		//untilNextSpawn = 0f;
		
		untilNextSpawn -= amount;
		if (untilNextSpawn > 0) return;
//		if (!canSpawn || beam.getBrightness() >= 1f) return;
		
		float perSpawn = SPAWN_SPACING;
		
		ShipAPI ship = beam.getSource();
		
		boolean spawnedMote = false;
		if (beam.getLength() > beam.getWeapon().getRange() - 10f) {
			float angle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
			loc.scale(beam.getLength());
			Vector2f.add(loc, beam.getFrom(), loc);
			
			spawnMote(ship, loc);
			spawnedMote = true;
		} else if (beam.getDamageTarget() != null) {
			Vector2f arcTo = getNextArcLoc(engine, beam, perSpawn);
			float thickness = beam.getWidth();
			//thickness = 20;
			float dist = Misc.getDistance(arcFrom, arcTo);
			if (dist < SPAWN_SPACING * 2f) {
				EmpArcEntityAPI arc = engine.spawnEmpArcVisual(arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), Color.white);
				arc.setCoreWidthOverride(Math.max(20f, thickness * 0.67f));
				//Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 1f, arc.getLocation(), arc.getVelocity());
			}
			spawnMote(ship, arcTo);
			spawnedMote = true;
			arcFrom = arcTo;
		}

		untilNextSpawn = SPAWN_INTERVAL;
		if (spawnedMote) {
			spawned++;
			if (spawned >= numToSpawn) {
				doneSpawningMotes = true;
			}
		}
	}
	
	public Vector2f getNextArcLoc(CombatEngineAPI engine, BeamAPI beam, float perSpawn) {
		CombatEntityAPI target = beam.getDamageTarget();
		float radiusOverride = -1f;
		if (target instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) target;
			if (ship.getParentStation() != null && ship.getStationSlot() != null) {
				//radiusOverride = Misc.getDistance(ship.getLocation(), ship.getParentStation().getLocation());
				//radiusOverride += ship.getCollisionRadius();
				radiusOverride = Misc.getDistance(beam.getRayEndPrevFrame(), ship.getParentStation().getLocation()) + 0f;
				target = ship.getParentStation();
			}
		}
		
		if (arcFrom == null) {
			arcFrom = new Vector2f(beam.getRayEndPrevFrame());
//			Vector2f loc = Misc.getUnitVectorAtDegreeAngle(beamAngle);
//			loc.scale(beam.getLengthPrevFrame());
//			//loc.scale(200f);
//			Vector2f.add(loc, beam.getFrom(), loc);
//			arcFrom = loc;
			
			float beamAngle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
			float beamSourceToTarget = Misc.getAngleInDegrees(beam.getFrom(), target.getLocation());
			
			// this is the direction we'll rotate - from the target's center - so that it's spawning mines around the side
			// closer to the beam's straight line
			spawnDir = Misc.getClosestTurnDirection(beamAngle, beamSourceToTarget);
			if (spawnDir == 0) spawnDir = 1;
			
			boolean computeNextLoc = false;
			if (prevMoteLoc != null) {
				float dist = Misc.getDistance(arcFrom, prevMoteLoc);
				if (dist < perSpawn) {
					perSpawn -= dist;
					computeNextLoc = true;
				}
			}
			if (!computeNextLoc) {
				return arcFrom;
			}
		}
		
//		target = Global.getCombatEngine().getPlayerShip();
//		target.getLocation().y += 750f;
		
		Vector2f targetLoc = target.getLocation();
		float targetRadius = target.getCollisionRadius();
		if (radiusOverride >= 0) {
			targetRadius = radiusOverride;
		}
		
//		if (target instanceof ShipAPI) {
//			ShipAPI ship = (ShipAPI) target;
//			targetLoc = ship.getShieldCenterEvenIfNoShield();
//			targetRadius = ship.getShieldRadiusEvenIfNoShield();
//		}
		
		boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getRayEndPrevFrame());
		if (hitShield) perSpawn *= 0.67f;
//		float beamAngle = Misc.getAngleInDegrees(beam.getFrom(), beam.getRayEndPrevFrame());
//		float beamSourceToTarget = Misc.getAngleInDegrees(beam.getFrom(), targetLoc);
//		
//		// this is the direction we'll rotate - from the target's center - so that it's spawning mines around the side
//		// closer to the beam's straight line
//		float dir = Misc.getClosestTurnDirection(beamAngle, beamSourceToTarget);
		
		float prevAngle = Misc.getAngleInDegrees(targetLoc, arcFrom);
		float anglePerSegment = 360f * perSpawn / (3.14f * 2f * targetRadius);
		if (anglePerSegment > 90f) anglePerSegment = 90f;
		float angle = prevAngle + anglePerSegment * spawnDir;
		
		
		Vector2f arcTo = Misc.getUnitVectorAtDegreeAngle(angle);
		arcTo.scale(targetRadius);
		Vector2f.add(targetLoc, arcTo, arcTo);
		
		float actualRadius = Global.getSettings().getTargetingRadius(arcTo, target, hitShield);
		if (radiusOverride >= 0) {
			actualRadius = radiusOverride;
		}
		if (!hitShield) {
			//actualRadius *= 1f + 0.1f * (float) Math.random();
			actualRadius += 30f + 50f * (float) Math.random();
		} else {
//			actualRadius = target.getShield().getRadius();
//			actualRadius += 20f + 20f * (float) Math.random();
			actualRadius += 30f + 50f * (float) Math.random();
		}
		
//		float angleDiff = Misc.getAngleDiff(beamSourceToTarget + 180f, angle);
//		if (angleDiff > 150f) {
//			actualRadius += perSpawn * (180f - angleDiff) / 30f;
//		}
		
		arcTo = Misc.getUnitVectorAtDegreeAngle(angle);
		arcTo.scale(actualRadius);
		Vector2f.add(targetLoc, arcTo, arcTo);
		
		
		// now we've got an arcTo location somewhere roughly circular; try to cleave more closely to the hull
		// if the target is a ship
		if (target instanceof ShipAPI && !hitShield) {
			ShipAPI ship = (ShipAPI) target;
			BoundsAPI bounds = ship.getExactBounds();
			if (bounds != null) {
				Vector2f best = null;
				float bestDist = Float.MAX_VALUE;
				for (SegmentAPI segment : bounds.getSegments()) {
					float test = Misc.getDistance(segment.getP1(), arcTo);
					if (test < bestDist) {
						bestDist = test;
						best = segment.getP1();
					}
				}
				if (best != null) {
					Object o = Global.getSettings().getWeaponSpec("dpl_phase_cm").getProjectileSpec();
					if (o instanceof MissileSpecAPI) {
						MissileSpecAPI spec = (MissileSpecAPI) o;
						float explosionRadius = 50f;
						
						Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(best, arcTo));
						dir.scale(explosionRadius * 0.9f);
						Vector2f.add(best, dir, dir);
						arcTo = dir;
						
//						dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(prevMineLoc, arcTo));
//						dir.scale(perSpawn);
//						Vector2f.add(prevMineLoc, dir, dir);
					}
				}
			}
			
		}
		
		return arcTo;
	}
	
	public void spawnMote(ShipAPI source, Vector2f moteLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		MissileAPI mote = (MissileAPI) engine.spawnProjectile(source, null, 
															"dpl_phase_cm", 
															moteLoc, 
															  (float) Math.random() * 360f, null);
		
		// "spawned" does not include this mine
		
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.ENERGY, false, mote.getDamage());
		}
		
		mote.setMine(true);
		
		Vector2f vel = new Vector2f(0,0);
		if (mote != null) vel.set(mote.getVelocity());
		Global.getSoundPlayer().playSound("riftcascade_rift", 1f, 1f, moteLoc, vel);
		
		prevMoteLoc = moteLoc;
	}

}
