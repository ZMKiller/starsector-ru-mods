package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIWeaponIndAdaption extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {


    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && (ship.getVariant().hasHullMod("HSI_Halo")||ship.getHullSpec().getBaseHullId().startsWith("HSI_"));
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null) {
            if (!(ship.getVariant().hasHullMod("HSI_Halo")||ship.getHullSpec().getBaseHullId().startsWith("HSI_")))
                return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        if(ship==null) return;
        float col1W = width - 12f;
        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                20f, true, true,
                new Object[] { HSII18nUtil.getHullModString("HSIAdaption"), col1W });
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_mjolnir"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_needler"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_iral"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_guardian"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_autopulse"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_plasma"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_hil"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_tachyonlance"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_phasebeam"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_heavyblaster"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_gravitonbeam"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_ionbeam"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_mininglaser"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_heavyac"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_ionpulser"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_gauss"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_amblaster"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_hveldriver"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_lightdualac"));
        tooltip.addRow(HSII18nUtil.getHullModString("HSIAdaptionText_heavymauler"));
        tooltip.addTable("", 0, opad);
    }

    @Override
    public float getTooltipWidth() {
        return 600f;
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return isApplicableToShip(ship);
    }
}
