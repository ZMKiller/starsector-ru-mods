package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.kit.HSITimeImageListener;

public class HSITimeShift extends BaseShipSystemScript{
    private ShipAPI ship;
    private HSITimeImageListener time;
    private boolean once = false;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(state == State.ACTIVE&&!once){
            time.goback();
            once = true;
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(time==null){
            time = new HSITimeImageListener(ship);
            ship.addListener(time);
        }
        once = false;
    }
}
