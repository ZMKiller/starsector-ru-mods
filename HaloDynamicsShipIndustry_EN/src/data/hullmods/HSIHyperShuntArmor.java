package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

public class HSIHyperShuntArmor extends BaseHullMod {
    //protected static final float EFFECTIVE_ARMOR_BONUS = 1f;

    protected static final float MAX_ARMOR_REDUCTION_BONUS = 0.08f;
    protected static final float BEAM_DAMAGE_TAKEN_BUFF = 0.25f;
    //protected static final float HIGH_EXPLOSIVE_TAKEN_BUFF = 0.5f;

    protected static final float DAMAGE_TO_SHIP_DEBUFF = 0.5f;
    protected static final float REPAIR_SPEED_DEBUFF = 0.5f;

    protected static final float HARD_FLUX_FRAC = 30f;
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getEffectiveArmorBonus().modifyMult(id,1f+EFFECTIVE_ARMOR_BONUS);
        stats.getMaxArmorDamageReduction().modifyFlat(id,MAX_ARMOR_REDUCTION_BONUS);
        stats.getDamageToCapital().modifyMult(id,1f-DAMAGE_TO_SHIP_DEBUFF);
        stats.getDamageToCruisers().modifyMult(id,1f-DAMAGE_TO_SHIP_DEBUFF);
        stats.getDamageToDestroyers().modifyMult(id,1f-DAMAGE_TO_SHIP_DEBUFF);
        stats.getDamageToFrigates().modifyMult(id,1f-DAMAGE_TO_SHIP_DEBUFF);
        stats.getBeamDamageTakenMult().modifyMult(id,1f-BEAM_DAMAGE_TAKEN_BUFF);
        stats.getRepairRatePercentPerDay().modifyMult(id,1f-REPAIR_SPEED_DEBUFF);
        stats.getHardFluxDissipationFraction().modifyFlat(id,HARD_FLUX_FRAC/100f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(HSITurbulanceShieldListenerV2.hasShield(ship)){
            HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
            if(shield.getShield().getShieldLevel()<0.5f){
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF,2f);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0){
            return ""+(int)(MAX_ARMOR_REDUCTION_BONUS*100f)+"%";
        }
        if(index == 1){
            return ""+(int)(BEAM_DAMAGE_TAKEN_BUFF*100f)+"%";
        }
        //if(index == 2){
            //return ""+(int)(HIGH_EXPLOSIVE_TAKEN_BUFF*100f)+"%";
        //}
        if(index == 2){
            return ""+(int)(HARD_FLUX_FRAC)+"%";
        }
        if(index == 3){
            return ""+(int)(DAMAGE_TO_SHIP_DEBUFF*100f)+"%";
        }
        if(index == 4){
            return ""+(int)(REPAIR_SPEED_DEBUFF*100f)+"%";
        }
        return null;
    }
}
