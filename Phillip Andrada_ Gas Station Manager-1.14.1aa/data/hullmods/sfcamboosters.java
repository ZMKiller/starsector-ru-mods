package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class sfcamboosters extends BaseHullMod {

	public static final float AMMO_MALUS = 0.75f;
	public static final float MISSILE_STAT_BOOST = 1.25f;
	public static final float SMOD_DAMAGE_BOOST = 1.10f;

	private static String sfc_amBoostersDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
	private static String sfc_amBoosterText1 = Global.getSettings().getString("sfc_pagsm", "sfc_amBoosterText1");
	private static String sfc_amBoosterText2 = Global.getSettings().getString("sfc_pagsm", "sfc_amBoosterText2");
	private static String sfc_amBoosterText3 = Global.getSettings().getString("sfc_pagsm", "sfc_amBoosterText3");
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();

		boolean sMod = isSMod(stats);
		float mult = 1f;
		if (variant != null && variant.hasHullMod("missleracks")) {
			mult = AMMO_MALUS;
		}
		stats.getMissileAmmoBonus().modifyMult(id, mult);
		stats.getMissileMaxSpeedBonus().modifyMult(id, MISSILE_STAT_BOOST);
		stats.getMissileAccelerationBonus().modifyMult(id, MISSILE_STAT_BOOST);
		stats.getMissileMaxTurnRateBonus().modifyMult(id, MISSILE_STAT_BOOST);
		stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_STAT_BOOST);

		if (sMod) {
			stats.getMissileWeaponDamageMult().modifyMult(id, SMOD_DAMAGE_BOOST);
		}
	}
	
	/*public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		return null;
	}*/
	@Override
	public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
		final Color green = new Color(55,245,65,255);
		final Color red = new Color(241, 50, 50,255);
		final Color flavor = new Color(110,110,110,255);
		final float pad = 10f;
		final float pad2 = 0f;
		float padList = 6f;
		final float padSig = 1f;
		tooltip.addSectionHeading(sfc_amBoostersDetails, Alignment.MID, pad);
		tooltip.addPara(sfc_amBoosterText1, padList, Misc.getHighlightColor(),"+25%");
		tooltip.addPara(sfc_amBoosterText2, pad2, Misc.getHighlightColor(),"+25%");
		tooltip.addPara(sfc_amBoosterText3, pad2, Misc.getHighlightColor(),"-25%");
		tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_amBoosterText4") }).italicize();
		tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_amBoosterText5") });
		/*tooltip.addPara(
				"• Increased Missile Maneuverability Bonus: %s"
						+ "\n• Increased Missile Range: %s",
				pad, green, new String[]{
						Misc.getRoundedValue(25.0f) + "%",
						Misc.getRoundedValue(25.0f) + "%",
				}
		);
		tooltip.addPara(
				"• Ammo Decrease If Expanded Missile Racks Is Installed: %s",
				pad2, red, new String[]{
						"-" + Misc.getRoundedValue(25.0f) + "%",
				}
		);
		tooltip.addPara("%s", padList, flavor, new String[] { "\"Sindrian-Brand Antimatter Fuel burns the best for all things! Ensure you have the best fuel today!\"" }).italicize();
		tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Sindrian Fuel Company Advert" });*/
	}
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return (int) (((SMOD_DAMAGE_BOOST) - 1f) * 100f) + "%";
		return null;
	}
}
