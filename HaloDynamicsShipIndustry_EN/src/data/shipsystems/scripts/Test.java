package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.AmmoTrackerAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class Test extends BaseShipSystemScript {
    private ShipAPI ship;
    private boolean once = false;
    private static float INCREASE_PERCENTAGE = 0.5f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (state == State.ACTIVE && !once) {
            increaseFighterAmmo(ship);
            once = true;
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        once = false;
    }

    public void increaseFighterAmmo(ShipAPI ship) {
        if (ship == null)
            return;
        if (ship.getVariant().getFittedWings().isEmpty())
            return;
        for (FighterWingAPI wing : ship.getAllWings()) {
            for (ShipAPI fighter : wing.getWingMembers()) {
                for (WeaponAPI weapon : fighter.getAllWeapons()) {
                    if (weapon.getType() == WeaponType.MISSILE && weapon.usesAmmo()) {
                        AmmoTrackerAPI tracker = weapon.getAmmoTracker();
                        int maxAmmo = tracker.getMaxAmmo();
                        tracker.setAmmo(tracker.getAmmo() + Math.round(maxAmmo * INCREASE_PERCENTAGE));
                    }
                }
            }
        }
    }
}
