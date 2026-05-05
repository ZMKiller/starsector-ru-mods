package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;

public class CJHM_nanohull extends BaseHullMod {

	public static final float REPAIR_FRACTION = .99f;
	public static final float REPAIR_BONUS = .99f;
	private static final float HP = -10.0F;		
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FIGHTER, 1f);
		mag.put(HullSize.FRIGATE, .75f);
		mag.put(HullSize.DESTROYER, .5f);
		mag.put(HullSize.CRUISER, .25f);
		mag.put(HullSize.CAPITAL_SHIP, .20f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullRepairRatePercentPerSecond().modifyFlat(id, (Float) mag.get(hullSize));
		stats.getMaxHullRepairFraction().unmodify(id);
		stats.getMaxHullRepairFraction().modifyFlat(id, REPAIR_FRACTION);
		stats.getHullBonus().modifyPercent(id, HP);		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return ".75";		
		if (index == 1) return ".5";
		if (index == 2) return ".25";
		if (index == 3) return ".20";
		if (index == 4) return "10";			
		return null;
	}
	
}
