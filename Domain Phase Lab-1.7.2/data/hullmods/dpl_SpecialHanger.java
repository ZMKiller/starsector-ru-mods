package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class dpl_SpecialHanger extends BaseHullMod {
	
	public static int OP_LIMIT = 10;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.removeListenerOfClass(data.hullmods.dpl_SpecialHanger.dpl_SpecialHangerListener.class);
		stats.addListener(new data.hullmods.dpl_SpecialHanger.dpl_SpecialHangerListener());
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) OP_LIMIT + "";
		return null;
	}

	public boolean affectsOPCosts() {
		return true;
	}
   
	public class dpl_SpecialHangerListener implements FighterOPCostModifier {
		public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
			if (!(fighter.hasTag("dpl_wing") || currCost <= OP_LIMIT))
				currCost = 99999;
			return currCost;
		}
	}
}
