package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_BeamCoherer extends BaseHullMod {

	public static float RANGE_BONUS = 40f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new EnergyBoltCohererRangeModifier());
	}
	
	public static class EnergyBoltCohererRangeModifier implements WeaponBaseRangeModifier {
		public EnergyBoltCohererRangeModifier() {
		}
		
		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.isBeam()) {
				return 1f + (RANGE_BONUS * 0.01f);
			}
			return 1f;
		}
		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
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
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		
		tooltip.addPara("Designed by the Phase Lab for use on Baritone class Light Cruiser, "
				+ "this special device greatly alleviates dispersion of beam weapons.", opad);
		tooltip.addPara("Increases the base range of all beam Energy and Hybrid weapons by %s.", opad, h,
				"" + (float)RANGE_BONUS + "%");
		
		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
		tooltip.addPara("Since the base range is increased, this range modifier"
				+ " - unlike most other flat modifiers in the game - "
				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
	}
	
//	@Override
//	public boolean isApplicableToShip(ShipAPI ship) {
//		return getUnapplicableReason(ship) == null;
//	}
//	
//	public String getUnapplicableReason(ShipAPI ship) {
//		if (ship != null && 
//				ship.getHullSize() != HullSize.CAPITAL_SHIP && 
//				ship.getHullSize() != HullSize.DESTROYER && 
//				ship.getHullSize() != HullSize.CRUISER) {
//			return "Can only be installed on destroyer-class hulls and larger";
//		}
//		return null;
//	}
	
}









