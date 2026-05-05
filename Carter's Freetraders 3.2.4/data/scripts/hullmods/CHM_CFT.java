package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CHM_CFT extends BaseHullMod {

	public static float SENSOR_HIDE = 20f;
	private static final float SENSOR_INCREASE = 10f;	
    private static final float CAPACITY_MULT = 0.1f;

	
    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorProfile().modifyMult(id, 1f - (SENSOR_HIDE * 0.01f));
        stats.getCargoMod().modifyPercent(id,CAPACITY_MULT*100f);	
		stats.getSensorStrength().modifyFlat(id, SENSOR_INCREASE);	
	}

    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SENSOR_HIDE + "%";
		if (index == 1) return "" + (int) SENSOR_INCREASE + "%";			
        if (index == 2) return "" + (int)(CAPACITY_MULT*100f) + "%";
	
		return null;
	}
}


