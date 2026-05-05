package data.ai.HSIMissileAI;

import com.fs.starfarer.api.combat.*;
import data.ai.HSIThreatSharedData;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.missilePriority;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.util.Misc;

public class HSIBaseMissileAI implements MissileAIPlugin, GuidedMissileAI {
    private CombatEntityAPI target;
    private MissileAPI missile;
    private final float DAMPING = 0.05f;

    public HSIBaseMissileAI(CombatEntityAPI target, MissileAPI missile) {
        this.target = target;
        this.missile = missile;
        HSIThreatSharedData.addProjAuto(missile,target);
    }

    @Override
    public void advance(float amount) {
        if (!isTargetLegal()) {
            repickTarget(missile.getWeapon());
        }
        if (!isTargetLegal()) {
            return;
        }
        HSIThreatSharedData.addProjAuto(missile,target);
        float expectedAngle = computeExpectedAngle(missile, target, amount);
        float rotation = MathUtils.getShortestRotation(VectorUtils.getFacing(missile.getVelocity()), expectedAngle);
        if(Math.abs(rotation)>missile.getMaxTurnRate()*amount){
            rotation = Math.signum(rotation)*missile.getMaxTurnRate()*amount;
        }
        expectedAngle = VectorUtils.getFacing(missile.getVelocity())+rotation;
        manuver(expectedAngle);
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        if (missile != null && !missile.getWeapon().hasAIHint(AIHints.IGNORES_FLARES))
            this.target = target;
    }

    private boolean isTargetLegal() {
        if (target instanceof ShipAPI) {
            return ((ShipAPI) target).isAlive();
        } else if (target instanceof MissileAPI) {
            return !((MissileAPI) target).isExpired() && (!((MissileAPI) target).isFading())
                    && (!((MissileAPI) target).isFizzling());
        }
        return target != null && !target.isExpired() && Global.getCombatEngine().isEntityInPlay(target);
    }

    private void repickTarget(WeaponAPI weapon) {
        if(weapon == null||!weapon.getSpec().getAIHints().contains(AIHints.ANTI_FTR)){
        target = MagicTargeting.randomMissile(missile, missilePriority.DAMAGE_PRIORITY, missile.getLocation(), missile.getFacing(), 180, (int)((missile.getMaxFlightTime()-missile.getFlightTime())*missile.getMaxSpeed()*0.75f));
        if(target == null){
            target = MagicTargeting.pickMissileTarget(missile, targetSeeking.FULL_RANDOM, (int)((missile.getMaxFlightTime()-missile.getFlightTime())*missile.getMaxSpeed()*0.75f), 360, 100, 40, 20, 10, 5);
        }}else{
            target = MagicTargeting.pickMissileTarget(missile, targetSeeking.FULL_RANDOM, (int)((missile.getMaxFlightTime()-missile.getFlightTime())*missile.getMaxSpeed()*0.75f), 360, 100, 40, 20, 10, 5);
            if(target == null){
                target = MagicTargeting.randomMissile(missile, missilePriority.DAMAGE_PRIORITY, missile.getLocation(), missile.getFacing(), 180, (int)((missile.getMaxFlightTime()-missile.getFlightTime())*missile.getMaxSpeed()*0.75f));
            }
        }
    }

    public float computeExpectedAngle(MissileAPI source, CombatEntityAPI target, float dt) {
        Vector2f sourceLoc = new Vector2f(source.getLocation());
        Vector2f targetLoc = new Vector2f(target.getLocation());
        Vector2f sourceLocNext = Vector2f.add(sourceLoc,(Vector2f)(new Vector2f(source.getVelocity()).scale(dt)),null);
        Vector2f targetLocNext = Vector2f.add(targetLoc,(Vector2f)(new Vector2f(target.getVelocity()).scale(dt)),null);
        float vs = source.getVelocity().length();
        //float vt = target.getVelocity().length();
        double deltaS = VectorUtils.getFacing(source.getVelocity());
        double deltaT = VectorUtils.getFacing(target.getVelocity());
        double q = VectorUtils.getFacing(Vector2f.sub(targetLoc, sourceLoc, null));
        double qnext = VectorUtils.getFacing(Vector2f.sub(targetLocNext, sourceLocNext, null));
        float r = MathUtils.getDistance(sourceLoc, targetLoc);
        //double yitaS = q - deltaS;
        //double yitaT = q - deltaT;
        double dq = (qnext-q)/dt;
        double As = getProp(r) * vs * dq;
        double dds = As / vs;
        double nextds = deltaS + dds * dt;
        return (float) (nextds);
    }

    public float getProp(float r) {
        return 3;
    }

    public void manuver(float expectedAngle){
        missile.getVelocity().set((new Vector2f((Vector2f)Misc.getUnitVectorAtDegreeAngle(expectedAngle).scale(missile.getMaxSpeed()))));
        missile.setFacing(expectedAngle);
        missile.getEngineController().forceShowAccelerating();
        missile.getEngineController().extendFlame(this,2f,0f,0f);
        for(ShipEngineControllerAPI.ShipEngineAPI e:missile.getEngineController().getShipEngines()){
            missile.getEngineController().setFlameLevel(e.getEngineSlot(),1f);
        }
    }
}
