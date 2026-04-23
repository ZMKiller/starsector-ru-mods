package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class dpl_GiganticCavity extends BaseHullMod {

	public static float PHASE_COOLDOWN_REDUCTION = 100f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f + PHASE_COOLDOWN_REDUCTION / 100f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(PHASE_COOLDOWN_REDUCTION) + "%";
		return null;
	}

}
