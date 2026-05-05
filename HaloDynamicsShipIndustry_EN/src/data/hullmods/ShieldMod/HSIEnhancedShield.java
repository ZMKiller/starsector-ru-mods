package data.hullmods.ShieldMod;

import com.fs.starfarer.api.combat.ShipAPI;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;

public class HSIEnhancedShield extends HSIBaseShieldModEffect{
    protected static final float SHIELD_CAP = 25f;
    protected static final float SHIELD_RECOV = 100f;

    @Override
    public void applyShieldModificationsAfterShipCreation(HSITurbulanceShieldListenerV2 shield) {
        shield.getStats().getShieldCap().modifyPercent(getStandardId(shield),SHIELD_CAP);
        shield.getStats().getShieldRecoveryRate().modifyPercent(getStandardId(shield),SHIELD_RECOV);
    }


    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0)
            return (int) (SHIELD_CAP)+"%";
        if (index == 1)
            return (int) (SHIELD_RECOV) + "%";
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship)&& !AjimusUtils.hasIncompatible(Incompatible,spec,ship);
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
