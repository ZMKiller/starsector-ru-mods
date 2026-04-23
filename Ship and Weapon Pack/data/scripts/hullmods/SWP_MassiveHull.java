package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SWP_MassiveHull extends BaseHullMod {

    public static final float SENSOR_MOD = 60f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getSensorStrength().modifyFlat(id, SENSOR_MOD);
        stats.getSensorProfile().modifyFlat(id, SENSOR_MOD);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) SENSOR_MOD;
        }
        return null;
    }
}
