package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicIncompatibleHullmods;


public class CJHM_booster_rockets extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 10f);
		mag.put(HullSize.DESTROYER, 10f);
		mag.put(HullSize.CRUISER, 5f);
		mag.put(HullSize.CAPITAL_SHIP, 5f);
	}

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) mag.get(hullSize));

		if(stats.getVariant().getHullMods().contains("safetyoverrides")){
			MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "CJHM_booster_rockets", "safetyoverrides"); 

		}
		if(stats.getVariant().getHullMods().contains("unstable_injector")){
			MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "CJHM_booster_rockets", "unstable_injector"); 

		}		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();

		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return !ship.getVariant().getHullMods().contains("unstable_injector") && !ship.getVariant().getHullMods().contains("safetyoverrides");
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("unstable_injector")) {
			return "Booster Rockets cannot be made unstable";
		}
		if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
			return "Booster Rockets are hard wired and cannot be overridden.";
		}
		return null;
	}

}