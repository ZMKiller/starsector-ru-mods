package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class sfcsolarlivery extends BaseLogisticsHullMod {

    public static final float CORONA_EFFECT_REDUCTION = 0.05f;
    public static final float CORONA_EFFECT_SS_REDUCTION = 0.4f;
    public static final float SMOD_ENERGY_DAMAGE_REDUCTION = 0.95f;

    private static String SLDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
    private static String SLText1 = Global.getSettings().getString("sfc_pagsm", "sfc_solarlivery1");
    private static String SLText2 = Global.getSettings().getString("sfc_pagsm", "sfc_solarlivery2");

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipVariantAPI variant = stats.getVariant();
        float mult = CORONA_EFFECT_REDUCTION;
        if (variant != null && variant.hasHullMod("solar_shielding")) {
            mult = CORONA_EFFECT_SS_REDUCTION;
        }
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, mult);

        boolean sMod = isSMod(stats);
        if (sMod) {
            stats.getEnergyDamageTakenMult().modifyMult(id, SMOD_ENERGY_DAMAGE_REDUCTION);
        }
    }


    /*public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round((1f - CORONA_EFFECT_REDUCTION) * 100f) + "%";
        if (index == 1) return "" + (int) Math.round((1f - CORONA_EFFECT_SS_REDUCTION) * 100f) + "%";
        return null;
    }*/

    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color yellow = new Color(255, 240, 0,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading(SLDetails, Alignment.MID, pad);
        tooltip.addPara(SLText1, padList, Misc.getHighlightColor(),"95" + "%");
        tooltip.addPara(SLText2, pad2, Misc.getHighlightColor(),"60" + "%");
        tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_solarlivery3") }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_solarlivery4") });
        /*tooltip.addPara(
                "• Decreased Solar Corona Effect: %s",
                pad, green, new String[]{
                        "-" + Misc.getRoundedValue(95.0f) + "%",
                }
        );*/
        /*tooltip.addPara(
                "• Effect On Solar Corona If Solar Shields Is Installed: %s",
                pad2, yellow, new String[]{
                        "-" + Misc.getRoundedValue(60.0f) + "%",
                }
        );*/
        /*tooltip.addPara("%s", padList, flavor, new String[] { "\"It's effective, but does it really have to be such a garish color?\"" }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Quote by an Ex-Sindrian Fuel Company Engineer" });*/
    }
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Math.round((0.9f - SMOD_ENERGY_DAMAGE_REDUCTION) * 100f) + "%";
        return null;
    }
}