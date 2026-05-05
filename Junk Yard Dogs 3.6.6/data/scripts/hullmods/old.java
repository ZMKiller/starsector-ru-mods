package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CHM_JYD extends BaseHullMod {
    private static final Map jydcom = new HashMap();

    public static final float MAINTENANCE_MULT = 0.90f;
    static {
        jydcom.put(HullSize.FRIGATE, 30f);
        jydcom.put(HullSize.DESTROYER, 25f);
        jydcom.put(HullSize.CRUISER, 20f);
        jydcom.put(HullSize.CAPITAL_SHIP, 15f);
        jydcom.put(HullSize.DEFAULT, 15f);
		jydcom.put(HullSize.FIGHTER, 0f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float timeMult = 1f / ((100f + (Float) jydcom.get(hullSize)) / 100f);
        stats.getFighterRefitTimeMult().modifyMult(id, timeMult);
        stats.getMinCrewMod().modifyMult(id, MAINTENANCE_MULT);
    }
	
	 	
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ((Float) jydcom.get(HullSize.FRIGATE)).intValue()  + "%";
        if (index == 1) return "" + ((Float) jydcom.get(HullSize.DESTROYER)).intValue()  + "%";
        if (index == 2) return "" + ((Float) jydcom.get(HullSize.CRUISER)).intValue()  + "%";
        if (index == 3) return "" + ((Float) jydcom.get(HullSize.CAPITAL_SHIP)).intValue()  + "%";
        if (index == 4) return "" + (int) ((1f - MAINTENANCE_MULT) * 100f) + "%";
        return null;
    }

	
    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(220,185,20);
    }
}


