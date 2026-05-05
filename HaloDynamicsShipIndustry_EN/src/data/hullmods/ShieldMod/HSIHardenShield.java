package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.HSITurbulanceShieldListenerV2.ParamType;

import java.util.HashMap;
import java.util.Map;

public class HSIHardenShield extends HSIBaseShieldModEffect {
    protected static final int REDUCE_THRESHOLD = 400;
    protected static final float EFFICIENCY_BUFF = 0.75f;

    protected  static  final float MAX_REDUCE_PERCENT = 0.15f;

    protected static final float SHIELD_LIM_REDUCTION = 10f;

    protected static Map<HullSize,Float> ARMOR_FLAT = new HashMap<>();
    static {
        ARMOR_FLAT.put(HullSize.FIGHTER,10f);
        ARMOR_FLAT.put(HullSize.FRIGATE,15f);
        ARMOR_FLAT.put(HullSize.DESTROYER,20f);
        ARMOR_FLAT.put(HullSize.CRUISER,27f);
        ARMOR_FLAT.put(HullSize.CAPITAL_SHIP,35f);
    }

    @Override
    public float processShieldEffect(float processedDamage, ParamType type, DamageAPI damage, Vector2f point,ShipAPI ship) {
        if (processedDamage >= REDUCE_THRESHOLD) {
            float couldReduce = processedDamage-REDUCE_THRESHOLD;
            float toReduce = 0f;
            if(couldReduce > 0){
                toReduce = couldReduce*MAX_REDUCE_PERCENT;
            }
            return processedDamage - toReduce;
        }
        return processedDamage;
    }

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldEffciency().modifyMult(getStandardId(shield), EFFICIENCY_BUFF);
        //shield.getStats().getShieldCap().modifyMult(getStandardId(shield),(100f-SHIELD_LIM_REDUCTION)/100f);
        //shield.getStats().getShieldArmorValue().modifyFlat(getStandardId(shield),ARMOR_FLAT.get(shield.getShip().getHullSize()));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) (100f-EFFICIENCY_BUFF*100f)+"%";
        //if( index == 1)
            //return ""+(int)(float)(ARMOR_FLAT.get(hullSize));
        if(index == 1)
            return ""+(int)(REDUCE_THRESHOLD);
        if(index == 2)
            return ""+(int)(MAX_REDUCE_PERCENT*100f)+"%";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship)&&!AjimusUtils.hasIncompatible(Incompatible,spec,ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if(!super.isApplicableToShip(ship)) {
            return super.getUnapplicableReason(ship);
        }else{
            return HSII18nUtil.getHullModString("HSIShieldModIncompatible");
        }
    }
}
