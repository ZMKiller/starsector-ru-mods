package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSITeleportDevice extends BaseShipSystemScript{
    private ShipAPI ship;
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for(FighterWingAPI wing:ship.getAllWings()){
            
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
       
    }
}
