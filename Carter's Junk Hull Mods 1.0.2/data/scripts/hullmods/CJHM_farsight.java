package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;

public class CJHM_farsight extends BaseHullMod {

	private static Map jydfar = new HashMap();
	static {
		jydfar.put(HullSize.FRIGATE, 70f);
		jydfar.put(HullSize.DESTROYER, 95f);
		jydfar.put(HullSize.CRUISER, 120f);
		jydfar.put(HullSize.CAPITAL_SHIP, 145f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) jydfar.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) jydfar.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) jydfar.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) jydfar.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getSightRadiusMod().modifyPercent(id, (Float) jydfar.get(hullSize));
		stats.getSensorStrength().modifyPercent(id, (Float) jydfar.get(hullSize));

	}

}