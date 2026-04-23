package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_PhaseTunnelerStats extends BaseShipSystemScript {

    private static final String CHARGEUP_SOUND = "swp_arcade_phasetunneler";
    private static final float DAMAGE_MOD_VS_CAPITAL = 0.2f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.4f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 1f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.7f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.8f;
    private static final float DISTORTION_BLAST_RADIUS = 1500f;
    private static final Color EXPLOSION_COLOR = new Color(255, 255, 255);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 5000f;
    @SuppressWarnings("SuspiciousNameCombination")
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .25f;
    private static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 5000f;
    private static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .25f;
    private static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .3f;
    private static final float EXPLOSION_PUSH_RADIUS = 1000f;
    private static final float EXPLOSION_VISUAL_RADIUS = 1500f;
    private static final float FORCE_VS_ASTEROID = 1500f;
    private static final float FORCE_VS_CAPITAL = 200f;
    private static final float FORCE_VS_CRUISER = 350f;
    private static final float FORCE_VS_DESTROYER = 900f;
    private static final float FORCE_VS_FIGHTER = 1250f;
    private static final float FORCE_VS_FRIGATE = 1000f;
    private static final int MAX_PARTICLES_PER_FRAME = 30; // Based on charge level
    private static final Color PARTICLE_COLOR = new Color(255, 255, 255);
    private static final float PARTICLE_OPACITY = 0.5f;
    private static final float PARTICLE_RADIUS = 600f;
    private static final float PARTICLE_SIZE = 10f;
    private static final Vector2f ZERO = new Vector2f();

    private final IntervalUtil interval = new IntervalUtil(0.035f, 0.035f);
    private final IntervalUtil interval2 = new IntervalUtil(0.015f, 0.015f);
    private boolean isActive = false;
    private StandardLight light = null;
    private Vector2f novaLocation = null;
    private float novaTime = -1f;
    private SoundAPI sound = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == State.IN) {
            if (!isActive) {
                isActive = true;
                sound
                        = Global.getSoundPlayer().playSound(CHARGEUP_SOUND, 1f, 2f, ship.getLocation(), ship.getVelocity());

                light = new StandardLight();
                light.setIntensity(1.25f);
                light.setSize(EXPLOSION_VISUAL_RADIUS);
                light.setColor(PARTICLE_COLOR);
                light.fadeIn(1.95f);
                light.setLifetime(0.1f);
                light.setAutoFadeOutTime(0.17f);
                LightShader.addLight(light);
            }

            Vector2f loc = new Vector2f(ship.getLocation());
            loc.x -= 70f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
            loc.y -= 70f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);
            light.setLocation(loc);

            interval2.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if (interval2.intervalElapsed()) {
                Vector2f particlePos, particleVel;
                int numParticlesThisFrame = Math.round(effectLevel * MAX_PARTICLES_PER_FRAME);
                for (int x = 0; x < numParticlesThisFrame; x++) {
                    particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), PARTICLE_RADIUS);
                    particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                    Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, PARTICLE_SIZE, PARTICLE_OPACITY,
                            1f, PARTICLE_COLOR);
                }
            }
        } else if (state == State.OUT) {
            if (isActive) {
                CombatEngineAPI engine = Global.getCombatEngine();
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
                        0.2f);
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS
                        / 2f, 0.2f);

                Vector2f loc = new Vector2f(ship.getLocation());
                loc.x -= 70f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                loc.y -= 70f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                light = new StandardLight(loc, ZERO, ZERO, null);
                light.setIntensity(2f);
                light.setSize(EXPLOSION_VISUAL_RADIUS * 2f);
                light.setColor(EXPLOSION_COLOR);
                light.fadeOut(1.25f);
                LightShader.addLight(light);

                WaveDistortion wave = new WaveDistortion(loc, ZERO);
                wave.setSize(DISTORTION_BLAST_RADIUS);
                wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.075f);
                wave.fadeInSize(1.2f);
                wave.fadeOutIntensity(0.9f);
                wave.setSize(DISTORTION_BLAST_RADIUS * 0.25f);
                DistortionShader.addDistortion(wave);

                if (ship.getHullLevel() <= 0.5f) {
                    novaLocation = loc;
                    novaTime = 0f;
                    engine.addHitParticle(loc, ZERO, 500f, 1f, 0.3f, EXPLOSION_COLOR);
                    engine.spawnExplosion(loc, ZERO, EXPLOSION_COLOR, 1000f, 0.05f);
                    Global.getSoundPlayer().playSound("swp_arcade_phasetunneler_blast", 1f, 2f, loc, ZERO);
                }

                try {
                    sound.setLocation(ship.getLocation().x, ship.getLocation().y);
                } catch (Exception ex) {
                    Global.getSoundPlayer().playSound(CHARGEUP_SOUND, 2f, 2f, ship.getLocation(), ship.getVelocity());
                }

                ShipAPI victim;
                Vector2f dir;
                float force, damage, emp, mod;
                for (CombatEntityAPI tmp : SWP_Util.getEntitiesWithinRange(ship.getLocation(), EXPLOSION_PUSH_RADIUS)) {
                    if ((tmp == ship) || (tmp == null)) {
                        continue;
                    }

                    mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                    force = FORCE_VS_ASTEROID * mod;
                    damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                    emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                    if (tmp instanceof ShipAPI) {
                        victim = (ShipAPI) tmp;

                        if (null != victim.getHullSize()) // Modify push strength based on ship class
                        {
                            switch (victim.getHullSize()) {
                                case FIGHTER:
                                    force = FORCE_VS_FIGHTER * mod;
                                    damage /= DAMAGE_MOD_VS_FIGHTER;
                                    break;
                                case FRIGATE:
                                    force = FORCE_VS_FRIGATE * mod;
                                    damage /= DAMAGE_MOD_VS_FRIGATE;
                                    break;
                                case DESTROYER:
                                    force = FORCE_VS_DESTROYER * mod;
                                    damage /= DAMAGE_MOD_VS_DESTROYER;
                                    break;
                                case CRUISER:
                                    force = FORCE_VS_CRUISER * mod;
                                    damage /= DAMAGE_MOD_VS_CRUISER;
                                    break;
                                case CAPITAL_SHIP:
                                    force = FORCE_VS_CAPITAL * mod;
                                    damage /= DAMAGE_MOD_VS_CAPITAL;
                                    break;
                                default:
                                    break;
                            }
                        }

                        if (victim.getOwner() == ship.getOwner()) {
                            damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                            emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                            force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                        }

                        float shipRadius = SWP_Util.effectiveRadius(victim);

                        if (victim.getShield() != null && victim.getShield().isOn() && victim.getShield().isWithinArc(
                                ship.getLocation())) {
                            victim.getFluxTracker().increaseFlux(damage * 2, true);
                        } else {
                            for (int x = 0; x < 5; x++) {
                                engine.spawnEmpArc(ship, MathUtils.getRandomPointInCircle(victim.getLocation(),
                                        shipRadius),
                                        victim, victim, EXPLOSION_DAMAGE_TYPE, damage / 10,
                                        emp / 5, EXPLOSION_PUSH_RADIUS, null, 2f,
                                        EXPLOSION_COLOR, EXPLOSION_COLOR);
                            }
                        }
                    }

                    if (tmp instanceof DamagingProjectileAPI) {
                        DamagingProjectileAPI proj = (DamagingProjectileAPI) tmp;
                        if (proj.getBaseDamageAmount() <= 0) {
                            continue;
                        }
                    }

                    dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                    dir.scale(force);

                    Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                }

                isActive = false;
            }
        }

        if (novaTime >= 0f) {
            novaTime += Global.getCombatEngine().getElapsedInLastFrame() * Global.getCombatEngine().getTimeMult().getModifiedValue();
            interval.advance(Global.getCombatEngine().getElapsedInLastFrame() * Global.getCombatEngine().getTimeMult().getModifiedValue());

            if (interval.intervalElapsed()) {
                float offset = (float) Math.random() * 360f;
                for (int i = 0; i < (int) (novaTime * 5f) + 4; i++) {
                    float angle = i / ((novaTime * 5f) + 4f) * 360f + offset;
                    if (angle >= 360f) {
                        angle -= 360f;
                    }
                    float distance = (float) Math.random() * 100f + novaTime * 1500f;
                    Vector2f point1 = MathUtils.getPointOnCircumference(novaLocation, distance, angle);
                    Vector2f point2 = MathUtils.getPointOnCircumference(novaLocation, distance, angle + 360f
                            / ((novaTime * 5f) + 4f)
                            * ((float) Math.random()
                            + 1f));
                    Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1),
                            new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f, 10000f,
                            null, 40f, EXPLOSION_COLOR, EXPLOSION_COLOR);
                }

                List<ShipAPI> targets = SWP_Util.getShipsWithinRange(novaLocation, novaTime * 1500f + 25f);
                for (ShipAPI target : targets) {
                    if (target == ship) {
                        continue;
                    }

                    float dist = MathUtils.getDistance(novaLocation, target.getLocation());
                    float dist2 = novaTime * 1500f + 50f;
                    if (dist - target.getCollisionRadius() <= dist2 && dist + target.getCollisionRadius() >= dist2) {
                        if (target.getOwner() == ship.getOwner()) {
                            Global.getCombatEngine().applyDamage(target, target.getLocation(), 300f,
                                    DamageType.ENERGY, 150f, false, false, ship, false);
                        } else {
                            Global.getCombatEngine().applyDamage(target, target.getLocation(), 3000f,
                                    DamageType.ENERGY, 1500f, false, false, ship, false);
                        }
                    }
                }
            }

            if (novaTime >= 1f) {
                novaTime = -1f;
            }
        }
    }
}
