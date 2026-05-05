package data.hullmods.WeaponMod;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIProtocalAssault extends HSIBaseWeaponModEffect{
    protected static final float BUFF = 10f;

    protected static final float lim = 0.6f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        float level = Math.min(1,ship.getFluxLevel()/lim);
        ship.getMutableStats().getBallisticRoFMult().modifyPercent(getStandardId(ship),BUFF*level);
        ship.getMutableStats().getEnergyRoFMult().modifyPercent(getStandardId(ship),BUFF*level);

        if(isSMod(ship)){
            ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult(getStandardId(ship),(100f-BUFF*level)/100f);
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(getStandardId(ship),(100f-BUFF*level)/100f);
        }
        //ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult(getStandardId(ship),(100f-BUFF*level)/100f);
        //ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(getStandardId(ship),(100f-BUFF*level)/100f);

    }

    @Override
    public boolean hasSModEffect() {
        return true;
    }


    @Override
    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if(index == 0) return (int)BUFF+"%";
        return null;
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==0) return (int)BUFF+"%";
        if(index==1) return (int)(lim*100f)+"%";
        if(index==2) return (int)(600)+"";
        return null;
    }

}
