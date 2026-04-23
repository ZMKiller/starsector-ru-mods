package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class ASF_energyBoltConverger extends BaseHullMod {
	
	public static float BONUS_SMALL = 200;
	public static float BONUS_MEDIUM = 100;
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new EnergyRangefinderRangeModifier());
	}
	
	public static class EnergyRangefinderRangeModifier implements WeaponBaseRangeModifier {
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.isBeam()) return 0f;
			if (weapon.getType() == WeaponType.ENERGY || weapon.getType() == WeaponType.HYBRID) {
				
				if (weapon.getSize() == WeaponSize.SMALL) {
					return BONUS_SMALL;
				}
				if (weapon.getSize() == WeaponSize.MEDIUM) {
					return BONUS_MEDIUM;
				}
			}
			return 0f;
			
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
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		
		tooltip.addPara("A prototype system that enhances field convergence of the ship's smaller energy weapons.",	opad);
		
		LabelAPI label = tooltip.addPara("Increases the base range of all %s / %s non-beam Energy and Hybrid weapons by %s / %s.", opad, h, "Small", "Medium", "" + (int)BONUS_SMALL, "" + (int)BONUS_MEDIUM);
		label.setHighlight("Small", "Medium", "" + (int)BONUS_SMALL, "" + (int)BONUS_MEDIUM);
		label.setHighlightColors(h, h, h, h);
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this range modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
	}
	
	
}
