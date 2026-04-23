package data.shipsystems.scripts;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class dpl_LayMineStats extends BaseShipSystemScript {
	
	protected static float MINE_RANGE = 1000f;
	
	public static final float MIN_SPAWN_DIST = 75f;
	public static final float MIN_SPAWN_DIST_FRIGATE = 110f;
	
	public static final float LIVE_TIME = 10f;
	
	public static final Color JITTER_COLOR = new Color(255,155,255,75);
	public static final Color JITTER_UNDER_COLOR = new Color(255,155,255,155);
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag("dpl_minelayer_system")) {
				w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
			}
		}
		
		if (state == State.IN) {
		} else if (effectLevel >= 1) {
			Vector2f target = ship.getMouseTarget();
			if (target == null) {
				Vector2f guess = ship.getLocation();
				Vector2f offset = Misc.getUnitVectorAtDegreeAngle(ship.getFacing());
				target = new Vector2f(guess.x + offset.x*500f, guess.y + offset.y*500f);
			}
			if (target != null) {
				float dist = Misc.getDistance(ship.getLocation(), target);
				float max = getMaxRange(ship) + ship.getCollisionRadius();
				if (dist > max) {
					float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
					target = Misc.getUnitVectorAtDegreeAngle(dir);
					target.scale(max);
					Vector2f.add(target, ship.getLocation(), target);
				}
				
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_minelayer_system")) {
						target = findClearLocation(ship, target);
						if (target != null) {
							spawnMine(ship, target, w.getLocation());
						}
					}
				}
			}
		} else if (state == State.OUT ) {
			
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public void spawnMine(ShipAPI source, Vector2f mineLoc, Vector2f initLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		//Vector2f currLoc = null;
		float start = (float) Math.random() * 360f;
		for (float angle = start; angle < start + 390; angle += 30f) {
			if (angle != start) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(50f + (float) Math.random() * 30f);
				currLoc = Vector2f.add(mineLoc, loc, new Vector2f());
			}
			for (MissileAPI other : Global.getCombatEngine().getMissiles()) {
				if (!other.isMine()) continue;
				
				float dist = Misc.getDistance(currLoc, other.getLocation());
				if (dist < other.getCollisionRadius() + 40f) {
					currLoc = null;
					break;
				}
			}
			if (currLoc != null) {
				break;
			}
		}
		if (currLoc == null) {
			currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		}
		
		
		
		//Vector2f currLoc = mineLoc;
		MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, null, 
															  "dpl_am_cl", 
															  currLoc, 
															  (float) Math.random() * 360f, null);
		if (source != null) {
			Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
											source, WeaponType.MISSILE, false, mine.getDamage());
		}
		
		mine.setMine(true);
		mine.getVelocity().scale(0);
		float liveTime = LIVE_TIME;
		//liveTime = 0.01f;
		mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
		
		float thickness = 50f;
		float coreWidthMult = 0.67f;
		
		EmpArcEntityAPI arc = engine.spawnEmpArcVisual(initLoc, 
				source, 
				mine.getLocation(), 
				source, 
				thickness, // thickness
				new Color(115,95,205,255),
				new Color(155,75,255,255));
		
		arc.setCoreWidthOverride(thickness * coreWidthMult);
		arc.setSingleFlickerMode();
		
		Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, mine.getLocation(), mine.getVelocity());
	}
	
	protected float getMaxRange(ShipAPI ship) {
		if (ship == null) return MINE_RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(MINE_RANGE);
	}

	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		Vector2f target = ship.getMouseTarget();
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target);
			float max = getMaxRange(ship) + ship.getCollisionRadius();
			if (dist > max) {
				return "OUT OF RANGE";
			} else {
				return "READY";
			}
		}
		return null;
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return ship.getMouseTarget() != null;
	}
	
	
	private Vector2f findClearLocation(ShipAPI ship, Vector2f dest) {
		if (isLocationClear(dest)) return dest;
		
		float incr = 50f;

		WeightedRandomPicker<Vector2f> tested = new WeightedRandomPicker<Vector2f>();
		for (float distIndex = 1; distIndex <= 32f; distIndex *= 2f) {
			float start = (float) Math.random() * 360f;
			for (float angle = start; angle < start + 360; angle += 60f) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(incr * distIndex);
				Vector2f.add(dest, loc, loc);
				tested.add(loc);
				if (isLocationClear(loc)) {
					return loc;
				}
			}
		}
		
		if (tested.isEmpty()) return dest; // shouldn't happen
		
		return tested.pick();
	}
	
	private boolean isLocationClear(Vector2f loc) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other.isShuttlePod()) continue;
			if (other.isFighter()) continue;
			
//			Vector2f otherLoc = other.getLocation();
//			float otherR = other.getCollisionRadius();
			
//			if (other.isPiece()) {
//				System.out.println("ewfewfewfwe");
//			}
			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			if (other.isPiece()) {
				otherLoc = other.getLocation();
				otherR = other.getCollisionRadius();
			}
			
			
//			float dist = Misc.getDistance(loc, other.getLocation());
//			float r = other.getCollisionRadius();
			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			//r = Math.min(r, Misc.getTargetingRadius(loc, other, false) + r * 0.25f);
			float checkDist = MIN_SPAWN_DIST;
			if (other.isFrigate()) checkDist = MIN_SPAWN_DIST_FRIGATE;
			if (dist < r + checkDist) {
				return false;
			}
		}
		for (CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
			float dist = Misc.getDistance(loc, other.getLocation());
			if (dist < other.getCollisionRadius() + MIN_SPAWN_DIST) {
				return false;
			}
		}
		
		return true;
	}


	public float getFuseTime() {
		return 3f;
	}
	
}








