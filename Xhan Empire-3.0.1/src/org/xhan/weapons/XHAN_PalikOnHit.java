package org.xhan.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_PalikOnHit implements OnHitEffectPlugin {
    private static final float MAX_DURATION = 0.2f;
    private static final float RADIUS = 15f;
    private static final float DAMAGE_AMOUNT = 5f;
    private static final DamageType DAMAGE_TYPE = DamageType.HIGH_EXPLOSIVE;
    private static final Color EXPLOSION_COLOUR = new Color(153, 255, 51, 255);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        engine.spawnExplosion(point, (Vector2f) target.getVelocity().scale(1f), EXPLOSION_COLOUR, RADIUS, MAX_DURATION);

        engine.applyDamage(target, point, DAMAGE_AMOUNT, DAMAGE_TYPE, 0f, false, false, projectile.getSource(), false);
    }
}
