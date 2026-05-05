package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class CJHM_combatadjustment extends BaseHullMod {
	
        public static final float FLUX_HANDLING_MULT = 1.05f;
		
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 50f);
		mag.put(HullSize.DESTROYER, 75f);
		mag.put(HullSize.CRUISER, 100f);
		mag.put(HullSize.CAPITAL_SHIP, 200f);

	}		
	
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNumFighterBays().modifyFlat(id, -1);
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
		
        stats.getFluxCapacity().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getFluxDissipation().modifyMult(id, FLUX_HANDLING_MULT);				
    }
    
    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
        if (index == 4) return "" + (int) ((FLUX_HANDLING_MULT - 0.99f) * 100f) + "%";
		return null;  
	}
	
    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return new Color(225, 193, 110);
    }	

	public boolean isApplicableToShip(ShipAPI ship) {
		int bays = (int) Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
		if (bays > 0) return true;
		return false;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship does not have fighter bays";
	}
}