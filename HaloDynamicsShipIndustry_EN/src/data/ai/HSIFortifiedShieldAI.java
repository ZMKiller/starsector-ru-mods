package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIFortifiedShieldAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private float fraction;
	private ShipSystemAPI system;
	private ShipwideAIFlags flags;
	private HSITurbulanceShieldListenerV2 shield = null;

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
		if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
			shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
		}
	}

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if (shield == null && ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
			shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
		}
		if (shield == null)
			return;
		float currShield = shield.getShield().getCurrent();
		float maxShield = shield.getShield().getShieldCap();
		float frac = currShield / maxShield;
		Vector2f shipLoc = ship.getLocation();
		float collisionRadius = ship.getCollisionRadius();
		boolean shouldUseSystem = false;
		if (frac > 0.01f && !(target != null && target.getFluxTracker().isOverloadedOrVenting()
				&& (target.getFluxTracker().getOverloadTimeRemaining() > 3f
						|| target.getFluxTracker().getTimeToVent() > 3f))) {
			for (ShipAPI s : engine.getShips()) {
				if (!shouldUseSystem && s.getOwner() != ship.getOwner()) {
					for (WeaponAPI w : s.getAllWeapons()) {
						if (!shouldUseSystem
								&& (Misc.getDistance(w.getLocation(), shipLoc) < w.getRange() + collisionRadius
										&& w.isFiring() && w.getDamage().getDamage() > 200f
										&& (s.getShipTarget() != null && s.getShipTarget() == ship))) {
							shouldUseSystem = true;
						}
					}
				}
			}
			if (!shouldUseSystem) {
				for (DamagingProjectileAPI proj : engine.getProjectiles()) {
					if (!shouldUseSystem && proj.getDamageAmount() > shield.getShield().getShieldCap()*0.01f && Misc.getDistance(proj.getLocation(),
							shipLoc) < proj.getMoveSpeed() * 0.1f + collisionRadius) {
						shouldUseSystem = true;
					}
				}
				for(BeamAPI beam:engine.getBeams()){
					if(!shouldUseSystem&&beam.getDamage().getDamage()*0.1f>shield.getShield().getShieldCap()*0.01f&&Misc.getDistance(beam.getRayEndPrevFrame(),
					shipLoc)<collisionRadius){
						shouldUseSystem = true;
					}
				}
			}
		}
		if (ship.getPhaseCloak() != null && ship.getPhaseCloak().getSpecAPI().getId().equals("HSI_FortifiedShield")) {
			if (shouldUseSystem && !ship.getPhaseCloak().isActive()) {
				ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
			}
		} else if (ship.getSystem() != null && ship.getSystem().getSpecAPI().getId().equals("HSI_FortifiedShield")) {
			if (shouldUseSystem && !ship.getSystem().isOn()) {
				ship.giveCommand(ShipCommand.USE_SYSTEM, ship.getMouseTarget(), 0);
			}
		}
	}
}
