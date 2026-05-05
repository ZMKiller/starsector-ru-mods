package data.scripts.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.BaseHullMod;

public class CJHM_brawlermissiles extends BaseHullMod {
  public static final float M_S_PER = 50.0F;  
  public static final float M_R_PER = -50.0F;
  
  public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    stats.getMissileMaxSpeedBonus().modifyPercent(id, M_S_PER);
    stats.getMissileWeaponRangeBonus().modifyPercent(id, M_R_PER);
  }
  
  public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
    if (index == 0)
      return "50.0"; 
    if (index == 1)
      return "-50.0"; 
    return null;
  }
}
