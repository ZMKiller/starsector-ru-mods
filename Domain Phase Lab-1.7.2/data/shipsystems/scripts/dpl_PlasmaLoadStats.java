package data.shipsystems.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.util.Misc;

public class dpl_PlasmaLoadStats extends BaseShipSystemScript {
	
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
			float flux_level = ship.getFluxTracker().getFluxLevel();
			float time = 1f + 5f * flux_level;
			for (WeaponAPI weapon: ship.getAllWeapons()) {
				if (!weapon.isDecorative() && !weapon.isDisabled() && weapon.getSpec().isBeam()) {
					weapon.setRemainingCooldownTo(Math.max(0f, weapon.getCooldownRemaining() - time));
				}
			}
		} else if (state == State.OUT ) {
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != SystemState.IDLE) return null;
		
		boolean canUse = true;
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			if (!weapon.isDecorative() && !weapon.isDisabled() && weapon.getSpec().isBeam()) {
				if (weapon.isFiring()) {
					canUse = false;
				}
			}
		}
		
		if (!canUse) {
			return "WEAPONS ARE FIRING";
		}
		
		return "READY";
	}
	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		boolean canUse = true;
		for (WeaponAPI weapon: ship.getAllWeapons()) {
			if (!weapon.isDecorative() && !weapon.isDisabled() && weapon.getSpec().isBeam()) {
				if (weapon.isFiring()) {
					canUse = false;
				}
			}
		}
		return canUse;
	}
}
