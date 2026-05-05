package data.weapons.scripts.proj;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;


public class HWIAromredMissileEffect implements OnFireEffectPlugin{
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(projectile instanceof MissileAPI)
        engine.addPlugin(new HWIAromredMissileScript((MissileAPI)projectile));

    }
}
