package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.Map;


public class CJHM_adaptivedamping extends BaseHullMod {
	
	public static final float ROF_REDUCE = 5f;
	public static final float RANGE_BONUS = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().modifyMult(id, 1f - (ROF_REDUCE * 0.01f));
		stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) RANGE_BONUS;
        if (index == 1) return "" + (int) ROF_REDUCE + "%";
        return null;
    }
}
