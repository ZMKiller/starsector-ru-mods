package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;

import java.awt.*;

public class HSIGuardFleet1 extends BaseHullMod {
    protected static final float MAX_SPEED = 7f;

    protected static final float MANUVER = 7f;

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyPercent(id,MAX_SPEED);
        stats.getAcceleration().modifyPercent(id,MANUVER);
        stats.getDeceleration().modifyPercent(id,MANUVER);
        stats.getMaxTurnRate().modifyPercent(id,MANUVER);
        stats.getTurnAcceleration().modifyPercent(id,MANUVER);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIGuardFleet1Section"), Alignment.MID, opad);
        {
            LabelAPI desc = tooltip.addPara(HSII18nUtil.getHullModString("HSIGuardFleet1Intro0"), opad);
            desc.setColor(b);
            tooltip.addImageWithText(opad);
        }
    }

    @Override
    public float getTooltipWidth() {
        return 425f;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==0) return (int)MAX_SPEED+"";
        if(index==1) return (int)MANUVER+"";
        return null;
    }
}
