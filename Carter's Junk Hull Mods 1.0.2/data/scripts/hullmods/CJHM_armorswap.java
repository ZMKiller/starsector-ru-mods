package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_armorswap extends BaseHullMod {
  public static final float ARMOR_PLUS = 1.0F;  
  public static final float HP_MINUS = -1.0F;
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getArmorBonus().modifyPercent(id, ARMOR_PLUS);
    stats.getHullBonus().modifyPercent(id, HP_MINUS);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) ARMOR_PLUS + "%";
		if (index == 1) return "" + (int) HP_MINUS + "%";		
		return null;
	}
  
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_hullswap"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_hullswap"))
			return "Incompatible with CJHM Hull Swap";
		return super.getUnapplicableReason(ship);
	}
	
}



