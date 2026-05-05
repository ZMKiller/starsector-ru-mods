package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import java.util.HashMap;
import java.util.Map;

public class CJHM_beltarmor extends BaseHullMod {
	
	public static final float MANEUVER_MINUS = -25f;
	public static final float HULL_PLUS = 20f;
	public static final float CARGO_MINUS = -70.0F;  	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 40f);
		mag.put(HullSize.DESTROYER, 80f);
		mag.put(HullSize.CRUISER, 120f);
		mag.put(HullSize.CAPITAL_SHIP, 160f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));		
		stats.getAcceleration().modifyMult(id, 1f + MANEUVER_MINUS * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + MANEUVER_MINUS * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + MANEUVER_MINUS * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f + MANEUVER_MINUS * 0.01f);
		stats.getCargoMod().modifyPercent(id, CARGO_MINUS);		
		stats.getHullBonus().modifyPercent(id, HULL_PLUS);

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_PLUS + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 5) return "" + (int) CARGO_MINUS + "%";		
		if (index == 6) return "" + (int) MANEUVER_MINUS + "%";
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_cargocarver") || ship.getVariant().hasHullMod("CJHM_cargofiller"))
			return false;
		return super.isApplicableToShip(ship);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_cargocarver") || ship.getVariant().hasHullMod("CJHM_cargofiller"))
			return "Incompatible with CJHM cargo filler or cargo carver";
		return super.getUnapplicableReason(ship);
	}
	
}



