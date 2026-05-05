package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.HSITurbulanceShieldListenerV2;

import java.util.HashMap;
import java.util.Map;

public class HSIArmoredShield extends HSIBaseShieldModEffect{

    protected static Map<ShipAPI.HullSize,Float> ARMOR_VALUE = new HashMap<>();
    static{
        ARMOR_VALUE.put(ShipAPI.HullSize.FRIGATE,100f);
        ARMOR_VALUE.put(ShipAPI.HullSize.DESTROYER,125f);
        ARMOR_VALUE.put(ShipAPI.HullSize.CRUISER,150f);
        ARMOR_VALUE.put(ShipAPI.HullSize.CAPITAL_SHIP,200f);
    }

    //protected static final float SHIELD_DEBUFF = 0.1f;
    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getShield().setEngageArmorProcess(true);
        shield.getStats().getShieldArmorValue().modifyFlat(getStandardId(shield),getAromorValueFlat(shield.getShip().getHullSize()));
        //shield.getStats().getShieldCap().modifyMult(getStandardId(shield),(1-SHIELD_DEBUFF));
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)getAromorValueFlat(hullSize)+"";
        //if(index == 1) return (int)(SHIELD_DEBUFF*100f)+"%";
        return null;
    }

    protected static float getAromorValueFlat(ShipAPI.HullSize hullsize){
        return ARMOR_VALUE.get(hullsize);
    }
}
