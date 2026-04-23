package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class SWP_RedeemerOnHitEffect implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(255, 150, 70, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target == null || point == null) {
            return;
        }
        engine.spawnExplosion(point, new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f), EXPLOSION_COLOR, 200f, 1.5f);
    }
}
