package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CJHM_cargopods extends BaseHullMod {
	
    public static final float SPEED_MANUEVER_BURN = 35f;
	public static final float DEGRADE_INCREASE_PERCENT = 10f;		
	
    private static Map<Object, Float> POD_SIZE = new HashMap<Object, Float>();
    static {
        POD_SIZE.put(HullSize.FRIGATE, 300f);
        POD_SIZE.put(HullSize.DESTROYER, 600f);
        POD_SIZE.put(HullSize.CRUISER, 900f);
        POD_SIZE.put(HullSize.CAPITAL_SHIP, 1200f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().modifyPercent(id, -SPEED_MANUEVER_BURN);
        stats.getMaxTurnRate().modifyPercent(id, -SPEED_MANUEVER_BURN);
        stats.getFuelUseMod().modifyPercent(id, SPEED_MANUEVER_BURN);
        stats.getCargoMod().modifyFlat(id, (Float) POD_SIZE.get(hullSize));
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);				
    }


    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return Math.round(((Float) POD_SIZE.get(HullSize.FRIGATE)).intValue()) + "";
        if (index == 1) return Math.round(((Float) POD_SIZE.get(HullSize.DESTROYER)).intValue()) + "";
        if (index == 2) return Math.round(((Float) POD_SIZE.get(HullSize.CRUISER)).intValue()) + "";
        if (index == 3) return Math.round(((Float) POD_SIZE.get(HullSize.CAPITAL_SHIP)).intValue()) + "";
        if (index == 4) return Math.round(SPEED_MANUEVER_BURN) + "%";
        if (index == 5) return Math.round(SPEED_MANUEVER_BURN) + "%";
        if (index == 6) return Math.round(DEGRADE_INCREASE_PERCENT) + "%";		
		
        return null;
    }
}