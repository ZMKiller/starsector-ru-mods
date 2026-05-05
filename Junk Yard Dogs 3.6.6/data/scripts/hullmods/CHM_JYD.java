package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_JYD extends BaseHullMod {

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSuppliesPerMonth().modifyFlat(id, -2f); 
                stats.getSuppliesToRecover().modifyFlat(id, -2f);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
}



