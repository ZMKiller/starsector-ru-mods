package data.hullmods;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sfcfuelstoragerefit extends BaseLogisticsHullMod {

    public static final float FUEL_MOD = 0.75f; //percentage of cargo space converted to fuel storage
    public static final float CARGO_MOD = 0.05f; // percentage of cargo space remaining
    public static final float DAMAGE_MULT = 2f; // damage death explosion damage does
    public static final float RADIUS_MULT = 1.5f; // range of death explosion
    public static final float RECOVERY_MULT = 0.05f; // reduced chance of recovery
    public static final float SMOD_RECOVERY_MULT = 1f;

    private static String sfc_refitDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
    private static String sfc_refitText1 = Global.getSettings().getString("sfc_pagsm", "sfc_refitText1");
    private static String sfc_refitText2 = Global.getSettings().getString("sfc_pagsm", "sfc_refitText2");
    private static String sfc_refitText3 = Global.getSettings().getString("sfc_pagsm", "sfc_refitText3");
    private static String sfc_refitText4 = Global.getSettings().getString("sfc_pagsm", "sfc_refitText4");
    private static String sfc_refitText5 = Global.getSettings().getString("sfc_pagsm", "sfc_refitText5");

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

            stats.getCargoMod().unmodifyMult(id); //Unapply our 0 so it doesn't calculate 0
            stats.getFuelMod().modifyFlat(id, stats.getVariant() != null ? stats.getCargoMod().    computeEffective(stats.getVariant().getHullSpec().getCargo()) * FUEL_MOD : 0f);
            stats.getCargoMod().modifyMult(id, CARGO_MOD); //Reapply the 0
            stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
            stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
            stats.getBreakProb().modifyMult(id, 100f);

            boolean sMod = isSMod(stats);
            float mult = RECOVERY_MULT;
            if (sMod) mult = SMOD_RECOVERY_MULT;
            stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, mult);
        }


    /*@Override
    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) {
            return "75%";
        }
        if (index == 1) {
            return "5%";
        }
        if (index == 2) {
            return "" + Math.round((1f - RECOVERY_MULT) * 100f) + "%";
        }
        if (index == 3) {
            return "" + Math.round(RADIUS_MULT * 100f) + "%";
        }
        if (index == 4) {
            return "" + Math.round(DAMAGE_MULT * 100f) + "%";
        }
        return null;

    }*/


    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color red = new Color(245,55,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading(sfc_refitDetails, Alignment.MID, pad);
        tooltip.addPara(sfc_refitText1, padList, Misc.getHighlightColor(),"75%");
        tooltip.addPara(sfc_refitText2, pad2, Misc.getHighlightColor(),"-95%");
        tooltip.addPara(sfc_refitText3, pad2, Misc.getHighlightColor(),"+100%");
        tooltip.addPara(sfc_refitText4, pad2, Misc.getHighlightColor(),"+50%");
        tooltip.addPara(sfc_refitText5, pad2, Misc.getHighlightColor(),"-95%");
        tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_refitText6") }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_refitText7") });
        /*tooltip.addPara(
                "• Storage to Fuel Conversion: %s Cargo Storage to Fuel",
                pad, green, new String[]{
                        Misc.getRoundedValue(75.0f) + "%",
                }
        );
        tooltip.addPara(
                "• Reduced Cargo Storage: -%s Storage"
                        + "\n• Increased Death Explosion Damage: %s"
                        + "\n• Increased Death Explosion Radius: %s"
                        + "\n• Decreased Recovery Chance: -%s",
                pad2, red, new String[]{
                        Misc.getRoundedValue(95.0f) + "%",
                        Misc.getRoundedValue(100.0f) + "%",
                        Misc.getRoundedValue(50.0f) + "%",
                        Misc.getRoundedValue(95.0f) + "%",
                }
        );
        tooltip.addPara("%s", padList, flavor, new String[] { "\"What do you mean, leaving unsecured Antimatter Fuel lying around the ship is a bad idea? The Sindrian Fuel Company must be able to carry as much fuel as possible, no matter where we have to put it! Why, there's so much space in the engine room, let's put some over there!\"" }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Spender Balashi" });*/
    }
}
