package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

public class dpl_PhaseIntegration extends BaseHullMod {

	public static float PHASE_COOLDOWN_REDUCTION = 90f;
	public static float PHASE_DISSIPATION_MULT = 2f;
	public static float ACTIVATION_COST_MULT = 0f;
	
	public static class dpl_PhaseIntegrationScript implements AdvanceableListener {
		public ShipAPI ship;
		String id = "dpl_phase_integration_modifier";
		public dpl_PhaseIntegrationScript(ShipAPI ship) {
			this.ship = ship;
		}
		
		public void advance(float amount) {
			boolean phased = ship.isPhased();
			if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isChargedown()) {
				phased = false;
			}
			
			MutableShipStatsAPI stats = ship.getMutableStats();
			if (phased) {
				stats.getFluxDissipation().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getBallisticRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getEnergyRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getMissileRoFMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getBallisticAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getEnergyAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
				stats.getMissileAmmoRegenMult().modifyMult(id, PHASE_DISSIPATION_MULT);
			} else {
				stats.getFluxDissipation().unmodifyMult(id);
				stats.getBallisticRoFMult().unmodifyMult(id);
				stats.getEnergyRoFMult().unmodifyMult(id);
				stats.getMissileRoFMult().unmodifyMult(id);
				stats.getBallisticAmmoRegenMult().unmodifyMult(id);
				stats.getEnergyAmmoRegenMult().unmodifyMult(id);
				stats.getMissileAmmoRegenMult().unmodifyMult(id);
			}
		}

	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new dpl_PhaseIntegrationScript(ship));
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - PHASE_COOLDOWN_REDUCTION / 100f);
		stats.getPhaseCloakActivationCostBonus().modifyMult(id, 0f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(PHASE_COOLDOWN_REDUCTION) + "%";
		if (index == 1) return "zero";
		if (index == 2) return "" + (int)PHASE_DISSIPATION_MULT;
		return null;
	}

}





