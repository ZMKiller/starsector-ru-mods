package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;

public class CJHM_brawlerenergy extends BaseHullMod {
	
	public static final float DAMAGE = 15f;
	public static final float FLUX = -15f;
	public static final float RANGE = -30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().modifyPercent(id,DAMAGE);
		stats.getEnergyProjectileSpeedMult().modifyPercent(id,DAMAGE);		
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id,FLUX);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id,RANGE);		
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) DAMAGE + "%";
        if (index == 1) return "" + (int) FLUX + "%";
        if (index == 2) return "" + (int) RANGE + "%";
        return null;
    }
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("high_scatter_amp") || ship.getVariant().hasHullMod("advancedoptics"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("high_scatter_amp") || ship.getVariant().hasHullMod("advancedoptics"))
			return "Incompatible with High Scatter Amplifiers or Advanced Optics";
		return super.getUnapplicableReason(ship);
	}		
	
}
