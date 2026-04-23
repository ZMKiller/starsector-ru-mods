package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class dpl_ModifiedHanger extends BaseHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_CREW_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REARM_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_DP_INCREASE).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY).modifyFlat(id, 1f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}








