package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_ResonanceMotivatorStats extends BaseShipSystemScript {

	private CombatEngineAPI engine;
	public static final float FLUX_REDUCTION = 0.5f;
	public static final float DAM_RES = 0.25f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (engine != Global.getCombatEngine()) {
            engine = Global.getCombatEngine();
        }
		ShipAPI ship = (ShipAPI)stats.getEntity();
		
		
		// -50% weapon flux cost
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * effectLevel));
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * effectLevel));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * effectLevel));
		
		// damage resist
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (DAM_RES * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAM_RES * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAM_RES * effectLevel));
		
		// ship jitter
		float ALPHA = 25f + (45f * effectLevel);
		Color JITTER_UNDER_COLOR = new Color(128,26,42,(int)ALPHA);
		
		float jitterRangeBonus = 12f * (1f + effectLevel);
		float jitterLevel = (float) Math.sqrt(effectLevel);
		
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 23, 0f, 12f + jitterRangeBonus);
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);

		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float displayBonus = (int) ((FLUX_REDUCTION * effectLevel) * 100f);
		
		if (index == 0) {
			return new StatusData("Weapon Flux Costs Reduced by " + (int) displayBonus + "%", false);
		}
		return null;
	}
}
