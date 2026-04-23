package data.scripts.hullmods;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class TTSC_ocbeams extends BaseHullMod {

	public static final float BEAM_RANGE_BONUS = 50f;
	public static final float BEAM_DAMAGE_BONUS = 10f;
	public static final float BEAM_TURN_BONUS = 15f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
		stats.getBeamWeaponDamageMult().modifyPercent(id, BEAM_DAMAGE_BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyMult(id, 1f - BEAM_TURN_BONUS * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_RANGE_BONUS;
		if (index == 1) return "" + (int) BEAM_DAMAGE_BONUS + "%";
		if (index == 2) return "" + (int) BEAM_TURN_BONUS + "%";
		return null;
	}
	
}
