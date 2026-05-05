package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSIReplenish extends BaseShipSystemScript{
    private ShipAPI ship;
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        ship.getMutableStats().getHullDamageTakenMult().modifyMult(id, (1-effectLevel));
        if(effectLevel>=1){
            float range = Global.getCombatEngine().getMapHeight()/1.5f;
            ship.setRetreating(false, false);
            ship.getLocation().set(ship.getLocation().getX(),((ship.getOwner()==0)?(-1):1)*range);
            ship.setFacing(((ship.getOwner()==0)?90f:270f));
        }else{
            if(state == State.OUT){
                ship.turnOnTravelDrive(Global.getCombatEngine().getElapsedInLastFrame()*1.5f);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.isDecorative()) continue;
            weapon.repair();
            if(weapon.usesAmmo()) weapon.setAmmo(weapon.getMaxAmmo());
        }
    }
}
