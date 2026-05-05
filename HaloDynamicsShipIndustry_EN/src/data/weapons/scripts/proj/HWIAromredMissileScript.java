package data.weapons.scripts.proj;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HWIAromredMissileScript extends BaseEveryFrameCombatPlugin {
    private DamagingExplosionSpec explosion;
    private ShipAPI am = null;
    private String id;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private ShipAPI target = null;
    private MANUVER_TYPE action = MANUVER_TYPE.DIRECT_ATTACK;
    private float targetRange = 0f;
    private IntervalUtil manuverTimer = new IntervalUtil(0.05f, 0.05f);

    public static final String HEAVY_AM_ID = "HWI_AM_Heavy";
    public static final String MEDIUM_AM_ID = "HWI_AM_MEDIUM";

    public static enum MANUVER_TYPE {
        DIRECT_ATTACK, PULL_BACK, HOLDING, AVOID_SHIELD;
    }

    public HWIAromredMissileScript(MissileAPI originalMissile) {
        explosion = originalMissile.getSpec().getExplosionSpec();
        float maxSpeed = originalMissile.getMaxSpeed();
        float maxTurnRate = originalMissile.getMaxTurnRate();
        float Acceleration = originalMissile.getAcceleration();
        float turnAcceleration = originalMissile.getTurnAcceleration();
        if (originalMissile.getWeapon().getSpec().getTags().contains("HWI_heavy_am")) {
            id = HEAVY_AM_ID;
        } else {
            id = MEDIUM_AM_ID;
        }
        ShipHullSpecAPI spec = Global.getSettings().getHullSpec(id);
        ShipVariantAPI v = Global.getSettings().createEmptyVariant(id, spec);
        am = engine.createFXDrone(v);
        am.setDrone(true);
        am.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        am.setOwner(originalMissile.getOwner());
        am.getMutableStats().getEmpDamageTakenMult().modifyMult(id, 0.5f);
        am.getMutableStats().getAcceleration().modifyFlat(id, Acceleration-am.getAcceleration());
        am.getMutableStats().getMaxSpeed().modifyFlat(id, maxSpeed-am.getMaxSpeed());
        am.getMutableStats().getMaxTurnRate().modifyFlat(id, maxTurnRate-am.getMaxTurnRate());
        am.getMutableStats().getTurnAcceleration().modifyFlat(id, turnAcceleration-am.getTurnAcceleration());
        engine.addEntity(am);
        if (originalMissile.getAI() != null && originalMissile.getAI() instanceof GuidedMissileAI) {
            GuidedMissileAI ai = (GuidedMissileAI) originalMissile.getAI();
            if (ai.getTarget() instanceof ShipAPI)
                target = (ShipAPI) ai.getTarget();
        } else if (originalMissile.getSource() != null && originalMissile.getSource().getShipTarget() != null) {
            target = originalMissile.getSource().getShipTarget();
        }
        updateTargetWeaponRange();
        engine.removeEntity(originalMissile);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if(engine.isPaused()) return;
        if(!engine.isEntityInPlay(am)||!am.isAlive()){
            engine.removePlugin(this);
            return;
        }
        manuverTimer.advance(amount);
        if(!manuverTimer.intervalElapsed()) return;
        if (target != null && (!engine.isEntityInPlay(target) || !target.isAlive())) {
            target = null;
        }
        if (target == null) {
            target = MagicTargeting.pickTarget(am, targetSeeking.FULL_RANDOM, 2000, 150, 0, 0, 1, 8, 10, true);
            updateTargetWeaponRange();
        }
        if (target == null) {
            action = MANUVER_TYPE.HOLDING;
        } else {
            am.setShipTarget(target);
            float dis = Misc.getDistance(am.getLocation(), target.getLocation()) - target.getCollisionRadius();
            if (dis > targetRange) {
                action = MANUVER_TYPE.DIRECT_ATTACK;
            } else {
                if (target.getShield() != null) {
                    ShieldAPI shield = target.getShield();
                    if (shield.isOn()&&(shield.getActiveArc()<=300f||shield.isWithinArc(am.getLocation()))) {
                        float shieldFacing = shield.getFacing();
                        float shieldArc = shield.getActiveArc();
                        float minEdge = shieldFacing-shieldArc/2;
                        float maxEdge = shieldFacing+shieldArc/2;
                        if(minEdge<0) minEdge+=360f;
                        if(maxEdge>360) maxEdge-=360f;
                        float amAngle = Misc.getAngleInDegrees(target.getLocation(), am.getLocation());
                        if(amAngle<0) amAngle+=360f;
                        if(amAngle<minEdge||amAngle>maxEdge){
                            action = MANUVER_TYPE.DIRECT_ATTACK;
                        }else{
                            action = MANUVER_TYPE.AVOID_SHIELD;
                        }
                    }else if (shield.isOn() && shield.getActiveArc() > 300f) {
                        if(dis<targetRange){
                            action = MANUVER_TYPE.PULL_BACK;
                        }else{
                            action = MANUVER_TYPE.HOLDING;
                        } 
                    }
                } else {
                    action = MANUVER_TYPE.DIRECT_ATTACK;
                }
            }
        }
        switch(action){
            case DIRECT_ATTACK:
                am.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 0.05f, target.getLocation());
                break;
            case HOLDING:
                am.getAIFlags().setFlag(AIFlags.CAMP_LOCATION,0.05f,am.getLocation());
                break;
            case PULL_BACK:
                am.getAIFlags().setFlag(AIFlags.BACK_OFF_MIN_RANGE, 0.05f,targetRange);
                break;
            case AVOID_SHIELD:
                ShieldAPI shield = target.getShield();
                float facing = shield.getFacing();
                facing+=180f;
                if(facing>360f) facing-=360f;
                if(facing<0f) facing+=360f;
                Vector2f modifier = Misc.getUnitVectorAtDegreeAngle(facing);
                modifier.scale(target.getCollisionRadius());
                am.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET,0.05f, Vector2f.add(target.getLocation(), modifier, null));
                break;
        }
        if(action == MANUVER_TYPE.DIRECT_ATTACK&&Misc.getDistance(am.getLocation(), target.getLocation())<explosion.getCoreRadius()){
            engine.spawnDamagingExplosion(explosion, target, am.getLocation());
            engine.removeEntity(am);
            engine.removePlugin(this);
        }
    }

    private void updateTargetWeaponRange() {
        targetRange = 0;
        if (target != null) {
            for (WeaponAPI weapon : target.getAllWeapons()) {
                if (weapon.getRange() > targetRange) {
                    targetRange = weapon.getRange();
                }
            }
        }
    }
}
