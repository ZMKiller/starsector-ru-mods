package data.weapons.scripts.proj;

import java.awt.Color;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import data.ai.HSIThreatSharedData;
import data.ai.HSIWeaponAI.HSIMissilePDAI;

public class HWIMGSystem implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if(projectile instanceof MissileAPI){
            MissileAPI m = (MissileAPI)projectile;
            engine.addPlugin(new HWIVTMissilePlugin(m, createExplosionSpec(m)));
            if(weapon.getShip()!=null&&weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon) instanceof HSIMissilePDAI){
                ((HSIMissilePDAI)weapon.getShip().getWeaponGroupFor(weapon).getAutofirePlugin(weapon)).addGuidingMissile(m);
            }
        }
    }

    public DamagingExplosionSpec createExplosionSpec(MissileAPI missile) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                80f, // radius
                60f, // coreRadius
                missile.getDamageAmount(), // maxDamage
                missile.getDamageAmount() / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                150, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(100, 100, 255, 175) // explosionColor
        );

        spec.setDamageType(missile.getDamageType());
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }
}
