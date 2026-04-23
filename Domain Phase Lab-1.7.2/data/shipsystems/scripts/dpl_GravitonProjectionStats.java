package data.shipsystems.scripts;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_GravitonProjectionStats extends com.fs.starfarer.api.impl.combat.BaseShipSystemScript {
	//public static final float ENERGY_DAM_PENALTY_MULT = 0.5f;
	protected static float SLOW_DUR = 5f;
	protected static float PRIMARY_RANGE = 1500f;
	protected static float SECONDARY_RANGE = 500f;
	protected static float SLOW_BONUS = 0.05f;
	
	public static final Color JITTER_COLOR = new Color(155,225,255,75);

	public static class GravProjSpeedDebuff implements AdvanceableListener {
		public static String DEBUFF_ID = "dpl_grav_proj_debuff";
		
		public ShipAPI ship;
		public float dur = SLOW_DUR;
		public GravProjSpeedDebuff(ShipAPI ship) {
			//Things staying constant such as ship stats if this listener is activated go here.
			this.ship = ship;
			
			ship.getMutableStats().getMaxSpeed().modifyMult(DEBUFF_ID, SLOW_BONUS);
			ship.getMutableStats().getMaxTurnRate().modifyMult(DEBUFF_ID, SLOW_BONUS);
		}

		public void resetDur() {
			dur = SLOW_DUR;
		}
		
		public void advance(float amount) {
			//Things that can change via time go here.
			dur -= amount;
			
			if (dur <= 0) {
				ship.removeListener(this);
				ship.getMutableStats().getMaxSpeed().unmodify(DEBUFF_ID);
				ship.getMutableStats().getMaxTurnRate().unmodify(DEBUFF_ID);
			}
		}
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
		} else {
			return;
		}
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag("dpl_bagpipe_system")) {
				w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
			}
		}
		
		String targetKey = ship.getId() + "_gravtproj_target";
		Object foundTarget = Global.getCombatEngine().getCustomData().get(targetKey); 
		if (state == State.IN) {
			if (foundTarget == null) {
				ShipAPI target = findTarget(ship);
				if (target != null) {
					Global.getCombatEngine().getCustomData().put(targetKey, target);
				}
			}
		} else if (effectLevel >= 1) {
			if (foundTarget instanceof ShipAPI) {
				ShipAPI target = (ShipAPI) foundTarget;
				if (target.getFluxTracker().isOverloadedOrVenting()) target = ship;
				applyEffectToTarget(ship, target);
			}
		} else if (state == State.OUT && foundTarget != null) {
			Global.getCombatEngine().getCustomData().remove(targetKey);
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = PRIMARY_RANGE;
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();
		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)){
			target = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM);
		}
		
		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true);
				} else {
					Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
					if (test instanceof ShipAPI) {
						target = (ShipAPI) test;
						float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
						float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
						if (dist > range + radSum) target = null;
					}
				}
			}
			if (target == null) {
				target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true);
			}
		}
		if (target == null || target.getFluxTracker().isOverloadedOrVenting()) target = ship;
		
		return target;
	}

	protected void applyEffectToTarget(final ShipAPI ship, final ShipAPI target) {
		if (target.getFluxTracker().isOverloadedOrVenting()) {
			return;
		}
		if (target == ship) return;
		
		WeaponAPI theWeapon = null;
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag("dpl_bagpipe_system")) {
				theWeapon = w;
			}
		}
		
		if (theWeapon == null) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		float thickness = 125f;
		float coreWidthMult = 0.67f;
		Color color = theWeapon.getSpec().getGlowColor();
		
		EmpArcEntityAPI arc = engine.spawnEmpArc(ship, theWeapon.getLocation(), ship,
				   target,
				   DamageType.ENERGY, 
				   5f,
				   0f, // emp 
				   100000f, // max range 
				   "shock_repeater_emp_impact",
				   thickness, // thickness
				   color,
				   new Color(5,135,175,255)
				   );
		arc.setCoreWidthOverride(thickness * coreWidthMult);
		arc.setSingleFlickerMode();
		
		List<GravProjSpeedDebuff> listeners = target.getListeners(GravProjSpeedDebuff.class);
		if (listeners.isEmpty()) {
			target.addListener(new GravProjSpeedDebuff(target));
		} else {
			listeners.get(0).resetDur();
		}
		
		if (target.getFluxTracker().showFloaty() || 
				ship == Global.getCombatEngine().getPlayerShip() ||
				target == Global.getCombatEngine().getPlayerShip()) {
			target.getFluxTracker().showOverloadFloatyIfNeeded("Graviton Projection!", JITTER_COLOR, 4f, true);
		}
		
		float secRange = SECONDARY_RANGE;
		Vector2f from = arc.getTargetLocation();
		
		Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
				secRange*2, secRange*2);
		int owner = ship.getOwner();
		
		while (iter.hasNext()) {
			Object o = iter.next();
			if (!(o instanceof ShipAPI)) continue;
			ShipAPI other = (ShipAPI) o;
			if (other.getOwner() == owner) continue;
			if (other.equals(target)) continue;
			if (other.isHulk()) continue;
			
			if (other.getCollisionClass() == CollisionClass.NONE) continue;

			float radius = Misc.getTargetingRadius(from, other, false);
			float dist = Misc.getDistance(from, other.getLocation()) - radius;
			if (dist < 2) continue;
			if (dist > secRange) continue;
			
			EmpArcEntityAPI sec_arc = engine.spawnEmpArc(ship, from, target,
					   other,
					   DamageType.ENERGY, 
					   5f,
					   0f, // emp 
					   100000f, // max range 
					   "shock_repeater_emp_impact",
					   thickness*0.5f, // thickness
					   color,
					   new Color(5,135,175,255)
					   );
			sec_arc.setCoreWidthOverride(thickness * coreWidthMult);
			sec_arc.setSingleFlickerMode();
			
			List<GravProjSpeedDebuff> otherListeners = other.getListeners(GravProjSpeedDebuff.class);
			if (otherListeners.isEmpty()) {
				other.addListener(new GravProjSpeedDebuff(other));
			} else {
				otherListeners.get(0).resetDur();
			}
			
			if (other.getFluxTracker().showFloaty() || 
					ship == Global.getCombatEngine().getPlayerShip() ||
					other == Global.getCombatEngine().getPlayerShip()) {
				other.getFluxTracker().showOverloadFloatyIfNeeded("Graviton Projection!", JITTER_COLOR, 4f, true);
			}
		}
		
	}

	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null || target == ship) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
		//return super.getInfoText(system, ship);
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
		//return super.isUsable(system, ship);
	}
	

	
}








