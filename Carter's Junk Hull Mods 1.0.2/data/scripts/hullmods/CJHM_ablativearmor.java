package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;


public class CJHM_ablativearmor extends BaseHullMod {

	public static final float EXPLOSIVE_DAMAGE_REDUCTION = .79f;
	public static float MANEUVER_PENALTY = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHighExplosiveDamageTakenMult().modifyMult(id, EXPLOSIVE_DAMAGE_REDUCTION);
		stats.getAcceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f - MANEUVER_PENALTY * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - EXPLOSIVE_DAMAGE_REDUCTION) * 100f);
		if (index == 1) return "" + (int) MANEUVER_PENALTY + "%";
		return null;
	}


}