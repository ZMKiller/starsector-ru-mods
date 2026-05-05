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
import com.fs.starfarer.api.util.TimeoutTracker;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSISiegeModeAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private CombatFleetManagerAPI manager;
	private float range = 0;
	private TimeoutTracker<Object> SiegeKeeper = new TimeoutTracker<>();
	private static final Object KEY = new Object();

	//private WeaponAPI mainWeapon;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		manager = engine.getFleetManager(ship.getOwner());
		for (WeaponAPI weapon : ship.getAllWeapons()) {
			range = Math.max(range, weapon.getRange());
			//if(!weapon.isDecorative()) mainWeapon = weapon;
		}
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		SiegeKeeper.advance(amount);
		if (system.isCoolingDown())
			return;
		boolean shouldSiege = false;
		if(!ship.getMutableStats().getWeaponRangeMultPastThreshold().isUnmodified()) return;
		CombatTaskManagerAPI tasks = manager.getTaskManager(false);
		AssignmentInfo assignment = tasks.getAssignmentFor(ship);
		if (assignment != null && (assignment.getType() == CombatAssignmentType.CAPTURE
				|| assignment.getType() == CombatAssignmentType.DEFEND
				|| assignment.getType() == CombatAssignmentType.CONTROL)) {
			Vector2f loc = assignment.getTarget().getLocation();
			if (Misc.getDistance(ship.getLocation(), loc) < ship.getCollisionRadius() + 400f) {
				shouldSiege = true;
			}
		}
		if (target != null) {
			if (Misc.getDistance(ship.getLocation(), target.getLocation()) > range + target.getCollisionRadius()
					&& Misc.getDistance(ship.getLocation(), target.getLocation()) < range * 2.2f
							+ target.getCollisionRadius()) {
				shouldSiege = true;
			} else {
				shouldSiege = false;
			}
		}
		if (!SiegeKeeper.getItems().isEmpty()) {
			shouldSiege = true;
		}
		if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE) || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)
				|| flags.hasFlag(AIFlags.BACK_OFF))
			shouldSiege = false;
		if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
			HSITurbulanceShieldListenerV2 shield = ship.getListenerManager().getListeners(HSITurbulanceShieldListenerV2.class).get(0);
			if (shield.getShield().getCurrent() < shield.getShield().getShieldCap() * 0.6f)
				shouldSiege = false;
		}
		if (assignment != null && (assignment.getType() == CombatAssignmentType.RETREAT))
			shouldSiege = false;
		doSiege(shouldSiege);
	}

	protected void doSiege(boolean shouldSiege) {
		if (!shouldSiege) {
			if (system.isOn()) {
				system.deactivate();
			}
		} else {
			if (!system.isOn()) {
				ship.useSystem();
				SiegeKeeper.add(KEY, 1.5f);
			}
		}
	}
}
