package data.ai.HSIMissileAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;

import java.awt.*;

public class HSIAntiWeaponMissileAI implements MissileAIPlugin, GuidedMissileAI {
    private ShipAPI s = null;
    private MissileAPI m = null;
    private CombatEngineAPI engine;
    private Vector2f target = new Vector2f(0f, 0f);
    private final float DAMPING = 0.06f;
    private CombatEntityAPI t = null;

    private  WeaponAPI tw = null;

    public HSIAntiWeaponMissileAI(ShipAPI source, MissileAPI missile) {
        s = source;
        m = missile;
        engine = Global.getCombatEngine();
        targetUpdate();
    }

    private void targetUpdate() {
        if (m == null)
            return;
        if (t == null || !engine.isEntityInPlay(t) || (t instanceof  ShipAPI&&!((ShipAPI) t).isAlive())) {
            t = MagicTargeting.pickMissileTarget(m, MagicTargeting.targetSeeking.NO_RANDOM, getRangeLeft(), 360, 5, 25, 20, 15, 10);
            weaponUpdate();
            if (tw != null)
                target = tw.getLocation();
            return;
        }
        if (s.getAI() == null) {
            target = s.getMouseTarget();
        } else {
            if (s.getShipTarget() != null) {
                t = s.getShipTarget();
                weaponUpdate();
                if(tw!=null) target = tw.getLocation();
            } else {
                t = MagicTargeting.pickMissileTarget(m, MagicTargeting.targetSeeking.NO_RANDOM, getRangeLeft(), 360, 5, 25, 20, 15, 10);
                weaponUpdate();
                if (tw != null)
                    target = tw.getLocation();
            }
        }
    }

    private void weaponUpdate() {
        WeightedRandomPicker<WeaponAPI> w = new WeightedRandomPicker<>();
        if(t!=null) {
            if(t instanceof  ShipAPI) {
                ShipAPI targetS = (ShipAPI) t;
                for(WeaponAPI weapon:targetS.getAllWeapons()) {
                    if(weapon.getSlot().isHidden()||weapon.getSlot().isDecorative()) continue;
                    w.add(weapon,4*weapon.getSize().ordinal()+1);
                }
                tw = w.pick();
            }
        }
    }

    private  void explosionUpdate(){
        if(Misc.getDistance(m.getLocation(),tw.getLocation())<10f){
            m.explode();
        }
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
        explosionUpdate();
    }

    @Override
    public CombatEntityAPI getTarget() {
        return t;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.t = target;
        weaponUpdate();
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

    private  int getRangeLeft(){
        return  (int)((m.getMaxFlightTime()-m.getFlightTime())*m.getMaxSpeed()*0.9);
    }
}
