package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.VectorUtils;

public class SWP_SuperJetStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (state == State.IN && effectLevel > 0f) {
            ship.setCollisionClass(CollisionClass.NONE);
            if (ship.getVelocity().length() <= 0.1f) {
                ship.getVelocity().set(VectorUtils.getDirectionalVector(ship.getLocation(), ship.getMouseTarget()));
            }
            Misc.normalise(ship.getVelocity());
            ship.getVelocity().scale(1000f);
        } else if (state == State.ACTIVE) {
            ship.setCollisionClass(CollisionClass.NONE);
        } else {
            ship.setCollisionClass(CollisionClass.SHIP);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }
}
