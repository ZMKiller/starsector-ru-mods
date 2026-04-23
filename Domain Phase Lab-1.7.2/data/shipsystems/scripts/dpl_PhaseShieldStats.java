package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class dpl_PhaseShieldStats extends BaseShipSystemScript {

	public static float DAMAGE_MULT = 0.875f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - DAMAGE_MULT * effectLevel);
		
		stats.getShieldUpkeepMult().modifyMult(id, 0f);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getShieldArcBonus().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getShieldTurnRateMult().unmodify(id);
		stats.getShieldUnfoldRateMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("shield absorbs 8x damage", false);
		}
		return null;
	}
}
