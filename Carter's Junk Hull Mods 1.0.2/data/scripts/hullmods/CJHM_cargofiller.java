package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_cargofiller extends BaseHullMod {
  public static final float CARGO_MINUS = -20.0F;  
  public static final float HP_PLUS = 20.0F;
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getCargoMod().modifyPercent(id, CARGO_MINUS);
    stats.getHullBonus().modifyPercent(id, HP_PLUS);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0)
      return "-20.0"; 
    if (index == 1)
      return "20.0"; 
    return null;
  }
  
 	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_beltarmor") || ship.getVariant().hasHullMod("CJHM_cargocarver"))
			return false;
		return super.isApplicableToShip(ship);
	}    
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("CJHM_beltarmor") || ship.getVariant().hasHullMod("CJHM_cargocarver"))
			return "Incompatible with CJHM Cargo Carver or Belt Armor";
		return super.getUnapplicableReason(ship);
	}		
}

