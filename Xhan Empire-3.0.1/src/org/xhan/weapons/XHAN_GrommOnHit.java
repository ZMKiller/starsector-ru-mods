package org.xhan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class XHAN_GrommOnHit implements OnHitEffectPlugin {
    private static final Color CORE_EXPLOSION_COLOR = new Color(255, 107, 107, 255);
    private static final Color CORE_GLOW_COLOR = new Color(255, 105, 105, 255);
    private static final Color EXPLOSION_COLOR = new Color(255, 56, 56, 169);
    private static final Vector2f ZERO = new Vector2f();


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (projectile.didDamage() && !(target instanceof MissileAPI)) {

            // Blast visuals
            float CoreExplosionRadius = 60f;
            float CoreExplosionDuration = 0.1f;
            float ExplosionRadius = 120f;
            float ExplosionDuration = 0.25f;
            float CoreGlowRadius = 65f;
            float CoreGlowDuration = 0.15f;

            engine.spawnExplosion(point, ZERO, CORE_EXPLOSION_COLOR, CoreExplosionRadius, CoreExplosionDuration);
            engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, ExplosionRadius, ExplosionDuration);
            engine.addHitParticle(point, ZERO, CoreGlowRadius, 1f, CoreGlowDuration, CORE_GLOW_COLOR);
            //Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, point, ZERO);


            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_ring"),
                    point,
                    new Vector2f(),
                    new Vector2f(40, 40),
                    new Vector2f(1940, 1940),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 84, 54, 77),
                    true,
                    0,
                    0.05f,
                    0.2f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_Sparks2"),
                    point,
                    new Vector2f(),
                    new Vector2f(40, 40),
                    new Vector2f(940, 940),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 129, 79, 255),
                    true,
                    0,
                    0.05f,
                    0.37f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_Sparks3"),
                    point,
                    new Vector2f(),
                    new Vector2f(40, 40),
                    new Vector2f(1080, 1080),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(253, 128, 79, 255),
                    true,
                    0,
                    0.05f,
                    0.32f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_Sparks1"),
                    point,
                    new Vector2f(),
                    new Vector2f(40, 40),
                    new Vector2f(980, 980),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 0, 0, 255),
                    true,
                    0,
                    0.05f,
                    0.3f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_Sparks1"),
                    point,
                    new Vector2f(),
                    new Vector2f(60, 60),
                    new Vector2f(890, 890),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(255, 0, 0, 255),
                    true,
                    0.1f,
                    0.0f,
                    0.4f
            );
            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx", "Xhan_Gromm_Smoke1"),
                    point,
                    new Vector2f(),
                    new Vector2f(80, 80),
                    new Vector2f(85, 85),
                    //angle,
                    360 * (float) Math.random(),
                    0,
                    new Color(33, 22, 22, 223),
                    false,
                    0.1f,
                    0.0f,
                    3.5f
            );

        }
    }
}
