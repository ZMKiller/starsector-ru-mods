package data.weapons.scripts.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.abilities.GoDarkAbility;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class HSIScatterBeamEveryFrameEffect implements EveryFrameWeaponEffectPlugin {
    private static final float TURN_RATE = 20f;

    private List<ShipAPI> target = new ArrayList<>();
    private List<Vector2f> targetLoc = new ArrayList<>();
    private IntervalUtil timer = new IntervalUtil(0.9f,1.5f);

    private WeaponAPI weapon = null;
    private ShipAPI source = null;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(engine.isPaused()) return;
        this.weapon = weapon;
        if(source == null){
            source = weapon.getShip();
        }
        if(target.isEmpty()){
            target.add(null);
            target.add(null);
            target.add(null);
            target.add(null);
        }
        if(targetLoc.isEmpty()){
            targetLoc.add(null);
            targetLoc.add(null);
            targetLoc.add(null);
            targetLoc.add(null);
        }

        float max_change = TURN_RATE*amount;
        weapon.ensureClonedSpec();

        if(weapon.isFiring()){
            timer.advance(amount);
        }else{
            timer.advance(amount*6f);
        }

        if(timer.intervalElapsed()){
            pickTarget(weapon);
            for(int i = 0;i<3;i++){
                pickTargetLoc(i);
            }
        }
        Global.getLogger(this.getClass()).info("Targets:"+target.toString());
        if (weapon.getSlot().isTurret()) {
            for (int i = 0; i < 4; i++) {
                if(target.get(i)!=null) {
                    if (!isTargetLegal(i,weapon)){
                        targetLoc.set(i,null);
                        pickTargetForSpecific(weapon, i);
                        pickTargetLoc(i);
                        continue;
                    }
                    Vector2f t = targetLoc.get(i);
                    if(t == null) t = new Vector2f(target.get(i).getLocation());
                    float linkFacing = VectorUtils.getAngle(weapon.getFirePoint(i), t);
                    float currentFacing = weapon.getCurrAngle() + weapon.getSpec().getTurretAngleOffsets().get(i);
                    currentFacing = Misc.normalizeAngle(currentFacing);
                    float dist = MathUtils.getShortestRotation(currentFacing, linkFacing);
                    float abs = Math.abs(dist);
                    if (abs > max_change) {
                        if (dist < 0) {
                            weapon.getSpec().getTurretAngleOffsets().set(i, weapon.getSpec().getTurretAngleOffsets().get(i) - max_change);
                        } else {
                            weapon.getSpec().getTurretAngleOffsets().set(i, weapon.getSpec().getTurretAngleOffsets().get(i) + max_change);
                        }
                    } else {
                        weapon.getSpec().getTurretAngleOffsets().set(i, weapon.getSpec().getTurretAngleOffsets().get(i) + dist);
                    }
                }else{
                    pickTargetForSpecific(weapon, i);
                    pickTargetLoc(i);
                }
            }

        } else if (weapon.getSlot().isHardpoint()) {
            for (int i = 0; i < 4; i++) {
                if(target.get(i)!=null) {
                    if (!isTargetLegal(i,weapon)) {
                        targetLoc.set(i,null);
                        pickTargetForSpecific(weapon, i);
                        pickTargetLoc(i);
                        continue;
                    }
                    Vector2f t = targetLoc.get(i);
                    if(t == null) t = new Vector2f(target.get(i).getLocation());

                    float linkFacing = VectorUtils.getAngle(weapon.getFirePoint(i), t);
                    float currentFacing = weapon.getCurrAngle() + weapon.getSpec().getHardpointAngleOffsets().get(i);
                    currentFacing = Misc.normalizeAngle(currentFacing);
                    float dist = MathUtils.getShortestRotation(currentFacing, linkFacing);
                    float abs = Math.abs(dist);
                    if (abs > max_change) {
                        if (dist < 0) {
                            weapon.getSpec().getHardpointAngleOffsets().set(i, weapon.getSpec().getHardpointAngleOffsets().get(i) - max_change);
                        } else {
                            weapon.getSpec().getHardpointAngleOffsets().set(i, weapon.getSpec().getHardpointAngleOffsets().get(i) + max_change);
                        }
                    } else {
                        weapon.getSpec().getHardpointAngleOffsets().set(i, weapon.getSpec().getHardpointAngleOffsets().get(i) + dist);
                    }
                }else{
                    pickTargetForSpecific(weapon, i);
                    pickTargetLoc(i);
                }
            }
        }
    }


    public boolean hasLegalTarget(){
       boolean legal = false;
       for(ShipAPI m:target){
           if (isPossibleTargetLegal(m)) {
               legal = true;
               break;
           }
       }
       return legal;
    }

    private boolean isTargetLegal(int i,WeaponAPI weapon){
        return weapon!=null&&target.size()>i&&target.get(i)!=null&&!target.get(i).isHulk()&&target.get(i).isAlive()
                &&weapon.getShip().getOwner()!=target.get(i).getOwner()
                &&Misc.getDistance(weapon.getLocation(),target.get(i).getLocation())-target.get(i).getCollisionRadius()<=weapon.getRange()
                &&Math.abs(weapon.distanceFromArc(target.get(i).getLocation()))<=15;
    }

    private boolean isPossibleTargetLegal(ShipAPI ship){
        return weapon!=null&&ship!=null&&!ship.isHulk()&&ship.isAlive()
                &&weapon.getShip().getOwner()!=ship.getOwner()
                &&Misc.getDistance(weapon.getLocation(),ship.getLocation())-ship.getCollisionRadius()<=weapon.getRange()
                &&Math.abs(weapon.distanceFromArc(ship.getLocation()))<=15&&ship.isTargetable()&&(ship.getCollisionClass() != CollisionClass.NONE);
    }

    protected void pickTarget(WeaponAPI weapon){
        if(source!=null
                &&source.getShipTarget()!=null&&source.getShipTarget().isAlive()
                ){
            if(isPossibleTargetLegal(source.getShipTarget())) {
                target.set(0, source.getShipTarget());
                target.set(1, source.getShipTarget());
                target.set(2, source.getShipTarget());
                target.set(3, source.getShipTarget());
            }else{
                List<ShipAPI> toPick = new ArrayList<>();
                for(ShipAPI ship:CombatUtils.getShipsWithinRange(weapon.getLocation(),weapon.getRange())){
                    if(isPossibleTargetLegal(ship)) toPick.add(ship);
                }
                WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
                picker.addAll(toPick);
                if(picker.isEmpty()){
                    for (int i = 0; i < 4; i++) {
                        target.set(i, null);
                    }
                }else {
                    for (int i = 0; i < 4; i++) {
                        target.set(i, picker.pick());
                    }
                }
            }
        }else{
            List<ShipAPI> toPick = new ArrayList<>();
            for(ShipAPI ship:CombatUtils.getShipsWithinRange(weapon.getLocation(),weapon.getRange())){
                if(isPossibleTargetLegal(ship)) toPick.add(ship);
            }
            WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
            picker.addAll(toPick);
            if(picker.isEmpty()){
                for (int i = 0; i < 4; i++) {
                    target.set(i, null);
                }
            }else {
                for (int i = 0; i < 4; i++) {
                    target.set(i, picker.pick());
                }
            }
        }
    }

    protected void pickTargetForSpecific(WeaponAPI weapon,int i){
        if(source!=null
                &&source.getShipTarget()!=null&&source.getShipTarget().isAlive()){
            if(Math.abs(weapon.distanceFromArc(source.getShipTarget().getLocation()))<=5
                    &&MathUtils.getDistance(source.getLocation(),weapon.getLocation())<weapon.getRange()) {
                target.set(i, source.getShipTarget());
            }else{
                List<ShipAPI> toPick = new ArrayList<>();
                for(ShipAPI ship:CombatUtils.getShipsWithinRange(weapon.getLocation(),weapon.getRange()*0.99f)){
                    if(isPossibleTargetLegal(ship)) toPick.add(ship);
                    toPick.add(ship);
                }
                WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
                picker.addAll(toPick);
                if(picker.isEmpty()){
                    target.set(i, null);
                }else {
                    target.set(i, picker.pick());
                }
            }
        }else{
            List<ShipAPI> toPick = new ArrayList<>();
            for(ShipAPI ship:CombatUtils.getShipsWithinRange(weapon.getLocation(),weapon.getRange()*0.99f)){
                if(isPossibleTargetLegal(ship)) toPick.add(ship);
                toPick.add(ship);
            }

            WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();
            picker.addAll(toPick);
            if(picker.isEmpty()){
                    target.set(i, null);
            }else {
                    target.set(i, picker.pick());
            }
        }
    }

    protected void pickTargetLoc(int i){
        if(target.get(i)!=null){
            ShipAPI targetShip = target.get(i);
            WeightedRandomPicker<Vector2f> toPick = new WeightedRandomPicker<>();
            toPick.add(targetShip.getLocation());
            if(!targetShip.isFighter()){
                for(WeaponAPI weapon:targetShip.getAllWeapons()){
                    if(weapon.isDecorative()) continue;
                    if(weapon.isDisabled()) continue;
                    toPick.add(weapon.getLocation());
                }
            }
            targetLoc.set(i, toPick.pick());
        }
    }

    public Vector2f getTargetLoc(WeaponAPI weapon){
        Vector2f R = weapon.getLocation();
        if(!targetLoc.isEmpty()) {
            Vector2f v = new Vector2f(0,0);
            int n = 0;
            for (int i = 0; i < 4; i++) {
                if (targetLoc.get(i) != null) {
                    v.set(v.getX()+targetLoc.get(i).getX(),v.getY()+targetLoc.get(i).getY());
                    n++;
                }
            }
            R.set(v.getX()/Math.max(1,n),v.getY()/Math.max(1,n));
        }
        return R;
    }

    public ShipAPI getTargetShip(){
        ShipAPI s = null;
        if(!target.isEmpty()){
            for(int i = 0;i<4;i++){
                if(target.get(i)!=null){
                    s = target.get(i);
                    return s;
                }
            }
        }
        return s;
    }


}
