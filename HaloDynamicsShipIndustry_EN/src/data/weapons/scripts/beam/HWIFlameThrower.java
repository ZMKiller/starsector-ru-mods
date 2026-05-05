package data.weapons.scripts.beam;

import java.awt.Color;
import java.util.Iterator;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HWIFlameThrower implements BeamEffectPlugin {
    public class FlameCone {
        float currAngle;
        Vector2f startPoint;
        Vector2f startPointVelocity;
        Vector2f FlameVelocity;
        float time;
        IntervalUtil timer;
        IntervalUtil damageChecker = new IntervalUtil(0.25f, 0.25f);
        float range;
        float damage;
        DamageType type = DamageType.FRAGMENTATION;
        float ringRange;
        float startFacing;
        CombatEngineAPI engine = Global.getCombatEngine();

        public FlameCone(float currAngle, Vector2f startPoint, Vector2f startPointVelocity, Vector2f FlameVelocity,
                float time, float range, float damage) {
            this.currAngle = currAngle;
            this.startPoint = startPoint;
            this.startPointVelocity = new Vector2f(startPointVelocity);
            this.FlameVelocity = new Vector2f(FlameVelocity);
            this.time = time;
            timer = new IntervalUtil(time, time);
            this.range = range;
            this.damage = damage;
            startFacing = VectorUtils.getFacing(FlameVelocity);
            ringRange = 0.5f * FlameVelocity.length();
        }

        public void advance(float amount) {
            timer.advance(amount);
            damageChecker.advance(amount);
            if (damageChecker.intervalElapsed()) {
                checkDamage();
            }
        }

        public void checkDamage() {
            Vector2f thisFrameStartPoint = Vector2f.add(startPoint,
                    (Vector2f) (new Vector2f(startPointVelocity).scale(timer.getElapsed())), null);
            float dis = FlameVelocity.length() * timer.getElapsed();
            for (ShipAPI s : engine.getShips()) {
                if (isInRange(s, thisFrameStartPoint, startFacing, dis - ringRange / 2, dis + ringRange / 2)) {
                    float sdis = Misc.getDistance(s.getLocation(), thisFrameStartPoint);
                    engine.applyDamage(s, s.getLocation(),
                            damage * (1 - sdis / range), type, 0f,
                            false, true,
                            null, false);
                }
            }
            for (MissileAPI m : engine.getMissiles()) {
                if (isInRange(m, thisFrameStartPoint, startFacing, dis - ringRange / 2, dis + ringRange / 2)) {
                    float sdis = Misc.getDistance(m.getLocation(), thisFrameStartPoint);
                    engine.applyDamage(m, m.getLocation(),
                            damage * (1 - sdis / range), type, 0f,
                            false, true,
                            null, false);
                }
            }
        }
    }

    private IntervalUtil fireInterval = new IntervalUtil(0.25f, 0.25f);
    private TimeoutTracker<FlameCone> flames = new TimeoutTracker<FlameCone>();
    private boolean wasZero = false;
    private float speed = 250f;

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (engine.isPaused())
            return;
        if (beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset
            // every frame as it should be
            if (!wasZero)
                dur = 0;
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            if(flames.getItems().size() == 0) fireInterval.setElapsed(0.2f);
            flames.advance(dur);
            fireInterval.advance(dur);
            if(flames.getItems().size()>0){
                Iterator<FlameCone> flamesLeft = flames.getItems().iterator();
                while(flamesLeft.hasNext()){
                    flamesLeft.next().advance(amount);
                }
            }
            Color color = Color.ORANGE;
            if (fireInterval.intervalElapsed()) {
                float time = beam.getWeapon().getRange() / speed;
                WeaponAPI weapon = beam.getWeapon();
                if(weapon.getSpec().getWeaponId().contentEquals("Inferno")) color = Color.BLUE;
                flames.add(new FlameCone(weapon.getCurrAngle(), weapon.getFirePoint(0), weapon.getShip().getVelocity(),
                        (Vector2f)(Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(speed)), time, weapon.getRange(), beam.getDamage().getDamage()/4f), time);
                engine.addNebulaParticle(weapon.getFirePoint(0), Vector2f.add(weapon.getShip().getVelocity(),(Vector2f)(Misc.getUnitVectorAtDegreeAngle(weapon.getCurrAngle()).scale(speed)),null), 8f,
                3f, 0.5f,
                0.7f, time, color);
            }
        }
    }

    private boolean isInRange(CombatEntityAPI entity, Vector2f startpoint, float startFacing, float rangeMin,
            float rangeMax) {
        Vector2f wloc = startpoint;
        Vector2f eloc = entity.getLocation();
        float linkAngle = Misc.getAngleInDegrees(wloc, eloc);
        float currAngle = startFacing;
        if (linkAngle < 0) {
            linkAngle += 360f;
        }
        float diff = linkAngle - currAngle;
        if (diff > 180f)
            diff = linkAngle - currAngle - 360f;
        float dis = Misc.getDistance(wloc, eloc);
        float JudgeAngle = 30f;

        return (Math.abs(diff) < JudgeAngle) && (dis < rangeMax && dis > rangeMin);
    }
}
