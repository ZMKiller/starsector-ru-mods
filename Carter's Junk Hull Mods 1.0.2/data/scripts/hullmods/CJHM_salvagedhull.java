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

public class CJHM_salvagedhull extends BaseHullMod 
{
	public static final float HULL_BONUS = -15f;
	public static final float AMMO_BONUS = 20f;
	
    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) 
	{
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getBallisticAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getEnergyAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);		
	}
	
    @Override	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		if (index == 1) return "" + (int) HULL_BONUS + "%";
		return null;

	}


}
