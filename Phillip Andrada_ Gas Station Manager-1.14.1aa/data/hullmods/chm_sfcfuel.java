package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class chm_sfcfuel extends BaseHullMod {

    public static final float BURN_BONUS = 1;
    public static float FUEL_PERCENT = 10;

    private static String chm_fuelDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
    private static String chm_fuelText1 = Global.getSettings().getString("sfc_pagsm", "chm_fuelText1");
    private static String chm_fuelText2 = Global.getSettings().getString("sfc_pagsm", "chm_fuelText2");

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxBurnLevel().modifyFlat(id, BURN_BONUS);
        stats.getFuelUseMod().modifyPercent(id, -FUEL_PERCENT);
    }

    /*public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BURN_BONUS;
        if (index == 1) return "10%";
        return null;
    }*/

    private final Color color = new Color(222, 109, 158, 255);
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
        ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
    }
    @Override
    public Color getNameColor() {
        return new Color(160,0,95,255);
    }
    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading(chm_fuelDetails, Alignment.MID, pad);
        tooltip.addPara(chm_fuelText1, padList, Misc.getHighlightColor(), "+1");
        tooltip.addPara(chm_fuelText2, pad2, Misc.getHighlightColor(),"-10%");
        tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "chm_fuelText3") }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "chm_fuelText4") });
        /*tooltip.addPara(
                "• Increased Ship Burn Level: %s"
                        + "\n• Decreased Ship Fuel Consumption: %s",
                pad, green, new String[]{
                        Misc.getRoundedValue(1.0f),
                       "-" + Misc.getRoundedValue(10.0f) + "%",
                }
        );
        tooltip.addPara("%s", padList, flavor, new String[] { "\"When you're looking for the best fuel in town, Sindrian Fuel has the best around!\"" }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Sindrian Fuel Company Jingle" });*/
    }
}