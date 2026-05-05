package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_internaltank extends BaseHullMod {
  public static final float TANK_PLUS = 20.0F;  
  public static final float HP_MINUS = -20.0F;
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getFuelMod().modifyPercent(id, TANK_PLUS);
    stats.getHullBonus().modifyPercent(id, HP_MINUS);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0)
      return "20.0"; 
    if (index == 1)
      return "-20.0"; 
    return null;
  }
  
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_beltarmor") || ship.getVariant().hasHullMod("CJHM_cargofiller"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_beltarmor") || ship.getVariant().hasHullMod("CJHM_cargofiller"))
			return "Incompatible with CJHM Cargo Filler or Belt Armor";
		return super.getUnapplicableReason(ship);
	}	
	
}
