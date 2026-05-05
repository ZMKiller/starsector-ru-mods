package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;

public class HSICitadelDeployAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private CombatFleetManagerAPI manager;
	private float Group_Range;
	private float Citadel_Range;
	private ShipAPI citadel = null;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		manager = engine.getFleetManager(ship.getOwner());
		if (!ship.getChildModulesCopy().isEmpty()) {
			citadel = ship.getChildModulesCopy().get(0);
		}
		float max_Range = 0;
		if (citadel != null) {
			for (WeaponAPI weapon : citadel.getAllWeapons()) {
				if (weapon.getSpec().getMaxRange() > max_Range) {
					max_Range = weapon.getSpec().getMaxRange();
				}
			}
			Group_Range = citadel.getMutableStats().getBallisticWeaponRangeBonus().computeEffective(max_Range);
		} else {
			Group_Range = 1000;
		}
		Citadel_Range = Group_Range + 1.75f * max_Range;
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (citadel == null) {
			if (!ship.getChildModulesCopy().isEmpty()) {
				citadel = ship.getChildModulesCopy().get(0);
				float max_Range = 0;
				if (citadel != null) {
					for (WeaponAPI weapon : citadel.getAllWeapons()) {
						if (weapon.getSpec().getMaxRange() > max_Range) {
							max_Range = weapon.getSpec().getMaxRange();
						}
					}
					Group_Range = citadel.getMutableStats().getBallisticWeaponRangeBonus().computeEffective(max_Range);
					Citadel_Range = Group_Range + 1.75f * max_Range;
				} else {
					return;
				}
			}else{
				return;
			}
		}
		if (system.isCoolingDown() || system.isOn())
			return;
		CombatTaskManagerAPI tasks = manager.getTaskManager(false);
		AssignmentInfo assignment = tasks.getAssignmentFor(ship);
		if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
			HSITurbulanceShieldListenerV2 shield = ship.getListenerManager().getListeners(HSITurbulanceShieldListenerV2.class).get(0);
			if (shield.getShield().getCurrent() < shield.getShield().getShieldCap() * 0.4f) {
				if (system.getDisplayName().equals(HSII18nUtil.getShipSystemString("HSIDeployCitadelRecall"))) {
					ship.useSystem();
					return;
				}
			}
		}
		Vector2f calLoc = ship.getLocation();
		if (ship.getCustomData().containsKey("HSICitadelDeployed")) {
			calLoc = (Vector2f) ship.getCustomData().get("HSICitadelDeployed");
		}
		if (assignment == null) {
			if (target != null && (Misc.getDistance(calLoc, target.getLocation()) > Group_Range
					&& Misc.getDistance(calLoc, target.getLocation()) < Citadel_Range)) {
				Vector2f tgt = (Vector2f) (Misc.getUnitVectorAtDegreeAngle(ship.getFacing())
						.scale(800f + 4 * ship.getVelocity().length() + ship.getShieldRadiusEvenIfNoShield()));
				Vector2f.add(ship.getLocation(), tgt, tgt);
				flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, Vector2f.add(ship.getLocation(), tgt, null));
			}
			if (flags.hasFlag(AIFlags.SYSTEM_TARGET_COORDS) && isUsable(system, ship)) {
				ship.useSystem();
			} else if (system.getDisplayName().equals(HSII18nUtil.getShipSystemString("HSIDeployCitadelRecall"))) {
				ship.useSystem();
			}
			// engine.maintainStatusForPlayerShip(ship, null,"ShipAssignment", "no
			// assignment", false);
		} else {
			// engine.maintainStatusForPlayerShip(ship, null,"ShipAssignment",
			// assignment.getType().toString(), false);
			if (assignment.getType() == CombatAssignmentType.CAPTURE
					|| assignment.getType() == CombatAssignmentType.DEFEND
					|| assignment.getType() == CombatAssignmentType.CONTROL) {
				flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 2f, assignment.getTarget().getLocation());
			} else if (assignment.getType() == CombatAssignmentType.RETREAT) {
				flags.setFlag(AIFlags.SYSTEM_TARGET_COORDS, 1f, ship.getLocation());
			} else {
				if (system.getDisplayName().equals(HSII18nUtil.getShipSystemString("HSIDeployCitadelRecall"))) {
					ship.useSystem();
				}
			}
			if (flags.hasFlag(AIFlags.SYSTEM_TARGET_COORDS) && isUsable(system, ship)) {
				ship.useSystem();
			}
		}
	}

	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		if (ship.getChildModulesCopy().isEmpty())
			return false;
		ShipAPI citadel = ship.getChildModulesCopy().get(0);
		if (!citadel.isAlive())
			return false;
		Vector2f Loc = ship.getMouseTarget();
		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
			Loc = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
		}
		if (Loc != null) {
			return Misc.getDistance(Loc, ship.getLocation()) - ship.getCollisionRadius() < getRange(ship);
		} else {
			return false;
		}
	}

	public static float getRange(ShipAPI ship) {
		if (ship == null)
			return 2000f;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(2000f);
	}
}
