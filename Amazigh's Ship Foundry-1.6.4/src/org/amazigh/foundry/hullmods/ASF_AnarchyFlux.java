package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_AnarchyFlux extends BaseHullMod {

	public static final float MAINT_MALUS = 10f;
	
	public static final float DISS_BONUS = 15f;
	public static final float DISS_BONUS_S = 20f;
	
	public static final float ARMOUR_MALUS = 10f;
	public static final float ARMOUR_MALUS_S = 5f;
	public static final float EMP_MALUS = 50f;
	public static final float EMP_MALUS_S = 25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getSuppliesToRecover().modifyPercent(id, MAINT_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, MAINT_MALUS);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getFluxDissipation().modifyMult(id, 1f + (DISS_BONUS_S * 0.01f));
			
			stats.getArmorBonus().modifyMult(id, 1f - (0.01f * ARMOUR_MALUS_S));
			stats.getEmpDamageTakenMult().modifyMult(id, 1f + (EMP_MALUS_S * 0.01f));
		} else {
			stats.getFluxDissipation().modifyMult(id, 1f + (DISS_BONUS * 0.01f));
			
			stats.getArmorBonus().modifyMult(id, 1f - (0.01f * ARMOUR_MALUS));
			stats.getEmpDamageTakenMult().modifyMult(id, 1f + (EMP_MALUS * 0.01f));
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
		
		LabelAPI label = tooltip.addPara("External plating is removed from flux radiators alongside the disabling of a variety of safety measures. This allows improved venting of excess flux in exchange for notably reduced safety margins.", pad);

		label = tooltip.addPara("Flux dissipation rate increased by %s.", opad, h, "" + (int)DISS_BONUS + "%");
		label.setHighlight("" + (int)DISS_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Deployment cost and supply costs increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Armor rating reduced by %s.", pad, bad, "" + (int)ARMOUR_MALUS + "%");
		label.setHighlight("" + (int)ARMOUR_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("EMP damage taken increased by %s.", pad, bad, "" + (int)EMP_MALUS + "%");
		label.setHighlight("" + (int)EMP_MALUS + "%");
		label.setHighlightColors(bad);
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)DISS_BONUS_S + "%";
		if (index == 1) return "" + (int)ARMOUR_MALUS_S + "%";
		if (index == 2) return "" + (int)EMP_MALUS_S + "%";
		return null;
	}

}
