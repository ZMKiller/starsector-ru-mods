package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HSIDroneGroundSupport extends BaseHullMod {
    public static final String DGS = "HSI_DGS";
    public static final float GROUND_BONUS = 150;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int num = getDGSwings(stats);
        if (num > 0) {
            stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, GROUND_BONUS*num);
        }else{
            stats.getVariant().removeMod(this.spec.getId());
        }
        // stats.getDynamic().getMod(Stats.FLEET_BOMBARD_COST_REDUCTION).modifyFlat(id,
        // GROUND_BONUS);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) GROUND_BONUS;
        return null;
    }

    public static int getDGSwings(MutableShipStatsAPI stats) {
        int num = 0;
        for (String wing : stats.getVariant().getWings()) {
            if (Global.getSettings().getFighterWingSpec(wing) != null
                    && Global.getSettings().getFighterWingSpec(wing).hasTag(DGS)) {
                num++;
            }
        }
        return num;
    }
}
