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
import data.kit.HSIIds;

public class HSIPredictShield extends BaseHullMod {
    protected static final float PREDICT_BEAM_MIN = 500f;
    protected static final float PREDICT_BEAM_MAX = 2000f;
    protected static final float PREDICT_REDUCE_MAX = 0.3f;
    protected static final float PREDICT_BALLISTIC_MIN = 1.5f;
    protected static final float PREDICT_BALLISTIC_MAX = 3f;

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getVariant().hasHullMod("HSI_Halo")
                && !ship.getVariant().hasHullMod(HSIIds.HullMod.REFLECT_SHIELD);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null) {
            if (!ship.getVariant().hasHullMod("HSI_Halo"))
                return HSII18nUtil.getHullModString("HSINoTurbulanceWarning");
            if (ship.getVariant().hasHullMod(HSIIds.HullMod.REFLECT_SHIELD))
                return HSII18nUtil.getHullModString("HSIReflectShieldConflictWarning");
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
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIHullModSupplement"), Alignment.MID, opad);
        LabelAPI label = tooltip.addPara(HSII18nUtil.getHullModString("HSIPredictShield"), opad);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) (PREDICT_REDUCE_MAX*100f) + "%";
        return null;
    }
}
