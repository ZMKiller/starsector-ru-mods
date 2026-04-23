package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SFCOverclock extends BaseShipSystemScript {

	public static final float RANGE_BONUS = 0.25f;
	public static final float ROF_BONUS = 1f;
	public static final float FLUX_REDUCTION = 75f;
	public static final float STAT_NEGATIVE = 50f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + ROF_BONUS * effectLevel;
		float mult1 = 1f + RANGE_BONUS * effectLevel;
		stats.getEnergyRoFMult().modifyMult(id, mult);
		stats.getEnergyProjectileSpeedMult().modifyMult(id, mult);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getEnergyWeaponRangeBonus().modifyMult(id, mult1);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticProjectileSpeedMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getBallisticWeaponRangeBonus().modifyMult(id, mult1);
		stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
		stats.getEnergyAmmoRegenMult().modifyMult(id, mult);
		stats.getWeaponTurnRateBonus().modifyMult(id, mult);
		stats.getMaxSpeed().modifyMult(id, 1f - (STAT_NEGATIVE * 0.01f));
		stats.getTurnAcceleration().modifyMult(id, 1f - (STAT_NEGATIVE * 0.01f));
		stats.getAcceleration().modifyMult(id, 1f - (STAT_NEGATIVE* 0.01f));
		
//		ShipAPI ship = (ShipAPI)stats.getEntity();
//		ship.blockCommandForOneFrame(ShipCommand.FIRE);
//		ship.setHoldFireOneFrame(true);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().unmodify(id);
		stats.getEnergyProjectileSpeedMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticProjectileSpeedMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getEnergyAmmoRegenMult().unmodify(id);
		stats.getWeaponTurnRateBonus().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("boosted weapon performance", false);
		}
		if (index == 1) {
			return new StatusData("decreased mobility", false);
		}
		/*if (index == 0) {
			return new StatusData("weapons rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("projectile speed +" + (int) bonusPercent + "%", false);
		}
		if (index == 2) {
			return new StatusData("weapons flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		if (index == 3) {
			return new StatusData("mobility decreased -" + (int) STAT_NEGATIVE + "%", true);
		}*/
		return null;
	}
}
