package data.campaign.HSIStellaArena.Buff;

import com.fs.starfarer.api.combat.ShipAPI;

public class HSISASunShine implements HSISABuff{

    @Override
    public void applyToShip(ShipAPI ship) {

    }

    @Override
    public void advance(ShipAPI ship, float amount) {
        if(ship.getHitpoints()<ship.getMaxHitpoints()){
            ship.setMaxHitpoints(Math.min(ship.getMaxHitpoints(), ship.getMaxHitpoints()*0.02f*amount+ship.getHitpoints()));
        }

        if(ship.getFluxTracker().getCurrFlux()>0){
            ship.getFluxTracker().decreaseFlux(ship.getMaxFlux()*0.02f*amount);
        }
    }
}
