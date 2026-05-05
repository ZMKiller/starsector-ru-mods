package data.weapons.scripts.proj;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HWITimeFuseProjectile implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        Vector2f loc = ship.getMouseTarget();
        float dist = weapon.getRange();
        if (ship.getWeaponGroupFor(weapon)!=null&&ship.getWeaponGroupFor(weapon).isAutofiring()) {
            loc = ship.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTarget();
            if (loc != null) {
                dist = Misc.getDistance(loc, projectile.getLocation());
            } else {
                loc = ship.getMouseTarget();
                if (Misc.getDistance(loc, weapon.getLocation()) < weapon.getRange()) {
                    dist = Misc.getDistance(loc, projectile.getLocation());
                }
            }
        } else {
            if (Misc.getDistance(loc, weapon.getLocation()) < weapon.getRange()) {
                dist = Misc.getDistance(loc, projectile.getLocation());
            }
        }
        float time = dist / weapon.getProjectileSpeed()+(float)(Math.random()-0.5)*0.1f;
        if (projectile instanceof MissileAPI) {
            MissileAPI bomb = (MissileAPI) projectile;
            bomb.setMaxFlightTime(time);
        }
    }
}
