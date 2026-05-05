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
import java.util.HashMap;
import java.util.Map;

public class HSIGuardFleet3 extends BaseHullMod {
    protected static final float CORONA_EFFECT = 0f;

    protected static final float HYPER_SPEED_BUFF = 1f;

    protected static Map<String,Integer> DP_MAP = new HashMap<>();
    static {
        DP_MAP.put("HSI_Xianfeng_GF3",2);
        DP_MAP.put("HSI_Oath_GF3",3);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxBurnLevel().modifyFlat(id, HYPER_SPEED_BUFF);
        stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id,CORONA_EFFECT);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id,getDPModifier(stats.getVariant().getHullSpec().getDParentHullId()));
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        Color bg = new Color(75, 75, 175, 200);
        Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIGuardFleet3Section"), Alignment.MID, opad);
        {
            TooltipMakerAPI image = tooltip.beginImageWithText("graphics/illustrations/HSI_GuradFleet3_Intro.png",96f);
            LabelAPI desc = image.addPara(HSII18nUtil.getHullModString("HSIGuardFleet3Intro0"), opad);
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
        if(index==0) return (int)HYPER_SPEED_BUFF+"";
        return null;
    }

    protected static int getDPModifier(String hullspecId){
        if(DP_MAP.containsKey(hullspecId)) return DP_MAP.get(hullspecId);
        return 0;
    }
}
