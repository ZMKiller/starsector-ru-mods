package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class CJHM_fetchdeck extends BaseHullMod {

    public static final float FIGHTER_RANGE_BOOST = 250f;
	public static final float RATE_DECREASE_MODIFIER = 10f;	

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterWingRange().modifyFlat(id, FIGHTER_RANGE_BOOST);
		stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id, 1f - RATE_DECREASE_MODIFIER / 100f);		
    }
    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + Math.round(FIGHTER_RANGE_BOOST);
       	if (index == 1) return "" + (int) RATE_DECREASE_MODIFIER + "%";
            return null;
        }
 
}
