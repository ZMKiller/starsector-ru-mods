package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ForceBurstStats extends BaseShipSystemScript {

    public static final float FEEDBACK_PARTICLE_BRIGHTNESS = 1f;
    public static final float FEEDBACK_PARTICLE_DURATION = 1f;
    public static final int FEEDBACK_PARTICLE_MAX_PER_REPULSE = 5;
    public static final float FEEDBACK_PARTICLE_SIZEFACTOR = 5f;
    public static final float MIN_REPULSOR_PARTICLE_SIZE = 5f;
    public static final float REPULSOR_SIZE_FACTOR_MAX = 0.10f;
    public static final float REPULSOR_SIZE_FACTOR_MIN = 0.02f;
    private static final float COLLISION_RADIUS_REPULSOR_RADIUS_FACTOR = 10.0f;
    private static final Color LIGHT_COLOR = new Color(255, 255, 255);
    private static final float LIGHT_DURATION = 0.5f;
    private static final float LIGHT_INTENSITY = 1.25f;
    private static final float LIGHT_SIZE_INCREASE = 300.0f;
    private static final float MIN_REPULSOR_RADIUS = 750f;
    private static final float REPULSOR_FORCE = 400000f;
    private static final int REPULSOR_PARTICLE_COUNT = 350;
    private static final float REPULSOR_PARTICLE_DURATION = 0.6f;

    private static final Vector2f ZERO = new Vector2f();

    private boolean done = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (done) {
            Global.getSoundPlayer().playSound("swp_arcade_forceburst", 1f, 2f, ship.getLocation(), ship.getVelocity());
        }
        done = false;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (!(stats.getEntity() instanceof ShipAPI) || engine.isPaused() || done) {
            return;
        }

        done = true;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        Vector2f shipLoc = ship.getLocation();
        float repulseRadius = Math.max(ship.getCollisionRadius() * COLLISION_RADIUS_REPULSOR_RADIUS_FACTOR,
                                       MIN_REPULSOR_RADIUS);

        StandardLight light = new StandardLight(shipLoc, ZERO, ZERO, null);
        light.setIntensity(LIGHT_INTENSITY);
        light.setSize(repulseRadius + LIGHT_SIZE_INCREASE);
        light.setColor(LIGHT_COLOR);
        light.fadeOut(LIGHT_DURATION);
        LightShader.addLight(light);

        WaveDistortion wave = new WaveDistortion(shipLoc, ZERO);
        wave.setSize(repulseRadius * 3f);
        wave.setIntensity(repulseRadius * 0.15f);
        wave.fadeInSize(LIGHT_DURATION * 1.5f);
        wave.fadeOutIntensity(LIGHT_DURATION);
        DistortionShader.addDistortion(wave);

        float radiusExpansionRate = repulseRadius / REPULSOR_PARTICLE_DURATION;

        ship.setCollisionClass(CollisionClass.SHIP);

        for (float angle = 0; angle < Math.PI * 2; angle += (Math.PI * 2) / REPULSOR_PARTICLE_COUNT) {
            float speed = (float) Math.pow(Math.random(), 0.3);

            float randDur = (REPULSOR_PARTICLE_DURATION + REPULSOR_PARTICLE_DURATION * (float) Math.random()) / 2;
            Vector2f randPoint = MathUtils.getPointOnCircumference(shipLoc, ship.getCollisionRadius(), angle * (180 /
                                                                                                                (float) Math.PI));
            Vector2f velocity = new Vector2f(ship.getVelocity().x + (float) Math.cos(angle) * radiusExpansionRate *
                     speed, ship.getVelocity().y +
                                             (float) Math.sin(angle) *
                                             radiusExpansionRate *
                                             speed);
            float randSize = MathUtils.getRandomNumberInRange(REPULSOR_SIZE_FACTOR_MIN * repulseRadius,
                                                              REPULSOR_SIZE_FACTOR_MAX * repulseRadius);
            randSize = Math.max(randSize * 0.4f, MIN_REPULSOR_PARTICLE_SIZE);
            engine.addHitParticle(randPoint, velocity, randSize, 1f, randDur, LIGHT_COLOR);
        }

        List<ShipAPI> nearbyEnemies = SWP_Util.getShipsWithinRange(shipLoc, repulseRadius); // List of enemies to push back, smoothly
        for (ShipAPI thisEnemy : nearbyEnemies) {
            // Don't push phased ships
            if (thisEnemy.getCollisionClass() == CollisionClass.NONE) {
                continue; // Phase ship, skip it
            }
            push(ship, thisEnemy, 1 - MathUtils.getDistance(ship, thisEnemy) / repulseRadius); // Push it
        }

        List<CombatEntityAPI> nearbyAsteroids = SWP_Util.getAsteroidsWithinRange(shipLoc, repulseRadius); // List of enemies to push back, smoothly
        for (CombatEntityAPI asteroid : nearbyAsteroids) {
            push(ship, asteroid, 1 - MathUtils.getDistance(ship, asteroid) / repulseRadius); // Push it always
        }

        // Iterates through nearby enemy projectiles and reverses their course, "repulsing" them
        // In theory, two groups with Repulsors active could ping-pong shots back and forth at one another nearly forever
        // In reality, the time intervals don't allow for it
        int numRepulseParticles = 0;
        for (DamagingProjectileAPI thisProj : engine.getProjectiles()) {
            if (thisProj.getBaseDamageAmount() <= 0) {
                continue;
            }

            if (thisProj.getOwner() != ship.getOwner()) {
                // Get the projectile to be reversed's vital stats, including the inverse of its velocity (to send it right back where it came from)
                Vector2f thisProjLoc = thisProj.getLocation();
                Vector2f thisProjVel = thisProj.getVelocity();
                if (MathUtils.getDistanceSquared(shipLoc, thisProjLoc) > (repulseRadius * repulseRadius)) {
                    continue;
                }

                if (numRepulseParticles < FEEDBACK_PARTICLE_MAX_PER_REPULSE) {
                    numRepulseParticles++;
                    // Creates a feedback effect
                    float scaleFactor;
                    if (thisProj.getDamageType() == DamageType.FRAGMENTATION) {
                        scaleFactor = (float) Math.sqrt(thisProj.getDamageAmount() * 0.25f);
                    } else {
                        scaleFactor = (float) Math.sqrt(thisProj.getDamageAmount() * 1f);
                    }

                    scaleFactor = Math.min(scaleFactor, 40);

                    engine.addHitParticle(thisProjLoc, ship.getVelocity(), FEEDBACK_PARTICLE_SIZEFACTOR * scaleFactor,
                                          FEEDBACK_PARTICLE_BRIGHTNESS,
                                          FEEDBACK_PARTICLE_DURATION, LIGHT_COLOR);
                }

                // Reverse projectile with a 10 degree margin of error
                float returnAngle = MathUtils.clampAngle(VectorUtils.getFacing(thisProjVel) +
                      MathUtils.getRandomNumberInRange(170f, 190f));
                thisProjVel.set(MathUtils.getPointOnCircumference(null, thisProjVel.length() * 1.3f, returnAngle));

                thisProj.setFacing(returnAngle);
                thisProj.setOwner(ship.getOwner());
                thisProj.setSource(ship);
            }
        }
    }

    private void push(CombatEntityAPI source, CombatEntityAPI target, float forceMultiplier) {
        if (source.getOwner() == target.getOwner()) {
            return;
        }

        Vector2f pushVec = VectorUtils.getDirectionalVector(source.getLocation(), target.getLocation());
        float magnitude = (1 / Math.max(1f, target.getMass())) * forceMultiplier;
        pushVec.x *= magnitude * REPULSOR_FORCE;
        pushVec.y *= magnitude * REPULSOR_FORCE;
        Vector2f.add(pushVec, target.getVelocity(), target.getVelocity());
    }
}
