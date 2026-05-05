package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CJHM_overclockshields extends BaseHullMod {

	public static final float SHIELD_BONUS_TURN = 60f;
	public static final float SHIELD_BONUS_UNFOLD = 60f;
	public static final float OVERLOAD_REDUCTION = -20f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_BONUS_TURN);
		stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);
		stats.getOverloadTimeMod().modifyMult(id, 1f - (OVERLOAD_REDUCTION / 100f));		
	}
	
   @Override	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_BONUS_TURN + "%";
		if (index == 1) return "" + (int) OVERLOAD_REDUCTION + "%";
		return null;

	}	
	
}
