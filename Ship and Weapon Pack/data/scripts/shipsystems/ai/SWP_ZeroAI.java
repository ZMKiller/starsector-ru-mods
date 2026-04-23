package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_ZeroAI implements ShipSystemAIScript {

    private static final float SECONDS_TO_LOOK_AHEAD = 0.25f;

    private static Vector2f getClamped(Vector2f origin, Vector2f target, float clamp) {
        Vector2f d = new Vector2f();

        Vector2f.sub(target, origin, d);
        float l = d.length();
        if (l > clamp) {
            d.scale(clamp / l);
        }

        Vector2f.add(origin, d, d);

        return d;
    }

    private CombatEngineAPI engine;

    private final CollectionUtils.CollectionFilter<DamagingProjectileAPI> filterMisses = new CollectionUtils.CollectionFilter<DamagingProjectileAPI>() {
        @Override
        public boolean accept(DamagingProjectileAPI proj) {
            if (proj.getOwner() == ship.getOwner() && (!(proj instanceof MissileAPI) || !((MissileAPI) proj).isFizzling())) {
                return false;
            }

            if (proj instanceof MissileAPI missileAPI) {
                if (missileAPI.getEngineController().isTurningLeft() || missileAPI.getEngineController().isTurningRight()) {
                    return false;
                }
            }

            return (CollisionUtils.getCollides(proj.getLocation(), Vector2f.add(proj.getLocation(), (Vector2f) new Vector2f(proj.getVelocity()).scale(
                    SECONDS_TO_LOOK_AHEAD), null), ship.getLocation(), ship.getCollisionRadius())
                    && Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), ship.getLocation()))) <= 90f);
        }
    };

    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);
    private final IntervalUtil tracker2 = new IntervalUtil(0.05f, 0.3f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);
        tracker2.advance(amount);

        if (tracker.intervalElapsed()) {
            if (ship.getHullLevel() > 0.67f) {
                return;
            }

            List<ShipAPI> possibleTargets = SWP_Util.getShipsWithinRange(ship.getLocation(), 5000f);
            Iterator<ShipAPI> iter = possibleTargets.iterator();
            while (iter.hasNext()) {
                ShipAPI possibleTarget = iter.next();
                if (possibleTarget.isFighter() || possibleTarget.isDrone() || !possibleTarget.isAlive() || possibleTarget == ship
                        || possibleTarget.getOwner()
                        == ship.getOwner()) {
                    iter.remove();
                }
            }

            if (!possibleTargets.isEmpty()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
        }

        if (tracker2.intervalElapsed()) {
            if (!AIUtils.canUseSystemThisFrame(ship) || ship.getPhaseCloak().isActive()) {
                return;
            }

            boolean shouldUseSystem = false;

            List<DamagingProjectileAPI> nearbyThreats = new ArrayList<>(100);
            for (DamagingProjectileAPI tmp : engine.getProjectiles()) {
                if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), 1250f)) {
                    nearbyThreats.add(tmp);
                }
            }
            nearbyThreats = CollectionUtils.filter(nearbyThreats, filterMisses);
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, 500f);
            for (MissileAPI missile : nearbyMissiles) {
                if (!missile.getEngineController().isTurningLeft() && !missile.getEngineController().isTurningRight()) {
                    continue;
                }

                nearbyThreats.add(missile);
            }

            float decisionLevel = 0f;
            for (DamagingProjectileAPI threat : nearbyThreats) {
                decisionLevel += Math.pow(threat.getDamageAmount() * ((threat.getDamageType() == DamageType.FRAGMENTATION) ? 0.25f : 1f) / 125f, 1.3f);
            }
            List<BeamAPI> nearbyBeams = engine.getBeams();
            for (BeamAPI beam : nearbyBeams) {
                if (beam.getDamageTarget() == ship) {
                    float damage;
                    if (beam.getWeapon().getDerivedStats().getSustainedDps() < beam.getWeapon().getDerivedStats().getDps()) {
                        damage = beam.getWeapon().getDerivedStats().getBurstDamage() / beam.getWeapon().getDerivedStats().getBurstFireDuration();
                    } else {
                        damage = beam.getWeapon().getDerivedStats().getDps();
                    }
                    decisionLevel += Math.pow(damage * ((beam.getWeapon().getDamageType() == DamageType.FRAGMENTATION) ? 0.25f : 1f) / 125f, 1.3f);
                }
            }
            decisionLevel *= 0.33f;
            if (!flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
                decisionLevel *= 0.75f;
            }
            if (flags.hasFlag(AIFlags.PURSUING)) {
                decisionLevel *= 0.5f;
                decisionLevel += 10f;
            } else if (flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACK_OFF_MIN_RANGE) || flags.hasFlag(AIFlags.BACKING_OFF)) {
                decisionLevel *= 0.5f;
                decisionLevel += 10f;
            }
            List<WeaponAPI> weapons = ship.getAllWeapons();
            for (WeaponAPI weapon : weapons) {
                if (weapon.isFiring() && weapon.getCooldownRemaining() <= 0f) {
                    if (weapon.getId().contentEquals("swp_arcade_zeroannihilator")) {
                        decisionLevel -= 30f;
                    }
                    if (weapon.getId().contentEquals("swp_arcade_zerobeam")) {
                        decisionLevel -= 60f;
                    }
                }
            }

            if (decisionLevel * MathUtils.getRandomNumberInRange(0.5f, 1f) >= (float) Math.random() * 12.5f + 7.5f) {
                shouldUseSystem = true;
            }

            float maxDistance;
            if (ship.getHullLevel() > 0.67f) {
                maxDistance = 300f;
            } else if (ship.getHullLevel() > 0.33f) {
                maxDistance = 450f;
            } else {
                maxDistance = 600f;
            }
            switch (ship.getHullSpec().getBaseHullId()) {
                case "swp_arcade_superzero" ->
                    maxDistance += 150f;
                case "swp_arcade_hyperzero" ->
                    maxDistance += 300f;
                default -> {
                }
            }

            if (shouldUseSystem) {
                if (flags.hasFlag(AIFlags.PURSUING) && ship.getShipTarget() != null) {
                    ship.getMouseTarget().set(ship.getShipTarget().getLocation());
                    ship.giveCommand(ShipCommand.USE_SYSTEM, getClamped(ship.getLocation(), ship.getShipTarget().getLocation(), maxDistance), 0);
                } else if ((flags.hasFlag(AIFlags.BACK_OFF) || flags.hasFlag(AIFlags.BACK_OFF_MIN_RANGE) || flags.hasFlag(AIFlags.BACKING_OFF))
                        && ship.getShipTarget() != null) {
                    Vector2f point = new Vector2f(ship.getShipTarget().getLocation());
                    Vector2f.sub(ship.getLocation(), point, point);
                    Vector2f.add(point, ship.getLocation(), point);
                    ship.getMouseTarget().set(point);
                    ship.giveCommand(ShipCommand.USE_SYSTEM, getClamped(ship.getLocation(), point, maxDistance), 0);
                } else {
                    Vector2f point = MathUtils.getPointOnCircumference(ship.getLocation(), 300f, ship.getFacing() + (Math.random() > 0.5 ? 90f : -90f)
                            * ((float) Math.random() * 0.5f
                            + 0.75f));
                    ship.getMouseTarget().set(point);
                    ship.giveCommand(ShipCommand.USE_SYSTEM, getClamped(ship.getLocation(), point, maxDistance), 0);
                }
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
    }

}
