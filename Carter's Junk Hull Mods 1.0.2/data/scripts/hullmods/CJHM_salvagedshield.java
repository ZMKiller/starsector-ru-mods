package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CJHM_salvagedshield extends BaseHullMod {
	
	public static final float SHIELD_ARC = 120f;
	public static final float HULL_BONUS = -15f;		


	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ShieldAPI shield = ship.getShield();
		if (shield == null) {
			ship.setShield(ShieldType.FRONT, 0.5f, 1f, SHIELD_ARC);
		}	
	}
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);	
		stats.getArmorBonus().modifyPercent(id, HULL_BONUS);			
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) { return "" + (int) SHIELD_ARC; }
		if (index == 1) return "" + (int) HULL_BONUS;			
		
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && ship.getHullSpec().getDefenseType() == ShieldType.NONE;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.getHullSpec().getDefenseType() == ShieldType.PHASE) {
			return "Phase ships can not have shields";
		} 
		return "Ship already has shields";
	}
}
