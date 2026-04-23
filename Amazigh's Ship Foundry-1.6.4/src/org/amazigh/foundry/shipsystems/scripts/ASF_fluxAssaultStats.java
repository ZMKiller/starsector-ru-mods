package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_fluxAssaultStats extends BaseShipSystemScript {
	
	public static final float DAMAGE_BONUS_PERCENT = 35f;

	public static final float ACC_BOOST = 160f;
	public static final float MAN_BOOST = 80f;
	public static final float TURN_FLAT = 20f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyPercent(id, bonusPercent);
		
		stats.getAcceleration().modifyPercent(id, ACC_BOOST * effectLevel);
		stats.getDeceleration().modifyPercent(id, ACC_BOOST * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, MAN_BOOST * effectLevel);
		stats.getMaxTurnRate().modifyFlat(id, TURN_FLAT * effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().unmodify(id);
		
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float bonusPercent = DAMAGE_BONUS_PERCENT * effectLevel;
		if (index == 0) {
			return new StatusData("+" + (int) bonusPercent + "% energy weapon damage" , false);
		} else if (index == 1) {
			return new StatusData("improved maneuverability", false);
		}
		return null;
	}
}