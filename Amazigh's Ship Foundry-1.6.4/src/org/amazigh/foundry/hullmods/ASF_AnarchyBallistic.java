package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_AnarchyBallistic extends BaseHullMod {
	
	public static final float MAINT_MALUS = 10f;
	
	public static final float RoF_BONUS = 15f;
	public static final float RoF_BONUS_S = 20f;
	public static final float REGEN_BONUS = 10f;
	public static final float REGEN_BONUS_S = 15f;
	
	public static final float RECOIL_MALUS = 30f;
	public static final float RECOIL_MALUS_S = 15f;
	public static final float TURRET_TURN_MALUS = 40f;
	public static final float TURRET_TURN_MALUS_S = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getSuppliesToRecover().modifyPercent(id, MAINT_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, MAINT_MALUS);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			
			stats.getBallisticRoFMult().modifyMult(id, 1f + (RoF_BONUS_S * 0.01f));
			stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + (REGEN_BONUS_S * 0.01f));

			stats.getMaxRecoilMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS_S));
			stats.getRecoilPerShotMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS_S));
			stats.getRecoilDecayMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS_S));
			stats.getWeaponTurnRateBonus().modifyMult(id, 1f - (TURRET_TURN_MALUS_S * 0.01f));
		} else {
			stats.getBallisticRoFMult().modifyMult(id, 1f + (RoF_BONUS * 0.01f));
			stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + (REGEN_BONUS * 0.01f));
			
			stats.getMaxRecoilMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS));
			stats.getRecoilPerShotMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS));
			stats.getRecoilDecayMult().modifyMult(id, 1f + (0.01f * RECOIL_MALUS));
			stats.getWeaponTurnRateBonus().modifyMult(id, 1f - (TURRET_TURN_MALUS * 0.01f));
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
		
		LabelAPI label = tooltip.addPara("Removes safety interlinks on ballistic ammunition loaders, this provides a modest increase to weapon fire rate, at the cost of compromising recoil handling and turret traverse, alongside a significantly increased requirement for replacement parts on the vessels munitions feeds.", pad);

		label = tooltip.addPara("Ballistic weapon Rate of Fire increased by %s.", opad, h, "" + (int)RoF_BONUS + "%");
		label.setHighlight("" + (int)RoF_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Ballistic weapon Ammo Regeneration increased by %s.", pad, h, "" + (int)REGEN_BONUS + "%");
		label.setHighlight("" + (int)REGEN_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Deployment cost and supply costs increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Recoil of all weapons increased by %s.", pad, bad, "" + (int)RECOIL_MALUS + "%");
		label.setHighlight("" + (int)RECOIL_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Weapon turn rate decreased by %s.", pad, bad, "" + (int)TURRET_TURN_MALUS + "%");
		label.setHighlight("" + (int)TURRET_TURN_MALUS + "%");
		label.setHighlightColors(bad);
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)RoF_BONUS_S + "%";
		if (index == 1) return "" + (int)REGEN_BONUS_S + "%";
		if (index == 2) return "" + (int)RECOIL_MALUS_S + "%";
		if (index == 3) return "" + (int)TURRET_TURN_MALUS_S + "%";
		return null;
	}
	
	
}
