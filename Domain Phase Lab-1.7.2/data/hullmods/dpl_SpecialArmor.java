package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class dpl_SpecialArmor extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 25f);
		mag.put(HullSize.DESTROYER, 20f);
		mag.put(HullSize.CRUISER, 15f);
		mag.put(HullSize.CAPITAL_SHIP, 15f);
	}
	
	private static final float ARMOR_MULT = 0.8f;
	
	//private static final float ACCELERATION_BONUS = 100f;
	//private static final float EXTRA_DAMAGE = 300f;
	//private static final int BURN_LEVEL_BONUS = 1;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) mag.get(hullSize));
		stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
		stats.getShieldDamageTakenMult().modifyMult(id, 2f-ARMOR_MULT);
		
		//stats.getAcceleration().modifyPercent(id, ACCELERATION_BONUS);
		//stats.getEngineDamageTakenMult().modifyPercent(id, EXTRA_DAMAGE);
		
		//stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) Math.round((1f - ARMOR_MULT) * 100f) + "%";
		if (index == 5) return "" + (int) Math.round((1f - ARMOR_MULT) * 100f) + "%";
//		if (index == 4) return "" + (int) ACCELERATION_BONUS;
//		//if (index == 5) return "four times";
//		if (index == 5) return "4" + Strings.X;
//		if (index == 6) return "" + BURN_LEVEL_BONUS;
		return null;
	}
	

}
