package data.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HSISmartMissileAI implements MissileAIPlugin {
    protected static final String KEY = "HSISmartMissileAI";
    private ShipAPI target;
    private ShipAPI source;
    private State state;
    private MissileAPI missile;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private HSISmartMissileShareData data;
    private IntervalUtil scanTimer = new IntervalUtil(0.15f, 0.15f);
    private float targetPDRange = 0f;
    private float r = (float) Math.random();
    private IntervalUtil updateListTracker = new IntervalUtil(0.05f, 0.1f);
    private List<CombatEntityAPI> hardAvoidList = new ArrayList<CombatEntityAPI>();
    public static float MAX_HARD_AVOID_RANGE = 200;
    public static float AVOID_RANGE = 50;
    public static float COHESION_RANGE = 100;
    public TimeoutTracker<State> switchmodeTimer = new TimeoutTracker<State>();

    public class HSISmartMissileShareData {
        public Map<ShipAPI, List<MissileAPI>> missiles = new HashMap<>();
        public List<ShipAPI> criticalTarget = new ArrayList<>();
    }

    public enum State {
        MOVE, WAIT, ATTACK;
    }

    public HSISmartMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        state = State.MOVE;
        target = launchingShip.getShipTarget();
        source = launchingShip;
        searchTargetIfNecessary();
        data = getShareData();
        if (data.missiles.containsKey(target)) {
            if (!data.missiles.get(target).contains(missile))
                data.missiles.get(target).add(missile);
        } else {
            ArrayList<MissileAPI> list = new ArrayList<>();
            list.add(missile);
            data.missiles.put(target, list);
        }
        scanTimer.setElapsed((float) (0.15f * Math.random()));
        if (target != null) {
            for (WeaponAPI weapon : target.getAllWeapons()) {
                if (weapon.getSpec().getAIHints().contains(AIHints.PD)
                        || weapon.getSpec().getAIHints().contains(AIHints.PD_ALSO)
                        || weapon.getSpec().getAIHints().contains(AIHints.PD_ONLY)) {
                    targetPDRange = Math.max(targetPDRange, weapon.getRange());
                }
            }
        }
    }

    @Override
    public void advance(float amount) {
        if (missile == null || missile.isFizzling() || missile.isExpired() || !engine.isEntityInPlay(missile)) {
            data.missiles.get(target).remove(missile);
            return;
        }
        switchmodeTimer.advance(amount);
        scanTimer.advance(amount);
        updateListTracker.advance(amount);
        if (updateListTracker.intervalElapsed()) {
            updateHardAvoidList();
        }
        searchTargetIfNecessary();
        if (target == null)
            return;
        if (scanTimer.intervalElapsed()) {
            if (target == null)
                return;
            if (target.getFluxTracker().isOverloadedOrVenting()) {
                setCritical(target);
            }
            if (!data.criticalTarget.contains(target)) {
                List<ShipAPI> uselessTargets = new ArrayList<>();
                for (ShipAPI criticalTarget : data.criticalTarget) {
                    if (criticalTarget == null || !criticalTarget.isAlive() || !engine.isEntityInPlay(criticalTarget)
                            || !target.getFluxTracker().isOverloadedOrVenting())
                        uselessTargets.add(criticalTarget);
                    if (canReach(criticalTarget)) {
                        switchTarget(target, criticalTarget);
                    }
                }
                if (!uselessTargets.isEmpty())
                    data.criticalTarget.removeAll(uselessTargets);
            } else {
                if (!target.getFluxTracker().isOverloadedOrVenting()) {
                    data.criticalTarget.remove(target);
                }
            }
        }
        think();
        // float dist = Misc.getDistance(missile.getLocation(), target.getLocation()) -
        // target.getCollisionRadius();
        switch (state) {
            case WAIT:
                doFlocking();
                break;
            default:
                Vector2f targetLoc = engine.getAimPointWithLeadForAutofire(missile, 1.5f, target, 50);
                engine.headInDirectionWithoutTurning(missile,
                        Misc.getAngleInDegrees(missile.getLocation(), targetLoc),
                        10000);
                // AIUtils.turnTowardsPointV2(missile, targetLoc);
                if (r > 0.5f) {
                    missile.giveCommand(ShipCommand.TURN_LEFT);
                } else {
                    missile.giveCommand(ShipCommand.TURN_RIGHT);
                }
                missile.getEngineController().forceShowAccelerating();
                break;
        }
    }

    public void doAttack() {
        state = State.ATTACK;
    }

    public void think() {
        if (target == null)
            return;
        if (forceAttack()) {
            state = State.ATTACK;
            return;
        }
        if (switchmodeTimer.contains(State.WAIT)) {
            state = State.WAIT;
            return;
        }
        switch (state) {
            case ATTACK:
                if (!shouldAttack()&&(target != null && target.getShield() != null
                        && target.getShield().isWithinArc(missile.getLocation()))) {
                    state = State.WAIT;
                    switchmodeTimer.add(State.WAIT, 0.3f);
                }
                break;
            case MOVE:
                float dist = Misc.getDistance(missile.getLocation(), target.getLocation())
                        - target.getCollisionRadius();
                if (dist < targetPDRange + 200f) {
                    state = State.WAIT;
                }
                break;
            case WAIT:
                if (shouldAttack()) {
                    doAttack();
                    if (shouldCallFellow(target)) {
                        callFellow(target);
                    }
                }
                break;
            default:
                break;
        }
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
        if (target == null)
            return;

        // ShipAPI source = missile.getSource();
        CombatEngineAPI engine = Global.getCombatEngine();

        float avoidRange = AVOID_RANGE;
        float cohesionRange = COHESION_RANGE;

        float sourceRejoin = 200f;

        float sourceRepel = 50f;
        float sourceCohesion = 600f;

        float sin = (float) Math.sin(engine.getElapsedInContactWithEnemy() * 1f);
        float mult = 1f + sin * 0.25f;
        avoidRange *= mult;

        Vector2f total = new Vector2f();

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

        if (target != null) {
            float dist = Misc.getDistance(missile.getLocation(), target.getLocation());
            if (dist >targetPDRange) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(missile.getLocation(), target.getLocation()));
                float f = dist / (sourceRejoin + targetPDRange) - 1f;
                dir.scale(f * 0.5f);

                Vector2f.add(total, dir, total);
            }

            if (dist < targetPDRange-sourceRepel) {
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(
                        Misc.getAngleInDegrees(target.getLocation(), missile.getLocation()));
                float f = 1f - dist / (sourceRepel + targetPDRange);
                dir.scale(f * 5f);
                Vector2f.add(total, dir, total);
            }

            if (dist < targetPDRange && target.getVelocity().length() > 20f) {
                Vector2f dir = new Vector2f(target.getVelocity());
                Misc.normalise(dir);
                float f = 1f - dist / targetPDRange;
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

    public void searchTargetIfNecessary() {
        if (target == null)
            switchTarget(target, source.getShipTarget());
        if (target == null || target.isHulk() || !target.isAlive() || !engine.isEntityInPlay(target)) {
            switchTarget(target, AIUtils.getNearestEnemy(missile));
        }
    }

    public void switchTarget(ShipAPI prevTarget, ShipAPI newTarget) {
        if (data != null) {
            data.missiles.get(prevTarget).remove(missile);
            if (data.missiles.containsKey(newTarget)) {
                data.missiles.get(newTarget).add(missile);
            } else {
                ArrayList<MissileAPI> list = new ArrayList<>();
                list.add(missile);
                data.missiles.put(newTarget, list);
            }
        }
        target = newTarget;
        if (target != null) {
            for (WeaponAPI weapon : target.getAllWeapons()) {
                if (weapon.getSpec().getAIHints().contains(AIHints.PD)
                        || weapon.getSpec().getAIHints().contains(AIHints.PD_ALSO)
                        || weapon.getSpec().getAIHints().contains(AIHints.PD_ONLY)) {
                    targetPDRange = Math.max(targetPDRange, weapon.getRange());
                }
            }
        }
        state = State.MOVE;
    }

    private HSISmartMissileShareData getShareData() {
        HSISmartMissileShareData data = new HSISmartMissileShareData();
        if (engine.getCustomData().containsKey(KEY)) {
            data = (HSISmartMissileShareData) engine.getCustomData().get(KEY);
        } else {
            engine.getCustomData().put(KEY, data);
        }
        return data;
    }

    public void setCritical(ShipAPI criticalTarget) {
        if (!data.criticalTarget.contains(criticalTarget))
            data.criticalTarget.add(criticalTarget);
    }

    public boolean canReach(ShipAPI target) {
        float leftTime = getLeftTime();
        float leftRange = leftTime * 0.8f * missile.getMaxSpeed();
        return Misc.getDistance(missile.getLocation(), target.getLocation()) - target.getCollisionRadius() <= leftRange;
    }

    public float getLeftTime() {
        return missile.getMaxFlightTime() - missile.getFlightTime();
    }

    public State getState() {
        return state;
    }

    public boolean shouldCallFellow(ShipAPI target) {
        List<MissileAPI> fellow = data.missiles.get(target);
        if (fellow == null)
            return false;
        int wait = 0;
        for (MissileAPI m : fellow) {
            if (m.getAI() != null && m.getAI() instanceof HSISmartMissileAI) {
                HSISmartMissileAI ai = (HSISmartMissileAI) m.getAI();
                if (ai.getState() == State.WAIT && ai.shouldAttack())
                    wait++;
            }
        }
        float frac = ((float) wait) / fellow.size();
        return frac > 0.5f || target.getShield() == null || target.getShield().isOff()
                || target.getFluxTracker().isOverloadedOrVenting();
    }

    public void callFellow(ShipAPI target) {
        List<MissileAPI> fellow = data.missiles.get(target);
        if (fellow == null)
            return;
        for (MissileAPI m : fellow) {
            if (m.getAI() != null && m.getAI() instanceof HSISmartMissileAI) {
                HSISmartMissileAI ai = (HSISmartMissileAI) m.getAI();
                if (ai.getState() == State.WAIT)
                    doAttack();
            }
        }
    }

    public boolean forceAttack() {
        return (getLeftTime() < 1.5f * (Misc.getDistance(missile.getLocation(), target.getLocation())
                / missile.getMaxSpeed()) || target.getFluxTracker().isOverloadedOrVenting());
    }

    public boolean shouldAttack() {
        if (target == null)
            return false;
        if (missile.getHitpoints() / missile.getMaxHitpoints() < 0.5f)
            return true;
        if (getLeftTime() < 1.1f * Misc.getDistance(target.getLocation(), missile.getLocation())
                / missile.getMaxSpeed())
            return true;
        if (forceAttack())
            return true;
        if (target.getShield() != null && target.getShield().isWithinArc(missile.getLocation()))
            return false;
        if (target.getShield() == null || target.getShield().isOff()
        // || !target.getShield().isWithinArc(missile.getLocation())
        )
            return true;
        List<MissileAPI> fellow = data.missiles.get(target);
        int fellowWaiting = 0;
        for (MissileAPI m : fellow) {
            if (m.getAI() != null && m.getAI() instanceof HSISmartMissileAI) {
                HSISmartMissileAI ai = (HSISmartMissileAI) m.getAI();
                if (ai.getState() == State.WAIT && ai.shouldAttack())
                    fellowWaiting++;
            }
        }
        if (((float) (fellowWaiting) / fellow.size()) >= 0.5)
            return true;
        return false;
    }

}
