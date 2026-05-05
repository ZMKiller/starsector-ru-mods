package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSIACE extends BaseShipSystemScript {
    private ShipAPI ship;
    //private boolean once = true;
    private CombatEngineAPI engine = Global.getCombatEngine();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        float decreaseFlux = ship.getMaxFlux()*engine.getElapsedInLastFrame()*0.25f;
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.getSpec().getWeaponId().equals("HWI_HammerOfHumanity")){
                weapon.getDamage().getModifier().modifyMult(id, 2.5f);
            }
        }
        if(effectLevel>=1&&ship.getFluxTracker().getCurrFlux()>0){
            if(ship.getFluxTracker().getCurrFlux()>decreaseFlux){
                ship.getFluxTracker().decreaseFlux(decreaseFlux);
            }else{
                ship.getFluxTracker().setCurrFlux(0);
            }
        }
        if(state==State.OUT&&ship.getCurrFlux()>0){
            ship.getMutableStats().getVentRateMult().modifyPercent(id, 100f);
            ship.giveCommand(ShipCommand.VENT_FLUX, 0, 0);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        stats.getFluxDissipation().unmodify(id);
        ship.getFluxTracker().stopVenting();
        ship.getMutableStats().getVentRateMult().unmodify(id);
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.getSpec().getWeaponId().equals("HWI_HammerOfHumanity")){
                weapon.getDamage().getModifier().unmodify(id);
            }
        }
    }

}
