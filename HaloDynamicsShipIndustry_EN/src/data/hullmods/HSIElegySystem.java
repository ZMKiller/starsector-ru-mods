package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.kit.HSII18nUtil;

public class HSIElegySystem extends BaseHullMod {
    public static final float DAMAGE_BUFF_FLAT = 10;
    public static final float DAMAGE_BUFF_MULT = 1.33f;
    public static final float RANGE = 4000f;
    public static final Color LINK = new Color(241, 233, 126, 255);
    public static Map<HullSize, Integer> NUM_AT_KILL = new HashMap<>();
    static {
        NUM_AT_KILL.put(HullSize.CAPITAL_SHIP, 15);
        NUM_AT_KILL.put(HullSize.CRUISER, 10);
        NUM_AT_KILL.put(HullSize.DESTROYER, 6);
        NUM_AT_KILL.put(HullSize.FRIGATE, 3);
        NUM_AT_KILL.put(HullSize.FIGHTER, 1);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        //if(Global.getCurrentState().equals(GameState.COMBAT))
        HSIElegySystemScript.getInstance(ship);
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
            boolean isForModSpec) {
        // float pad = 3f;
        float opad = 10f;
        // Color h = Misc.getHighlightColor();
        // Color bad = Misc.getNegativeHighlightColor();
        //Color bg = new Color(75, 75, 175, 200);
        //Color b = new Color(225, 225, 255, 225);
        Color c = new Color(175, 175, 225, 225);
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIElegyHeading0"), Alignment.MID, opad);
        tooltip.addPara(HSII18nUtil.getHullModString("HSIElegyDesc0"), opad, c, "+" + DAMAGE_BUFF_FLAT);
        // desc.setColor(b);
        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIElegyHeading1"), Alignment.MID, opad);
        tooltip.addPara(HSII18nUtil.getHullModString("HSIElegyDesc1"), opad, c, "400", "100",
                NUM_AT_KILL.get(HullSize.FIGHTER) + "/" + NUM_AT_KILL.get(HullSize.FRIGATE) + "/"
                        + NUM_AT_KILL.get(HullSize.DESTROYER) + "/" + NUM_AT_KILL.get(HullSize.CRUISER) + "/"
                        + NUM_AT_KILL.get(HullSize.CAPITAL_SHIP));
    }

}
