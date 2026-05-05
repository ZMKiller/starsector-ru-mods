package data.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HWIIntercepterController implements EveryFrameWeaponEffectPlugin {
    public enum AIStage {
        TAKE_OFF, LANDING, MOVE, ATTACK, REACHED_ATTACK_POINT,  MANUVER_BACK_TO_FACING
    }

    private List<HSIInterceptorDroneScript> drones = new ArrayList<>();

    protected static final int MAX_AMMO = 8;

    private int ammo = MAX_AMMO;

    private static final float DRONE_COST = 1000f;

    private IntervalUtil reloadTimer = new IntervalUtil(8f, 8f);

    private IntervalUtil fireTimer = new IntervalUtil(0.5f, 0.5f);

    //private TimeoutTracker<CombatEntityAPI> s = new TimeoutTracker<>();

    private float emptyElapsed = 0;

    private CombatEntityAPI target = null;



    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        List<HSIInterceptorDroneScript> toRemove = new ArrayList<>();
        for (HSIInterceptorDroneScript drone : drones) {
            if (!drone.getDrone().isAlive() || !engine.isEntityInPlay(drone.getDrone()) || drone.getDrone().isHulk()) {
                toRemove.add(drone);
                if(!drone.landing){
                    ammo--;
                }
                drone.notifyEnd();
            }
        }
        drones.removeAll(toRemove);
        toRemove.clear();
        if (ammo <= 0) weapon.setForceNoFireOneFrame(true);
        if (ammo < MAX_AMMO) {
            if(weapon.getShip().getFluxTracker().increaseFlux(DRONE_COST*amount/8f,false)) {
                reloadTimer.advance(amount);
            }
            if (reloadTimer.intervalElapsed()) ammo++;
        }
        boolean hasTarget = isTargetLegal(weapon);
        if (hasTarget) {
            if(target instanceof ShipAPI) {
                if (((ShipAPI) target).isAlive()) {
                    if (drones.size() < ammo) {
                        fireTimer.advance(amount);
                        if (fireTimer.intervalElapsed()) {
                            spawnAndBindScript(engine, weapon);
                        }
                    }
                    for (HSIInterceptorDroneScript drone : drones) {
                        drone.setTarget(target);
                        drone.advance(amount);
                    }
                }
            }
        } else {
            target = null;
            findTarget(weapon);
            if(!isTargetLegal(weapon)){
                for (HSIInterceptorDroneScript drone : drones) {
                    drone.setStage(AIStage.LANDING);
                    drone.setTarget(null);
                    drone.advance(amount);
                    if (MathUtils.getDistanceSquared(drone.getDrone().getLocation(), weapon.getFirePoint(0)) < 2500f) {
                        drone.beginLand();
                    }
                }
            }
        }
    }

    private boolean isTargetLegal(WeaponAPI weapon){
        if(target == null) return false;
        if(target.getOwner()==100||target.getOwner() == weapon.getShip().getOwner()) return false;
        if(!Global.getCombatEngine().isEntityInPlay(target)) return false;
        return Misc.getDistance(weapon.getLocation(), target.getLocation()) < target.getCollisionRadius() + weapon.getRange() * 1.5f;
    }

    private boolean isTargetPickLegal(WeaponAPI weapon,ShipAPI e){
        if(e == null) return false;
        if(e.getOwner()==100||e.getOwner() == weapon.getShip().getOwner()) return false;
        if(!Global.getCombatEngine().isEntityInPlay(e)) return false;
        return Misc.getDistance(weapon.getLocation(), e.getLocation()) < e.getCollisionRadius() + weapon.getRange();
    }

    private void findTarget(WeaponAPI weapon){
        ShipAPI e = AIUtils.getNearestEnemy(weapon.getShip());
        if(isTargetPickLegal(weapon,e)){
            target = e;
        }
    }

    public void spawnAndBindScript(CombatEngineAPI engine,WeaponAPI weapon) {
        ShipHullSpecAPI spec = Global.getSettings().getHullSpec("HSI_Wingman");
        ShipVariantAPI v = Global.getSettings().createEmptyVariant("HSI_Wingman_Drone", spec);
        v.addWeapon("WS 000", "HWI_IonLance");
        WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
        g.addSlot("WS 000");
        g.setAutofireOnByDefault(true);
        v.addWeaponGroup(g);
        ShipAPI w = engine.createFXDrone(v);
        w.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        w.setOwner(weapon.getShip().getOwner());
        if(weapon.getShip().isAlly()){
            w.setAlly(true);
        }
        w.setDrone(true);
        w.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP, 100000f,weapon.getShip());
        MutableShipStatsAPI self = w.getMutableStats();
        MutableShipStatsAPI source = weapon.getShip().getMutableStats();
        self.getEnergyWeaponDamageMult()
                .applyMods(source.getMissileWeaponDamageMult());
        self.getDamageToFighters().applyMods(source.getDamageToFighters());
        self.getDamageToFrigates().applyMods(source.getDamageToFrigates());
        self.getDamageToDestroyers().applyMods(source.getDamageToDestroyers());
        self.getDamageToCruisers().applyMods(source.getDamageToCruisers());
        self.getDamageToCapital().applyMods(source.getDamageToCapital());
        self.getEnergyWeaponFluxCostMod().modifyMult("ANYWAYITSWINGMAN",0f);
        self.getEnergyRoFMult().modifyMult("ANYWAYITSWINGMAN",0.5f);
        self.getEnergyWeaponRangeBonus().modifyMult("ANYWAYITSWINGMAN",0.5f);
        if(w.getShield()!=null){
            w.getShield().setRadius(w.getShieldRadiusEvenIfNoShield()*0.66f);
        }
        self.getHardFluxDissipationFraction().modifyFlat("ANYWAYITSWINGMAN",1f);
        w.setDoNotFlareEnginesWhenStrafingOrDecelerating(true);
        self.getFluxCapacity().modifyMult("ANYWAYITSWINGMAN",0.25f);
        w.setCollisionClass(CollisionClass.FIGHTER);
        w.setFacing(weapon.getCurrAngle());
        Global.getCombatEngine().addEntity(w);
        w.getLocation().set(weapon.getFirePoint(0));
        w.setShipAI(null);
        drones.add(new HSIInterceptorDroneScript(w,weapon));
    }


    public static class HSIInterceptorDroneScript {
        private ShipAPI drone;
        private AIStage stage;

        private WeaponAPI source;

        private CombatEntityAPI target = null;

        private Vector2f expectedLocation = new Vector2f();

        private Random random = new Random();

        private float turnRate,maxSpeed;

        private boolean ended = false;

        private boolean landing = false;

        public HSIInterceptorDroneScript(ShipAPI drone, WeaponAPI weapon) {
            this.drone = drone;
            stage = AIStage.MOVE;
            source = weapon;
            turnRate = drone.getMaxTurnRate();
            maxSpeed = drone.getMaxSpeed();
        }

        public void advance(float amount) {
            if(ended) return;
            if (target == null) setStage(AIStage.LANDING);
            if(target!=null&&stage.equals(AIStage.LANDING)) setStage(AIStage.MOVE);
            if (drone.getShield() != null && drone.getShield().isOff() && !drone.getFluxTracker().isOverloadedOrVenting()) {
                drone.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, drone.getMouseTarget(), 0);
            }
            if(landing) setStage(AIStage.LANDING);
            float toFace = drone.getFacing();
            Vector2f speed = new Vector2f();
            switch (stage) {
                case TAKE_OFF:
                    break;
                case LANDING:
                    expectedLocation = source.getFirePoint(0);
                    break;
                case MOVE:
                    expectedLocation = target.getLocation();
                    if (MathUtils.getDistanceSquared(expectedLocation, drone.getLocation()) < 3f * target.getCollisionRadius() * target.getCollisionRadius() + 40000) {
                        setStage(AIStage.REACHED_ATTACK_POINT);
                    }
                    break;
                case ATTACK:
                    expectedLocation = target.getLocation();
                    if(MathUtils.getDistanceSquared(drone.getLocation(),expectedLocation)<2500f&&
                            (drone.getAllWeapons().isEmpty()||(!drone.getAllWeapons().isEmpty()&&drone.getAllWeapons().get(0).getCooldownRemaining()>0))){
                        setStage(AIStage.MANUVER_BACK_TO_FACING);
                    }
                    break;
                case REACHED_ATTACK_POINT:
                    float facing = VectorUtils.getAngle(target.getLocation(),drone.getLocation());
                    expectedLocation = MathUtils.getPoint(target.getLocation(),target.getCollisionRadius()+100f,MathUtils.clampAngle(facing+Math.signum(1f-random.nextFloat())*random.nextFloat()*180f+90f));
                    setStage(AIStage.ATTACK);
                    break;
                case MANUVER_BACK_TO_FACING:
                    expectedLocation = target.getLocation();
                    if((!drone.getAllWeapons().isEmpty()&&!drone.getAllWeapons().get(0).isFiring())){
                        drone.getAllWeapons().get(0).setForceNoFireOneFrame(true);
                    }
                    break;
            }
            //Global.getLogger(this.getClass()).info("Stage:"+stage);
            float expectedFacing = VectorUtils.getAngle(drone.getLocation(),expectedLocation);
            float rotation = MathUtils.getShortestRotation(drone.getFacing(),expectedFacing);
            if(Math.abs(rotation)>turnRate*amount){
                toFace = drone.getFacing()+Math.signum(rotation)*turnRate*amount;
            }else{
                toFace = drone.getFacing()+rotation;
                if(stage.equals(AIStage.MANUVER_BACK_TO_FACING)){
                    setStage(AIStage.REACHED_ATTACK_POINT);
                }
            }
            speed = (Vector2f) Misc.getUnitVectorAtDegreeAngle(toFace).scale(maxSpeed);
            if(landing){
                setStage(AIStage.LANDING);
                drone.setApplyExtraAlphaToEngines(true);
                float alpha = Misc.getDistance(drone.getLocation(),source.getFirePoint(0))/50f;
                MathUtils.clamp(alpha,0,1);
                drone.setAlphaMult(alpha);
                speed.scale(alpha);
                if(alpha<=0.2f){
                    clearDrone();
                    return;
                }
            }
            drone.setFacing(toFace);
            drone.getVelocity().set(speed);
        }

        public void notifyEnd(){
            ended = true;
        }

        public void beginLand(){
            landing = true;
        }

        public void clearDrone(){
            if(drone!=null) Global.getCombatEngine().removeEntity(drone);
        }

        public void setStage(AIStage stage) {
            this.stage = stage;
        }

        public AIStage getStage() {
            return stage;
        }

        public ShipAPI getDrone() {
            return drone;
        }

        public void setTarget(CombatEntityAPI target) {
            this.target = target;
        }
    }
}
