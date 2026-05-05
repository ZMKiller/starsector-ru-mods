package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_hullswap extends BaseHullMod {
  public static final float HP_PLUS = 1.0F;	
  public static final float ARMOR_MINUS = -1.0F;  
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getHullBonus().modifyPercent(id, HP_PLUS);	  
    stats.getArmorBonus().modifyPercent(id, ARMOR_MINUS);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "" + (int) HP_PLUS + "%";	
		if (index == 1) return "" + (int) ARMOR_MINUS + "%";		
		return null;
	}
  
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_armorswap"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_armorswap"))
			return "Incompatible with CJHM Armor Swap";
		return super.getUnapplicableReason(ship);
	}
	
}
