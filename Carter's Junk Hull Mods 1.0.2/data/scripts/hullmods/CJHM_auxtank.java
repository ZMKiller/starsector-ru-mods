package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_auxtank extends BaseHullMod {
  public static final float TANK_PLUS = 15.0F;  
  public static final float HP_MINUS = -25.0F;
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getFuelMod().modifyPercent(id, TANK_PLUS);
    stats.getMaxCrewMod().modifyPercent(id, HP_MINUS);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0)
      return "15.0"; 
    if (index == 1)
      return "-25.0"; 
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
