package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.PhaseCloakStats;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class dpl_AnomalyHanger extends BaseHullMod {
	public static float SMOD_CRUISER = 15f;
	public static float SMOD_CAPITAL = 25f;
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int) SMOD_CRUISER + "%";
		if (index == 1) return "" + (int) SMOD_CAPITAL + "%";
		return null;
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		if (sMod) {
			float bonus = 0f;
			if (hullSize == HullSize.CRUISER) bonus = SMOD_CRUISER;
			else if (hullSize == HullSize.CAPITAL_SHIP) bonus = SMOD_CAPITAL;
			if (bonus != 0) {
				stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, bonus);
			}
		}
		
		stats.removeListenerOfClass(data.hullmods.dpl_AnomalyHanger.dpl_AnomalyHangerListener.class);
		stats.addListener(new data.hullmods.dpl_AnomalyHanger.dpl_AnomalyHangerListener());
   }
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && !ship.isFrigate() && !ship.isDestroyer() && ship.getHullSpec().getFighterBays() > 0 &&
								!ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR) &&
								!ship.getVariant().hasHullMod(HullMods.CONVERTED_BAY) &&
								!ship.getHullSpec().isPhase();
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.isFrigate()) return "Can not be installed on a frigate";
		if (ship != null && ship.isDestroyer()) return "Can not be installed on a destroyer";
		if (ship != null && ship.getHullSpec().getFighterBays() <= 0) return "Ship doesn't have fighter bays";
		if (ship != null && ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) return "The ship must have fighter bays, not converted hangers";
		if (ship != null && ship.getVariant().hasHullMod(HullMods.CONVERTED_BAY)) return "The ship's bay is already converted to cargo space";
		return "Can not be installed on a phase ship";
	}
	
	public boolean affectsOPCosts() {
		return true;
	}
   
	public class dpl_AnomalyHangerListener implements FighterOPCostModifier {
		public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
			if (fighter.hasTag("dpl_anomaly_drone"))
				currCost = 20;
			return currCost;
		}
	}
}
