package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_FaerieVortexStats extends BaseShipSystemScript {

    private static final float ANGLE_FORCE_MULTIPLIER = 0.5f;
    private static final float DAMAGE_MOD_VS_CAPITAL = 0.15f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.20f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 0.5f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.95f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.9f;
    private static final Map<DamageType, Float> DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS = new HashMap<>(5);
    private static final Color EXPLOSION_COLOR = new Color(255, 75, 50);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 2000f;
    @SuppressWarnings("SuspiciousNameCombination")
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .25f;
    private static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 4000f;
    private static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .05f;
    private static final float EXPLOSION_RADIUS = 750f;
    private static final String EXPLOSION_SOUND = "swp_arcade_vortex_blast";
    private static final float EXPLOSION_VISUAL_RADIUS = 750f;
    private static final float MAX_EXPLOSION_RADIUS = 5000.0f;
    private static final float MAX_POWER_MULTIPLIER = 100.0f;
    private static final float MAX_RANGE_MULTIPLIER = 40f;
    private static final float MIN_POWER_MULTIPLIER = 0.5f;
    private static final float POWER_PER_FRIENDLY_DAMAGE_ABSORBED = 0.0004f;
    private static final float POWER_PER_HOSTILE_DAMAGE_ABSORBED = 0.0008f;
    private static final float SPARK_BRIGHTNESS = 2.5f;
    private static final Color SPARK_COLOR = new Color(255, 50, 25);
    private static final float SPARK_DURATION = 0.5f;
    private static final float SPARK_RADIUS = 10f;
    private static final float TEXT_AMOUNT_MULTIPLIER = 1000.0f;
    private static final Color TEXT_COLOR = new Color(255, 187, 0);
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
    private ShipAPI ship;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ship = (ShipAPI) stats.getEntity();

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
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (ship == null) {
            return;
        }
        ship.setPhased(false);
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
        ship.setHitpoints(Math.min(ship.getHitpoints() + powerAbsorbed * 200f, ship.getMaxHitpoints()));

        float sparkAngle = VectorUtils.getAngle(proj.getLocation(), ship.getLocation());
        sparkAngle *= Math.PI / 180f;
        Vector2f sparkVect = new Vector2f((float) Math.cos(sparkAngle), (float) Math.sin(sparkAngle));
        float distance = MathUtils.getDistance(proj, ship);
        float visualEffect = (float) Math.sqrt(powerAbsorbed * 1000);

        sparkVect.scale(3 * distance / SPARK_DURATION);

        Global.getSoundPlayer().playSound("swp_arcade_vortex_absorb", 1, Math.min(Math.max(visualEffect * 0.04f, 0.1f), 0.5f),
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

        float power = absorbedPower;
        power = Math.max(power, MIN_POWER_MULTIPLIER);
        power = Math.min(power, MAX_POWER_MULTIPLIER);
        power = (float) Math.sqrt(power);

        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power,
                0.21f * power);
        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power
                / 2f, 0.19f * power);

        StandardLight light = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
        light.setIntensity(3f);
        light.setSize(EXPLOSION_VISUAL_RADIUS * 2f);
        light.setColor(EXPLOSION_COLOR);
        light.fadeOut(1.25f);
        LightShader.addLight(light);

        Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, power, ship.getLocation(), ship.getVelocity());

        float damage, emp, mod, explosionRadius;
        explosionRadius = Math.min(MAX_EXPLOSION_RADIUS, EXPLOSION_RADIUS * power);

        List<ShipAPI> targets = SWP_Util.getShipsWithinRange(ship.getLocation(), explosionRadius);
        for (ShipAPI tmp : targets) {
            if (tmp == ship) {
                continue;
            }

            mod = 1f - (MathUtils.getDistance(ship, tmp) / explosionRadius);
            mod *= power;
            damage = EXPLOSION_DAMAGE_AMOUNT * mod;
            emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

            if (null != tmp.getHullSize()) {
                switch (tmp.getHullSize()) {
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

            if (tmp.getOwner() == ship.getOwner()) {
                damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
            }

            float shipRadius = SWP_Util.effectiveRadius(tmp);

            for (int x = 0; x < 4; x++) {
                engine.spawnEmpArc(ship, ship.getLocation(), tmp, tmp,
                        EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                        explosionRadius * 3, null, 20 * power, EXPLOSION_COLOR,
                        EXPLOSION_COLOR);

                engine.spawnEmpArc(ship,
                        MathUtils.getRandomPointInCircle(tmp.getLocation(), shipRadius),
                        tmp, tmp, EXPLOSION_DAMAGE_TYPE, damage / 10,
                        emp / 5, explosionRadius, null, 10f * power,
                        EXPLOSION_COLOR, EXPLOSION_COLOR);
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
}
