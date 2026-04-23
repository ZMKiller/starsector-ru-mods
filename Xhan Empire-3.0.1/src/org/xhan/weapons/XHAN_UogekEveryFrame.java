package org.xhan.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

/*
code by Tomatopaste
*/

public class XHAN_UogekEveryFrame implements EveryFrameWeaponEffectPlugin {
    private static final Color CHARGEUP_GLOW_COLOUR = new Color(182, 22, 235, 255);
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 10f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 65f;
    private static final float CHARGEUP_PARTICLE_VEL_MAX = 400f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 20f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 3f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.2f;

    private static final Color MUZZLE_GLOW_COLOUR = new Color(254, 205, 255, 255);
    private static final Color MUZZLE_GLOW_COLOUR_EXTRA = new Color(182, 22, 235, 255);
    private static final float MUZZLE_GLOW_SIZE = 95f;

    private final IntervalUtil particleInterval = new IntervalUtil(0.05f, 0.01f);
    private boolean hasFired = false;
    private float oldChargeLevel = 0f;
    private float oldCooldown = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        float chargeLevel = weapon.getChargeLevel();
        float cooldown = weapon.getCooldownRemaining();

        Vector2f muzzleLocation = weapon.getLocation();

        if (chargeLevel > oldChargeLevel || cooldown < oldCooldown) {
            if (weapon.isFiring()) {
                particleInterval.advance(amount);

                if (particleInterval.intervalElapsed()) {
                    float dist = (CHARGEUP_PARTICLE_DISTANCE_MIN + (chargeLevel * (CHARGEUP_PARTICLE_DISTANCE_MAX - CHARGEUP_PARTICLE_DISTANCE_MIN)));
                    Vector2f loc = new Vector2f(0f, dist);
                    VectorUtils.rotate(loc, (float) Math.random() * 360f);
                    Vector2f.add(loc, muzzleLocation, loc);

                    Vector2f vel = Vector2f.sub(muzzleLocation, loc, new Vector2f());
                    vel.normalise();
                    vel.scale(CHARGEUP_PARTICLE_VEL_MAX * chargeLevel);
                    Vector2f.add(vel, weapon.getShip().getVelocity(), vel);

                    float size = CHARGEUP_PARTICLE_SIZE_MIN + (chargeLevel * (CHARGEUP_PARTICLE_SIZE_MAX - CHARGEUP_PARTICLE_SIZE_MIN));

                    engine.addSmoothParticle(loc, vel, size, 10f, CHARGEUP_PARTICLE_DURATION, CHARGEUP_GLOW_COLOUR);
                }
            }
        }

        if (!hasFired && chargeLevel >= 1f && oldChargeLevel < 1f) {
            hasFired = true;
            engine.spawnExplosion(muzzleLocation, weapon.getShip().getVelocity(), MUZZLE_GLOW_COLOUR, MUZZLE_GLOW_SIZE, 0.8f);
            engine.addHitParticle(muzzleLocation, weapon.getShip().getVelocity(), MUZZLE_GLOW_SIZE * 2f, 0.25f, 0.4f, MUZZLE_GLOW_COLOUR_EXTRA);
        } else {
            hasFired = false;
        }

        oldChargeLevel = chargeLevel;
        oldCooldown = cooldown;
    }
}
