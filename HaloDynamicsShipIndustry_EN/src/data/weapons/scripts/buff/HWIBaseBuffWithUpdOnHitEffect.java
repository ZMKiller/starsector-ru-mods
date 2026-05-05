package data.weapons.scripts.buff;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import data.kit.HSIIds;

public class HWIBaseBuffWithUpdOnHitEffect extends HWIBaseBuffOnHitEffect{
    
    public boolean shouldAddBuff(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, boolean shieldHit,
    ApplyDamageResultAPI damageResult, CombatEngineAPI engine){
        if(projectile.getSource()!=null) return projectile.getSource().getVariant().hasHullMod(HSIIds.HullMod.HWI_UPD);
        return false;
    }

}
