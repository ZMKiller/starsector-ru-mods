package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class CJHM_projectileaccelerator extends BaseHullMod {

	public static final float TURRET_SPEED = -50f;
	public static final float PROJECTILE_SPEED = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED);
		stats.getProjectileSpeedMult().modifyPercent(id , PROJECTILE_SPEED);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) TURRET_SPEED + "%";
		if (index == 1) return "" + (int) PROJECTILE_SPEED + "%";		
		return null;
	}


}
