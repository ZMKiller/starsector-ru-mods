package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.StatusData;

public class dpl_MissileFluxStats extends BaseShipSystemScript {

	public static final float FLUX_REDUCTION = 80f;
	public static final float SPEED_REDUCTION = 95f;
	public static final float DAMAGE_BUFF = 25f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getMaxSpeed().modifyMult(id, 1f - (SPEED_REDUCTION * 0.01f));
		stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));
		stats.getMissileWeaponDamageMult().modifyMult(id, 1f + (DAMAGE_BUFF * 0.01f));
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMissileWeaponDamageMult().unmodify(id);
		stats.getMissileWeaponFluxCostMod().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("missile flux use -" + (int) FLUX_REDUCTION + "%", false);
		}
		if (index == 1) {
			return new StatusData("missile damage +" + (int) DAMAGE_BUFF + "%", false);
		}
		if (index == 2) {
			return new StatusData("ship max speed -" + (int) SPEED_REDUCTION + "%", false);
		}
		return null;
	}
}
