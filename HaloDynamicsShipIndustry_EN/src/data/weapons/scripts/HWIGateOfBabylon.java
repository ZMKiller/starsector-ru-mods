package data.weapons.scripts;

import java.util.Random;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class HWIGateOfBabylon implements OnFireEffectPlugin{

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        Vector2f loc = getAimLoc(weapon);
        if(loc==null){
            loc = weapon.getShip().getMouseTarget();
        }
        ShipAPI ship = weapon.getShip();
        projectile.getLocation().set(Misc.getPointWithinRadiusUniform(ship.getLocation(), ship.getCollisionRadius(), ship.getCollisionRadius()*1.25f, new Random()));
        reAimProjectile(projectile,loc);
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

    private void reAimProjectile(DamagingProjectileAPI projectile,Vector2f target){
        Vector2f currVel = projectile.getVelocity();
        Vector2f aimVec = Vector2f.sub(target, projectile.getLocation(), null);
        aimVec.scale(currVel.length()/aimVec.length());
        projectile.getVelocity().set(aimVec);
        projectile.setFacing(VectorUtils.getFacing(aimVec));
    }

}
