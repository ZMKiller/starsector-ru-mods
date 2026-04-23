package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CHM_TTSC extends BaseHullMod {

    private static final Map coom = new HashMap();
    private static final Map coom2 = new HashMap();
    static {
        coom.put(HullSize.FIGHTER, 0f);
        coom.put(HullSize.FRIGATE, 250f);
        coom.put(HullSize.DESTROYER, 400f);
        coom.put(HullSize.CRUISER, 600f);
        coom.put(HullSize.CAPITAL_SHIP, 1000f);
        coom.put(HullSize.DEFAULT, 0f);
        coom2.put(HullSize.FIGHTER, 0f);
        coom2.put(HullSize.FRIGATE, 15f);
        coom2.put(HullSize.DESTROYER, 25f);
        coom2.put(HullSize.CRUISER, 35f);
        coom2.put(HullSize.CAPITAL_SHIP, 50f);
        coom2.put(HullSize.DEFAULT, 0f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getVariant().getSMods().size() > 0) {
            stats.getFluxCapacity().modifyFlat(id, (Float) coom.get(hullSize));
            stats.getFluxDissipation().modifyFlat(id, (Float) coom2.get(hullSize));
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) return "" + ((Float) coom.get(HullSize.FRIGATE)).intValue();
        if (index == 1) return "" + ((Float) coom.get(HullSize.DESTROYER)).intValue();
        if (index == 2) return "" + ((Float) coom.get(HullSize.CRUISER)).intValue();
        if (index == 3) return "" + ((Float) coom.get(HullSize.CAPITAL_SHIP)).intValue();
        if (index == 4) return "" + ((Float) coom2.get(HullSize.FRIGATE)).intValue();
        if (index == 5) return "" + ((Float) coom2.get(HullSize.DESTROYER)).intValue();
        if (index == 6) return "" + ((Float) coom2.get(HullSize.CRUISER)).intValue();
        if (index == 7) return "" + ((Float) coom2.get(HullSize.CAPITAL_SHIP)).intValue();
        return null;
    }
    
    
    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(135,206,255,255);
    }
}