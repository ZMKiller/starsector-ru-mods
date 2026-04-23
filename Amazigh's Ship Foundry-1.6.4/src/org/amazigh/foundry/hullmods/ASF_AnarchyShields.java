package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_AnarchyShields extends BaseHullMod {

	public static final float MAINT_MALUS = 10f;
	
	public static final float SHIELD_BONUS = 15f;
	public static final float SHIELD_BONUS_S = 20f;
	
	public static final float MANEUVER_MALUS = 20f;
	public static final float MANEUVER_MALUS_S = 10f;
	public static final float SHIELD_MALUS = 40f;
	public static final float SHIELD_MALUS_S = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getSuppliesPerMonth().modifyPercent(id, MAINT_MALUS);
		stats.getSuppliesToRecover().modifyPercent(id, MAINT_MALUS);
		stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, MAINT_MALUS);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS_S * 0.01f);
			
			stats.getMaxSpeed().modifyMult(id, 1f - (MANEUVER_MALUS_S * 0.01f));
			stats.getAcceleration().modifyMult(id, 1f - (MANEUVER_MALUS_S * 0.01f));
			stats.getDeceleration().modifyMult(id, 1f - (MANEUVER_MALUS_S * 0.01f));
			stats.getTurnAcceleration().modifyMult(id, 1f - (MANEUVER_MALUS_S * 0.01f));
			stats.getMaxTurnRate().modifyMult(id, 1f - (MANEUVER_MALUS_S * 0.01f));
			stats.getShieldTurnRateMult().modifyMult(id, 1f - (SHIELD_MALUS_S * 0.01f));
			stats.getShieldUnfoldRateMult().modifyMult(id, 1f - (SHIELD_MALUS_S * 0.01f));
		} else {
			stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
			
			stats.getMaxSpeed().modifyMult(id, 1f - (MANEUVER_MALUS * 0.01f));
			stats.getAcceleration().modifyMult(id, 1f - (MANEUVER_MALUS * 0.01f));
			stats.getDeceleration().modifyMult(id, 1f - (MANEUVER_MALUS * 0.01f));
			stats.getTurnAcceleration().modifyMult(id, 1f - (MANEUVER_MALUS * 0.01f));
			stats.getMaxTurnRate().modifyMult(id, 1f - (MANEUVER_MALUS * 0.01f));
			stats.getShieldTurnRateMult().modifyMult(id, 1f - (SHIELD_MALUS * 0.01f));
			stats.getShieldUnfoldRateMult().modifyMult(id, 1f - (SHIELD_MALUS * 0.01f));
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
		
		LabelAPI label = tooltip.addPara("A simple modification to the safety interlocks on the power intakes for shield emitters, increases their effective defensive performance but interferes with secondary energy feeds, the power intakes also require far more frequent replacement due to increased wear from heightened power levels.", pad);
		
		label = tooltip.addPara("Reduces the amount of damage taken by shields by %s.", opad, h, "" + (int)SHIELD_BONUS + "%");
		label.setHighlight("" + (int)SHIELD_BONUS + "%");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("Deployment cost and supply costs increased by %s.", opad, bad, "" + (int)MAINT_MALUS + "%");
		label.setHighlight("" + (int)MAINT_MALUS + "%");
		label.setHighlightColors(bad);
		
		label = tooltip.addPara("Top speed and maneuverability in combat reduced by %s.", pad, bad, "" + (int)MANEUVER_MALUS + "%");
		label.setHighlight("" + (int)MANEUVER_MALUS + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Shield turn and unfold rate reduced by %s.", pad, bad, "" + (int)SHIELD_MALUS + "%");
		label.setHighlight("" + (int)SHIELD_MALUS + "");
		label.setHighlightColors(bad);
		
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)SHIELD_BONUS_S + "%";
		if (index == 1) return "" + (int)MANEUVER_MALUS_S + "%";
		if (index == 2) return "" + (int)SHIELD_MALUS_S + "%";
		return null;
	}

}
