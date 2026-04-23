package org.amazigh.foundry.hullmods;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import org.magiclib.util.MagicIncompatibleHullmods;

public class ASF_MaliceCoils extends BaseHullMod {
	
	public static float PHASE_DISSIPATION_MULT = 2f; // ship (part of) phase anchor (no emergency dive, but other buffs remain)
	public static float FLUX_THRESHOLD_INCREASE_PERCENT = 80f; // and super adaptive phase coils
	public static float PHASE_COOLDOWN_REDUCTION = 50f; // shorter phase cooldown, so you can flicker it more effectively (as you only really want to flicker it, based on the high upkeep)
		// but you have Malice Storm.... so :thinking:
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getDynamic().getMod(Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyPercent(id, FLUX_THRESHOLD_INCREASE_PERCENT);
		
		stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - (PHASE_COOLDOWN_REDUCTION / 100f));
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		if(stats.getVariant().getHullMods().contains("phase_anchor")){
			//if someone tries to install phase anchor, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"phase_anchor",
					"A_S-F_MaliceCoils"
					);	
		}
		if(stats.getVariant().getHullMods().contains("adaptive_coils")){
			//if someone tries to install adaptive phase coils, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"adaptive_coils",
					"A_S-F_MaliceCoils"
					);	
		}
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){

		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        MutableShipStatsAPI stats = ship.getMutableStats();
        
        boolean phased = ship.isPhased();
		if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isChargedown()) {
			phased = false;
		}
		
		if (phased) {
			stats.getFluxDissipation().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getBallisticRoFMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getEnergyRoFMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getMissileRoFMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getBallisticAmmoRegenMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getEnergyAmmoRegenMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
			stats.getMissileAmmoRegenMult().modifyMult(spec.getId(), PHASE_DISSIPATION_MULT);
		} else {
			stats.getFluxDissipation().unmodifyMult(spec.getId());
			stats.getBallisticRoFMult().unmodifyMult(spec.getId());
			stats.getEnergyRoFMult().unmodifyMult(spec.getId());
			stats.getMissileRoFMult().unmodifyMult(spec.getId());
			stats.getBallisticAmmoRegenMult().unmodifyMult(spec.getId());
			stats.getEnergyAmmoRegenMult().unmodifyMult(spec.getId());
			stats.getMissileAmmoRegenMult().unmodifyMult(spec.getId());
		}        	
	}
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("This vessel features a novel and advanced phase coil geometry that provides several advantages over traditional coils.", pad);
		
		label = tooltip.addPara("The flux threshold at which speed bottoms out while phased is increased to %s.", opad, h, "90%");
		label.setHighlight("90%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Soft flux dissipation and weapon recharge rate is increased by %s while phased.", pad, h, (int)PHASE_DISSIPATION_MULT + Strings.X);
		label.setHighlight((int)PHASE_DISSIPATION_MULT + Strings.X);
		label.setHighlightColors(h);
		label = tooltip.addPara("Phase cloak cooldown is reduced by %s.", pad, h, (int)PHASE_COOLDOWN_REDUCTION + "%");
		label.setHighlight((int)PHASE_COOLDOWN_REDUCTION + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("%s and %s are incompatible with this vessels phase coils and as such cannot be installed on this vessel.", opad, bad, "Phase Anchor", "Adaptive Phase Coils");
		label.setHighlight("Phase Anchor", "Adaptive Phase Coils");
		label.setHighlightColors(bad, bad);
		
	}
	

}
