package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;

public class dpl_MissileOverloadStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS_PERCENT = 40f;
	public static final float FLUX_BONUS_PERCENT = 20f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float fluxPercent = FLUX_BONUS_PERCENT * effectLevel;
		stats.getMissileWeaponDamageMult().modifyPercent(id, bonusPercent);
		stats.getMissileWeaponFluxCostMod().modifyPercent(id, fluxPercent);
		
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMissileWeaponDamageMult().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		float fluxPercent = FLUX_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% missile weapon damage" , false);
		}
		if (index == 1) {
			return new StatusData("+" + (int) fluxPercent + "% missile weapon flux generation" , false);
		}
		return null;
	}
}
