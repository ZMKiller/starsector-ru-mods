package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_floggerMounts extends BaseHullMod {

	public static final float M_RATE_BONUS = 50f;
	public static final float M_FLUX_BONUS = 50f;
	public static final float M_AMMO_BONUS = 50f;
	
	public static final float B_AMMO_BONUS = 100f;
	public static final float B_RATE_BONUS = 40f;
	
	public static final float O_RATE_BONUS_B = 35f;
	public static final float O_FLUX_BONUS = 30f;
	public static final float O_RATE_BONUS_M = 25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		if (stats.getVariant().getWeaponSpec("WS0001") != null) {
			if (stats.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.MISSILE) {
				stats.getBallisticRoFMult().modifyMult(id, 1f + (M_RATE_BONUS * 0.01f));
				stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + (M_RATE_BONUS * 0.01f));
				stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (M_FLUX_BONUS * 0.01f));
				stats.getBallisticAmmoBonus().modifyPercent(id, M_AMMO_BONUS);
			} else if (stats.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.BALLISTIC) {
				stats.getMissileAmmoBonus().modifyPercent(id, B_AMMO_BONUS);
				stats.getMissileRoFMult().modifyMult(id, 1f + (B_RATE_BONUS * 0.01f));
				stats.getMissileAmmoRegenMult().modifyMult(id, 1f + (B_RATE_BONUS * 0.01f));
			} else {
				stats.getBallisticRoFMult().modifyMult(id, 1f + (O_RATE_BONUS_B * 0.01f));
				stats.getBallisticAmmoRegenMult().modifyMult(id, 1f + (O_RATE_BONUS_B * 0.01f));
				stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (O_FLUX_BONUS * 0.01f));
				stats.getMissileRoFMult().modifyMult(id, 1f + (O_RATE_BONUS_M * 0.01f));
			}
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
		Color grey = Misc.getGrayColor();
		
		Color banner = new Color(21,64,77);
		
		Color ball = Misc.getBallisticMountColor();
		Color miss = Misc.getMissileMountColor();
		Color other = Misc.MOUNT_UNIVERSAL;
		
		LabelAPI label = tooltip.addPara("Redundant systems in the ships medium mount are able to be diverted to boost the performance of other installed weapons depending on the type of the installed weapon.", pad);
		
		//"Redundant systems in the ships medium mount are able to be diverted to boost performance of any other installed weapons that are not of the same weapon type."
		//"Redundant systems in the ships medium mount are able to be diverted to boost the performance of other installed weapons depending on the type of the installed weapon."
		
		if (!Global.CODEX_TOOLTIP_MODE) {
			if (ship.getVariant().getWeaponSpec("WS0001") != null) {
				if (ship.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.MISSILE) {
					tooltip.addSectionHeading("Currently installed: Missile", miss, banner, Alignment.MID, opad);
					label = tooltip.addPara("Ballistic weapon Rate of Fire and Ammo Regeneration increased by %s.", opad, h, "" + (int)M_RATE_BONUS + "%");
					label.setHighlight("" + (int)M_RATE_BONUS + "%");
					label.setHighlightColors(h);
					label = tooltip.addPara("Ballistic weapon flux cost reduced by %s.", pad, h, "" + (int)M_FLUX_BONUS + "%");
					label.setHighlight("" + (int)M_FLUX_BONUS + "%");
					label.setHighlightColors(h);
					label = tooltip.addPara("Ballistic weapon Ammo Capacity increased by %s.", pad, h,  "" + (int)M_AMMO_BONUS + "%");
					label.setHighlight("" + (int)M_AMMO_BONUS + "%");
					label.setHighlightColors(h);
				} else if (ship.getVariant().getWeaponSpec("WS0001").getType() == WeaponAPI.WeaponType.BALLISTIC) {
					tooltip.addSectionHeading("Currently installed: Ballistic", ball, banner, Alignment.MID, opad);
					label = tooltip.addPara("Missile weapon Ammo Capacity increased by %s.", opad, h, "" + (int)B_AMMO_BONUS + "%");
					label.setHighlight("" + (int)B_AMMO_BONUS + "%");
					label.setHighlightColors(h);
					label = tooltip.addPara("Missile weapon Rate of Fire and Ammo Regeneration increased by %s.", pad, h, "" + (int)B_RATE_BONUS + "%");
					label.setHighlight("" + (int)B_RATE_BONUS + "%");
					label.setHighlightColors(h);
				} else {
					tooltip.addSectionHeading("Currently installed: Other", other, banner, Alignment.MID, opad);
					label = tooltip.addPara("Ballistic weapon Rate of Fire and Ammo Regeneration increased by %s.", opad, h, "" + (int)O_RATE_BONUS_B + "%");
					label.setHighlight("" + (int)O_RATE_BONUS_B + "%");
					label.setHighlightColors(h);
					label = tooltip.addPara("Ballistic weapon flux cost reduced by %s.", pad, h, "" + (int)O_FLUX_BONUS + "%");
					label.setHighlight("" + (int)O_FLUX_BONUS + "%");
					label.setHighlightColors(h);
					label = tooltip.addPara("Missile weapon Rate of Fire and Ammo Regeneration increased by %s.", pad, h, "" + (int)O_RATE_BONUS_M + "%");
					label.setHighlight("" + (int)O_RATE_BONUS_M + "%");
					label.setHighlightColors(h);
				}
				
			} else {
				tooltip.addSectionHeading("Currently installed: None", grey, banner, Alignment.MID, opad);
				label = tooltip.addPara("Install a weapon in the vessels medium mount to recieve a bonus.", opad);
			}
		}
	}

}
