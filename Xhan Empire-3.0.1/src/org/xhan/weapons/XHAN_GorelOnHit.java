package org.xhan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class XHAN_GorelOnHit implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(153, 255, 51, 255);
    private static final Color CORE_GLOW_COLOR = new Color(222, 255, 164, 150);
    private static final Color EXPLOSION_COLOR = new Color(204, 255, 220, 10);
    private static final Vector2f ZERO = new Vector2f();


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 70f;
            float CoreExplosionDuration = 0.7f;
            float ExplosionRadius = 120f;
            float ExplosionDuration = 0.2f;
            float CoreGlowRadius = 200f;
            float CoreGlowDuration = 0.2f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            //Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "xhan_Gorel_Splat"),
                    point,
                    new Vector2f(),
                    new Vector2f(100, 100),
                    new Vector2f(360, 360),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(146, 255, 120, 215),
                    true,
                    0,
                    0.05f,
                    0.1f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "xhan_Gorel_Splat"),
                    point,
                    new Vector2f(),
                    new Vector2f(60, 60),
                    new Vector2f(240, 240),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(127, 215, 106, 184),
                    true,
                    0.1f,
                    0.0f,
                    0.15f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "xhan_Gorel_Splat"),
                    point,
                    new Vector2f(),
                    new Vector2f(160, 160),
                    new Vector2f(100, 100),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(127, 215, 106, 163),
                    true,
                    0.2f,
                    0.0f,
                    0.2f
            );

        }
    }
}
