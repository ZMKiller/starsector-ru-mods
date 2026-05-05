package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;


public class CJHM_reflectivearmor extends BaseHullMod {

	public static final float BEAM_DAMAGE_REDUCTION = .79f;
	public static final float SHIELD_UPKEEP_MULTIPLIER = 1.1f;
	public static final float SHIELD_UNFOLD_REDUCTION = 0.90f;	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamDamageTakenMult().modifyMult(id, BEAM_DAMAGE_REDUCTION);
		stats.getShieldUpkeepMult().modifyMult(id, SHIELD_UPKEEP_MULTIPLIER);
		stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_UNFOLD_REDUCTION);		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ((1f - BEAM_DAMAGE_REDUCTION) * 100f);
		if (index == 1) return "" + (int) ((1f - SHIELD_UPKEEP_MULTIPLIER) * -100f);
		if (index == 2) return "" + (int) ((1f - SHIELD_UNFOLD_REDUCTION) * 100f);		
		return null;
	}


}