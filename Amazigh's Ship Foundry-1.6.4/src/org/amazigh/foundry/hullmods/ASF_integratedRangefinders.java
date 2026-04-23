package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

import org.magiclib.util.MagicIncompatibleHullmods;

public class ASF_integratedRangefinders extends BaseHullMod {
	
	public static float BONUS_SMALL = 300;
	public static float BONUS_MEDIUM = 200;
	public static float BONUS_LARGE = 100;
	
	public static float MAX_RANGE = 1000;
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new IntegratedRangefinderRangeModifier());
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		if(stats.getVariant().getHullMods().contains("ballistic_rangefinder")){
			//if someone tries to install BRF, remove it
			MagicIncompatibleHullmods.removeHullmodWithWarning(
					stats.getVariant(),
					"ballistic_rangefinder",
					"A_S-F_integratedRangefinders"
					);	
		}
	}
	
	public static class IntegratedRangefinderRangeModifier implements WeaponBaseRangeModifier {
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getType() == WeaponType.BALLISTIC) {
				
				float bonus = 0;
				
				if (weapon.getSize() == WeaponSize.SMALL) {
					bonus = BONUS_SMALL;
				}
				if (weapon.getSize() == WeaponSize.MEDIUM) {
					bonus = BONUS_MEDIUM;
				}
				if (weapon.getSize() == WeaponSize.LARGE) {
					bonus = BONUS_LARGE;
				}
				if (weapon.hasAIHint(AIHints.PD)) {
					bonus *= 0.5f;
				}
				float base = weapon.getSpec().getMaxRange();
				
				if (base + bonus > MAX_RANGE) {
					bonus = MAX_RANGE - base;
				}
				if (bonus < 0) bonus = 0;
				return bonus;
				
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
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("Utilizes targeting data from an integrated targeting system to extend the base range of typical ballistic weapons.", opad, h, "base range");
		tooltip.addPara("The range bonus is capped, but still subject to other modifiers.", opad);
		
		
		tooltip.addSectionHeading("Ballistic weapon range", Alignment.MID, opad);
		tooltip.addPara("Affects ballistic weapons of all sizes.", opad);
		
		float col1 = 86;
		float col2 = 99;
		float col3 = 86;
		float col4 = 85;
		
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				20f, true, true, 
				new Object [] {"Small wpn", col1, "Medium wpn", col2, "Large wpn", col3, "Range cap", col4});
		
		tooltip.addRow(Alignment.MID, h, "+" + (int) BONUS_SMALL,
				Alignment.MID, h, "+" + (int) BONUS_MEDIUM,
				Alignment.MID, h, "+" + (int) BONUS_LARGE,
				Alignment.MID, h, "" + (int)MAX_RANGE);
		tooltip.addTable("", 0, opad);
		
		
		tooltip.addSectionHeading("Point-defense weapon range", Alignment.MID, opad);		
		tooltip.addPara("Ballistic Point-defense weapons recieve a reduced range bonus.", opad);
		
		tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
				20f, true, true, 
				new Object [] {"Small wpn", col1, "Medium wpn", col2, "Large wpn", col3, "Range cap", col4});
		
		tooltip.addRow(Alignment.MID, h, "+" + (int) (BONUS_SMALL * 0.5f),
				Alignment.MID, h, "+" + (int) (BONUS_MEDIUM * 0.5f),
				Alignment.MID, h, "+" + (int) (BONUS_LARGE * 0.5f),
				Alignment.MID, h, "" + (int)MAX_RANGE);
		tooltip.addTable("", 0, opad);
		
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this range modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
		
		tooltip.addPara("The standard style of %s is incompatible with this integrated system and as such cannot be installed on this vessel.", opad, bad, "Ballistic Rangefinder");
		
	}
	
	public float getTooltipWidth() {
		return 375f;	
	}
	
	
}
