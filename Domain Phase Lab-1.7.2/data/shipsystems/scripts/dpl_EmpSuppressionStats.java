package data.shipsystems.scripts;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_EmpSuppressionStats extends BaseShipSystemScript {
	//public static final float ENERGY_DAM_PENALTY_MULT = 0.5f;
	
	protected static float SUPPRESSION_RATE = 0.6f;
	protected static float SUPPRESSION_RANGE = 2500f;
	protected float time = 0f;
	protected float fireThreshold = 0.5f;
	protected boolean showFloaty = true;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		
		CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        float amount = 0;
        if (!engine.isPaused()) {
            amount = engine.getElapsedInLastFrame();
        }
        
        time += amount;
		
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			//player = ship == Global.getCombatEngine().getPlayerShip();
		} else {
			return;
		}

		//It needs 1.5 second to lock the target for some reasons I don't know. Must take consideration of this.
		//This function is 0 on [0,1.5], grows at slope 1 on [1.5,2.5], stays at 1 on [2.5,9], drops at slope -1 on [9,10], and stays at 0 for anything else.
		float Supression_Mult = Math.max(1-Math.max(time - 9f, Math.max(2.5f-time, 0f)), 0f);
		
		String targetKey = ship.getId() + "_empsup_target";
		Object foundTarget = Global.getCombatEngine().getCustomData().get(targetKey); 
		if (state == State.IN) {
			if (foundTarget == null) {
				ShipAPI target = findTarget(ship);
				if (target != null) {
					Global.getCombatEngine().getCustomData().put(targetKey, target);
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							float facing = Misc.getAngleInDegrees(w.getLocation(), target.getLocation());
							w.setFacing(facing);
						}
					}
					if (time >= fireThreshold) {
						for (WeaponAPI w : ship.getAllWeapons()) {
							if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
								w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
								w.setForceFireOneFrame(true);
							}
						}
						fireThreshold += 0.25f;
					} else {
						for (WeaponAPI w : ship.getAllWeapons()) {
							if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
								w.setForceNoFireOneFrame(true);
							}
						}
					}
					
					if (!target.equals(ship)) {
						target.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
						target.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
						
						if (target.isShipWithModules() || target.isStation()) {
							for (ShipAPI module: target.getChildModulesCopy()) {
								module.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
								module.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
							}
						}
						
						if (target.getFluxTracker().showFloaty() || 
								ship == Global.getCombatEngine().getPlayerShip() ||
								target == Global.getCombatEngine().getPlayerShip()) {
							if (showFloaty) {
								target.getFluxTracker().showOverloadFloatyIfNeeded("EMP Suppressed!", new Color(0,205,255,255), 4f, true);
								showFloaty = false;
							}
						}
						
						target.setJitter(target, new Color(0,205,255,155), effectLevel*0.5f, 3, 0f, 5f);
					}
				}
			}
		} else if (effectLevel >= 1) {
			if (foundTarget instanceof ShipAPI) {
				ShipAPI target = (ShipAPI) foundTarget;
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
						float facing = Misc.getAngleInDegrees(w.getLocation(), target.getLocation());
						w.setFacing(facing);
					}
				}
				if (time >= fireThreshold) {
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
							w.setForceFireOneFrame(true);
						}
					}
					fireThreshold += 0.25f;
				} else {
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							w.setForceNoFireOneFrame(true);
						}
					}
				}
				
				if (!target.equals(ship)) {
					target.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
					target.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
					
					if (target.isShipWithModules() || target.isStation()) {
						for (ShipAPI module: target.getChildModulesCopy()) {
							module.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
							module.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
						}
					}
					
					if (target.getFluxTracker().showFloaty() || 
							ship == Global.getCombatEngine().getPlayerShip() ||
							target == Global.getCombatEngine().getPlayerShip()) {
						if (showFloaty) {
							target.getFluxTracker().showOverloadFloatyIfNeeded("EMP Suppressed!", new Color(0,205,255,255), 4f, true);
							showFloaty = false;
						}
					}
					
					target.setJitter(target, new Color(0,205,255,155), effectLevel*0.5f, 3, 0f, 5f);
				}
			}
		} else if (state == State.OUT && foundTarget != null) {
			if (foundTarget instanceof ShipAPI) {
				ShipAPI target = (ShipAPI) foundTarget;
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
						float facing = Misc.getAngleInDegrees(w.getLocation(), target.getLocation());
						w.setFacing(facing);
					}
				}
				if (time >= fireThreshold) {
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
							w.setForceFireOneFrame(true);
						}
					}
					fireThreshold += 0.25f;
				} else {
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							w.setForceNoFireOneFrame(true);
						}
					}
				}
				
				if (!target.equals(ship)) {
					target.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
					target.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
					
					if (target.isShipWithModules() || target.isStation()) {
						for (ShipAPI module: target.getChildModulesCopy()) {
							module.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
							module.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent("dpl_piccolo_system", 100*(-SUPPRESSION_RATE*Supression_Mult));
						}
					}
					
					if (target.getFluxTracker().showFloaty() || 
							ship == Global.getCombatEngine().getPlayerShip() ||
							target == Global.getCombatEngine().getPlayerShip()) {
						if (showFloaty) {
							target.getFluxTracker().showOverloadFloatyIfNeeded("EMP Suppressed!", new Color(0,205,255,255), 4f, true);
							showFloaty = false;
						}
					}
					
					target.setJitter(target, new Color(0,205,255,155), effectLevel*0.5f, 3, 0f, 5f);
				}
				
				if (effectLevel <= 0.01) {
					Global.getCombatEngine().getCustomData().remove(targetKey);
					target.getMutableStats().getBallisticWeaponRangeBonus().unmodify("dpl_piccolo_system");
					target.getMutableStats().getEnergyWeaponRangeBonus().unmodify("dpl_piccolo_system");
					
					if (target.isShipWithModules() || target.isStation()) {
						for (ShipAPI module: target.getChildModulesCopy()) {
							module.getMutableStats().getBallisticWeaponRangeBonus().unmodify("dpl_piccolo_system");
							module.getMutableStats().getEnergyWeaponRangeBonus().unmodify("dpl_piccolo_system");
						}
					}
					
					for (WeaponAPI w : ship.getAllWeapons()) {
						if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
							w.setForceNoFireOneFrame(true);
							w.setGlowAmount(0, null);
						}
					}
				}
			}
		} else {
			Global.getCombatEngine().getCustomData().remove(targetKey);
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.isDecorative() && w.getSpec().hasTag("dpl_piccolo_system")) {
					w.setForceNoFireOneFrame(true);
					w.setGlowAmount(0, null);
				}
			}
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		showFloaty = true;
		time = 0f;
		fireThreshold = 0.5f;
	}
	
	protected ShipAPI findTarget(ShipAPI ship) {
		float range = SUPPRESSION_RANGE;
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
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
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








