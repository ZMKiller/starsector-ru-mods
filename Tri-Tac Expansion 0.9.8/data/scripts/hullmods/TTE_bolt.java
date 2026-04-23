package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;

public class TTE_bolt extends BaseHullMod {
	
	public static final float DAMAGE = 20f;
	public static final float SPEED = 20f;		
	public static final float FLUX = -20f;
	public static final float RANGE = 15f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyPercent(id,DAMAGE);
		stats.getEnergyProjectileSpeedMult().modifyPercent(id,SPEED);			
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id,FLUX);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id,RANGE);		
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) DAMAGE + "%";
        if (index == 1) return "" + (int) FLUX + "%";
        if (index == 2) return "" + (int) RANGE + "%";
        if (index == 3) return "" + (int) SPEED + "%";		
        return null;
    }
}
