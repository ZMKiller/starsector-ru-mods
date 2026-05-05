package data.ai;

import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;

public class HSILAAMAI implements MissileAIPlugin, GuidedMissileAI {
    private ShipAPI s = null;
    private MissileAPI m = null;
    private CombatEngineAPI engine;
    private Vector2f target = new Vector2f(0f, 0f);
    private final float DAMPING = 0.06f;
    private ShipAPI t = null;

    public HSILAAMAI(ShipAPI source, MissileAPI missile) {
        s = source;
        m = missile;
        engine = Global.getCombatEngine();
        targetUpdate();
        vtupdate();
    }

    private void targetUpdate() {
        if (m == null)
            return;
        if (s == null || !engine.isEntityInPlay(s) || !s.isAlive()) {
            t = MagicTargeting.pickMissileTarget(m, targetSeeking.NO_RANDOM, 2000, 360, 5, 25, 20, 15, 10);
            if (t != null)
                target = t.getLocation();
            return;
        }
        if (s.getAI() == null) {
            target = s.getMouseTarget();
        } else {
            if (s.getShipTarget() != null) {
                target = s.getShipTarget().getLocation();
            } else {
                t = MagicTargeting.pickMissileTarget(m, targetSeeking.NO_RANDOM, 2000, 360, 5, 25, 20, 15, 10);
                if (t != null)
                    target = t.getLocation();
            }
        }
    }

    private boolean vtupdate() {
        for (MissileAPI missile : engine.getMissiles()) {
            if (missile.getOwner() != m.getOwner()) {
                if (Misc.getDistance(missile.getLocation(), m.getLocation()) < 20f) {
                    engine.spawnDamagingExplosion(createExplosionSpec(m.getDamageAmount()), s, m.getLocation(),
                            false);
                    engine.removeEntity(m);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void advance(float amount) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (Global.getCombatEngine().isPaused() || m.isFading() || m.isFizzling() || m.didDamage()) {
            return;
        }
        targetUpdate();
        if (vtupdate())
            return;
        if (target == null)
            return;
        float correctAngle = VectorUtils.getAngle(
                m.getLocation(),
                target);

        float offCourseAngle = MathUtils.getShortestRotation(
                VectorUtils.getFacing(m.getVelocity()),
                correctAngle);

        float correction = MathUtils.getShortestRotation(
                correctAngle,
                VectorUtils.getFacing(m.getVelocity()) + 180)
                * 0.5f *
                (float) ((FastTrig.sin(MathUtils.FPI / 90 * (Math.min(Math.abs(offCourseAngle), 45)))));

        correctAngle = correctAngle + correction;

        float aimAngle = MathUtils.getShortestRotation(m.getFacing(), correctAngle);
        if (aimAngle < 0) {
            m.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            m.giveCommand(ShipCommand.TURN_LEFT);
        }
        if (Math.abs(aimAngle) < 45) {
            m.giveCommand(ShipCommand.ACCELERATE);
        }

        if (Math.abs(aimAngle) < Math.abs(m.getAngularVelocity()) * DAMPING) {
            m.setAngularVelocity(aimAngle / DAMPING);
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return t;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {

    }

    public void init(CombatEngineAPI engine) {
    }

    public DamagingExplosionSpec createExplosionSpec(float damage) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.15f, // duration
                20f, // radius
                15f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                1f, // particleSizeMin
                2f, // particleSizeRange
                0.4f, // particleDuration
                35, // particleCount
                new Color(255, 255, 255, 255), // particleColor
                new Color(100, 100, 255, 175) // explosionColor
        );

        spec.setDamageType(DamageType.FRAGMENTATION);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("explosion_guardian");
        return spec;
    }
}