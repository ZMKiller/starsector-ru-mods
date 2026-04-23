package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_AnarchyTargeting extends BaseHullMod {
	
	public static final float MAINT_MALUS = 10f;
	
	public static final float RANGE_BONUS = 15f;
	public static final float RANGE_BONUS_S = 20f;
	
	public static final float DISS_MALUS = 10f;
	public static final float DISS_MALUS_S = 5f;
	public static final float PPT_MALUS = 15f;
	public static final float PPT_MALUS_S = 7.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getSuppliesToRecover().modifyPercent(id, MAINT_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, MAINT_MALUS);

		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_S);
			stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_S);
			stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS); // you get +5% PD range if s-modded, but i'm not advertising it because it's funnier not to.
			stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);

			stats.getFluxDissipation().modifyMult(id, 1f - (DISS_MALUS_S * 0.01f));
			stats.getPeakCRDuration().modifyMult(id, 1f - (PPT_MALUS_S * 0.01f));
		} else {
			stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
			stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
			stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);
			stats.getBeamPDWeaponRangeBonus().modifyPercent(id, -RANGE_BONUS);

			stats.getFluxDissipation().modifyMult(id, 1f - (DISS_MALUS * 0.01f));
			stats.getPeakCRDuration().modifyMult(id, 1f - (PPT_MALUS * 0.01f));
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
		
		LabelAPI label = tooltip.addPara("While it is not advised to do so, a direct feed from the ship's flux core proves more than adequate in improving the output of targeting systems, albeit with some consequences with regards to reliability margins.", pad);

		label = tooltip.addPara("Extends the range of ballistic and energy weapons by %s.", opad, h, "" + (int)RANGE_BONUS + "%");
		label.setHighlight("" + (int)RANGE_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("This bonus does not apply to point-defense weapons.", pad);
		
		label = tooltip.addPara("Deployment cost and supply costs increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Flux dissipation rate reduced by %s.", pad, bad, "" + (int)DISS_MALUS + "%");
		label.setHighlight("" + (int)DISS_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Peak performance time is reduced by %s.", pad, bad, "" + (int)PPT_MALUS + "%");
		label.setHighlight("" + (int)PPT_MALUS + "%");
		label.setHighlightColors(bad);
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)RANGE_BONUS_S + "%";
		if (index == 1) return "" + (int)DISS_MALUS_S + "%";
		if (index == 2) return "" + (double)PPT_MALUS_S + "%";
		return null;
	}

}
