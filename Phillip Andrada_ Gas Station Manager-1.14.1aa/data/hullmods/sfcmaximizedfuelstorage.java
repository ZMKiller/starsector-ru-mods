package data.hullmods;

import java.awt.*;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class sfcmaximizedfuelstorage extends BaseLogisticsHullMod {

    public static final float MIN_FRACTION = 1.20f;
    public static final float DAMAGE_MULT = 2f; // damage death explosion damage does
    public static final float RADIUS_MULT = 1.5f; // range of death explosion
    public static final float RECOVERY_MULT = 0.05f; // reduced chance of recovery
    public static final float SMOD_RECOVERY_MULT = 1f;
    private static final Color CRITICAL_DAMAGE_COLOR = new Color(255, 166, 0, 245);
    private static final int MAX_CRITICAL_PARTICLE_PARTICLES_PER_FRAME = 10;
    private static final float CRITICAL_PARTICLE_OPACITY = 0.85f;
    private static final float CRITICAL_PARTICLE_RADIUS = 130f;
    private static final float CRITICAL_PARTICLE_SIZE = 5f;

    private static String sfc_maxFuelDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
    private static String sfc_maxFuelText1 = Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText1");
    private static String sfc_maxFuelText2 = Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText2");
    private static String sfc_maxFuelText3 = Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText3");
    private static String sfc_maxFuelText4 = Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText4");

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFuelMod().modifyMult(id, MIN_FRACTION);
        stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, DAMAGE_MULT);
        stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, RADIUS_MULT);
        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, RECOVERY_MULT);
        stats.getBreakProb().modifyMult(id, 100f);
        boolean sMod = isSMod(stats);
        float mult = RECOVERY_MULT;
        if (sMod) mult = SMOD_RECOVERY_MULT;
        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyMult(id, mult);
    }

    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        final Color green = new Color(55,245,65,255);
        final Color red = new Color(245,55,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float pad2 = 0f;
        float padList = 6f;
        final float padSig = 1f;
        tooltip.addSectionHeading(sfc_maxFuelDetails, Alignment.MID, pad);
        tooltip.addPara(sfc_maxFuelText1, padList, Misc.getHighlightColor(), "+20%");
        tooltip.addPara(sfc_maxFuelText2, pad2, Misc.getHighlightColor(),"+100%");
        tooltip.addPara(sfc_maxFuelText3, pad2, Misc.getHighlightColor(), "+50%");
        tooltip.addPara(sfc_maxFuelText4, pad2, Misc.getHighlightColor(),"-95%");
        tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText5") }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_maxFuelText6") });
        /*tooltip.addPara(
                "• Increased Fuel Storage: %s",
                pad, green, new String[]{
                        Misc.getRoundedValue(20.0f) + "%",
                }
        );
        tooltip.addPara(
                "• Increased Death Explosion Damage: %s"
                        + "\n• Increased Death Explosion Radius: %s"
                        + "\n• Decreased Recovery Chance: -%s",
                pad2, red, new String[]{
                        Misc.getRoundedValue(100.0f) + "%",
                        Misc.getRoundedValue(50.0f) + "%",
                        Misc.getRoundedValue(95.0f) + "%",
                }
        );
        tooltip.addPara("%s", padList, flavor, new String[] { "\"What do you mean, overloading the AM-fuel storage tanks is a bad idea? The Sindrian Fuel Company must be able to carry as much fuel as possible! If you're so worried about overloading the fuel tanks, there's plenty of unused space in the engine room for some extra fuel tanks!\"" }).italicize();
        tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Spender Balashi" });*/
    }
    /*public boolean isApplicableToShip(ShipAPI ship) {
        if (!super.isApplicableToShip(ship)) return false;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (!super.isApplicableToShip(ship)) return super.getUnapplicableReason(ship);
    }*/
}
