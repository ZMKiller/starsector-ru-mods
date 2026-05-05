package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;

public class HIVER_BioHull extends BaseHullMod {

	public static final float REPAIR_FRACTION = .95f;
	public static final float REPAIR_BONUS = .99f;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 1.5f);
		mag.put(HullSize.FRIGATE, 1f);
		mag.put(HullSize.DESTROYER, .75f);
		mag.put(HullSize.CRUISER, .5f);
		mag.put(HullSize.CAPITAL_SHIP, .25f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullRepairRatePercentPerSecond().modifyFlat(id, (Float) mag.get(hullSize));
		stats.getMaxHullRepairFraction().unmodify(id);
		stats.getMaxHullRepairFraction().modifyFlat(id, REPAIR_FRACTION);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "1.5";
		if (index == 1) return "1";		
		if (index == 2) return ".75";
		if (index == 3) return ".5";
		if (index == 4) return ".25";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		String shipHull = ship.getHullSpec().getHullId();
		
		return shipHull.contains("HIVER_");
	}


}
