package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_bunkerShieldStats extends BaseShipSystemScript {
	
	public static float DAMAGE_MULT = 0.85f;
	public static float UPKEEP_MULT = 0.5f;
	
	public static final float HARD_DISS = 25f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - DAMAGE_MULT * effectLevel);
		stats.getShieldUpkeepMult().modifyMult(id, 1f - UPKEEP_MULT * effectLevel);
		
		stats.getHardFluxDissipationFraction().modifyFlat(id, effectLevel * HARD_DISS * 0.01f);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
		
		stats.getHardFluxDissipationFraction().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("shield absorbs 8x damage", false);
		}
		return null;
	}
}
