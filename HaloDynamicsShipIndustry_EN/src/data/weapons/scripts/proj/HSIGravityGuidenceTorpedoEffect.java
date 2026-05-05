package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;

public class HSIGravityGuidenceTorpedoEffect implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(projectile instanceof MissileAPI){
            HSIGravityGuidenceTorpedoScript script = new HSIGravityGuidenceTorpedoScript((MissileAPI) projectile);
            Global.getCombatEngine().addPlugin(script);
        }
    }
}
