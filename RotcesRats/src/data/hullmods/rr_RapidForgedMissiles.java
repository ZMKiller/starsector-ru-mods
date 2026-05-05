package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class rr_RapidForgedMissiles extends BaseHullMod {

	public static final float DAMAGE_MALUS = 0.35f;
	public static final float HEALTH_MALUS = 0.25f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileWeaponDamageMult().modifyMult(id, (1f - DAMAGE_MALUS));
		stats.getMissileHealthBonus().modifyMult(id, (1f - HEALTH_MALUS));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "35%";
		if (index == 1) return "25%";
		return null;
	}

}
