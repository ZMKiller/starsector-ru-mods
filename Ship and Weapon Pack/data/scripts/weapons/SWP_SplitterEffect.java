package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.everyframe.SWP_Trails;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_SplitterEffect implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin {

    private static final String DATA_KEY_PREFIX = "SWP_SpliterWeapon_";

    private static final float GUNGNIR_DEFAULT_RANGE = 1500f;
    private static final float GUNGNIR_DEFAULT_SPEED = 550f;
    private static final Color GUNGNIR_DETONATION_COLOR = new Color(255, 150, 75, 225);
    private static final float GUNGNIR_DETONATION_DURATION = 0.6f;
    private static final float GUNGNIR_DETONATION_SIZE = 120f;
    private static final String GUNGNIR_DETONATION_SOUND_ID = "swp_gungnir_split";
    private static final float GUNGNIR_FUSE_DISTANCE = 200f;
    private static final Color GUNGNIR_PARTICLE_COLOR = new Color(255, 150, 150, 200);
    private static final int GUNGNIR_PARTICLE_COUNT = 40;
    private static final String GUNGNIR_PROJECTILE_ID = "swp_gungnir_shot";
    private static final float GUNGNIR_SPLIT_DISTANCE = 600f;
    private static final float GUNGNIR_SPREAD_FORCE_MAX = 80f;
    private static final float GUNGNIR_SPREAD_FORCE_MIN = 30f;
    private static final int GUNGNIR_SUBMUNITIONS = 8;
    private static final String GUNGNIR_SUBMUNITION_WEAPON_ID = "swp_gungnir_sub";

    private static final float CANISTER_DEFAULT_RANGE = 3000f;
    private static final float CANISTER_DEFAULT_SPEED = 700f;
    private static final Color CANISTER_DETONATION_COLOR = new Color(255, 100, 75, 255);
    private static final float CANISTER_DETONATION_DURATION = 1f;
    private static final float CANISTER_DETONATION_SIZE = 200f;
    private static final String CANISTER_DETONATION_SOUND_ID = "swp_boss_canistercannon_split";
    private static final float CANISTER_FUSE_DISTANCE = 500f;
    private static final Color CANISTER_PARTICLE_COLOR = new Color(255, 150, 150, 255);
    private static final int CANISTER_PARTICLE_COUNT = 100;
    private static final String CANISTER_PROJECTILE_ID = "swp_boss_canistercannon_shot";
    private static final float CANISTER_SPLIT_DISTANCE = 300f;
    private static final float CANISTER_SPREAD_FORCE_MAX = 300f;
    private static final float CANISTER_SPREAD_FORCE_MIN = 100f;
    private static final int CANISTER_SUBMUNITIONS = 20;
    private static final String CANISTER_SUBMUNITION_WEAPON_ID = "swp_boss_canister_sub";

    private static final float ORIGINAL_PROJECTILE_DAMAGE_MULTIPLIER = 0.5f;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        if ((projectile == null) || (weapon == null)) {
            return;
        }

        SWP_Trails.createIfNeeded();

        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> splitterProjectiles = localData.splitterProjectiles;

        projectile.getDamage().setDamage(projectile.getDamage().getDamage() * ORIGINAL_PROJECTILE_DAMAGE_MULTIPLIER);
        splitterProjectiles.add(projectile);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        final String DATA_KEY = DATA_KEY_PREFIX + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
        LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            engine.getCustomData().put(DATA_KEY, localData);
        }
        final Set<DamagingProjectileAPI> splitterProjectiles = localData.splitterProjectiles;

        if (splitterProjectiles.isEmpty()) {
            return;
        }

        Iterator<DamagingProjectileAPI> iter = splitterProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj)) {
                iter.remove();
            }
        }

        iter = splitterProjectiles.iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI projectile = iter.next();
            String spec = projectile.getProjectileSpecId();
            if (spec == null) {
                iter.remove();
                continue;
            }

            String newSpec;
            int submunitions;
            float fuseDistance;
            float splitDistance;
            float splitForceMin;
            float splitForceMax;
            float defaultRange;
            float defaultSpeed;
            Color detonateColor;
            float detonateSize;
            float detonateDuration;
            String detonateSound;
            Color particleColor;
            int particleCount;
            switch (spec) {
                case GUNGNIR_PROJECTILE_ID -> {
                    newSpec = GUNGNIR_SUBMUNITION_WEAPON_ID;
                    submunitions = GUNGNIR_SUBMUNITIONS;
                    fuseDistance = GUNGNIR_FUSE_DISTANCE;
                    splitDistance = GUNGNIR_SPLIT_DISTANCE;
                    splitForceMin = GUNGNIR_SPREAD_FORCE_MAX;
                    splitForceMax = GUNGNIR_SPREAD_FORCE_MIN;
                    defaultRange = GUNGNIR_DEFAULT_RANGE;
                    defaultSpeed = GUNGNIR_DEFAULT_SPEED;
                    detonateColor = GUNGNIR_DETONATION_COLOR;
                    detonateSize = GUNGNIR_DETONATION_SIZE;
                    detonateDuration = GUNGNIR_DETONATION_DURATION;
                    detonateSound = GUNGNIR_DETONATION_SOUND_ID;
                    particleColor = GUNGNIR_PARTICLE_COLOR;
                    particleCount = GUNGNIR_PARTICLE_COUNT;
                }
                case CANISTER_PROJECTILE_ID -> {
                    newSpec = CANISTER_SUBMUNITION_WEAPON_ID;
                    submunitions = CANISTER_SUBMUNITIONS;
                    fuseDistance = CANISTER_FUSE_DISTANCE;
                    splitDistance = CANISTER_SPLIT_DISTANCE;
                    splitForceMin = CANISTER_SPREAD_FORCE_MAX;
                    splitForceMax = CANISTER_SPREAD_FORCE_MIN;
                    defaultRange = CANISTER_DEFAULT_RANGE;
                    defaultSpeed = CANISTER_DEFAULT_SPEED;
                    detonateColor = CANISTER_DETONATION_COLOR;
                    detonateSize = CANISTER_DETONATION_SIZE;
                    detonateDuration = CANISTER_DETONATION_DURATION;
                    detonateSound = CANISTER_DETONATION_SOUND_ID;
                    particleColor = CANISTER_PARTICLE_COLOR;
                    particleCount = CANISTER_PARTICLE_COUNT;
                }
                default -> {
                    continue;
                }
            }

            if (projectile.isFading() || projectile.didDamage()) {
                iter.remove();
                continue;
            }

            boolean shouldSplit = false;
            Vector2f loc = projectile.getLocation();
            Vector2f vel = projectile.getVelocity();
            float speedScalar;
            float rangeScalar;
            if (projectile.getSource() != null) {
                speedScalar = projectile.getSource().getMutableStats().getProjectileSpeedMult().getModifiedValue();
                rangeScalar = projectile.getSource().getMutableStats().getBallisticWeaponRangeBonus().computeEffective(defaultRange) / defaultRange;
            } else {
                rangeScalar = 1f;
                speedScalar = 1f;
            }
            float speed = defaultSpeed * speedScalar;

            // Real quick and dirty fuse distance that works in most cases
            float fuseTime = fuseDistance / speed;
            if (projectile.getElapsed() < fuseTime) {
                continue;
            }

            splitDistance *= rangeScalar;

            // This is some bullshit to make the weapon fade sooner than normal
            float detonateTime;
            if (projectile.getWeapon() != null) {
                detonateTime = (projectile.getWeapon().getRange() - splitDistance) / speed;
            } else {
                detonateTime = (defaultRange - splitDistance) / speed;
            }
            if (projectile.getElapsed() >= detonateTime) {
                shouldSplit = true;
            }

            if (!shouldSplit) {
                // Check to see if the projectile should detonate
                Vector2f projection = new Vector2f(splitDistance, 0f);
                VectorUtils.rotate(projection, projectile.getFacing(), projection);
                Vector2f.add(loc, projection, projection);

                List<ShipAPI> checkList = engine.getShips();
                List<ShipAPI> finalList = new LinkedList<>();
                int listSize = checkList.size();
                for (int j = 0; j < listSize; j++) {
                    ShipAPI ship = checkList.get(j);
                    boolean isInShields = false;
                    if (ship.getShield() != null && ship.getShield().isOn()) {
                        if (MathUtils.isWithinRange(loc, ship.getLocation(), ship.getShield().getRadius() + splitDistance)) {
                            isInShields = ship.getShield().isWithinArc(loc);
                        }
                    }

                    if (!isInShields) {
                        if (!MathUtils.isWithinRange(loc, ship.getLocation(), ship.getCollisionRadius() + splitDistance)) {
                            continue;
                        }
                    }

                    if (isInShields) {
                        if (CollisionUtils.getCollides(loc, projection, ship.getLocation(), ship.getShield().getRadius())) {
                            finalList.add(ship);
                        }
                    } else if (CollisionUtils.getCollides(loc, projection, ship.getLocation(), ship.getCollisionRadius())) {
                        // Workaround until LazyLib is patched
                        BoundsAPI bounds = ship.getExactBounds();
                        if (bounds != null) {
                            bounds.update(ship.getLocation(), ship.getFacing());
                        }
                        Vector2f point = CollisionUtils.getCollisionPoint(loc, projection, ship);
                        if (point != null && MathUtils.getDistance(loc, point) <= splitDistance) {
                            finalList.add(ship);
                        }
                    }

                }

                ShipAPI closest = null;
                float closestSquareDistance = Float.MAX_VALUE;
                listSize = finalList.size();
                for (int j = 0; j < listSize; j++) {
                    ShipAPI ship = finalList.get(j);
                    float squareDistance = MathUtils.getDistanceSquared(loc, ship.getLocation());
                    if (squareDistance < closestSquareDistance) {
                        closestSquareDistance = squareDistance;
                        closest = ship;
                    }
                }

                if (closest != null) {
                    if (((closest.getOwner() == 1) || (closest.getOwner() == 0)) && (closest.getOwner() != projectile.getOwner())) {
                        shouldSplit = true;
                    }
                }
            }

            if (shouldSplit) {
                Vector2f scaledVel = new Vector2f(vel);
                scaledVel.scale(0.5f);
                engine.spawnExplosion(loc, scaledVel, detonateColor, detonateSize, detonateDuration);
                Global.getSoundPlayer().playSound(detonateSound, 1f, 1f, loc, scaledVel);
                float forceMultiplier = vel.length() / speed;

                for (int j = 0; j < particleCount; j++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, (speedScalar / rangeScalar) * forceMultiplier
                            * MathUtils.getRandomNumberInRange(splitForceMin, splitForceMax));
                    randomVel.scale((float) Math.random() + 0.75f);
                    Vector2f.add(vel, randomVel, randomVel);
                    randomVel.scale((float) Math.random() + 0.25f);
                    engine.addHitParticle(loc, randomVel, (float) Math.random() * 2f + 6f, 1f, ((float) Math.random() * 0.75f + 1.25f) * detonateDuration, particleColor);
                }

                Vector2f defaultVel = new Vector2f(defaultSpeed * speedScalar, 0f);
                VectorUtils.rotate(defaultVel, projectile.getFacing(), defaultVel);
                Vector2f actualVel = new Vector2f();
                for (int j = 0; j < submunitions; j++) {
                    Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, (speedScalar / rangeScalar) * forceMultiplier
                            * MathUtils.getRandomNumberInRange(splitForceMin, splitForceMax));
                    Vector2f.add(defaultVel, randomVel, actualVel);
                    Vector2f.add(vel, randomVel, randomVel);
                    DamagingProjectileAPI subProj = (DamagingProjectileAPI) engine.spawnProjectile(projectile.getSource(),
                            projectile.getWeapon(), newSpec, loc, VectorUtils.getFacing(actualVel), randomVel);
                    Vector2f subVel = subProj.getVelocity();
                    Vector2f.sub(subVel, defaultVel, subVel);
                }
                iter.remove();
                engine.removeEntity(projectile);
            }
        }
    }

    private static final class LocalData {

        final Set<DamagingProjectileAPI> splitterProjectiles = new LinkedHashSet<>(100);
    }
}
