package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import java.util.HashMap;
import java.util.Map;

public class CJHM_arsenallockers extends BaseHullMod {
	public static final float GROUND_BONUS = 10f;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, -20f);
		mag.put(HullSize.DESTROYER, -15f);
		mag.put(HullSize.CRUISER, -10f);
		mag.put(HullSize.CAPITAL_SHIP, -5f);

	}			
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, GROUND_BONUS);
		stats.getMaxCrewMod().modifyPercent(id, (Float) mag.get(hullSize));		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {	
		if (index == 0) return "" + (int) GROUND_BONUS;
		if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";				
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("additional_berthing"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("additional_berthing"))
			return "Incompatible with Additional Berthing";
		return super.getUnapplicableReason(ship);
	}	
		
	
}




