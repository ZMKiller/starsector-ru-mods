package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class sfc_talariahullmod extends BaseHullMod {

    public static final float WEP_RANGE = 50f;
    public static final float PROJ_SPEED = 1.2f;
    public static final float SMOD_BOOST = 1.15f;

    private static String sfc_talariahullmodDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
    private static String sfc_talariahullmodText1 = Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText1");
    private static String sfc_talariahullmodText2 = Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText2");
    private static String sfc_talariahullmodText3 = Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText3");
    private static String sfc_talariahullmodText4 = Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText4");

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipVariantAPI variant = stats.getVariant();

        boolean sMod = isSMod(stats);
        float mult1 = 1f;
        float mult2 = 1f;
        if (variant != null && variant.hasHullMod("converted_fighterbay")) {
            mult1 = 1.2f;
            mult2 = 0.8f;
        }
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, WEP_RANGE);
        stats.getEnergyProjectileSpeedMult().modifyMult(id, PROJ_SPEED);
        stats.getEnergyRoFMult().modifyMult(id, mult1);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult2);

        if (sMod) {
        stats.getEnergyWeaponDamageMult().modifyMult(id, SMOD_BOOST);
        }
    }
    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color red = new Color(241, 50, 50,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading(sfc_talariahullmodDetails, Alignment.MID, pad);
        tooltip.addPara(sfc_talariahullmodText1, padList, Misc.getHighlightColor(),"+50");
        tooltip.addPara(sfc_talariahullmodText2, pad2, Misc.getHighlightColor(),"+20%");
        tooltip.addPara(sfc_talariahullmodText3, pad2, Misc.getHighlightColor(),"20%");
        tooltip.addPara(sfc_talariahullmodText4, pad2, Misc.getHighlightColor(),"-20%");
        tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText5") }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_talariahullmodText6") });
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
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "+" + (int) ((1f - (SMOD_BOOST)) * 100f) + "%";
        return null;
    }
}

