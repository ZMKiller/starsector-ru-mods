package data.ai;

import java.util.Iterator;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.shipsystems.scripts.HSIBurial;

public class HSIBurialAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private boolean systemuse = true;
	private float shortest = 1000f;
	private ShipwideAIFlags flags;
	private IntervalUtil tracker = new IntervalUtil(0.2f, 0.3f);
	private float range = 2000f;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.engine = engine;
		this.flags = flags;
		this.system = system;
		switch (ship.getHullSpec().getBaseHullId()) {
			case "HSI_T_01_68":
				range = HSIBurial.getRange(ship);
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (target == null)
			return;
		if (system.getCooldownRemaining() > 0)
			return;
		if (system.isOutOfAmmo())
			return;
		if (system.isOn())
			return;
		tracker.advance(amount);
		systemuse = true;
		if (tracker.intervalElapsed()) {
			if (systemuse) {
				if (ship.getFluxTracker().getHardFlux() / ship.getMaxFlux() > 0.75f) {
					if (ship.getSystem().getId().equals("HSI_Burial")) {
						Vector2f to = null;
						for (int i = 0; i <= 10; i++) {
							Vector2f testTo = Vector2f.add(ship.getLocation(),
									(Vector2f) (Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + 180f + i * 9f)
											.scale(HSIBurial.getRange(ship) / 0.75f)),
									null);
							boolean available = checkDanger(testTo);
							if (available) {
								to = testTo;
								break;
							}
							testTo = Vector2f.add(ship.getLocation(),
									(Vector2f) (Misc.getUnitVectorAtDegreeAngle(ship.getFacing() + 180f + i * 9f)
											.scale(HSIBurial.getRange(ship) / 0.75f)),
									null);
							available = checkDanger(testTo);
							if (available) {
								to = testTo;
								break;
							}
						}
						if (to == null)
							return;
						flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, to);
						ship.useSystem();
						return;
					} 
				}
				if (Misc.getDistance(ship.getLocation(), target.getLocation()) > shortest || shieldDefending(target)) {
					Vector2f to = null;
					for (int i = 0; i <= 10; i++) {
						Vector2f testTo = Vector2f.add(target.getLocation(),
								(Vector2f) (Misc.getUnitVectorAtDegreeAngle(target.getFacing() + 180f + i * 9f)
										.scale(ship.getCollisionRadius() * 1.5f + target.getCollisionRadius() * 1.5f)),
								null);
						boolean available = checkDanger(testTo);
						if (available) {
							to = testTo;
							break;
						}
						testTo = Vector2f.add(target.getLocation(),
								(Vector2f) (Misc.getUnitVectorAtDegreeAngle(target.getFacing() + 180f - i * 9f)
										.scale(ship.getCollisionRadius() * 1.5f + target.getCollisionRadius() * 1.5f)),
								null);
						available = checkDanger(testTo);
						if (available) {
							to = testTo;
							break;
						}
					}
					if (to == null)
						return;
					flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 5f, to);
					ship.useSystem();
					return;
				}
			}
			/*
			 * for (MissileAPI m : engine.getMissiles()) {
			 * if (m.getOwner() != ship.getOwner() && m.getDamageAmount() > 500f
			 * && Misc.getDistance(m.getLocation(), ship.getLocation()) < 400f) {
			 * if (systemuse) {
			 * ship.useSystem();
			 * return;
			 * }
			 * }
			 * }
			 */
		}
	}

	private boolean checkDanger(Vector2f testTo) {
		Iterator<Object> ships = engine.getAiGridShips().getCheckIterator(testTo, 1500f, 1500f);
		float dmg = 0;
		while (ships.hasNext()) {
			ShipAPI s = (ShipAPI) ships.next();
			if (Misc.getDistance(testTo, s.getLocation()) < s.getCollisionRadius())
				return false;
			for (WeaponAPI weapon : s.getAllWeapons()) {
				if ((weapon.hasAIHint(AIHints.STRIKE) || weapon.getSize() == WeaponSize.LARGE
						|| weapon.getType() == WeaponType.MISSILE)
						&& ((!weapon.usesAmmo())
								|| (weapon.usesAmmo() && weapon.getAmmo() > 0) || weapon.getCooldownRemaining() < 0)) {
					float wdmg = weapon.getDamage().getDamage();
					dmg += 0.25f * wdmg
							/ ((Misc.getDistance(s.getLocation(), weapon.getLocation()) - ship.getCollisionRadius())
									/ 1200f);
					if (dmg > 1000 * ship.getHullSize().ordinal())
						break;
				}
			}
			if (dmg > 1000 * ship.getHullSize().ordinal())
				break;
		}
		return dmg <= Math.min(1000 * ship.getHullSize().ordinal(), ship.getHitpoints());
	}

	private boolean shieldDefending(ShipAPI target) {
		boolean isDefending = false;
		ShieldAPI s = target.getShield();
		if (s == null)
			return false;
		if (s.getType() == ShieldType.FRONT || s.getType() == ShieldType.OMNI) {
			float link = Misc.getAngleInDegrees(target.getLocation(), ship.getLocation());
			if (s.isOn()) {
				if (link < s.getFacing() + s.getActiveArc() / 2 || link > s.getFacing() - s.getActiveArc() / 2) {
					isDefending = true;
				}
			}
		}
		return isDefending;
	}
}
