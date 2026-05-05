package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class CJHM_ballisticintegration extends BaseHullMod {

	public static final float COST_REDUCTION  = 5f;
	public static final float ARMOR_REDUCTION  = 10f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
		stats.getArmorBonus().modifyPercent(id, -ARMOR_REDUCTION);	
		stats.getHullBonus().modifyPercent(id, -ARMOR_REDUCTION);			
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION;
		if (index == 1) return "" + (int) ARMOR_REDUCTION + "%";	
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}