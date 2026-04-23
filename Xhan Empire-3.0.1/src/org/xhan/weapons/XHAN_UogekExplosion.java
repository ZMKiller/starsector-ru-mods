package org.xhan.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_UogekExplosion implements OnHitEffectPlugin {
    private static final float DURATION = 20f;
    private static final float RADIUS = 50f;
    private static final float CORE_RADIUS = 25f;
    private static final float MAX_DAMAGE = 150f;
    private static final float MIN_DAMAGE = 50f;
    private static final float PARTICLE_SIZE_MIN = 4f;
    private static final float PARTICLE_SIZE_RANGE = 3f;
    private static final float PARTICLE_DURATION = 30f;
    private static final int PARTICLE_COUNT = 5;
    private static final Color PARTICLE_COLOUR = new Color(184, 183, 117, 255);
    private static final Color EXPLOSION_COLOUR = new Color(255, 122, 0, 255);
    private static final boolean CAN_DAMAGE_SOURCE = false;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                DURATION,
                RADIUS,
                CORE_RADIUS,
                MAX_DAMAGE,
                MIN_DAMAGE,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
                CollisionClass.HITS_SHIPS_AND_ASTEROIDS,
                PARTICLE_SIZE_MIN,
                PARTICLE_SIZE_RANGE,
                PARTICLE_DURATION,
                PARTICLE_COUNT,
                PARTICLE_COLOUR,
                EXPLOSION_COLOUR
        );

        if (projectile.getSource() == null) return;
        engine.spawnDamagingExplosion(spec, projectile.getSource(), point, CAN_DAMAGE_SOURCE);
    }
}
