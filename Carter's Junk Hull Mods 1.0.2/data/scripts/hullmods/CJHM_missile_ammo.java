package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CJHM_missile_ammo extends BaseHullMod {
	
	public static final float CARGO_PENALTY = 0.6f;
	public static final float MISSILE_BONUS = 50f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCargoMod().modifyMult(id, CARGO_PENALTY);		
		stats.getMissileAmmoBonus().modifyPercent(id, MISSILE_BONUS);

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - CARGO_PENALTY )* 100) + "%";		
		if (index == 1) return "" + (int) MISSILE_BONUS;

		return null;
	}


}
