package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HSITimeAnchor extends BaseHullMod {
    protected static final float CR_LOSS_DECREASE = 33f;
    protected static final String KEY = "HSI_TimeAnchor_Fix";

    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id) {
        stats.getCRLossPerSecondPercent().modifyMult(id, (1f - CR_LOSS_DECREASE / 100f));
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        MutableStat time = ship.getMutableStats().getTimeMult();
        if (time.getModifiedValue() > time.getBaseValue()&&ship.getPeakTimeRemaining()>0&&ship.areSignificantEnemiesInRange()) {
                float curr = 0;
                if (ship.getMutableStats().getPeakCRDuration().getFlatBonus(KEY) != null) {
                    curr = ship.getMutableStats().getPeakCRDuration().getFlatBonus(KEY).getValue();
                }
            /*Global.getLogger(this.getClass()).info(
                    "TIMEMULT:M:"+time.getModifiedValue()
                    +"||B:"+time.getBaseValue()
                    +"||AMT:"+amount
                    +"||EAMT:"+Global.getCombatEngine().getElapsedInLastFrame()
                    +"||ETIMEMULT:"+Global.getCombatEngine().getTimeMult().getModifiedValue()
                    +"||TOTALELAPSED:"+Global.getCombatEngine().getTotalElapsedTime(false)
                    +"||PEAK:"+ship.getPeakTimeRemaining()
                    +"||TESTBUFF:"+(time.getModifiedValue()-time.getBaseValue())/time.getModifiedValue()*amount
            );*/
            //Global.getLogger(this.getClass()).info("PEAKTIM:M:"+time.getModifiedValue()+" B:"+time.getBaseValue()+" AMT:"+amount+" EAMT:"+Global.getCombatEngine().getElapsedInLastFrame());

            ship.getMutableStats().getPeakCRDuration().modifyFlat(KEY, curr +(time.getModifiedValue()-time.getBaseValue())/time.getModifiedValue()*amount);
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) CR_LOSS_DECREASE + "%";
        return null;
    }
}
