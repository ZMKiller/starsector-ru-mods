package data.weapons.scripts.buff;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class HWIBaseBuffOnHitEffect implements OnHitEffectPlugin {

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if(target instanceof ShipAPI){
            if(shouldAddBuff(projectile, (ShipAPI)target, point, shieldHit, damageResult, engine))
            addBuff(projectile, (ShipAPI)target, point, shieldHit, damageResult, engine);
        }
    }

    public void addBuff(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, boolean shieldHit,
    ApplyDamageResultAPI damageResult, CombatEngineAPI engine){

    }

    public boolean shouldAddBuff(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, boolean shieldHit,
    ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        return true;
    }
}
