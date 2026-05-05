package data.weapons.scripts.proj;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.dem.DEMEffect;
import data.scripts.HSIRenderer.HSIButterflyRenderObject;
import data.scripts.HSIRenderer.HSICombatRendererV2;

public class HSIDancingButterflyOnfire implements OnFireEffectPlugin {
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        DEMEffect effect = new DEMEffect();
        effect.onFire(projectile, weapon, engine);
        HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(projectile,7,15));
        if(projectile instanceof MissileAPI){
            ((MissileAPI)projectile).setSpriteAlphaOverride(0f);
        }
    }
}
