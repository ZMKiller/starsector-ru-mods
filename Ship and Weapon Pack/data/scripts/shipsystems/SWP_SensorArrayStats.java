package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SWP_SensorArrayStats extends BaseShipSystemScript {

    public static final float SENSOR_RANGE_PERCENT = 100f;
    public static final float WEAPON_RANGE_PERCENT = 50f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;

        stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);

        stats.getBallisticWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, weaponRangePercent);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float weaponRangePercent = WEAPON_RANGE_PERCENT * effectLevel;
        switch (index) {
            case 0:
                return new StatusData("sensor range +" + (int) sensorRangePercent + "%", false);
            case 1:
                return null;
            case 2:
                return new StatusData("weapon range +" + (int) weaponRangePercent + "%", false);
            default:
                break;
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().unmodify(id);

        stats.getBallisticWeaponRangeBonus().unmodify(id);
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
    }
}
