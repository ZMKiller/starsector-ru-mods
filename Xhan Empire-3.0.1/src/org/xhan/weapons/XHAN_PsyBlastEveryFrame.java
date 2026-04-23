package org.xhan.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class XHAN_PsyBlastEveryFrame implements EveryFrameWeaponEffectPlugin {
    private static final Color CHARGEUP_GLOW_COLOUR = new Color(213, 28, 255, 255);
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 250f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 250f;
    private static final float CHARGEUP_PARTICLE_VEL_MAX = 550f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 25f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 1f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.45f;

    private static final Color MUZZLE_GLOW_COLOUR = new Color(193, 122, 255, 255);
    private static final Color MUZZLE_GLOW_COLOUR_EXTRA = new Color(235, 22, 114, 230);
    private static final float MUZZLE_GLOW_SIZE = 95f;

    private static final float EMP_RATE = 5f; // 1/5
    private static final float EMP_THICKNESS_SCALE = 10f;
    private static final float JITTER_INTENSITY = 0.35f;
    private static final float MAX_UNDER_JITTER_RANGE = 80f;

    private static final String CHARGE_SOUND = "XHAN_NOUS_CHARGE_SOUND";
    private static final String EMP_SOUND = "realitydisruptor_emp_impact";

    private final IntervalUtil particleInterval = new IntervalUtil(0.000001f, 0.000001f);
    private boolean hasFired = false;
    private boolean hasCharged = false;
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
                if (!hasCharged) {
                    Global.getSoundPlayer().playSound(CHARGE_SOUND, 1.0f, 1.0f, weapon.getLocation(), new Vector2f());
                    hasCharged = true;
                }

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

                    if (chargeLevel > oldChargeLevel) {
                        /*
                        engine.spawnEmpArcVisual(weapon.getLocation(),
                                weapon.getShip(),
                                MathUtils.getRandomPointInCircle(weapon.getLocation(), weapon.getShip().getShieldRadiusEvenIfNoShield()),
                                weapon.getShip(),
                                10f,
                                MUZZLE_GLOW_COLOUR,
                                Color.white
                        );
                        */
                        if (MathUtils.getRandomNumberInRange(0f, EMP_RATE) < chargeLevel) {
                            EmpArcEntityAPI e = engine.spawnEmpArcPierceShields(weapon.getShip(), weapon.getShip().getAllWeapons().get(MathUtils.getRandomNumberInRange(0, weapon.getShip().getAllWeapons().size() - 1)).getLocation(), weapon.getShip(), weapon.getShip(), DamageType.OTHER, 0f, 0f, 99999f, EMP_SOUND, chargeLevel * EMP_THICKNESS_SCALE, MUZZLE_GLOW_COLOUR_EXTRA, Color.white);
                            e.setSingleFlickerMode();
                        }

                        float jitterRangeBonus = chargeLevel * MAX_UNDER_JITTER_RANGE;
                        weapon.getShip().setJitterUnder(this, MUZZLE_GLOW_COLOUR, (float) Math.pow(chargeLevel, 2), 11, 0f, 3f + jitterRangeBonus);
                        weapon.getShip().setJitter(this, MUZZLE_GLOW_COLOUR, chargeLevel * JITTER_INTENSITY, 11, 0f, 3f);
                    }
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

        if (chargeLevel == oldChargeLevel && cooldown == oldCooldown) {
            hasCharged = false;
        }

        oldChargeLevel = chargeLevel;
        oldCooldown = cooldown;
    }
}
