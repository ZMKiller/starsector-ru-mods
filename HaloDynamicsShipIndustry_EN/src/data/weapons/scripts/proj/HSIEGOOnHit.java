package data.weapons.scripts.proj;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import data.hullmods.HSIEGO.HSIEGOStats;

public class HSIEGOOnHit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
           if(projectile.getSource()!=null){
                ShipAPI source = projectile.getSource();
                if(source.hasListenerOfClass(HSIEGOStats.class)){
                    source.getListeners(HSIEGOStats.class).get(0).addDamageDealt(damageResult.getDamageToHull());
                }
           }
    }
}

