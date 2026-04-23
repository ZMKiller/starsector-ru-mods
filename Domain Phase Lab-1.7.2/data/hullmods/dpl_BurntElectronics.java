package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class dpl_BurntElectronics extends BaseHullMod {
	
	public static float HULL_PENALTY_MULT = 0.3f;
	public static float SHIELD_PERCENT = 10f;
	public static final float WEAPON_MALFUNCTION_PROB = 1f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float hullMult = HULL_PENALTY_MULT + (1f - HULL_PENALTY_MULT) * (1f - effect);
		
		stats.getCriticalMalfunctionChance().modifyFlat(id, 1f * effect);
		stats.getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALFUNCTION_PROB * effect);
		
		stats.getShieldDamageTakenMult().modifyPercent(id, SHIELD_PERCENT * effect);
		stats.getHullBonus().modifyMult(id, hullMult);
	}
		
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return null;
	}
	
	
}




