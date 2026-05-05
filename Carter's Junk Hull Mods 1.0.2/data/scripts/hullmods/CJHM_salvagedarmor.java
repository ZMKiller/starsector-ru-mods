package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CJHM_salvagedarmor extends BaseHullMod 
{

	public static final float MANEUVER_PENALTY = 15f;
	
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 75f);
		mag.put(HullSize.CRUISER, 150f);
		mag.put(HullSize.CAPITAL_SHIP, 200f);
	}
    @Override	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) 
	{
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
		
		stats.getAcceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getDeceleration().modifyPercent(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getTurnAcceleration().modifyPercent(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getMaxTurnRate().modifyPercent(id, 1f - MANEUVER_PENALTY * 0.01f);
	}
	
    @Override	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) MANEUVER_PENALTY + "%";
		return null;

	}


}
