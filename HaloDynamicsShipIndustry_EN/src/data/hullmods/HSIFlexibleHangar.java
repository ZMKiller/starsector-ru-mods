package data.hullmods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WingRole;

public class HSIFlexibleHangar extends BaseHullMod {
    protected static final float REFIT_PUNISHMENT = 10f;
    protected static final float CR_PUNISHMENT = 20f;
    protected static final float CR_PUNISHMENT_EXTRA = 25f;
    protected static final float REFIT_BONUS = 1.5f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id) {
        List<String> wings = stats.getVariant().getNonBuiltInWings();
        int wingCount = wings.size();
        if (wingCount < 6) {
            stats.getNumFighterBays().unmodify(id);
        }else if (wingCount >= 6 && wingCount < 12) {
            stats.getNumFighterBays().modifyFlat(id, wingCount - 5);
        }else{
            stats.getNumFighterBays().modifyFlat(id, 6);
        }
        Map<String, Integer> wingNum = new HashMap<>();
        for (String w : wings) {
            if (wingNum.containsKey(w)) {
                wingNum.put(w, wingNum.get(w) + 1);
            } else {
                wingNum.put(w, 1);
            }
        }
        int total = wings.size();
        // sendLog("total:"+total);
        float punishment = 0;
        int majority_legacy = 0;
        if (total > 6) {
            int majority = 0;
            int support = 0;
            int ex = total - 6;
            for (String key : wingNum.keySet()) {
                int specNum = wingNum.get(key);
                // sendLog(key+":"+specNum);
                if (specNum >= (int) (total * 2 / 3)) {
                    majority = specNum;
                    // sendLog("majority:"+key);
                }
                if (Global.getSettings().getFighterWingSpec(key) != null
                        && Global.getSettings().getFighterWingSpec(key).getRole() == WingRole.SUPPORT) {
                    support += specNum;
                }
            }
            majority_legacy = majority;
            // sendLog("support:"+support);
            punishment = ex;
            if (majority + support > 0) {
                if (majority > 4)
                    majority = 4;
                if (majority > ex)
                    majority = ex;
                // sendLog("finalMajority:"+majority);
                if (majority > 0) {
                    punishment = ex - majority / 2f;
                }
                if (support > 0) {
                    if (support > ex)
                        support = ex;
                    punishment = Math.max(0, punishment - support / 2f);
                }
                // sendLog("finalSupport:"+support);
            }
        }
        // sendLog("punishment:"+punishment);
        stats.getFighterRefitTimeMult().modifyMult(id, (1 + punishment * REFIT_PUNISHMENT / 100));
        if (wingCount <= 10) {
            stats.getMaxCombatReadiness().modifyFlat(id, -punishment * CR_PUNISHMENT / 100);
        } else {
            stats.getMaxCombatReadiness().modifyFlat(id, -punishment * CR_PUNISHMENT_EXTRA / 100);
        }
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_DECREASE_MULT).modifyMult(id,
                (1f - majority_legacy * REFIT_BONUS / 100f));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int)CR_PUNISHMENT + "%";
        if (index == 1)
            return "" + (int)CR_PUNISHMENT_EXTRA + "%";
        if (index == 2)
            return "" + (int)REFIT_PUNISHMENT + "%";
        if (index == 3)
            return "" + (int)REFIT_BONUS + "%";
        return null;
    }

    /*
     * private void sendLog(String info){
     * Global.getLogger(this.getClass()).info(info);
     * }
     */
}
