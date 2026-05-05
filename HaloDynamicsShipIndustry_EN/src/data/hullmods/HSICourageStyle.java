package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSICourageStyle extends BaseHullMod {
    protected static float FRONT_SHEILD_ABSORB_BUFF = 50f;
    protected static Map<HullSize, Float> RANGE_DEBUFF = new HashMap<HullSize, Float>();
    static {
        RANGE_DEBUFF.put(HullSize.FRIGATE, 20f);
        RANGE_DEBUFF.put(HullSize.DESTROYER, 35f);
        RANGE_DEBUFF.put(HullSize.CRUISER, 50f);
        RANGE_DEBUFF.put(HullSize.CAPITAL_SHIP, 75f);
    }
    protected static float ENERGY_ROF_BONUS = 20f;
    protected static float ENERGY_FLUX_BONUS = 20f;
    protected static float MANUVER_BONUS = 30f;

    protected static float EXTRA_SHIELD = 15f;
    protected static float EXTRA_BURNDRIVE_SPEED_REDUCTION = 15f;

    protected static float BREAK_EXTRA_SHIELD = 30f;
    protected static float BREAK_EXTRA_REFILL = 60f;

    protected static float COURAGE_DECREASE_TIME = 5f;

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null
                && ship.getVariant().hasHullMod("HSI_Halo");
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI label = tooltip.addPara(HSII18nUtil.getHullModString("HSICombatStyle"), opad, h);
        label = tooltip.addPara(HSII18nUtil.getHullModString("HSICourage0"), opad, h,
                "" + FRONT_SHEILD_ABSORB_BUFF + "%", "" + RANGE_DEBUFF.get(HullSize.FRIGATE) + "%",
                "" + RANGE_DEBUFF.get(HullSize.DESTROYER) + "%", "" + RANGE_DEBUFF.get(HullSize.CRUISER) + "%",
                "" + RANGE_DEBUFF.get(HullSize.CAPITAL_SHIP) + "%");
        label.setHighlight("" + FRONT_SHEILD_ABSORB_BUFF + "%", "" + RANGE_DEBUFF.get(HullSize.FRIGATE) + "%",
                RANGE_DEBUFF.get(HullSize.DESTROYER) + "%", RANGE_DEBUFF.get(HullSize.CRUISER) + "%",
                RANGE_DEBUFF.get(HullSize.CAPITAL_SHIP) + "%");
        label.setHighlightColors(h, bad, bad, bad, bad);

        label = tooltip.addPara(HSII18nUtil.getHullModString("HSICourage1"), opad, h,
                "" + ENERGY_ROF_BONUS + "%", "" + MANUVER_BONUS + "%",
                "" + ENERGY_FLUX_BONUS + "%", "" + COURAGE_DECREASE_TIME + HSII18nUtil.getHullModString("HSIUnitSec"));
        label.setHighlight("" + ENERGY_ROF_BONUS + "%", "" + MANUVER_BONUS + "%",
                "" + ENERGY_FLUX_BONUS + "%", "" + COURAGE_DECREASE_TIME + HSII18nUtil.getHullModString("HSIUnitSec"));
        label.setHighlightColors(h, h, h, h);

        label = tooltip.addPara(HSII18nUtil.getHullModString("HSICourage2"), opad, h,
                "" + BREAK_EXTRA_SHIELD + "%", "" + BREAK_EXTRA_REFILL + "%");
        label.setHighlight("" + BREAK_EXTRA_SHIELD + "%", "" + BREAK_EXTRA_REFILL + "%");
        label.setHighlightColors(h, h);

        if (ship != null && ((ship.getSystem() != null && ship.getSystem().getId().equals("burndrive"))
                || (ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().equals("burndrive")))) {
            TooltipMakerAPI text;
            tooltip.addSectionHeading(ship.getSystem().getDisplayName(),
                    Alignment.TMID, 4f);
            text = tooltip.beginImageWithText(ship.getSystem().getSpecAPI().getIconSpriteName(), 32);
            text.addPara(HSII18nUtil.getHullModString("HSISystemModifier"), new Color(25, 200, 200, 255), 4f);
            text.addPara(HSII18nUtil.getHullModString("HSICourageSystem0"), 0, Misc.getHighlightColor(),
                    "" + (int) (EXTRA_SHIELD) + "%", "" + (int) EXTRA_BURNDRIVE_SPEED_REDUCTION + "%");
            tooltip.addImageWithText(pad);
        }
        if (ship != null && ((ship.getSystem() != null && ship.getSystem().getId().equals("burndrive"))
                || (ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().equals("burndrive")))) {
            TooltipMakerAPI text;
            tooltip.addSectionHeading(ship.getSystem().getDisplayName(),
                    Alignment.TMID, 4f);
            text = tooltip.beginImageWithText(ship.getSystem().getSpecAPI().getIconSpriteName(), 32);
            text.addPara(HSII18nUtil.getHullModString("HSISystemModifier"), new Color(25, 200, 200, 255), 4f);
            text.addPara(HSII18nUtil.getHullModString("HSICourageSystem1"), 0, Misc.getHighlightColor(),
                    "" + (int) (EXTRA_SHIELD) + "%", "" + (int) EXTRA_BURNDRIVE_SPEED_REDUCTION + "%");
            tooltip.addImageWithText(pad);
        }
    }
}