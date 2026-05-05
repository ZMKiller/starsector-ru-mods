package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIEliteForge extends HSIBaseShieldModEffect{

    private static final float SHIELD_CAP_BUFF = 5f;
    private static final float SHIELD_REG_BUFF = 5f;

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldBlockWhenVenting().modifyFlat(getStandardId(shield),2f);
        shield.getStats().getShieldCap().modifyPercent(getStandardId(shield),SHIELD_CAP_BUFF);
        shield.getStats().getShieldRegen().modifyPercent(getStandardId(shield),SHIELD_REG_BUFF);
        
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if(index == 0){
            return (int)SHIELD_CAP_BUFF+"%";
        }
        if(index == 1){
            return (int)SHIELD_REG_BUFF+"%";
        }
        return null;
    }
}
