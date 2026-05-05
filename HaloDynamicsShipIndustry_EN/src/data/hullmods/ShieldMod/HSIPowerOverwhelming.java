package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.hullmods.HSITurbulanceShieldListenerV2;

public class HSIPowerOverwhelming extends HSIBaseShieldModEffect{
    protected static final float SHIELDCAP_INCREASE = 60f;
    protected static final float SHIELDRECOVERYRATE_DECREASE = 0.5f;

    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldCap().modifyPercent(shield.getShip().getId()+"_"+spec.getId(), SHIELDCAP_INCREASE);
        shield.getStats().getShieldRecoveryRate().modifyMult(shield.getShip().getId()+"_"+spec.getId(), SHIELDRECOVERYRATE_DECREASE);
        //shield.getStats().getShieldRegenBlock().modifyFlat(shield.getShip().getId()+"_"+spec.getId(), 0.09f);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) SHIELDCAP_INCREASE+"%";
        if (index == 1)
            return "" + (int) (100f-SHIELDRECOVERYRATE_DECREASE*100f)+"%";
        return null;
    }
}
