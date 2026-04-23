package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_PrismaticRayStats extends BaseShipSystemScript {

    private static final float ANGLE_FORCE_MULTIPLIER = 0.5f;
    private static final float DAMAGE_MOD_VS_CAPITAL = 0.15f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.20f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 1.0f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.95f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.9f;

    private static final Map<DamageType, Float> DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS = new HashMap<>(5);

    private static final String DATA_KEY = "SWP_PrismaticRay";

    private static final Color EXPLOSION_COLOR = new Color(255, 100, 200);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 1000f;
    @SuppressWarnings("SuspiciousNameCombination")
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .05f;
    private static final float EXPLOSION_RADIUS = 500f;
    private static final String EXPLOSION_SOUND = "swp_arcade_rayblast";
    private static final float EXPLOSION_VISUAL_RADIUS = 1000f;
    private static final float MAX_EXPLOSION_RADIUS = 1000.0f;
    private static final float MAX_POWER_MULTIPLIER = 100.0f;
    private static final float MAX_RANGE_MULTIPLIER = 100f;
    private static final float MIN_POWER_MULTIPLIER = 2f;
    private static final float POWER_PER_FRIENDLY_DAMAGE_ABSORBED = 0.001f;
    private static final float POWER_PER_HOSTILE_DAMAGE_ABSORBED = 0.0011f;
    private static final float SPARK_BRIGHTNESS = 2.5f;
    private static final Color SPARK_COLOR = new Color(255, 175, 200);
    private static final float SPARK_DURATION = 0.5f;
    private static final float SPARK_RADIUS = 5f;
    private static final float TEXT_AMOUNT_MULTIPLIER = 1000.0f;
    private static final Color TEXT_COLOR = new Color(0, 187, 255);
    private static final float VELOCITY_FORCE_MULTIPLIER = 2000f;
    private static final Vector2f ZERO = new Vector2f();

    static {
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.ENERGY, 1.0f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.FRAGMENTATION, 0.35f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.HIGH_EXPLOSIVE, 0.9f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.KINETIC, 0.85f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.OTHER, 0.5f);
    }

    private float absorbedPower = 0;
    private StandardLight prismalight = null;
    private ShipAPI ship;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        ship = (ShipAPI) stats.getEntity();
        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<MegaArc> arcs = localData.arcs;
        final List<MegaArc> newarcs = localData.newarcs;

        if (prismalight == null && state == State.IN) {
            absorbedPower = 0.01f;
            prismalight = new StandardLight(ZERO, ZERO, ZERO, ship);
            prismalight.setIntensity(1f);
            prismalight.setSize(EXPLOSION_VISUAL_RADIUS * 3f);
            prismalight.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());
            prismalight.fadeIn(0.1f);
            LightShader.addLight(prismalight);
        } else if (prismalight != null) {
            prismalight.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());
        }

        Iterator<MegaArc> iter = arcs.iterator();
        while (iter.hasNext()) {
            MegaArc arc = iter.next();
            if (Math.random() >= 0.5) {
                if (arc.advance(engine.getElapsedInLastFrame() * engine.getTimeMult().getModifiedValue() * 2f)) {
                    iter.remove();
                }
            }
        }

        iter = newarcs.iterator();
        while (iter.hasNext()) {
            MegaArc arc = iter.next();
            arcs.add(arc);
            iter.remove();
        }

        for (CombatEntityAPI entity : SWP_Util.getEntitiesWithinRange(ship.getLocation(), ship.getCollisionRadius()
                * MAX_RANGE_MULTIPLIER)) {
            if (!(entity instanceof DamagingProjectileAPI)) {
                continue;
            }

            DamagingProjectileAPI proj = (DamagingProjectileAPI) entity;
            if (proj.getBaseDamageAmount() <= 0) {
                continue;
            }

            if (state != State.OUT && MathUtils.getDistance(ship, proj) <= ship.getCollisionRadius()) {
                absorbProjectile(proj);
                continue;
            }

            suckInProjectile(proj, state, effectLevel);
        }

        if (state == State.OUT) {
            ship.setPhased(false);
        } else {
            ship.setPhased(true);
        }

        if (state == State.OUT && absorbedPower > 0) {
            doASplosion();
            absorbedPower = 0;
        }

        if (state == State.OUT && prismalight != null) {
            prismalight.fadeOut(5f);
            prismalight = null;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ship = (ShipAPI) stats.getEntity();
        ship.setPhased(false);

        if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
            Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        }
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData != null) {
            final List<MegaArc> arcs = localData.arcs;
            final List<MegaArc> newarcs = localData.newarcs;

            arcs.clear();
            newarcs.clear();
        }
    }

    private void absorbProjectile(DamagingProjectileAPI proj) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }
        float powerAbsorbed = proj.getDamageAmount();
        powerAbsorbed *= (proj.getOwner() == ship.getOwner()) ? POWER_PER_FRIENDLY_DAMAGE_ABSORBED
                : POWER_PER_HOSTILE_DAMAGE_ABSORBED;
        powerAbsorbed *= DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.get(proj.getDamageType());

        engine.addFloatingDamageText(ship.getLocation(), powerAbsorbed * TEXT_AMOUNT_MULTIPLIER, TEXT_COLOR, ship, proj);

        absorbedPower += powerAbsorbed;
        ship.setHitpoints(Math.min(ship.getHitpoints() + powerAbsorbed * 150f, ship.getMaxHitpoints()));

        float sparkAngle = VectorUtils.getAngle(proj.getLocation(), ship.getLocation());
        sparkAngle *= Math.PI / 180f;
        Vector2f sparkVect = new Vector2f((float) Math.cos(sparkAngle), (float) Math.sin(sparkAngle));
        float distance = MathUtils.getDistance(proj, ship);
        float visualEffect = (float) Math.sqrt(powerAbsorbed * 1000);

        sparkVect.scale(3 * distance / SPARK_DURATION);

        Global.getSoundPlayer().playSound("swp_arcade_crystalsuck", 1f, Math.min(Math.max(visualEffect * 0.2f, 0.4f), 2f),
                proj.getLocation(), sparkVect);

        engine.addHitParticle(proj.getLocation(), sparkVect, SPARK_RADIUS * visualEffect + SPARK_RADIUS,
                SPARK_BRIGHTNESS, SPARK_DURATION, SPARK_COLOR);
        engine.removeEntity(proj);
    }

    private void doASplosion() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final List<MegaArc> arcs = localData.arcs;

        float power = absorbedPower;
        power = Math.max(power, MIN_POWER_MULTIPLIER);
        power = Math.min(power, MAX_POWER_MULTIPLIER);
        power = (float) Math.sqrt(power);

        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power,
                0.21f * power);
        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power
                / 2f, 0.19f * power);

        for (int i = 0; i < 5; i++) {
            StandardLight light = new StandardLight();
            light.setLocation(MathUtils.getRandomPointInCircle(ship.getLocation(), EXPLOSION_VISUAL_RADIUS * power));
            light.setIntensity(1f);
            light.setSize(EXPLOSION_VISUAL_RADIUS * 2f);
            light.setColor((float) Math.random(), (float) Math.random(), (float) Math.random());
            light.fadeOut(5f);
            LightShader.addLight(light);
        }

        Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, power, ship.getLocation(), ship.getVelocity());

        for (int i = 0; i < (int) (power * 2.5f); i++) {
            Vector2f direction = new Vector2f(1f, 0f);
            VectorUtils.rotate(direction, (float) Math.random() * 360f, direction);
            Color color = new Color(SWP_Util.clamp255((int) ((float) Math.random() * 255f)),
                    SWP_Util.clamp255((int) ((float) Math.random() * 255f)),
                    SWP_Util.clamp255((int) ((float) Math.random() * 255f)),
                    150);
            Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            arcs.add(new MegaArc(ship, ship.getLocation(), direction, (float) Math.random() * 600f + 600f,
                    (float) Math.random() * 15f + 25f,
                    (float) Math.random() * 1.5f + 2f, color, color2));
        }

        ShipAPI victim;
        float damage, mod, explosionRadius;
        explosionRadius = Math.min(MAX_EXPLOSION_RADIUS, EXPLOSION_RADIUS * power);

        for (CombatEntityAPI tmp : SWP_Util.getEntitiesWithinRange(ship.getLocation(), explosionRadius)) {
            if (tmp == ship) {
                continue;
            }

            mod = 1f - (MathUtils.getDistance(ship, tmp) / explosionRadius);
            mod *= power;
            damage = EXPLOSION_DAMAGE_AMOUNT * mod;

            if (tmp instanceof ShipAPI) {
                victim = (ShipAPI) tmp;

                if (null != victim.getHullSize()) {
                    switch (victim.getHullSize()) {
                        case FIGHTER:
                            damage /= DAMAGE_MOD_VS_FIGHTER;
                            break;
                        case FRIGATE:
                            damage /= DAMAGE_MOD_VS_FRIGATE;
                            break;
                        case DESTROYER:
                            damage /= DAMAGE_MOD_VS_DESTROYER;
                            break;
                        case CRUISER:
                            damage /= DAMAGE_MOD_VS_CRUISER;
                            break;
                        case CAPITAL_SHIP:
                            damage /= DAMAGE_MOD_VS_CAPITAL;
                            break;
                        default:
                            break;
                    }
                }

                if (victim.getOwner() == ship.getOwner()) {
                    damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                }

                float shipRadius = SWP_Util.effectiveRadius(victim);

                for (int x = 0; x < 4; x++) {
                    engine.spawnEmpArc(ship, ship.getLocation(), victim, victim,
                            EXPLOSION_DAMAGE_TYPE, damage / 10, 0,
                            explosionRadius * 3, null, 20 * power, EXPLOSION_COLOR,
                            EXPLOSION_COLOR);

                    engine.spawnEmpArc(ship,
                            MathUtils.getRandomPointInCircle(victim.getLocation(),
                                    shipRadius),
                            victim, victim, EXPLOSION_DAMAGE_TYPE, damage / 10,
                            0, explosionRadius, null, 10f * power,
                            EXPLOSION_COLOR, EXPLOSION_COLOR);
                }
            }
        }
    }

    private void suckInProjectile(DamagingProjectileAPI proj, State state, float effectLevel) {
        if (proj instanceof MissileAPI) {
            ((MissileAPI) proj).flameOut();
        }

        if ((ship == null) || (proj == null)) {
            return;
        }

        float fromToAngle = VectorUtils.getAngle(ship.getLocation(), proj.getLocation());
        float angleDif = MathUtils.getShortestRotation(fromToAngle, MathUtils.clampAngle(proj.getFacing() + 180));
        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame() * Global.getCombatEngine().getTimeMult().getModifiedValue();
        float distance = MathUtils.getDistance(ship.getLocation(), proj.getLocation());
        float force = (ship.getCollisionRadius() / distance) * effectLevel * ANGLE_FORCE_MULTIPLIER;
        float dAngle = angleDif * objectiveAmount * force;
        fromToAngle *= Math.PI / 180;
        Vector2f speedUp = new Vector2f((float) Math.cos(fromToAngle) * objectiveAmount, (float) Math.sin(fromToAngle) * objectiveAmount);
        speedUp.scale(VELOCITY_FORCE_MULTIPLIER);

        if (state != State.OUT) {
            dAngle = -dAngle;
            speedUp.scale(-1);
        }

        Vector2f.add(proj.getVelocity(), speedUp, proj.getVelocity());
        VectorUtils.rotate(proj.getVelocity(), dAngle, proj.getVelocity());
        proj.setFacing(MathUtils.clampAngle(proj.getFacing() + dAngle * (float) (180 / Math.PI)));
    }

    private static final class LocalData {

        final List<MegaArc> arcs = new ArrayList<>(100);
        final List<MegaArc> newarcs = new ArrayList<>(100);
    }

    private static final class MegaArc {

        private final Color color;
        private final Color color2;
        private final Vector2f direction;
        private final IntervalUtil interval = new IntervalUtil(0.035f, 0.035f);
        private final Vector2f location;
        private float remaining;
        private final ShipAPI source;
        private final float speed;
        private final float thickness;

        private MegaArc(ShipAPI source, Vector2f location, Vector2f direction, float speed, float thickness,
                float lifetime, Color color, Color color2) {
            this.source = source;
            this.location = new Vector2f(location);
            this.direction = new Vector2f(direction);
            this.speed = speed;
            this.thickness = thickness;
            this.remaining = lifetime;
            this.color = color;
            this.color2 = color2;
        }

        private boolean advance(float amount) {
            final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
            if (localData == null) {
                return true;
            }

            final List<MegaArc> newarcs = localData.newarcs;

            interval.advance(amount);
            boolean intervalElapsed = interval.intervalElapsed();

            Vector2f newLocation = new Vector2f(direction);
            newLocation.scale(speed * amount);
            Vector2f.add(newLocation, location, newLocation);

            if (intervalElapsed) {
                Global.getCombatEngine().spawnEmpArc(source, location, new SimpleEntity(newLocation), new SimpleEntity(
                        newLocation), DamageType.ENERGY, 0f, 0f,
                        10000f, null, thickness, color, color2);
                VectorUtils.rotate(direction, (float) Math.random() * 40f - 20f, direction);
            }

            location.set(newLocation);

            if (intervalElapsed) {
                List<CombatEntityAPI> targets = SWP_Util.getEntitiesWithinRange(newLocation, 100f);
                for (CombatEntityAPI target : targets) {
                    if (target != source && target.getCollisionClass() != CollisionClass.NONE) {
                        if (target instanceof DamagingProjectileAPI) {
                            DamagingProjectileAPI proj = (DamagingProjectileAPI) target;
                            if (proj.getBaseDamageAmount() <= 0) {
                                continue;
                            }
                        }
                        if (target.getOwner() == source.getOwner()) {
                            Global.getCombatEngine().applyDamage(target, newLocation, 150f, EXPLOSION_DAMAGE_TYPE,
                                    30f, false, false, source, false);
                        } else {
                            Global.getCombatEngine().applyDamage(target, newLocation, 3000f, EXPLOSION_DAMAGE_TYPE,
                                    600f, false, false, source, false);
                        }
                    }
                }

                if (Math.random() > 0.96) {
                    Global.getSoundPlayer().playSound("swp_arcade_crystalsuck", 1f + (float) Math.random(), 0.5f, location, ZERO);
                    Vector2f newDirection = new Vector2f(1f, 0f);
                    VectorUtils.rotate(newDirection, (float) Math.random() * 360f, newDirection);
                    newarcs.add(new MegaArc(source, location, newDirection, speed + (float) Math.random() * 100f - 50f,
                            Math.max(thickness - (float) Math.random()
                                    * 5f, 2.5f),
                            Math.max(
                                    remaining + (float) Math.random() * 0.5f - 0.25f, 0.01f), color,
                            color2));
                }
            }

            remaining -= amount;
            return remaining <= 0f;
        }
    }
}
