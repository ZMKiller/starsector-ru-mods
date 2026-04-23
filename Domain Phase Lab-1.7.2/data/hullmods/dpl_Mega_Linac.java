package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class dpl_Mega_Linac extends BaseHullMod {

	public static final float ROF_BONUS = 1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float mult = 1f + ROF_BONUS;
		stats.getEnergyRoFMult().modifyMult(id, mult);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return "" + (int) ROF_BONUS * 100f + "%";
	}

}
