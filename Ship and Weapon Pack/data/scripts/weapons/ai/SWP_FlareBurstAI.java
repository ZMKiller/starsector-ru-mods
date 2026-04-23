package data.scripts.weapons.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_FlareBurstAI extends SWP_BaseMissile {

    private static final Color EXPLOSION_COLOR = new Color(100, 255, 100, 255);
    private static final float MIN_TRAVEL_TIME = 0.5f;
    private static final int NUM_PARTICLES = 30;
    private static final Color PARTICLE_COLOR = new Color(150, 255, 150, 255);
    private static final float PROXIMITY_RANGE = 500f;
    private static final String SOUND_FILE = "explosion_flak";
    private static final String SOUND_FILE_2 = "hit_heavy_energy";
    private static final String SOUND_FILE_3 = "swp_flareburst_explode";
    private static final float TARGET_ACQUISITION_RANGE = 1800f;
    private static final float VELOCITY_DAMPING_FACTOR = 0.05f;
    private static final Vector2f ZERO = new Vector2f();

    private static void explode(DamagingProjectileAPI projectile, Vector2f point, CombatEngineAPI engine) {
        float visualArea = 200f;
        engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, visualArea / 2f, 0.1f);
        engine.addSmoothParticle(point, ZERO, visualArea * 1.5f, 0.75f, 1f, EXPLOSION_COLOR);
        engine.addHitParticle(point, ZERO, visualArea * 1.25f, 0.5f, 0.75f, EXPLOSION_COLOR);
        float speed = 400f;
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                    speed * 0.05f, speed * 0.3f),
                    (float) Math.random() * 360f), 14f, 1f,
                    MathUtils.getRandomNumberInRange(0.5f, 1.2f), PARTICLE_COLOR);
        }

        if (!projectile.didDamage()) {
            StandardLight light = new StandardLight(point, ZERO, ZERO, null);
            light.setColor(EXPLOSION_COLOR);
            light.setSize(visualArea * 1.5f);
            light.setIntensity(0.3f);
            light.fadeOut(0.5f);
            LightShader.addLight(light);
        }

        for (int i = 0; i < 10; i++) {
            float angle = projectile.getFacing() + i * 36f - 162f + (float) Math.random() * 45f;
            if (angle < 0f) {
                angle += 360f;
            } else if (angle >= 360f) {
                angle -= 360f;
            }
            Vector2f location = MathUtils.getPointOnCircumference(projectile.getLocation(), 5f, angle);
            DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(
                    projectile.getSource(), projectile.getWeapon(),
                    "swp_burstflare_wpn", location, angle,
                    MathUtils.getPointOnCircumference(null,
                            (float) Math.random() * 50f + 50f,
                            (float) Math.random() * 360f));
            newProj.setFromMissile(true);
        }

        Global.getSoundPlayer().playSound(SOUND_FILE, 1f, 1f, point, ZERO);
        Global.getSoundPlayer().playSound(SOUND_FILE_2, 1f, 1f, point, ZERO);
        Global.getSoundPlayer().playSound(SOUND_FILE_3, 1f, 1f, point, ZERO);
    }

    private static List<MissileAPI> getSortedDirectMissileTargets(ShipAPI launchingShip) {
        List<MissileAPI> directTargets = SWP_Util.getMissilesWithinRange(launchingShip.getMouseTarget(), 300f);
        if (!directTargets.isEmpty()) {
            Collections.sort(directTargets, new CollectionUtils.SortEntitiesByDistance(launchingShip.getMouseTarget()));
        }
        return directTargets;
    }

    private float currentTime = 0f;

    public SWP_FlareBurstAI(MissileAPI missile, ShipAPI launchingShip) {
        super(missile, launchingShip);
    }

    @Override
    public void advance(float amount) {
        currentTime += amount;

        if (missile.isFizzling() || missile.isFading()) {
            explode(missile, missile.getLocation(), Global.getCombatEngine());
            Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 2f,
                    DamageType.FRAGMENTATION, 0f, false, false, missile, false);
            return;
        }

        float maxSpeed = missile.getMaxSpeed();
        if (!acquireTarget(amount)) {
            if (missile.getVelocity().length() >= (maxSpeed / 2f)) {
                missile.giveCommand(ShipCommand.DECELERATE);
            } else {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }
            return;
        }

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation())
                - target.getCollisionRadius();

        if (distance <= PROXIMITY_RANGE && target instanceof MissileAPI && currentTime >= MIN_TRAVEL_TIME) {
            explode(missile, missile.getLocation(), Global.getCombatEngine());
            Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 2f,
                    DamageType.FRAGMENTATION, 0f, false, false, missile, false);
            return;
        }

        if (missile.getEngineController() != null) {
            if (missile.getEngineController().isFlamedOut() || missile.getEngineController().isFlamingOut()) {
                return;
            }
        }

        distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());

        float acceleration = missile.getAcceleration();
        float guidance = 0.5f;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue()
                    - missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.5f;
        }
        Vector2f guidedTarget = interceptAdvanced(missile.getLocation(), missile.getVelocity().length(), acceleration,
                maxSpeed, target.getLocation(), target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float velocityFacing = VectorUtils.getFacing(missile.getVelocity());
        float absoluteDistance = MathUtils.getShortestRotation(velocityFacing, VectorUtils.getAngleStrict(
                missile.getLocation(), guidedTarget));
        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngleStrict(
                missile.getLocation(), guidedTarget));
        float compensationDifference = MathUtils.getShortestRotation(angularDistance, absoluteDistance);
        if (Math.abs(compensationDifference) <= 75f) {
            angularDistance += 0.5f * compensationDifference;
        }
        float absDVel = Math.abs(absoluteDistance);
        float absDAng = Math.abs(angularDistance);

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        if (absDVel >= 135f || ((absDVel <= 90f && (absDVel <= 45f || distance >= 500f))
                || missile.getVelocity().length() <= maxSpeed * 0.75f)) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        if (absDAng < 5) {
            float MFlightAng = VectorUtils.getAngleStrict(new Vector2f(0, 0), missile.getVelocity());
            float MFlightCC = MathUtils.getShortestRotation(missile.getFacing(), MFlightAng);
            if (Math.abs(MFlightCC) > 20) {
                missile.giveCommand(MFlightCC < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT);
            }
        }

        if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR) {
            missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
        }
    }

    @Override
    protected void assignMissileToShipTarget(ShipAPI launchingShip) {
        /* Do nothing */
    }

    @Override
    protected CombatEntityAPI findBestTarget() {
        CombatEntityAPI closest = null;
        float distance, closestDistance = TARGET_ACQUISITION_RANGE;
        List<MissileAPI> missiles = AIUtils.getEnemyMissilesOnMap(missile);
        int missilesSize = missiles.size();
        for (int i = 0; i < missilesSize; i++) {
            MissileAPI tmp = missiles.get(i);
            if (!isTargetValid(tmp)) {
                continue;
            }
            float damage = (float) Math.sqrt(tmp.getDamageAmount()
                    * (tmp.getDamageType() == DamageType.FRAGMENTATION ? 0.25f : 1f));
            if (damage <= 0f) {
                damage = 1f;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) / damage;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    protected CombatEntityAPI getMouseTarget(ShipAPI launchingShip) {
        ListIterator<MissileAPI> iter = getSortedDirectMissileTargets(launchingShip).listIterator();
        while (iter.hasNext()) {
            MissileAPI tmp = iter.next();
            if (isTargetValid(tmp)) {
                return tmp;
            }
        }
        return null;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target) {
        if (target instanceof MissileAPI msl) {
            return !(msl.isFlare() || msl.isFizzling() || msl.isFading() || (msl.getOwner() == missile.getOwner()) || (msl.getCollisionClass() == CollisionClass.NONE));
        }
        return false;
    }
}
