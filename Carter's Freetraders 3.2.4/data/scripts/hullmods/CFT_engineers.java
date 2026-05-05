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

public class CFT_engineers extends BaseHullMod
{
    public static final float INSTANT = 0.1f;
	public static float DMOD_EFFECT_MULT = 0.5f;
	public static float DMOD_AVOID_CHANCE = 25f;	
 
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
        stats.getDynamic().getMod(Stats.INSTA_REPAIR_FRACTION).modifyFlat(id, INSTANT);
		stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, (1f - DMOD_AVOID_CHANCE * 0.01f));		
    }

    @Override		
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) (INSTANT * 100) + "%";
		if (index == 1) return "" + (int) Math.round((1f - DMOD_EFFECT_MULT) * 100f) + "%";
		if (index == 2) return "" + (int) DMOD_AVOID_CHANCE + "%";		
		return null;		
    }
}
