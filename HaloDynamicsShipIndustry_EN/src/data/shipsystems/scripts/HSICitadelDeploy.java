package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.kit.HSII18nUtil;

public class HSICitadelDeploy extends BaseShipSystemScript {

	protected static float RANGE = 2000f;

	public static final Color JITTER_COLOR = new Color(255, 15, 255, 75);
	public static final Color JITTER_UNDER_COLOR = new Color(255, 15, 255, 155);
	private boolean CitadelDeployed = false;
	private ShipAPI CitadelDeploy;
	private Object STATUS_KEY = new Object();
	private Vector2f Loc;

	public static float getRange(ShipAPI ship) {
		if (ship == null)
			return RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		// boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		Loc = ship.getMouseTarget();
		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
			Loc = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
		}
		float jitterLevel = effectLevel;
		if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		if (state == State.OUT) {
		}
		if (ship.getChildModulesCopy().isEmpty())
			return;
		ShipAPI citadel = ship.getChildModulesCopy().get(0);
		if (!CitadelDeployed) {
			if (state == State.IN) {
			} else if (effectLevel >= 1) {
				Vector2f target = new Vector2f(Loc);

				if (target != null) {
					float dist = Misc.getDistance(ship.getLocation(), target);
					float max = getRange(ship) + ship.getCollisionRadius();
					if (dist > max) {
						float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
						target = Misc.getUnitVectorAtDegreeAngle(dir);
						target.scale(max);
						Vector2f.add(target, ship.getLocation(), target);
					}

					target = findClearLocation(ship, target);

					if (target != null) {
						CitadelDeploy = DeployCitadel(ship, target);
					}
				}

			} else if (state == State.OUT) {
			}
		} else {
			if (effectLevel >= 1) {
				CitadelDeployed = false;
				removeCitadelDeploy(ship);
			}
		}
		MutableShipStatsAPI citadelStats = citadel.getMutableStats();
		if (CitadelDeployed) {
			for (WeaponAPI weapon : citadel.getAllWeapons()) {
				weapon.setForceNoFireOneFrame(true);
			}
			stats.getAcceleration().modifyPercent(id, 50f);
			stats.getMaxSpeed().modifyPercent(id, 75f);
			stats.getMaxTurnRate().modifyPercent(id, 50f);
			citadel.setHitpoints(Math.max(CitadelDeploy.getHitpoints(), citadel.getMaxHitpoints() * 0.2f));

			ArmorGridAPI a = citadel.getArmorGrid();
			ArmorGridAPI b = CitadelDeploy.getArmorGrid();

			float[][] cag = a.getGrid();
			for (int i = 0; i < cag.length; i++) {
				for (int j = 0; j < cag[i].length; j++) {
					a.setArmorValue(i, j, b.getArmorValue(i,j));
				}
			}

			citadel.getFluxTracker().setCurrFlux(CitadelDeploy.getFluxTracker().getCurrFlux());
			citadel.getFluxTracker().setHardFlux(CitadelDeploy.getFluxTracker().getHardFlux());
		} else {
			// citadel.getLocation().set(ship.getLocation());
			// citadelStats.getHullDamageTakenMult().modifyMult(id, 0f);
			// citadelStats.getArmorDamageTakenMult().modifyMult(id, 0f);
			citadelStats.getBallisticRoFMult().modifyMult(id, 0.66f);
			citadelStats.getEnergyRoFMult().modifyMult(id, 0.66f);
			stats.getAcceleration().unmodify(id);
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
			float repair = Global.getCombatEngine().getElapsedInLastFrame() * citadel.getMaxHitpoints() * 0.05f;
			citadel.setHitpoints(Math.min(citadel.getHitpoints() + repair, citadel.getMaxHitpoints()));
		}
		if (ship == Global.getCombatEngine().getPlayerShip()) {
			Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, "graphics/icons/hullsys/drone_pd_high.png",
					(CitadelDeployed) ? (HSII18nUtil.getShipSystemString("HSIDeployCitadel"))
							: HSII18nUtil.getShipSystemString("HSIDeployCitadelRecall"),
					(CitadelDeployed) ? (HSII18nUtil.getShipSystemString("HSIDeployCitadelBuff"))
							: HSII18nUtil.getShipSystemString("HSIDeployCitadelRecallBuff"),
					!CitadelDeployed);
		}
		if (!CitadelDeployed) {
			citadel.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
			citadel.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
			citadel.setAlphaMult(Math.max(0,1 - effectLevel) );
		} else {
			citadel.setAlphaMult(0f);
		}
		if (CitadelDeploy != null && CitadelDeployed) {
			CitadelDeploy.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
			CitadelDeploy.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
			CitadelDeploy.setAlphaMult(Math.max(0,1 - effectLevel) );
		}
		citadel.setPhased(CitadelDeployed);
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
	}

	public ShipAPI DeployCitadel(ShipAPI ship, Vector2f Loc) {
		CombatEngineAPI engine = Global.getCombatEngine();
		ShipAPI citadel = ship.getChildModulesCopy().get(0);
		ShipVariantAPI variant = citadel.getVariant();
		// variant.addMod("axialrotation");
		ShipAPI citadelDeployed = engine.createFXDrone(variant);
		citadelDeployed.setCollisionClass(CollisionClass.SHIP);
		citadelDeployed.setOwner(ship.getOwner());
		citadelDeployed.setDrone(false);
		citadelDeployed.setHullSize(HullSize.CAPITAL_SHIP);
		citadelDeployed.setAlly(ship.isAlly());
		engine.addEntity(citadelDeployed);
		MutableShipStatsAPI citadelStats = citadelDeployed.getMutableStats();
		citadelStats.getBallisticRoFMult().modifyMult("DEPLOYCITADEL", 1.25f);
		citadelStats.getBallisticWeaponRangeBonus().modifyPercent("DEPLOYCITADEL", 150f);
		citadelStats.getEnergyRoFMult().modifyMult("DEPLOYCITADEL", 1.25f);
		citadelStats.getEnergyWeaponRangeBonus().modifyPercent("DEPLOYCITADEL", 150f);
		citadelDeployed.getCustomData().put("HSICamelotSource", ship);
		ShipAIConfig config = new ShipAIConfig();
		config.alwaysStrafeOffensively = true;
		config.backingOffWhileNotVentingAllowed = false;
		config.turnToFaceWithUndamagedArmor = false;
		config.burnDriveIgnoreEnemies = true;

		boolean carrier = false;
		if (citadelDeployed != null && citadelDeployed.getVariant() != null) {
			carrier = citadelDeployed.getVariant().isCarrier() && !citadelDeployed.getVariant().isCombat();
		}
		if (carrier) {
			config.personalityOverride = Personalities.AGGRESSIVE;
			config.backingOffWhileNotVentingAllowed = true;
		} else {
			config.personalityOverride = Personalities.RECKLESS;
		}
		citadelDeployed.setShipAI(Global.getSettings().createDefaultShipAI(citadelDeployed, config));
		citadelDeployed.getFluxTracker().setCurrFlux(citadel.getFluxTracker().getCurrFlux());
		citadelDeployed.getFluxTracker().setHardFlux(citadel.getFluxTracker().getHardFlux());

		ArmorGridAPI b = citadel.getArmorGrid();
		ArmorGridAPI a = citadelDeployed.getArmorGrid();

		float[][] cag = a.getGrid();
		for (int i = 0; i < cag.length; i++) {
			for (int j = 0; j < cag[i].length; j++) {
				a.setArmorValue(i, j, b.getArmorValue(i,j));
			}
		}

		Vector2f currLoc = Misc.getPointAtRadius(Loc, 30f + (float) Math.random() * 30f);
		float start = (float) Math.random() * 360f;
		for (float angle = start; angle < start + 390; angle += 30f) {
			if (angle != start) {
				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(50f + (float) Math.random() * 30f);
				currLoc = Vector2f.add(Loc, loc, new Vector2f());
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
			currLoc = Misc.getPointAtRadius(Loc, 30f + (float) Math.random() * 30f);
		}
		citadelDeployed.getLocation().set(currLoc);

		citadelDeployed.setCaptain(ship.getCaptain());
		CitadelDeployed = true;
		CombatEntityAPI nearestEnemy = AIUtils.getNearestEnemy(citadelDeployed);
		if (nearestEnemy != null) {
			citadelDeployed.setFacing(VectorUtils.getAngle(currLoc, nearestEnemy.getLocation()));
		} else {
			citadelDeployed.setFacing(citadel.getFacing());
		}
		citadelDeployed.setInvalidTransferCommandTarget(true);
		// mine.setFlightTime((float) Math.random());
		// liveTime = 0.01f;
		if (CitadelDeploy != null && engine.isEntityInPlay(CitadelDeploy)) {
			removeCitadelDeploy(ship);
		}
		Global.getSoundPlayer().playSound("mine_teleport", 1f, 1f, citadel.getLocation(), citadel.getVelocity());
		ship.setCustomData("HSICitadelDeployed", citadelDeployed.getLocation());
		return citadelDeployed;
	}

	public void removeCitadelDeploy(ShipAPI ship) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (CitadelDeploy != null && engine.isEntityInPlay(CitadelDeploy)) {
			if (CitadelDeploy.getHitpoints() < CitadelDeploy.getMaxHitpoints() * 0.2f)
				ship.setCurrentCR(Math.max(0.2f, ship.getCurrentCR() - 0.05f));
			engine.removeEntity(CitadelDeploy);
			ship.removeCustomData("HSICitadelDeployed");
			if (CitadelDeploy == engine.getPlayerShip()) {
				engine.setPlayerShipExternal(ship);
			}
		}
	}

	public boolean isDeployed() {
		return CitadelDeployed;
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (ship.getChildModulesCopy().isEmpty())
			return "N/A";
		if (!isUsable(system, ship))
			return "UNABLE";
		ShipAPI citadel = ship.getChildModulesCopy().get(0);
		float frac = citadel.getHitpoints() / citadel.getMaxHitpoints();
		return ":" + (int) (frac * 100f) + "%";
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (ship.getChildModulesCopy().isEmpty())
			return false;
		ShipAPI citadel = ship.getChildModulesCopy().get(0);
		if (!citadel.isAlive())
			return false;
		if (CitadelDeployed)
			return true;
		if (Loc == null)
			return false;
		return Misc.getDistance(Loc, ship.getLocation()) - ship.getCollisionRadius() < getRange(ship);
	}

	@Override
	public String getDisplayNameOverride(State state, float effectLevel) {
		if (CitadelDeployed)
			return HSII18nUtil.getShipSystemString("HSIDeployCitadelRecall");
		return HSII18nUtil.getShipSystemString("HSIDeployCitadel");
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
			float checkDist = other.getCollisionRadius();
			if (dist < r + checkDist) {
				return false;
			}
		}

		return true;
	}

}
