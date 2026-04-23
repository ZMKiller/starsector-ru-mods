package data.shipsystems.scripts;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.hullmods.dpl_MoteCounter.dpl_MoteCountData;

public class dpl_PlasmaMoteStats extends BaseShipSystemScript{
	
	public static String dpl_MoteCount_DATA_KEY = "dpl_mote_count_data_key";
	
	protected static float MINE_RANGE = 200f;
	
	protected float fireThreshold = 0.5f;
	protected float time = 0f;
	
	public static final Color JITTER_COLOR = new Color(255,155,255,75);
	public static final Color JITTER_UNDER_COLOR = new Color(255,155,255,155);

	
	public static float getRange(ShipAPI ship) {
		if (ship == null) return MINE_RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(MINE_RANGE);
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		//boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        float amount = 0;
        if (!engine.isPaused()) {
            amount = engine.getElapsedInLastFrame();
        }
        
        time += amount;
		
		String key = dpl_MoteCount_DATA_KEY + "_" + ship.getId();
		dpl_MoteCountData data = (dpl_MoteCountData) Global.getCombatEngine().getCustomData().get(key);
		
		if (data == null) return;
		
		float jitterLevel = effectLevel;
		
		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
		
		if (state == State.IN) {
			if (time >= fireThreshold) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_orchestra_system") && data.motes > 0) {
						w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
						w.setForceFireOneFrame(true);
						data.motes -= 1;
						ship.getFluxTracker().increaseFlux(300f, false);
					}
				}
				fireThreshold += 0.5f;
			}
		} else if (effectLevel >= 1) {
			if (time >= fireThreshold) {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_orchestra_system") && data.motes > 0) {
						w.setGlowAmount(effectLevel, w.getSpec().getGlowColor());
						w.setForceFireOneFrame(true);
						data.motes -= 1;
						ship.getFluxTracker().increaseFlux(300f, false);
					}
				}
				fireThreshold += 0.5f;
			} else {
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.isDecorative() && w.getSpec().hasTag("dpl_orchestra_system")) {
						w.setForceNoFireOneFrame(true);
					}
				}
			}
		} else if (state == State.OUT ) {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.isDecorative() && w.getSpec().hasTag("dpl_orchestra_system")) {
					w.setForceNoFireOneFrame(true);
					w.setGlowAmount(0, null);
					jitterLevel *= jitterLevel;
				}
			}
		}
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		time = 0f;
		fireThreshold = 0.5f;
	}

	
	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.getState() != SystemState.IDLE) return null;
		
		String key = dpl_MoteCount_DATA_KEY + "_" + ship.getId();
		dpl_MoteCountData data = (dpl_MoteCountData) Global.getCombatEngine().getCustomData().get(key);
		
		if (data == null) return null;
		
		Vector2f target = ship.getMouseTarget();
		if (target != null) {
			int motes = data.motes;
			if (motes == 0) {
				return "NO ANOMALY AVAILABLE";
			} else {
				return "READY";
			}
		}
		return null;
	}

	
	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		boolean hasTarget = ship.getMouseTarget() != null;
		boolean hasMoteCount = ship.getVariant().hasHullMod("dpl_mote_count");
		return (hasTarget && hasMoteCount);
	}
}








