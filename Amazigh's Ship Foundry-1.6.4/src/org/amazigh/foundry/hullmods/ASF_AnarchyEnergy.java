package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_AnarchyEnergy extends BaseHullMod {

	public static final float MAINT_MALUS = 10f;
	
	public static final float ENERGY_DAM_BONUS = 15f;
	public static final float ENERGY_DAM_BONUS_S = 20f;
	
	public static final float OVERLOAD_MALUS = 20f;	
	public static final float OVERLOAD_MALUS_S = 10f;
	public static final float CAPACITY_MALUS = 10f;
	public static final float CAPACITY_MALUS_S = 5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getSuppliesToRecover().modifyPercent(id, MAINT_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, MAINT_MALUS);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + (ENERGY_DAM_BONUS_S * 0.01f));
			
			stats.getOverloadTimeMod().modifyMult(id, 1f + (0.01f * OVERLOAD_MALUS_S));
			stats.getFluxCapacity().modifyMult(id, 1f - (CAPACITY_MALUS_S * 0.01f));
		} else {
			stats.getEnergyWeaponDamageMult().modifyMult(id, 1f + (ENERGY_DAM_BONUS * 0.01f));
			
			stats.getOverloadTimeMod().modifyMult(id, 1f + (0.01f * OVERLOAD_MALUS));
			stats.getFluxCapacity().modifyMult(id, 1f - (CAPACITY_MALUS * 0.01f));
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
		
		LabelAPI label = tooltip.addPara("The Logistical and Maintenance consequences of feeding excessive power levels though energy weapon power conduits make this an unpopular modification among engineering crews, but the increase in weapons power makes it tempting proposition for some captains.", pad);

		label = tooltip.addPara("Energy weapon damage increased by %s.", opad, h, "" + (int)ENERGY_DAM_BONUS + "%");
		label.setHighlight("" + (int)ENERGY_DAM_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Deployment cost and supply costs increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Overload duration increased by %s.", pad, bad, "" + (int)OVERLOAD_MALUS + "%");
		label.setHighlight("" + (int)OVERLOAD_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Flux capacity reduced by %s.", pad, bad, "" + (int)CAPACITY_MALUS + "%");
		label.setHighlight("" + (int)CAPACITY_MALUS + "%");
		label.setHighlightColors(bad);
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)ENERGY_DAM_BONUS_S + "%";
		if (index == 1) return "" + (int)OVERLOAD_MALUS_S + "%";
		if (index == 2) return "" + (int)CAPACITY_MALUS_S + "%";
		return null;
	}

}
