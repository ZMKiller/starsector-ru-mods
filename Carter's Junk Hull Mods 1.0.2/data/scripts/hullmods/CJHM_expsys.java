package data.scripts.hullmods;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CJHM_expsys extends BaseHullMod {
	
	private static final float CARGO_PLUS = 10.0f;  
	private static final float SENSOR_PLUS = 30.0f;  	
	private static final float CREW_PLUS = 75.0f;
	private static final float SUPPLY_USE_MULT = 1.1f;
	private static final float CORONA_EFFECT_REDUCTION = 0.5f;
	private static final float BURN_LEVEL_BONUS = 1.0f;	
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorStrength().unmodify(HullMods.CIVGRADE);
		stats.getSensorProfile().unmodify(HullMods.CIVGRADE);		
		stats.getSightRadiusMod().modifyPercent(id, SENSOR_PLUS);
		stats.getSensorStrength().modifyPercent(id, SENSOR_PLUS);
		stats.getCargoMod().modifyPercent(id, CARGO_PLUS);		
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);		
		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, CORONA_EFFECT_REDUCTION);
		stats.getMinCrewMod().modifyPercent(id, CREW_PLUS);		
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {

		if (index == 0) return "" + BURN_LEVEL_BONUS;	
		if (index == 1) return "" + (int) Math.round (SENSOR_PLUS) + "%";		
		if (index == 2) return "" + (int) Math.round (CARGO_PLUS) + "%";
		if (index == 3) return "" + (int) Math.round((1f - CORONA_EFFECT_REDUCTION) * 100f) + "%";	
		if (index == 4) return "" + (int) Math.round(CREW_PLUS) + "%";
		if (index == 5) return "" + (int) ((SUPPLY_USE_MULT - 1f) * 100f) + "%";				
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(HullMods.CIVGRADE) && super.isApplicableToShip(ship);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().hasHullMod(HullMods.CIVGRADE)) {
			return "Can only be installed on civilian-grade hulls";
		}
		return super.getUnapplicableReason(ship);
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("militarized_subsystems"))
			return false;
		return super.isApplicableToShip(ship);
	}  
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod("militarized_subsystems"))
			return "Incompatible with Militarized Subsystems";
		return super.getUnapplicableReason(ship);
	}		
	
}
