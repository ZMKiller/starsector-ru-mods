package org.xhan.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_ErlekhkeytOnHit implements OnHitEffectPlugin {
    private static final float MAX_DURATION = 0.40f;
    private static final float RADIUS = 80f;
    private static final float DAMAGE_AMOUNT = 500f;
    private static final DamageType DAMAGE_TYPE = DamageType.FRAGMENTATION;
    private static final Color EXPLOSION_COLOUR = new Color(58, 153, 255, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.spawnExplosion(point, (Vector2f) target.getVelocity().scale(0f), EXPLOSION_COLOUR, RADIUS, MAX_DURATION);

        engine.applyDamage(target, point, DAMAGE_AMOUNT, DAMAGE_TYPE, 0.15f, false, false, projectile.getSource(), false);
    }
}
