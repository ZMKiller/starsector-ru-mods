package data.weapons.scripts.buff;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class HWINeedlerUpdEffect extends HWIBaseBuffWithUpdOnHitEffect {

    public void addBuff(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        HWINeedlerUpdBuff.getInstance(target).add(projectile, 0.5f);
    }
}
