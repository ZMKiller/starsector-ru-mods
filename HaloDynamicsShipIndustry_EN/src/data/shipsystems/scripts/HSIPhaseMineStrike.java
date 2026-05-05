package data.shipsystems.scripts;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HSIPhaseMineStrike extends BaseShipSystemScript implements MineStrikeStatsAIInfoProvider {

	protected static float MINE_RANGE = 1000f;

	public static final float MIN_SPAWN_DIST = 75f;
	public static final float MIN_SPAWN_DIST_FRIGATE = 110f;

	public static final float LIVE_TIME = 5f;

	public static final Color JITTER_COLOR = new Color(255, 15, 255, 75);
	public static final Color JITTER_UNDER_COLOR = new Color(255, 15, 255, 155);

	public static float getRange(ShipAPI ship) {
		if (ship == null)
			return MINE_RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(MINE_RANGE);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		// boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}

		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		if (state == State.OUT) {
		}

		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

		if (state == State.IN) {
		} else if (effectLevel >= 1) {
			Vector2f target = ship.getMouseTarget();
			if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
				target = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
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

				target = findClearLocation(ship, target);

				if (target != null) {
					spawnMine(ship, target);
				}
			}

		} else if (state == State.OUT) {
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
	}

	public void spawnMine(ShipAPI source, Vector2f mineLoc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		Vector2f currLoc = Misc.getPointAtRadius(mineLoc, 30f + (float) Math.random() * 30f);
		// Vector2f currLoc = null;
		float start = (float) Math.random() * 360f;
		for (float angle = start; angle < start + 390; angle += 30f) {
			if (angle != start) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(50f + (float) Math.random() * 30f);
				currLoc = Vector2f.add(mineLoc, loc, new Vector2f());
			}
			for (MissileAPI other : Global.getCombatEngine().getMissiles()) {
				if (!other.isMine())
					continue;

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
		int side = 0;
		if (source != null) {
			side = source.getOwner();
		}

		CombatFleetManagerAPI manager = engine.getFleetManager(side);
		manager.setSuppressDeploymentMessages(true);
		ShipAPI mine = manager.spawnShipOrWing("HSI_PhaseMine_Attack", currLoc, 0f);
		mine.setHullSize(HullSize.FIGHTER);
		manager.setSuppressDeploymentMessages(false);

		Global.getCombatEngine().addPlugin(PhaseMineStats(mine, side));

		// mine.setFlightTime((float) Math.random());
		// liveTime = 0.01f;

		Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, mine.getLocation(), mine.getVelocity());
	}

	protected EveryFrameCombatPlugin PhaseMineStats(final ShipAPI mine, final int owner) {
		return new BaseEveryFrameCombatPlugin() {
			FaderUtil fader = new FaderUtil(0, 0.75f, 0.75f, false, false);
			IntervalUtil maxAimTime = new IntervalUtil(5f, 5f);
			IntervalUtil maxFlightTime = new IntervalUtil(120f, 120f);

			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused())
					return;
				maxFlightTime.advance(amount);
				if (maxFlightTime.intervalElapsed()) {
					Global.getCombatEngine().removeEntity(mine);
					Global.getCombatEngine().removePlugin(this);
					return;
				}
				if (fader.getBrightness() == 0 && mine.getAllWeapons().get(0).getCooldownRemaining() == 0) {
					Iterator<Object> c = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(
							mine.getLocation(),
							300f, 300f);
					while (c.hasNext()) {
						Object o = c.next();
						if (o instanceof ShipAPI) {
							ShipAPI s = (ShipAPI) o;
							if (s.getOwner() != owner && s.getOwner() != 100) {
								fader.fadeIn();
								break;
							}
						}
					}
				} else if (mine.getAllWeapons().get(0).getCooldownRemaining() > 0) {
					fader.fadeOut();
				}
				if (fader.getBrightness() < 0.2f) {
					mine.setPhased(true);
					mine.setOwner(100);
				} else {
					mine.setPhased(false);
					mine.setOwner(owner);
				}
				if (fader.getBrightness() < 1) {
					mine.getAllWeapons().get(owner).setAmmo(0);
					if (maxAimTime.getElapsed() > 0)
						maxAimTime.setElapsed(0f);
				} else {
					maxAimTime.intervalElapsed();
					mine.getAllWeapons().get(owner).setAmmo(1);
					if (maxAimTime.intervalElapsed())
						fader.fadeOut();
				}
				fader.advance(amount);
				mine.setAlphaMult(Math.max(0,0.5f + 0.5f * fader.getBrightness()) );
				mine.getVelocity().set(0f, 0f);
			}
		};

	}

	protected float getMaxRange(ShipAPI ship) {
		return getMineRange(ship);
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo())
			return null;
		if (system.getState() != SystemState.IDLE)
			return null;

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
		if (isLocationClear(dest))
			return dest;

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

		if (tested.isEmpty())
			return dest; // shouldn't happen

		return tested.pick();
	}

	private boolean isLocationClear(Vector2f loc) {
		for (ShipAPI other : Global.getCombatEngine().getShips()) {
			if (other.isShuttlePod())
				continue;
			if (other.isFighter())
				continue;

			// Vector2f otherLoc = other.getLocation();
			// float otherR = other.getCollisionRadius();

			// if (other.isPiece()) {
			// System.out.println("ewfewfewfwe");
			// }
			Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
			float otherR = other.getShieldRadiusEvenIfNoShield();
			if (other.isPiece()) {
				otherLoc = other.getLocation();
				otherR = other.getCollisionRadius();
			}

			// float dist = Misc.getDistance(loc, other.getLocation());
			// float r = other.getCollisionRadius();
			float dist = Misc.getDistance(loc, otherLoc);
			float r = otherR;
			// r = Math.min(r, Misc.getTargetingRadius(loc, other, false) + r * 0.25f);
			float checkDist = MIN_SPAWN_DIST;
			if (other.isFrigate())
				checkDist = MIN_SPAWN_DIST_FRIGATE;
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

	public float getMineRange(ShipAPI ship) {
		return getRange(ship);
		// return MINE_RANGE;
	}

}
