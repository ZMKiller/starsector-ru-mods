package data.weapons.scripts.proj;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HWIHOHOnFireEffect implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI target = weapon.getShip().getShipTarget();
        projectile.getVelocity().set((Vector2f)(new Vector2f(projectile.getVelocity()).scale(1.33f)));
        if(weapon.getShip().getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)){
            if(weapon.getShip().getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS) instanceof ShipAPI){
                target = (ShipAPI)(weapon.getShip().getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS));
            }
        }
        engine.addPlugin(new HWIHOHGuidencePlugin(projectile, target));
        //Global.getLogger(this.getClass()).info("Firing.");
        //Global.getLogger(this.getClass()).info("Weapon charge "+weapon.getSpec().getChargeTime());
    }
}
