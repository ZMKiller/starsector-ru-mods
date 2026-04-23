package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_Multi {

    public static ShipAPI getRoot(ShipAPI ship) {
        if (isMultiShip(ship)) {
            ShipAPI root = ship;
            while (root.getParentStation() != null) {
                root = root.getParentStation();
            }
            return root;
        } else {
            return ship;
        }
    }

    public static boolean isMultiShip(ShipAPI ship) {
        return ship.getParentStation() != null || ship.isShipWithModules();
    }

    public static boolean isRoot(ShipAPI ship) {
        return getRoot(ship) == ship;
    }

    public static boolean isWithinEmpRange(Vector2f loc, float dist, ShipAPI ship) {
        float distSq = dist * dist;
        if ((ship.getShield() != null) && ship.getShield().isOn() && ship.getShield().isWithinArc(loc)) {
            if ((MathUtils.getDistance(ship.getLocation(), loc) - ship.getShield().getRadius()) <= dist) {
                return true;
            }
        }

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (!weapon.getSlot().isHidden() && (weapon.getSlot().getWeaponType() != WeaponType.DECORATIVE)
                    && (weapon.getSlot().getWeaponType() != WeaponType.LAUNCH_BAY)
                    && (weapon.getSlot().getWeaponType() != WeaponType.SYSTEM)) {
                if (MathUtils.getDistanceSquared(weapon.getLocation(), loc) <= distSq) {
                    return true;
                }
            }
        }

        for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
            if (!engine.isSystemActivated()) {
                if (MathUtils.getDistanceSquared(engine.getLocation(), loc) <= distSq) {
                    return true;
                }
            }
        }

        return false;
    }
}
