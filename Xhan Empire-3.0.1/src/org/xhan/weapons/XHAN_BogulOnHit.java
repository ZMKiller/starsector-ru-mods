package org.xhan.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_BogulOnHit implements OnHitEffectPlugin {
    private static final float MAX_DURATION = 0.4f;
    private static final float RADIUS = 40f;
    private static final float DAMAGE_AMOUNT = 100f;
    private static final DamageType DAMAGE_TYPE = DamageType.ENERGY;
    private static final Color EXPLOSION_COLOUR = new Color(72, 255, 191, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.spawnExplosion(point, (Vector2f) target.getVelocity().scale(0.9f), EXPLOSION_COLOUR, RADIUS, MAX_DURATION);

        engine.applyDamage(target, point, DAMAGE_AMOUNT, DAMAGE_TYPE, 0f, false, false, projectile.getSource(), false);
    }
}
