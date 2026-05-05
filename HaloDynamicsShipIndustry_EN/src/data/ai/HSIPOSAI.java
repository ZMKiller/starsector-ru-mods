package data.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kit.AjimusUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HSIPOSAI implements MissileAIPlugin {

    public enum MODE{
        SAVE,REPAIR,DEFENSE,REVENGE;
    }
    public static final String KEY = "HSIPOSAI";
    private ShipAPI source;
    private MissileAPI missile;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private IntervalUtil scanTimer = new IntervalUtil(0.1f, 0.2f);
    private float r = (float) Math.random();
    private IntervalUtil updateListTracker = new IntervalUtil(0.05f, 0.1f);
    private List<CombatEntityAPI> hardAvoidList = new ArrayList<CombatEntityAPI>();
    public static float MAX_HARD_AVOID_RANGE = 200;
    public static float AVOID_RANGE = 50;
    public static float COHESION_RANGE = 100;
    private CombatEntityAPI target = null;

    public HSIPOSAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        source = launchingShip;
    }

    private MODE getMode(ShipAPI launchingShip){
        WeightedRandomPicker<MODE> picker = new WeightedRandomPicker<>();

        return picker.pick();
    }

    @Override
    public void advance(float amount) {
        scanTimer.advance(amount);
        updateListTracker.advance(amount);
        if (updateListTracker.intervalElapsed()) {
            updateHardAvoidList();
        }

        if(!AjimusUtils.isTargetLegal(target)){
            target = null;
        }
        if (target == null) {
            if (scanTimer.intervalElapsed()) {
                if (source.getShipTarget() != null) {
                    target = source.getShipTarget();
                } else if (missile.getMaxFlightTime() - missile.getFlightTime() < 10f) {
                    if (source.getShipTarget() != null) {
                        target = source.getShipTarget();
                    } else {
                        target = AIUtils.getNearestEnemy(source);
                    }
                }
            }
        } else if (target.isExpired() || !engine.isEntityInPlay(target)) {
            target = AIUtils.getNearestEnemy(missile);
        }
        doFlocking();
    }

    public void updateHardAvoidList() {
        hardAvoidList.clear();

        CollisionGridAPI grid = Global.getCombatEngine().getAiGridShips();
        Iterator<Object> iter = grid.getCheckIterator(missile.getLocation(), MAX_HARD_AVOID_RANGE * 2f,
                MAX_HARD_AVOID_RANGE * 2f);
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof ShipAPI))
                continue;

            ShipAPI ship = (ShipAPI) o;

            if (ship.isFighter())
                continue;
            hardAvoidList.add(ship);
        }

        grid = Global.getCombatEngine().getAiGridAsteroids();
        iter = grid.getCheckIterator(missile.getLocation(), MAX_HARD_AVOID_RANGE * 2f, MAX_HARD_AVOID_RANGE * 2f);
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof CombatEntityAPI))
                continue;

            CombatEntityAPI asteroid = (CombatEntityAPI) o;
            hardAvoidList.add(asteroid);
        }
    }

    public void doFlocking() {
        if (source == null)
            return;

        // ShipAPI source = missile.getSource();
        CombatEngineAPI engine = Global.getCombatEngine();

        float avoidRange = AVOID_RANGE;
        float cohesionRange = COHESION_RANGE;

        float sourceRejoin = 200f;

        float sourceRepel = 50f;
        float sourceCohesion = 300f;

        float sin = (float) Math.sin(engine.getElapsedInContactWithEnemy() * 1f);
        float mult = 1f + sin * 0.25f;
        avoidRange *= mult;

        Vector2f total = new Vector2f();

        if (target != null&&isUnBlockableToAttack()) {
            float dist = Misc.getDistance(missile.getLocation(), target.getLocation());
            Vector2f dir = Misc
                    .getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(missile.getLocation(), target.getLocation()));
            float f = dist / 200f;
            if (f > 1f)
                f = 1f;
            dir.scale(f * 3f);
            Vector2f.add(total, dir, total);

            avoidRange *= 3f;
        }

        boolean hardAvoiding = false;
        for (CombatEntityAPI other : hardAvoidList) {
            float dist = Misc.getDistance(missile.getLocation(), other.getLocation());
            float hardAvoidRange = other.getCollisionRadius() + avoidRange + 50f;
            if (dist < hardAvoidRange) {
                Vector2f dir = Misc
                        .getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(other.getLocation(), missile.getLocation()));
                float f = 1f - dist / (hardAvoidRange);
                dir.scale(f * 5f);
                Vector2f.add(total, dir, total);
                hardAvoiding = f > 0.5f;
            }
        }
        if(target!=null) {
            float dist = Misc.getDistance(missile.getLocation(), target.getLocation());
            if (dist > sourceRejoin) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(missile.getLocation(), target.getLocation()));
                float f = dist / (sourceRejoin + 400f) - 1f;
                dir.scale(f * 0.5f);

                Vector2f.add(total, dir, total);
            }

            if (dist < sourceRepel) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(target.getLocation(), missile.getLocation()));
                float f = 1f - dist / sourceRepel;
                dir.scale(f * 5f);
                Vector2f.add(total, dir, total);
            }

            if (dist < sourceCohesion && target.getVelocity().length() > 20f) {
                Vector2f dir = new Vector2f(target.getVelocity());
                Misc.normalise(dir);
                float f = 1f - dist / sourceCohesion;
                dir.scale(f * 1f);
                Vector2f.add(total, dir, total);
            }

            // if not strongly going anywhere, circle the source ship; only kicks in for
            // lone motes
            if (total.length() <= 0.05f) {
                float offset = r > 0.5f ? 90f : -90f;
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(missile.getLocation(), target.getLocation()) + offset);
                float f = 1f;
                dir.scale(f * 1f);
                Vector2f.add(total, dir, total);
            }
        }else if (source != null) {
            float dist = Misc.getDistance(missile.getLocation(), source.getLocation());
            if (dist > sourceRejoin) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(missile.getLocation(), source.getLocation()));
                float f = dist / (sourceRejoin + 400f) - 1f;
                dir.scale(f * 0.5f);

                Vector2f.add(total, dir, total);
            }

            if (dist < sourceRepel) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(source.getLocation(), missile.getLocation()));
                float f = 1f - dist / sourceRepel;
                dir.scale(f * 5f);
                Vector2f.add(total, dir, total);
            }

            if (dist < sourceCohesion && source.getVelocity().length() > 20f) {
                Vector2f dir = new Vector2f(source.getVelocity());
                Misc.normalise(dir);
                float f = 1f - dist / sourceCohesion;
                dir.scale(f * 1f);
                Vector2f.add(total, dir, total);
            }

            // if not strongly going anywhere, circle the source ship; only kicks in for
            // lone motes
            if (total.length() <= 0.05f) {
                float offset = r > 0.5f ? 90f : -90f;
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(missile.getLocation(), source.getLocation()) + offset);
                float f = 1f;
                dir.scale(f * 1f);
                Vector2f.add(total, dir, total);
            }
        }

        if (total.length() > 0) {
            float dir = Misc.getAngleInDegrees(total);
            engine.headInDirectionWithoutTurning(missile, dir, 10000);

            if (r > 0.5f) {
                missile.giveCommand(ShipCommand.TURN_LEFT);
            } else {
                missile.giveCommand(ShipCommand.TURN_RIGHT);
            }
            missile.getEngineController().forceShowAccelerating();
        }
    }

    private boolean isUnBlockableToAttack(){
        if(target == null||!(target instanceof ShipAPI)) return false;
        ShipAPI ship = (ShipAPI) target;
        boolean unbloackable = false;
        if(target.getShield()!=null){
            if(target.getShield().isOn()) {
                unbloackable =  !target.getShield().isWithinArc(missile.getLocation());
            }else{
                unbloackable = true;
            }
        }
        float W = 0;
        for(WeaponAPI weapon:ship.getAllWeapons()){
            if(weapon.isDecorative()||weapon.isDisabled()||weapon.getCooldownRemaining()>1f){
                continue;
            }

            if(Math.abs(weapon.distanceFromArc(missile.getLocation()))>1f) continue;

            float currW = weapon.getDamage().getDamage();
            if(weapon.isBeam()) currW*=0.5f;

            float diff = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), VectorUtils.getAngle(weapon.getLocation(),missile.getLocation())));
            currW = Math.max(0,weapon.getTurnRate()*0.8f-diff);

            diff*=currW;
            if(weapon.hasAIHint(WeaponAPI.AIHints.PD)) currW*=5f;
            W+=currW;
        }
        return W * Math.max(0, missile.getFlightTime()-5f) < 400f;
    }

}
