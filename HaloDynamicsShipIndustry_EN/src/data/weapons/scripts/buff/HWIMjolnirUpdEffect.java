package data.weapons.scripts.buff;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class HWIMjolnirUpdEffect extends HWIBaseBuffWithUpdOnHitEffect{
    protected static final float dur  = 0.3f;
    protected static final float mult = 0.3f;
    public void addBuff(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, boolean shieldHit,
    ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        HWIMjolnirUpdBuff.getInstance(target).add(projectile.getWeapon(), dur);
        float length = Math.max(1,target.getVelocity().length());
        target.getVelocity().scale(mult*target.getMaxSpeed()/length);
    }


}
