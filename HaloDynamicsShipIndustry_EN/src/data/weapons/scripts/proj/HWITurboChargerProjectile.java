package data.weapons.scripts.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import data.weapons.scripts.HWITurboChargerWeapon;

public class HWITurboChargerProjectile implements OnFireEffectPlugin, OnHitEffectPlugin {
    private static final Color PARTICLE = new Color(255, 255, 255, 255);
    private static final Color EXPLOSION = new Color(100, 100, 255, 175);

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        int shots = 0;
        if (weapon.getEffectPlugin() != null && weapon.getEffectPlugin() instanceof HWITurboChargerWeapon) {
            HWITurboChargerWeapon effect = (HWITurboChargerWeapon) weapon.getEffectPlugin();
            shots = effect.getShots();
            effect.reportShot();
            if (shots > 5) {
                int radius = 3;
                int thickness = 3;
                switch (weapon.getSize()) {
                    case LARGE:
                        radius = 3;
                        thickness = 5;
                        break;
                    case MEDIUM:
                        radius = 2;
                        thickness = 4;
                        break;
                    case SMALL:
                        radius = 1;
                        thickness = 3;
                        break;
                    default:
                }
                HWIGunFireShockWaveEffect.createShockWave(5*thickness, shots * radius, projectile.getLocation(),
                        projectile.getFacing(), thickness, 15 * radius, 0.4f, 1f, EXPLOSION);
                if(weapon.getSize() == WeaponSize.LARGE){
                    HWIGunFireShockWaveEffect.createShockWave(7*thickness, shots * radius/2, projectile.getLocation(),
                        projectile.getFacing(), thickness, 15 * radius, 0.4f, 0.6f, EXPLOSION);
                }
                projectile.getDamage().getModifier().modifyMult("EXPLOSION_INSTEAD", 0.001f);
            }
        }
        //projectile.setDamageAmount(projectile.getDamageAmount() * (1f + 0.03f * shots));
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        int shots = 0;
        WeaponAPI weapon = projectile.getWeapon();
        if (weapon == null)
            return;
        if (weapon.getEffectPlugin() != null && weapon.getEffectPlugin() instanceof HWITurboChargerWeapon) {
            HWITurboChargerWeapon effect = (HWITurboChargerWeapon) weapon.getEffectPlugin();
            shots = effect.getShots();
        }
        if (shots >= 6) {
            float radius = 35f;
            switch (weapon.getSize()) {
                case LARGE:
                    radius = 55f;
                    break;
                case MEDIUM:
                    radius = 35f;
                    break;
                case SMALL:
                    radius = 15f;
                    break;
                default:
            }
            DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(createExplosionSpec(projectile.getDamage().getBaseDamage(), radius),
                    projectile.getSource(), point);
        }
    }

    public DamagingExplosionSpec createExplosionSpec(float damage, float radius) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                radius * 1.5f, // radius
                radius, // coreRadius
                damage , // maxDamage
                damage/ 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.5f, // particleDuration
                150, // particleCount
                PARTICLE, // particleColor
                EXPLOSION // explosionColor
        );
        spec.setDamageType(DamageType.ENERGY);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }
}
