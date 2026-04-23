package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SFCAugmentedRangeStats extends BaseShipSystemScript {

	public static final float RANGE_BONUS = 0.5f;
	public static final float PROJECTILE_SPEED_BONUS = 1f;
	public static final float ROF_BOOST = 1.25f;
	public static final float WEAPON_DECREASE = 0.75f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

		float mult1 = 1f + RANGE_BONUS * effectLevel;
		float mult2 = 1f + PROJECTILE_SPEED_BONUS * effectLevel;
		float mult3 = ROF_BOOST;
		float mult4 = WEAPON_DECREASE;
		stats.getBallisticWeaponRangeBonus().modifyMult(id, mult1);
		stats.getBallisticProjectileSpeedMult().modifyMult(id, mult2);
		stats.getBallisticRoFMult().modifyMult(id, mult3);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, mult4);
		stats.getMaxRecoilMult().modifyMult(id, mult4);
		stats.getRecoilPerShotMult().modifyMult(id, mult4);
		stats.getRecoilDecayMult().modifyMult(id, mult4);

//		ShipAPI ship = (ShipAPI)stats.getEntity();
//		ship.blockCommandForOneFrame(ShipCommand.FIRE);
//		ship.setHoldFireOneFrame(true);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getBallisticProjectileSpeedMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getMaxRecoilMult().unmodify(id);
		stats.getRecoilPerShotMult().unmodify(id);
		stats.getRecoilDecayMult().unmodify(id);
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("ballistics weapons performance improved", false);
		}
		/*float mult1 = 1f + RANGE_BONUS * effectLevel;
		float bonusPercent1 = (int) ((mult1 - 1f) * 100f);
		float mult2 = 1f + PROJECTILE_SPEED_BONUS * effectLevel;
		float bonusPercent2 = (int) ((mult2 - 1f) * 100f);
		if (index == 0) {
			return new StatusData("ballistics weapon range +" + (int) bonusPercent1 + "%", false);
		}
		if (index == 1) {
			return new StatusData("ballistics projectile speed +" + (int) bonusPercent2 + "%", false);
		}*/
		return null;
	}
}
