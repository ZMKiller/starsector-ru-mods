package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class CJHM_mountstabilizer extends BaseHullMod {
	
    public static final float SHIP_TURN_ACCEL_PENALTY = 5f;
	
    private static Map<Object, Float> TURN_SPEED = new HashMap<Object, Float>();
    static {
        TURN_SPEED.put(HullSize.FRIGATE, 1.7f);
        TURN_SPEED.put(HullSize.DESTROYER, 1.5f);
        TURN_SPEED.put(HullSize.CRUISER, 1.4f);
        TURN_SPEED.put(HullSize.CAPITAL_SHIP, 1.3f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponTurnRateBonus().modifyMult(id, (Float) TURN_SPEED.get(hullSize));
		stats.getWeaponTurnRateBonus().modifyMult(id, (Float) TURN_SPEED.get(hullSize));
        stats.getTurnAcceleration().modifyPercent(id, -SHIP_TURN_ACCEL_PENALTY);
    }


    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return (Float) TURN_SPEED.get(HullSize.FRIGATE) + "";
        if (index == 1) return (Float) TURN_SPEED.get(HullSize.DESTROYER) + "";
        if (index == 2) return (Float) TURN_SPEED.get(HullSize.CRUISER) + "";
        if (index == 3) return (Float) TURN_SPEED.get(HullSize.CAPITAL_SHIP) + "";
        if (index == 4) return Math.round(SHIP_TURN_ACCEL_PENALTY) + "%";
		
        return null;
    }
}