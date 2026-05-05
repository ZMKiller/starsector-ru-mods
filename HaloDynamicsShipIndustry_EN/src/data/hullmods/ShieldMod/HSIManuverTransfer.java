package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIManuverTransfer extends HSIBaseShieldModEffect{

    protected static final float SHIELD_EFFICIENCY_DEBUFF = 40f;

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldEffciency().modifyPercent(getStandardId(shield),SHIELD_EFFICIENCY_DEBUFF);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)SHIELD_EFFICIENCY_DEBUFF+"%";
        return null;
    }
}
