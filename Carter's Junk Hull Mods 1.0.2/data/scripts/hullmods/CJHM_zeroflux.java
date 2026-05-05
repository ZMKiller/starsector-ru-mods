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

public class CJHM_zeroflux extends BaseHullMod 
{
	public static final float ZERO = 25f;
	
    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
	{	
		stats.getZeroFluxSpeedBoost().modifyPercent(id, ZERO);
	}
    @Override	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) ZERO + "%";
		return null;

	}		
}