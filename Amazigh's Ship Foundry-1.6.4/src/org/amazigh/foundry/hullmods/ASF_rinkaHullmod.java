package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_rinkaHullmod extends BaseHullMod {

	public static final float RECOIL_BONUS = 20f;
	public static final float RECOIL_BONUS_SO = 10f;
	
	public static final float DAMAGE_BONUS = 15f;
	public static final float VELOCITY_BONUS = 25f;
	public static final float VELOCITY_BONUS_SO = 10f;
	public static final float RANGE_BONUS = 10f;
	
	public static final float ROF_BONUS = 60f;
	public static final float FLUX_BONUS = 40f;
	public static final float ROF_BONUS_SO = 35f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		// lol, lmao.
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("RINKA_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		MutableShipStatsAPI stats = ship.getMutableStats();
        CombatEngineAPI engine = Global.getCombatEngine();	
		
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
			stats.getMaxRecoilMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS_SO));
			stats.getRecoilPerShotMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS_SO));
			stats.getRecoilDecayMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS_SO));
			stats.getBallisticProjectileSpeedMult().modifyPercent(spec.getId(), VELOCITY_BONUS_SO);
			stats.getBallisticRoFMult().modifyPercent(spec.getId(), ROF_BONUS_SO);
			stats.getBallisticAmmoRegenMult().modifyPercent(spec.getId(), (ROF_BONUS_SO * 0.5f) );
		} else {
			float timeModifier_in = 0f;
			float timeModifier_out = 0f;
			
			if (ship.getSystem().isActive()) {
				if (info.TIMER_IN > 0f) {
					info.TIMER_IN -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();	
				}
				if (info.TIMER_OUT < 1f) {
					info.TIMER_OUT += engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();	
				}
				if (info.TIMER_OUT > 1f) {
					info.TIMER_OUT = 1f;
				}
			} else {
				if (info.TIMER_OUT > 0f) {
					info.TIMER_OUT -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();	
				}
				if (info.TIMER_IN < 1f) {
					info.TIMER_IN += engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();	
				}
				if (info.TIMER_IN > 1f) {
					info.TIMER_IN = 1f;
				}
			}
			timeModifier_in = 1f - (Math.max(info.TIMER_IN, 0f));
			timeModifier_out = 1f - (Math.max(info.TIMER_OUT, 0f));
			
			stats.getBallisticRoFMult().modifyPercent(spec.getId(), ROF_BONUS * timeModifier_in);
			stats.getBallisticAmmoRegenMult().modifyPercent(spec.getId(), ROF_BONUS * timeModifier_in);
			stats.getBallisticWeaponFluxCostMod().modifyPercent(spec.getId(), -(FLUX_BONUS * timeModifier_in));

			stats.getBallisticWeaponRangeBonus().modifyPercent(spec.getId(), RANGE_BONUS * timeModifier_out);
			stats.getBallisticWeaponDamageMult().modifyPercent(spec.getId(), DAMAGE_BONUS * timeModifier_out);
			stats.getBallisticProjectileSpeedMult().modifyPercent(spec.getId(), VELOCITY_BONUS * timeModifier_out);
			stats.getMaxRecoilMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
			stats.getRecoilPerShotMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
			stats.getRecoilDecayMult().modifyMult(spec.getId(), 1f - (0.01f * RECOIL_BONUS));
		}
        engine.getCustomData().put("RINKA_DATA_KEY" + ship.getId(), info);
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
		
		boolean CodexMode = false;
		if (Global.CODEX_TOOLTIP_MODE) {
			CodexMode = true;
		}
		// An Advanced array of hardware that enhances the performance of ballistic weapons has been installed on this vessel.
		// Features Accelerator Rails that are active by default, but shut down when Lynx Jets are active and divert power to the Assault Coils.
		LabelAPI label = tooltip.addPara("An Advanced array of hardware that enhances the performance of ballistic weapons has been installed on this vessel.", pad);
		

		if (!CodexMode) {
			if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
				label = tooltip.addPara("Due to the installation of %s this hardware is operating in an overclocked mode and grants the following bonuses.", pad, bad, "Safety Overrides");
				label.setHighlight("Safety Overrides");
				label.setHighlightColors(bad);
				
				label = tooltip.addPara("Increases ballistic weapon rate of fire by %s.", opad, h, "" + (int)ROF_BONUS_SO + "%");
				label.setHighlight("" + (int)ROF_BONUS_SO + "%");
				label.setHighlightColors(h);
				label = tooltip.addPara("Reduces weapon recoil by %s.", pad, h, "" + (int)RECOIL_BONUS_SO + "%");
				label.setHighlight("" + (int)RECOIL_BONUS_SO + "%");
				label.setHighlightColors(h);
				label = tooltip.addPara("Increases ballistic projectile velocity by %s.", pad, h, "" + (int)VELOCITY_BONUS_SO + "%");
				label.setHighlight("" + (int)VELOCITY_BONUS_SO + "%");
				label.setHighlightColors(h);
				CodexMode = false;
			} else {
				CodexMode = true;
			}
		}
		
		if (CodexMode) {
			label = tooltip.addPara("Features %s that are active by default, but shut down when %s are active and divert power to the %s.", pad, h, "Accelerator Rails", "Lynx Jets", "Assault Coils");
			label.setHighlight("Accelerator Rails", "Lynx Jets", "Assault Coils");
			label.setHighlightColors(h, h, h);
			
			tooltip.addSectionHeading("Accelerator Rails:", Alignment.MID, opad);
			label = tooltip.addPara("Reduces weapon recoil by %s.", opad, h, "" + (int)RECOIL_BONUS + "%");
			label.setHighlight("" + (int)RECOIL_BONUS + "%");
			label.setHighlightColors(h);
			label = tooltip.addPara("Increases ballistic weapon damage by %s.", pad, h, "" + (int)DAMAGE_BONUS + "%");
			label.setHighlight("" + (int)DAMAGE_BONUS + "%");
			label.setHighlightColors(h);
			label = tooltip.addPara("Extends the range of ballistic weapons by %s.", pad, h, "" + (int)RANGE_BONUS + "%");
			label.setHighlight("" + (int)RANGE_BONUS + "%");
			label.setHighlightColors(h);
			label = tooltip.addPara("Increases ballistic projectile velocity by %s.", pad, h, "" + (int)VELOCITY_BONUS + "%");
			label.setHighlight("" + (int)VELOCITY_BONUS + "%");
			label.setHighlightColors(h);
			
			tooltip.addSectionHeading("Assault Coils:", Alignment.MID, opad);
			label = tooltip.addPara("Increases ballistic weapon rate of fire by %s.", opad, h, "" + (int)ROF_BONUS + "%");
			label.setHighlight("" + (int)ROF_BONUS + "%");
			label.setHighlightColors(h);
			label = tooltip.addPara("Reduces the flux cost of ballistic weapons by %s.", pad, h, "" + (int)FLUX_BONUS + "%");
			label.setHighlight("" + (int)FLUX_BONUS + "%");
			label.setHighlightColors(h);
			label = tooltip.addPara("Reduces weapon recoil by %s.", pad, h, "" + (int)RECOIL_BONUS + "%");
			label.setHighlight("" + (int)RECOIL_BONUS + "%");
			label.setHighlightColors(h);
			
			label = tooltip.addPara("Installation of %s results in altered performance of this hardware.", opad, bad, "Safety Overrides");
			label.setHighlight("Safety Overrides");
			label.setHighlightColors(bad);
		}
	}

    private class ShipSpecificData {
        private float TIMER_IN = 0f;
        private float TIMER_OUT = 0f;
    }
	
}
