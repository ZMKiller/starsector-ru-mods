package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSIRapidReinforcement extends BaseShipSystemScript {

    //private List<ShipAPI> ftrs = new ArrayList<>();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (effectLevel == 1) {
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                bay.makeCurrentIntervalFast();
            }
        }
    }
}
