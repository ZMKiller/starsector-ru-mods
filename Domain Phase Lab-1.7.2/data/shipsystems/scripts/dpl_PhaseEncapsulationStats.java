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
import com.fs.starfarer.api.util.Misc.FindShipFilter;

public class dpl_PhaseEncapsulationStats extends com.fs.starfarer.api.impl.combat.BaseShipSystemScript {
	//public static final float ENERGY_DAM_PENALTY_MULT = 0.5f;
	protected static float PHASE_DUR = 5f;
	protected static float PRIMARY_RANGE = 2000f;
	protected static float MEAN_EFFICIENCY = 0.1f;
	
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_COLOR = new Color(175,155,255,255);
	public static final Color JITTER_UNDER_COLOR = new Color(125,0,255,125);

	public static class PhaseEncapDebuff implements AdvanceableListener {
		public static String DEBUFF_ID = "dpl_phase_encap_debuff";
		
		public ShipAPI ship;
		public ShipAPI source;
		public float dur = PHASE_DUR;
		public float hardFluxPrevFrame = 0f;
		public PhaseEncapDebuff(ShipAPI source, ShipAPI ship) {
			//Things staying constant such as ship stats if this listener is activated go here.
			this.ship = ship;
			this.source = source;
			hardFluxPrevFrame = ship.getFluxTracker().getHardFlux();
		}

		public void resetDur() {
			dur = PHASE_DUR;
		}
		
		public void advance(float amount) {
			//Things that can change via time go here.
			dur -= amount;
			
			if (dur <= 0 || ship.getFluxTracker().isOverloadedOrVenting()) {
				ship.setPhased(false);
				ship.removeListener(this);
				ship.setExtraAlphaMult(1f);
				ship.setApplyExtraAlphaToEngines(false);
			} else {
				float jitterLevel = dur/PHASE_DUR;
				float currHardFlux = ship.getFluxTracker().getHardFlux();
				if (currHardFlux < hardFluxPrevFrame) {
					ship.getFluxTracker().increaseFlux(hardFluxPrevFrame - currHardFlux, true);
				}
				float shipMaxFlux = ship.getFluxTracker().getMaxFlux();
				float sourceMaxFlux = source.getFluxTracker().getMaxFlux();
				float flux_amount = amount * MEAN_EFFICIENCY * (shipMaxFlux + sourceMaxFlux);
				ship.getFluxTracker().increaseFlux(flux_amount, false);
				ship.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, 5f);
				ship.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 5f);
				ship.setExtraAlphaMult(Math.max(1f-dur, 0.2f));
				ship.setApplyExtraAlphaToEngines(true);
				ship.setPhased(true);
				for (WeaponAPI weapon: ship.getAllWeapons()) {
					weapon.setForceNoFireOneFrame(true);
				}
			}
		}
	}
	
	public static class PhaseEncapSoftFluxDebuff implements AdvanceableListener {
		public static String DEBUFF_ID = "dpl_phase_encap_debuff";
		
		public ShipAPI ship;
		public ShipAPI source;
		public float dur = PHASE_DUR;
		public float hardFluxPrevFrame = 0f;
		public PhaseEncapSoftFluxDebuff(ShipAPI source, ShipAPI ship) {
			//Things staying constant such as ship stats if this listener is activated go here.
			this.ship = ship;
			this.source = source;
		}

		public void resetDur() {
			dur = PHASE_DUR;
		}
		
		public void advance(float amount) {
			//Things that can change via time go here.
			dur -= amount;
			
			if (dur <= 0 || ship.getFluxTracker().isOverloadedOrVenting()) {
				for (WeaponAPI w : source.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_reedpipe_system")) {
						w.setGlowAmount(0f, w.getSpec().getGlowColor());
					}
				}
				source.removeListener(this);
			} else {
				float shipMaxFlux = ship.getFluxTracker().getMaxFlux();
				float sourceMaxFlux = source.getFluxTracker().getMaxFlux();
				float flux_amount = amount * MEAN_EFFICIENCY * (shipMaxFlux + sourceMaxFlux);
				source.getFluxTracker().increaseFlux(flux_amount, false);
				float jitterLevelSelf = dur/PHASE_DUR;
				source.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevelSelf, 5, 0f, 5f);
				source.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevelSelf, 2, 0f, 5f);
				for (WeaponAPI w : source.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_reedpipe_system")) {
						w.setGlowAmount(jitterLevelSelf, w.getSpec().getGlowColor());
					}
				}
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
		
		String targetKey = ship.getId() + "_phase_encap_target";
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
	
	public static ShipAPI findLowestFluxShipEnemyOf(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getShips();
		float minFluxLevel = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other.getHullSize().ordinal() < smallestToNote.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() != other.getOwner() && other.getOwner() != 100) {
				
				float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
				float fluxSort = other.getFluxTracker().getHardFlux()/other.getFluxTracker().getMaxFlux();
				float radSum = ship.getCollisionRadius() + other.getCollisionRadius();
				if (!considerShipRadius) radSum = 0;
				if (dist > maxRange + radSum) continue;
				if (minFluxLevel > fluxSort) {
					closest = other;
					minFluxLevel = fluxSort;
				}
			}
		}
		return closest;
	}
	
	public static ShipAPI findLargestFluxShipEnemyOf(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getShips();
		float minSizeScore = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other.getHullSize().ordinal() < smallestToNote.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() != other.getOwner() && other.getOwner() != 100) {
				
				float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
				float radSum = ship.getCollisionRadius() + other.getCollisionRadius();
				if (!considerShipRadius) radSum = 0;
				if (dist > maxRange + radSum) continue;
				
				float sizeScore = 1000f;
				if (other.isDrone()) {
					sizeScore = 100f;
				} else if (other.isFighter()) {
					sizeScore = 95f;
				} else if (other.isFrigate()) {
					sizeScore = 10f;
				} else if (other.isDestroyer()) {
					sizeScore = 5f;
				} else if (other.isCruiser()) {
					sizeScore = 2f;
				} else if (other.isCapital()) {
					sizeScore = 1f;
				}
				
				if (sizeScore < minSizeScore) {
					closest = other;
					minSizeScore = sizeScore;
				}
			}
		}
		return closest;
	}
	
	public static ShipAPI findClosestEnemyNear(ShipAPI ship, Vector2f locFromForSorting, HullSize smallestToNote, float maxRange, boolean considerShipRadius) {
		CombatEngineAPI engine = Global.getCombatEngine();
		List<ShipAPI> ships = engine.getShips();
		float minDist = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other.getHullSize().ordinal() < smallestToNote.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() != other.getOwner() && other.getOwner() != 100) {
				
				float dist = Misc.getDistance(ship.getLocation(), other.getLocation());
				float distSort = Misc.getDistance(locFromForSorting, other.getLocation());
				float radSum = ship.getCollisionRadius() + other.getCollisionRadius();
				if (!considerShipRadius) radSum = 0;
				if (dist > maxRange + radSum) continue;
				if (distSort < minDist) {
					closest = other;
					minDist = distSort;
				}
			}
		}
		return closest;
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = PRIMARY_RANGE;
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = null;
		
		if (player) {
			target = findClosestEnemyNear(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true);
		} else {
			target = findLargestFluxShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FRIGATE, range, true);
		}
		if (target == null) {
			target = findLowestFluxShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true);
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
			if (w.isDecorative() && w.getSpec().hasTag("dpl_reedpipe_system")) {
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
				   new Color(155,75,255,255)
				   );
		arc.setCoreWidthOverride(thickness * coreWidthMult);
		arc.setSingleFlickerMode();
		
		List<PhaseEncapSoftFluxDebuff> selfListeners = ship.getListeners(PhaseEncapSoftFluxDebuff.class);
		if (selfListeners.isEmpty()) {
			ship.addListener(new PhaseEncapSoftFluxDebuff(ship,target));
		} else {
			selfListeners.get(0).resetDur();
		}
		
		List<PhaseEncapDebuff> listeners = target.getListeners(PhaseEncapDebuff.class);
		if (listeners.isEmpty()) {
			target.addListener(new PhaseEncapDebuff(ship,target));
		} else {
			listeners.get(0).resetDur();
		}
		
		if (target.isShipWithModules() || target.isStation()) {
			for (ShipAPI module: target.getChildModulesCopy()) {
				List<PhaseEncapDebuff> moduleListeners = module.getListeners(PhaseEncapDebuff.class);
				if (moduleListeners.isEmpty()) {
					module.addListener(new PhaseEncapDebuff(ship,module));
				} else {
					moduleListeners.get(0).resetDur();
				}
			}
		}
		
		if (target.getFluxTracker().showFloaty() || 
				ship == Global.getCombatEngine().getPlayerShip() ||
				target == Global.getCombatEngine().getPlayerShip()) {
			target.getFluxTracker().showOverloadFloatyIfNeeded("Phase Encapsulation!", JITTER_COLOR, 4f, true);
		}
		
		if (ship.getFluxTracker().showFloaty() || 
				ship == Global.getCombatEngine().getPlayerShip() ||
				target == Global.getCombatEngine().getPlayerShip()) {
			ship.getFluxTracker().showOverloadFloatyIfNeeded("Phase Encapsulation!", JITTER_COLOR, 4f, true);
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








