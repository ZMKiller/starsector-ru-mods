package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIBarrier extends BaseHullMod {
    protected static float EXTRA_SHIELD = 15f;
    protected static float EXTRA_SHIELD_LIMIT = 50f;
    protected static float EXTRA_BURNDRIVESHIELD = 10f;
    protected static float EXTRA_BURNDRIVE_SPEED_REDUCTION = 10f;

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive())
            return;
        if (ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class)) {
            HSITurbulanceShieldListenerV2 shield = ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
            if (ship.getSystem() != null && ship.getSystem().getId().equals("burndrive")) {
                if ((ship.getSystem().getEffectLevel() < (amount * 2 / ship.getSystem().getChargeUpDur()))
                        && ship.getSystem().isChargeup())
                    shield.getShield().addExtraShield(shield.getShield().getShieldCap() * EXTRA_SHIELD / 100f);
                if (ship.getSystem().isActive()) {
                    ship.getMutableStats().getMaxSpeed().modifyPercent("HSIBarrierExtra",
                            -EXTRA_BURNDRIVE_SPEED_REDUCTION * ship.getSystem().getEffectLevel());
                } else {
                    ship.getMutableStats().getMaxSpeed().unmodify("HSIBarrierExtra");
                }
            }
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && !ship.getVariant().hasHullMod("HSI_HardenedShield")
                && ship.getVariant().hasHullMod("HSI_Halo");
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && !ship.getVariant().hasHullMod("HSI_Halo")) {
            return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
        } else {
            return HSII18nUtil.getHullModString("HSIBarrierConflictWarning");
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI label = tooltip.addPara(HSII18nUtil.getHullModString("HSIBarrier0"), opad, h,
                ""+45 ,"" + (int) EXTRA_SHIELD + "%", "" + (int) EXTRA_SHIELD_LIMIT + "%");
        label.setHighlight("" + (int) EXTRA_SHIELD + "%", "" + (int) EXTRA_SHIELD_LIMIT + "%");
        label.setHighlightColors(h, h);

        if (ship!=null&&((ship.getSystem() != null && ship.getSystem().getId().equals("burndrive"))
                || (ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().equals("burndrive")))) {
            TooltipMakerAPI text;
            tooltip.addSectionHeading(ship.getSystem().getDisplayName(),
                    Alignment.TMID, 4f);
            text = tooltip.beginImageWithText(ship.getSystem().getSpecAPI().getIconSpriteName(), 32);
            text.addPara(HSII18nUtil.getHullModString("HSISystemModifier"), new Color(25, 200, 200, 255), 4f);
            text.addPara(HSII18nUtil.getHullModString("HSIBarrierSystem0"), 0, Misc.getHighlightColor(),
                    "" + (int) (EXTRA_BURNDRIVESHIELD) + "%", "" + (int) EXTRA_BURNDRIVE_SPEED_REDUCTION + "%");
            tooltip.addImageWithText(pad);
        }
    }
}