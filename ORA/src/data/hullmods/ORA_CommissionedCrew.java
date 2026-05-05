package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import static data.scripts.util.ORA_txt.txt;

public class ORA_CommissionedCrew extends BaseHullMod {
    
    private final float MAX_BONUS=-4;
    private final float MIN_BONUS = -0.15f;
//    private final float MALUS = 15f;
    private final String DESC = txt("%");
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        
        if (index == 0) {
            return ""+MAX_BONUS;
        }
        if (index == 1) {
            return (int) (MIN_BONUS*100)+DESC;
        }
//        if (index == 2) {
//            return MALUS+DESC;
//        }
        return null;
    }
    
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        //cheaper maintenance
        if(stats.getSuppliesPerMonth().getBaseValue()>26.5){
            stats.getSuppliesPerMonth().modifyFlat(id, MAX_BONUS);
        } else {            
            stats.getSuppliesPerMonth().modifyMult(id, 1+MIN_BONUS);
        }
        //slower CR recovery
//        stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, -MALUS);
    }
}