package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SFCShieldBoosterStats extends BaseShipSystemScript {

	public static float DAMAGE_MULT = 0.95f;
	public static float SHIELD_MULT = 2f;
	public static float ROF_BONUS = 0.5f;
	//public static float DAMAGE_MULT = 0.8f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		//stats.getShieldTurnRateMult().modifyMult(id, 1f);
		//stats.getShieldUnfoldRateMult().modifyPercent(id, 2000);
		
		//stats.getShieldDamageTakenMult().modifyMult(id, 0.1f);
		stats.getShieldArcBonus().modifyMult(id, SHIELD_MULT);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - DAMAGE_MULT * effectLevel);
		stats.getShieldTurnRateMult().modifyMult(id, 2f);
		stats.getShieldUnfoldRateMult().modifyMult(id, 2f);
		stats.getShieldUpkeepMult().modifyMult(id, 0f);
		stats.getEnergyRoFMult().modifyMult(id, ROF_BONUS);
		stats.getBallisticRoFMult().modifyMult(id, ROF_BONUS);
		
		//System.out.println("level: " + effectLevel);
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		//stats.getShieldAbsorptionMult().unmodify(id);
		stats.getShieldArcBonus().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
		stats.getShieldTurnRateMult().unmodify(id);
		stats.getShieldUnfoldRateMult().unmodify(id);
		stats.getShieldUpkeepMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("boosted shield performance", false);
		}
//		else if (index == 1) {
//			return new StatusData("shield upkeep reduced to 0", false);
//		} else if (index == 2) {
//			return new StatusData("shield upkeep reduced to 0", false);
//		}
		return null;
	}
}
