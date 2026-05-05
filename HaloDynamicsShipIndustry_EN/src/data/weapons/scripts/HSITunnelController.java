package data.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class HSITunnelController implements EveryFrameWeaponEffectPlugin {

    public enum AIStage {
        WAITING_ORDER, MOVE, ATTACK,  MANUVER_TO_NEXT_ATTACK_POSITION,END_ATTACK,GO_TO_LAND
    }

    private List<HSITunnelScript> drones = new ArrayList<>();

    protected static final int MAX_AMMO = 8;

    private int ammo = MAX_AMMO;

    private static final float DRONE_COST = 1000f;

    private IntervalUtil reloadTimer = new IntervalUtil(8f, 8f);

    private IntervalUtil fireTimer = new IntervalUtil(0.5f, 0.5f);

    private int framesChange = 3;


    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(framesChange>0){
            framesChange--;
        }else{
            if(weapon.getAnimation()!=null){
                weapon.getAnimation().setFrame(1);
            }
        }
        boolean hasTarget = !weapon.getBeams().isEmpty() && weapon.getBeams().get(0).getDamageTarget() != null;
        for(int i = 0;i<3;i++){
            if(drones.isEmpty()) {
                for (int j = 0; j < 3; j++){
                    spawnAndBindScript(engine, weapon, j);
                }
                break;
            }else {
                drones.get(i).advance(amount);
                if (hasTarget) {
                    drones.get(i).setStage(AIStage.MOVE);
                    drones.get(i).setTarget(weapon.getBeams().get(0).getDamageTarget());
                }
            }
        }
        if(weapon.getShip()!=null&&!weapon.getShip().isAlive()){
            for(HSITunnelScript drone:drones){
                drone.notifyEnd();
            }
        }
    }

    public void spawnAndBindScript(CombatEngineAPI engine,WeaponAPI weapon,int point) {
        ShipHullSpecAPI spec = Global.getSettings().getHullSpec("HSI_Tunnel");
        ShipVariantAPI v = Global.getSettings().createEmptyVariant("HSI_Tunnel_Trail", spec);
        v.addWeapon("WS0001", "HWI_IonLance");
        WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
        g.addSlot("WS0001");
        g.setAutofireOnByDefault(false);
        v.addWeaponGroup(g);
        ShipAPI w = engine.createFXDrone(v);
        w.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        w.setOwner(weapon.getShip().getOwner());
        if(weapon.getShip().isAlly()){
            w.setAlly(true);
        }
        w.setForceHideFFOverlay(true);
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
        self.getEnergyWeaponRangeBonus().modifyMult("ANYWAYITSWINGMAN",1f);
        if(w.getShield()!=null){
            w.getShield().setRadius(w.getShieldRadiusEvenIfNoShield()*0.66f);
        }
        self.getHardFluxDissipationFraction().modifyFlat("ANYWAYITSWINGMAN",1f);
        self.getHullDamageTakenMult().modifyMult("ANYWAYITSWINGMAN",0f);
        self.getTimeMult().modifyMult("ANYWAYITSWINGMAN",1f);
        self.getEnergyRoFMult().modifyMult("ANYWAYITSWINGMAN",0.5f);
        self.getCRLossPerSecondPercent().modifyMult("ANYWAYITSWINGMAN",0f);
        self.getEnergyWeaponDamageMult().modifyMult("ANYWAYITSWINGMAN",0.5f);
        w.setDoNotFlareEnginesWhenStrafingOrDecelerating(true);
        w.setCollisionClass(CollisionClass.NONE);
        w.setFacing(weapon.getCurrAngle());
        Global.getCombatEngine().addEntity(w);
        w.getLocation().set(weapon.getFirePoint(point));
        w.setShipAI(null);
        drones.add(new HSITunnelScript(w,weapon,point));
    }


    public static class HSITunnelScript {
        private ShipAPI drone;
        private AIStage stage;

        private WeaponAPI source;

        private CombatEntityAPI target = null;

        private Vector2f expectedLocation = new Vector2f();

        private final float turnRate,maxSpeed;

        private boolean ended = false;

        private int point = 0;

        private boolean fired = false;

        public HSITunnelScript(ShipAPI drone, WeaponAPI weapon,int point) {
            this.drone = drone;
            stage =AIStage.WAITING_ORDER;
            source = weapon;
            turnRate = //drone.getMaxTurnRate();
                    720f;
            maxSpeed = //drone.getMaxSpeed();
                    600f;
            this.point = point;
        }

        public void advance(float amount) {
            if(ended) return;
            if(target == null) setStage(AIStage.GO_TO_LAND);
            if(target!=null&&stage.equals(AIStage.WAITING_ORDER)) setStage(AIStage.MOVE);
            float toFace = drone.getFacing();
            Vector2f speed = new Vector2f();
            float mult = 1f;
            switch (stage) {
                case WAITING_ORDER:
                    expectedLocation = source.getFirePoint(point);
                    break;
                case MOVE:
                    expectedLocation = target.getLocation();
                    mult = 2f;
                    if (MathUtils.getDistanceSquared(expectedLocation, drone.getLocation()) < 3f * target.getCollisionRadius() * target.getCollisionRadius() + 40000) {
                        setStage(AIStage.MANUVER_TO_NEXT_ATTACK_POSITION);
                    }
                    break;
                case ATTACK:
                    expectedLocation = Vector2f.add(target.getLocation(),
                            (Vector2f) Misc.getUnitVectorAtDegreeAngle(MathUtils.clampAngle(target.getFacing()+point*60f)+120f).scale(target.getCollisionRadius()+50f),null
                    );
                    if(Math.abs(MathUtils.getShortestRotation(drone.getFacing(),VectorUtils.getAngle(drone.getLocation(),target.getLocation())))<=1f&&MathUtils.getDistance(drone.getLocation(),target.getLocation())<= target.getCollisionRadius()+200f){
                            drone.giveCommand(ShipCommand.FIRE,target.getLocation(),0);
                            fired = true;
                    }
                    if(fired&&drone.getAllWeapons().get(0).getCooldownRemaining()>0&&drone.getAllWeapons().get(0).getBeams().isEmpty()){
                        setStage(AIStage.END_ATTACK);
                        target = null;
                        fired = false;
                    }
                    break;
                case MANUVER_TO_NEXT_ATTACK_POSITION:
                    expectedLocation = Vector2f.add(target.getLocation(),
                            (Vector2f) Misc.getUnitVectorAtDegreeAngle(MathUtils.clampAngle(target.getFacing()+point*60f)+120f).scale(target.getCollisionRadius()+300f),null
                            );
                    if(MathUtils.getDistanceSquared(expectedLocation,drone.getLocation())<=900f){
                        setStage(AIStage.ATTACK);
                    }
                    break;
                case END_ATTACK:
                    expectedLocation = Vector2f.add(target.getLocation(),
                            (Vector2f) Misc.getUnitVectorAtDegreeAngle(MathUtils.clampAngle(target.getFacing()+(4-point)*90f)).scale(target.getCollisionRadius()+150f),null
                    );
                    if(MathUtils.getDistanceSquared(expectedLocation,drone.getLocation())<=900f){
                        setStage(AIStage.GO_TO_LAND);
                    }
                    break;
                case GO_TO_LAND:
                    mult = 2f;
                    expectedLocation = source.getFirePoint(point);
                    if(MathUtils.getDistanceSquared(expectedLocation,drone.getLocation())<=900f){
                        setStage(AIStage.WAITING_ORDER);
                        if(!drone.getAllWeapons().isEmpty()){
                            drone.getAllWeapons().get(0).setRemainingCooldownTo(0f);
                        }
                    }
                    break;
            }
            //Global.getLogger(this.getClass()).info("Stage:"+stage);
            float expectedFacing = VectorUtils.getAngle(drone.getLocation(),expectedLocation);
            if(stage.equals(AIStage.ATTACK)){
                expectedFacing = VectorUtils.getAngle(drone.getLocation(),target.getLocation());
            }
            if(stage.equals(AIStage.WAITING_ORDER)){
                expectedFacing = source.getCurrAngle();
            }
            float rotation = MathUtils.getShortestRotation(drone.getFacing(),expectedFacing);
            if(Math.abs(rotation)>turnRate*amount){
                toFace = drone.getFacing()+Math.signum(rotation)*turnRate*amount;
            }else{
                toFace = drone.getFacing()+rotation;
            }
            drone.setFacing(toFace);
            //drone.getVelocity().set(new Vector2f(0,0));
            Vector2f diff = Vector2f.sub(expectedLocation,drone.getLocation(),null);

            if(diff.length()!=0) {
                if (!stage.equals(AIStage.WAITING_ORDER)){
                    if(diff.length() > maxSpeed * amount) {
                        diff.scale(maxSpeed * mult* amount / diff.length());
                    }
                    drone.getEngineController().forceShowAccelerating();
                    drone.giveCommand(ShipCommand.ACCELERATE,drone.getMouseTarget(),0);
                    for(ShipEngineControllerAPI.ShipEngineAPI e:drone.getEngineController().getShipEngines()){
                        drone.getEngineController().setFlameLevel(e.getEngineSlot(),2f);
                    }
                }
            }
            Vector2f shouldMove = Vector2f.add(drone.getLocation(),diff,null);
            drone.getLocation().set(shouldMove);
        }

        public void notifyEnd(){
            ended = true;
        }

        public void beginLand(){
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
