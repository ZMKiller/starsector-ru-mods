package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.util.List;

public class HSIGravityGuidenceTorpedoScript extends BaseEveryFrameCombatPlugin {

    private MissileAPI torpedo;

    private float cone = 90;

    private boolean no_ff = false;

    private float vt_dist = 0;

    public HSIGravityGuidenceTorpedoScript(MissileAPI torpedo){
        this.torpedo = torpedo;
        JSONObject json = torpedo.getSpec().getBehaviorJSON();
        if(json!=null) {
            //minDelayBeforeTriggering = (float) json.optDouble("minDelayBeforeTriggering", 1f);

            if (json.has("searchCone")) {
                try {
                    cone = (float) json.getDouble("searchCone");
                } catch (Exception ignored) {

                }
            }

            if (json.has("vtDist")) {
                try {
                    vt_dist = (float) json.getDouble("vtDist");
                } catch (Exception ignored) {

                }
            }

            if (json.has("no_ff")) {
                try {
                    no_ff = json.getBoolean("searchCone");
                } catch (Exception ignored) {

                }
            }
        }
        if(torpedo.getCollisionClass().equals(CollisionClass.MISSILE_NO_FF)){
            no_ff = true;
        }
    }

    private IntervalUtil scanTimer = new IntervalUtil(0.1f,0.1f);

    private CombatEntityAPI target = null;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if(Global.getCombatEngine().isPaused()) return;
        if(!torpedo.isArmed()) return;
        if(torpedo.isFizzling() || (torpedo.getHitpoints() <= 0 && !torpedo.didDamage())||torpedo.isExpired() || torpedo.didDamage() ||
                !Global.getCombatEngine().isEntityInPlay(torpedo)){
            Global.getCombatEngine().removePlugin(this);
            if( (torpedo.getHitpoints() <= 0 && !torpedo.didDamage())){
                torpedo.explode();

            }
            Global.getCombatEngine().removePlugin(this);
            return;
        }
        scanTimer.advance(amount);
        if(scanTimer.intervalElapsed()){
            List<ShipAPI> potential =  CombatUtils.getShipsWithinRange(torpedo.getLocation(),1500f);
            if(!potential.isEmpty()){
                ShipAPI highest = null;
                float weight = 0;
                for(ShipAPI ship:potential){
                    if(!ship.isAlive()||ship.isHulk()) continue;
                    if(ship == torpedo.getSource()) continue;
                    if(ship.isFighter()&&ship.getOwner()==torpedo.getOwner()) continue;
                    if(ship.isStationModule()&&ship.getParentStation()!=null&&ship.getParentStation().equals(torpedo.getSource())) continue;
                    if(no_ff&&ship.getOwner() == torpedo.getOwner()) continue;
                    if(Math.abs(MathUtils.getShortestRotation(torpedo.getFacing(),VectorUtils.getAngle(torpedo.getLocation(),ship.getLocation())))<=cone*0.5f){
                        float dist = MathUtils.getDistance(torpedo.getLocation(),ship.getLocation());
                        if(dist<=vt_dist){
                            highest = ship;
                            weight = Float.MAX_VALUE;
                            torpedo.explode();
                            break;
                        }else{
                            float w = ship.getMass()*ship.getMass()/dist;
                            if(w>weight){
                                highest = ship;
                                weight = w;
                            }
                        }
                    }
                }
                target = highest;
            }else{
                target = null;
            }
        }

        if(target!=null){
            float rate = torpedo.getMaxTurnRate() * amount;

            float dir = Misc.getAngleInDegrees(torpedo.getLocation(), target.getLocation());
            float diff = Misc.getAngleDiff(torpedo.getFacing(), dir);

            if (diff <= rate * 0.25f) {
                torpedo.setFacing(dir);
            } else {
                Misc.turnTowardsPointV2(torpedo, target.getLocation(), 0f);
            }
        }
    }
}
