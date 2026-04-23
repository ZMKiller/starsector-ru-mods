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

public class dpl_ExpWeaponStats extends BaseShipSystemScript {
	//public static final float ENERGY_DAM_PENALTY_MULT = 0.5f;
	
	protected float time = 0f;
	protected float fireThreshold = 0.1f;
	
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
		} else {
			return;
		}
 
		if (state == State.IN || effectLevel >= 1) {
			
			if (time == amount) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy")) {
						w.setForceFireOneFrame(true);
					}
				}
			}
			
			if (time >= fireThreshold) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy_ltn")) {
						w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
						w.setForceFireOneFrame(true);
					}
				}
				fireThreshold += 0.1f;
			} else {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy_ltn")) {
						w.setForceNoFireOneFrame(true);
						w.setGlowAmount(0, null);
					}
				}
			}
		} else if (state == State.OUT) {
			
			if (effectLevel <= 0.01) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy")) {
						w.setForceNoFireOneFrame(true);
						w.setGlowAmount(0, null);
					}
					
					if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy_ltn")) {
						w.setForceNoFireOneFrame(true);
						w.setGlowAmount(0, null);
					}
				}
			}
			
		} else {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy")) {
					w.setForceNoFireOneFrame(true);
					w.setGlowAmount(0, null);
				}
				
				if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy_ltn")) {
					w.setForceNoFireOneFrame(true);
					w.setGlowAmount(0, null);
				}
			}
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		time = 0f;
		fireThreshold = 0.25f;
		
		ShipAPI ship = (ShipAPI) stats.getEntity();
		if (ship == null) return;
		
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy")) {
				w.setForceNoFireOneFrame(true);
				w.setGlowAmount(0, null);
			}
			
			if (w.isDecorative() && w.getSpec().hasTag("dpl_prophecy_ltn")) {
				w.setForceNoFireOneFrame(true);
				w.setGlowAmount(0, null);
			}
		}
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}

	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
	

	
}








