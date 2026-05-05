package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HIVER_dHiveMind extends BaseHullMod {

	public static final float BONUS = -10f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getHullBonus().modifyPercent(id, BONUS);
		stats.getArmorBonus().modifyPercent(id, BONUS);		
		stats.getCargoMod().modifyPercent(id, BONUS);	
		stats.getFuelMod().modifyPercent(id, BONUS);		
		stats.getShieldTurnRateMult().modifyPercent(id, BONUS);
		stats.getShieldUnfoldRateMult().modifyPercent(id, BONUS);		
		stats.getEnergyWeaponDamageMult().modifyPercent(id,BONUS);
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id,-BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id,BONUS);	
        stats.getEnergyRoFMult().modifyPercent(id, BONUS);	
        stats.getWeaponHealthBonus().modifyPercent(id, BONUS);		
        stats.getEngineHealthBonus().modifyPercent(id, BONUS);		
		stats.getZeroFluxSpeedBoost().modifyPercent(id, BONUS);	
		stats.getSightRadiusMod().modifyPercent(id, BONUS);
		stats.getSensorStrength().modifyPercent(id, BONUS);	
		stats.getFluxCapacity().modifyPercent(id, BONUS);
		stats.getFluxDissipation().modifyPercent(id, BONUS);	
		stats.getMaxSpeed().modifyPercent(id, BONUS);	
		stats.getWeaponTurnRateBonus().modifyPercent(id, BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyPercent(id, BONUS);	
		stats.getDamageToMissiles().modifyPercent(id, BONUS);
		stats.getDamageToFighters().modifyPercent(id, BONUS);		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BONUS + "%";
		
		return null;
	}
 }
