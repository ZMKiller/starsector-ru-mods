package data.weapons.scripts.proj;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import data.weapons.scripts.HWITurboChargerWeapon;

public class HWIConquerorProjectile implements OnFireEffectPlugin{
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        int shots = 0;
        if (weapon.getEffectPlugin() != null && weapon.getEffectPlugin() instanceof HWITurboChargerWeapon) {
            HWITurboChargerWeapon effect = (HWITurboChargerWeapon) weapon.getEffectPlugin();
            shots = effect.getShots();
        }
        if(shots>5){
            FluxTrackerAPI tracker = weapon.getShip().getFluxTracker();
            if(tracker.getCurrFlux()>0){
                float toDecrease = tracker.getCurrFlux()*0.008f;
                tracker.decreaseFlux(toDecrease);
                projectile.setDamageAmount(projectile.getDamageAmount()+toDecrease*0.1f);
            }
        }
        projectile.setDamageAmount(projectile.getDamageAmount() * (1f + 0.03f * shots));
    }
}
