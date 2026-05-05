package data.weapons.scripts.proj;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.BreachOnHitEffect;

import data.kit.HSIIds;

public class HWIAutopulseOnHitEffect implements OnHitEffectPlugin {
    protected static float pass = 0.25f;
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if(target instanceof ShieldAPI&&projectile.getSource()!=null&&projectile.getSource().getVariant().hasHullMod(HSIIds.HullMod.HWI_UPD)){
            if(shieldHit){
                if(Math.random()<=pass){
                    engine.applyDamage(target, point, 25f, DamageType.ENERGY, 0f, false, false, projectile.getSource());
                }
            }else{
                if(Math.random()<=pass){
                    BreachOnHitEffect.dealArmorDamage(projectile, (ShipAPI) target, point, 10f);
                }
            }
        }
    }
}
