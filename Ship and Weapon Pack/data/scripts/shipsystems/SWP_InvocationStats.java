package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SWP_InvocationStats extends BaseShipSystemScript {

    public static final float ROF_BONUS = 3f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getEnergyRoFMult().modifyMult(id, ROF_BONUS);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyRoFMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float bonusPercent = (int) ((ROF_BONUS - 1f) * 100f);
        if (index == 0) {
            return new StatusData("energy rate of fire +" + (int) bonusPercent + "%", false);
        }
        return null;
    }
}
