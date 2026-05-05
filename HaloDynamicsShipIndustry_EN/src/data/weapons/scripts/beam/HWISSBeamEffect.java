package data.weapons.scripts.beam;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HWISSBeamEffect implements BeamEffectPlugin {
    private TimeoutTracker<CombatEntityAPI> damaged = new TimeoutTracker<CombatEntityAPI>();

    public static Color STANDARD_RIFT_COLOR = new Color(25, 25, 255, 255);
    public static Color EXPLOSION_UNDERCOLOR = new Color(100, 0, 175, 100);
    public static Color NEGATIVE_SOURCE_COLOR = new Color(200, 255, 200, 25);

    protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    public HWISSBeamEffect() {
    }

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        tracker.advance(amount);
        damaged.advance(amount);
        if (tracker.intervalElapsed()) {
            spawnNegativeParticles(engine, beam);
        }
        if (beam.getBrightness() < 1f)
            return;
        if (beam.getDamageTarget() != null) {
            if (!damaged.contains(beam.getDamageTarget())) {
                engine.spawnDamagingExplosion(createExplosionSpec(beam.getDamage().getDamage() * 0.1f),
                        beam.getSource(), beam.getRayEndPrevFrame());
                damaged.add(beam.getDamageTarget(), 0.1f);
            }
        }
    }

    public void spawnNegativeParticles(CombatEngineAPI engine, BeamAPI beam) {
        float length = beam.getLengthPrevFrame();
        if (length <= 10f)
            return;

        Vector2f from = beam.getFrom();
        Vector2f to = beam.getRayEndPrevFrame();

        ShipAPI ship = beam.getSource();

        float angle = Misc.getAngleInDegrees(from, to);
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);

        Color color = NEGATIVE_SOURCE_COLOR;

        float sizeMult = 1f;
        sizeMult = 0.67f;

        for (int i = 0; i < 3; i++) {
            float rampUp = 0.25f + 0.25f * (float) Math.random();
            float dur = 1f + 1f * (float) Math.random();
            // dur *= 2f;
            float size = 15f + 15f * (float) Math.random();
            size *= sizeMult;

            Vector2f loc = Misc.getPointAtRadius(beam.getWeapon().getLocation(), size * 0.33f);
            engine.addNegativeParticle(loc, ship.getVelocity(), size, rampUp / dur, dur, color);
        }

        if (true)
            return;

        float spawnOtherParticleRange = 25;
        if (length > spawnOtherParticleRange * 2f && (float) Math.random() < 0.25f) {
            // color = new Color(150,255,150,255);
            color = new Color(150, 255, 150, 75);
            int numToSpawn = (int) ((length - spawnOtherParticleRange) / 200f + 1);
            numToSpawn = 1;
            for (int i = 0; i < numToSpawn; i++) {
                float distAlongBeam = spawnOtherParticleRange
                        + (length - spawnOtherParticleRange * 2f) * (float) Math.random();
                float groupSpeed = 100f + (float) Math.random() * 100f;
                for (int j = 0; j < 7; j++) {
                    float rampUp = 0.25f + 0.25f * (float) Math.random();
                    float dur = 1f + 1f * (float) Math.random();
                    float size = 50f + 50f * (float) Math.random();
                    Vector2f loc = new Vector2f(dir);
                    float sign = Math.signum((float) Math.random() - 0.5f);
                    loc.scale(distAlongBeam + sign * (float) Math.random() * size * 0.5f);
                    Vector2f.add(loc, from, loc);

                    loc = Misc.getPointWithinRadius(loc, size * 0.25f);

                    float dist = Misc.getDistance(loc, to);
                    Vector2f vel = new Vector2f(dir);
                    if ((float) Math.random() < 0.5f) {
                        vel.negate();
                        dist = Misc.getDistance(loc, from);
                    }

                    float speed = groupSpeed;
                    float maxSpeed = dist / dur;
                    if (speed > maxSpeed)
                        speed = maxSpeed;
                    vel.scale(speed);
                    Vector2f.add(vel, ship.getVelocity(), vel);

                    engine.addNegativeParticle(loc, vel, size, rampUp, dur, color);
                }
            }
        }
    }

    public DamagingExplosionSpec createExplosionSpec(float damage) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                20f, // radius
                15f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                3f, // particleSizeMin
                3f, // particleSizeRange
                0.4f, // particleDuration
                12, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(175, 65, 255, 175) // explosionColor
        );
        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_flak");
        return spec;
    }

}
