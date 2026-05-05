package data.weapons.scripts;

import java.awt.Color;
import java.util.Iterator;
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HWIMineStrikeWeaponEffect implements EveryFrameWeaponEffectPlugin,OnFireEffectPlugin{
    private static final Color WEAPON_GLOW = new Color(155, 55, 255, 255);

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (weapon.getChargeLevel() > 0) {
            weapon.setGlowAmount(weapon.getChargeLevel(), WEAPON_GLOW);
        }
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        Vector2f loc = getAimLoc(weapon);
        if(loc==null){
            loc = weapon.getShip().getMouseTarget();
        }
        Vector2f from = secureLoc(projectile.getLocation());
        Vector2f to = secureLoc(loc);
        float dist = Misc.getDistance(to, from);
        if (dist < weapon.getRange()) {
            to = secureClearLocation(from, to);
        }else{
            to = Vector2f.sub(to, from, null);
            to.scale(weapon.getRange()/dist);
            to = Vector2f.add(weapon.getLocation(), to, null);
            secureClearLocation(from, to);
        }
        projectile.getLocation().set(to);
    }

    private Vector2f secureClearLocation(Vector2f from, Vector2f to) {
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f result = to;
        Iterator<Object> check = engine.getShipGrid().getCheckIterator(to, 100, 100);
        while (check.hasNext()) {
            ShipAPI ship = (ShipAPI) check.next();
            if (Misc.getDistance(result, ship.getLocation()) < ship.getCollisionRadius()) {
                result = Misc.getPointWithinRadiusUniform(ship.getLocation(), ship.getCollisionRadius(),
                        ship.getCollisionRadius() * 1.1f, new Random());
            }
        }
        return result;
    }

    private Vector2f secureLoc(Vector2f loc) {
        Vector2f nloc = new Vector2f(loc);
        return nloc;
    }

    private Vector2f getAimLoc(WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (ship.getAI() == null) {
            return ship.getMouseTarget();
        } else {
            AutofireAIPlugin autoFireAI = ship.getWeaponGroupFor(weapon).getAutofirePlugin(weapon);
            if (autoFireAI != null&&autoFireAI.getTargetShip()!=null) {
                return autoFireAI.getTargetShip().getLocation();
            } else {
                if (ship.getShipTarget() != null) {
                    return ship.getShipTarget().getLocation();
                } else {
                    ShipAPI target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), HullSize.FIGHTER, 800,
                            true);
                    if (target != null) {
                        return target.getLocation();
                    } else {
                        target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, weapon.getRange(),
                            true);
                        if(target!=null){
                            return target.getLocation();
                        }else{
                            return ship.getMouseTarget();
                        }
                    }
                }
            }
        }
    }

}
