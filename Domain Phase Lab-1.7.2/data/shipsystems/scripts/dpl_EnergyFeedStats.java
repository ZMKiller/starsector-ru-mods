package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class dpl_EnergyFeedStats extends BaseShipSystemScript {

	public static final float DAMAGE_BONUS = 0.5f;
	public static final float FLUX_REDUCTION = 50f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		float mult = 1f + DAMAGE_BONUS * effectLevel;
		stats.getEnergyWeaponDamageMult().modifyMult(id,mult);
		stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		
//		ShipAPI ship = (ShipAPI)stats.getEntity();
//		ship.blockCommandForOneFrame(ShipCommand.FIRE);
//		ship.setHoldFireOneFrame(true);
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getEnergyRoFMult().unmodify(id);
		stats.getEnergyWeaponFluxCostMod().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + DAMAGE_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("energy weapon damage +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("energy flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		return null;
	}
}
