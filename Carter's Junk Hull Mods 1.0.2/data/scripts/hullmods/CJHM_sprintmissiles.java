package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CJHM_sprintmissiles extends BaseHullMod {

	public final int Maxspeed = 30;
	public final int Maxturn = 25;
	public final int Hpreduct = -25;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileMaxSpeedBonus().modifyPercent(id, Maxspeed);
		stats.getMissileAccelerationBonus().modifyPercent(id, Maxspeed);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, Maxturn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, Maxturn);
		stats.getMissileHealthBonus().modifyPercent(id, Hpreduct);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {	
        if (index == 0) return Maxspeed + "%";
		if (index == 1) return Maxturn + "%"; 
		if (index == 2) return Hpreduct + "%";			
        return null;
    }
}

			
			