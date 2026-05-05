package data.weapons.scripts;

import com.fs.starfarer.api.combat.*;
import data.ai.HSIAutoFirePlugins.HSIAutoFireShipData;
import data.ai.HSIAutoFirePlugins.HSIAutoFireTargetingUnitData;
import data.ai.HSIAutoFirePlugins.HSIThreatAnalysisPDAI;
import data.ai.HSIThreatSharedData;
import data.kit.HSIAIUtils;
import data.kit.HSIExtraAmmoTracker;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class HWIMGSystemWeapon implements EveryFrameWeaponEffectPlugin {
    public static final String DATA_KEY = "HSIThreatSharedData";
    private boolean init = true;
    private boolean AIControl = false;
    private ShipAPI ship;
    private HWIMGSystemData data;
    private HSIExtraAmmoTracker ammoTracker;
    private ShipAPI targetShip;
    private MissileAPI targetMissile;
    private HSIAutoFireShipData shipData;
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(init){
            init = init(weapon);
        }
        if(init) return;
        ammoTracker.advance(amount);
        if(weapon.isDisabled()||ship.getFluxTracker().isOverloadedOrVenting()||!ship.isAlive()||ship.isHoldFire()||
        (ship.getPhaseCloak()!=null&&!ship.getPhaseCloak().getSpecAPI().isFiringAllowed()&&ship.getPhaseCloak().isActive())) return;
        if(!ammoTracker.isAvailable()) return;
        if(weapon.getCooldownRemaining()<=0&&((targetShip==null||!targetShip.isAlive())&&(targetMissile==null||targetMissile.isExpired()||!engine.isEntityInPlay(targetMissile)))) {
            int num = 4;
            List<HSIAutoFireTargetingUnitData> datas = new ArrayList<>();
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.MISSILE, data.getRange()));
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.FIGHTER, data.getRange()));
            Collections.sort(datas, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
            HSIAutoFireTargetingUnitData chosen = null;
            for (HSIAutoFireTargetingUnitData d : datas) {
                if (HSIAIUtils.isAcceptableAimLocation(d.getTarget().getLocation(), weapon)) {
                    chosen = d;
                    break;
                }
            }
            if (chosen == null) {
                num = 99;
                datas.clear();
            }
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.MISSILE, weapon.getRange()));
            datas.addAll(shipData.getBestTargetingDataInRange(num, HSIAutoFireShipData.TYPE.FIGHTER, weapon.getRange()));
            Collections.sort(datas, new HSIAutoFireTargetingUnitData.HSIAutoFireTargetingUnitDataScoreComparator());
            for (HSIAutoFireTargetingUnitData d : datas) {
                if (HSIAIUtils.isAcceptableAimLocation(d.getTarget().getLocation(), weapon)&&!HSIAIUtils.isOverKilled(d.getTarget())) {
                    chosen = d;
                    break;
                }
            }
            if (chosen == null) return;
            if(chosen.getTarget() instanceof MissileAPI){
                targetMissile = (MissileAPI) chosen.getTarget();
            }
            if(chosen.getTarget() instanceof ShipAPI){
                targetShip = (ShipAPI) chosen.getTarget();
            }
            float facing = weapon.getCurrAngle();
            Vector2f currLoc = new Vector2f(0, 0);
            VectorUtils.rotateAroundPivot(data.getBoundLocation().get(0), new Vector2f(0f, 0f), facing, currLoc);
            Vector2f.add(weapon.getLocation(), currLoc, currLoc);
            MissileAPI msl = (MissileAPI) engine.spawnProjectile(ship, weapon, data.getWeapon(), currLoc,
                    weapon.getCurrAngle(), null);
            if(msl.getSpec().getOnFireEffect()!=null)
                msl.getSpec().getOnFireEffect().onFire(msl, weapon, engine);
            ammoTracker.Fire();
            if (msl.getAI() instanceof GuidedMissileAI) {
                ((GuidedMissileAI) msl.getAI()).setTarget(chosen.getTarget());
                if (chosen.getTarget().getCustomData().containsKey(DATA_KEY)) {
                    HSIThreatSharedData data = (HSIThreatSharedData) chosen.getTarget().getCustomData().get(DATA_KEY);
                    data.addProj(msl);
                }else{
                    HSIThreatSharedData data = new HSIThreatSharedData(chosen.getTarget());
                    data.addProj(msl);
                    chosen.getTarget().setCustomData(DATA_KEY,data);
                }
            }
        }
    }

    private boolean init(WeaponAPI weapon){
        ship = weapon.getShip();
        data = HWIMGSystemDataProvider.data.get(weapon.getSpec().getWeaponId());
        if(data == null) return true;
        ammoTracker = new HSIExtraAmmoTracker(data.getAmmo(),data.getRegen(),data.getAmmo()/2,data.getCooldown());
        shipData = HSIAutoFireShipData.getInstance(ship);
        return false;
    }


    public static class HWIMGSystemData{
        private float cooldown = 1f;
        private int ammo = 4;
        private String weapon = "";
        private float regen = 1f;
        private float range = 1000f;
        private List<Vector2f> boundLocation = new ArrayList<>();

        public HWIMGSystemData(float cooldown,int ammo,String weapon,float regen,float range,List<Vector2f> boundLocation){
            setAmmo(ammo);
            setWeapon(weapon);
            setCooldown(cooldown);
            setRegen(regen);
            setRange(range);
            this.boundLocation = boundLocation;
        }

        public float getCooldown() {
            return cooldown;
        }

        public void setCooldown(float cooldown) {
            this.cooldown = cooldown;
        }

        public int getAmmo() {
            return ammo;
        }

        public void setAmmo(int ammo) {
            this.ammo = ammo;
        }

        public String getWeapon() {
            return weapon;
        }

        public void setWeapon(String weapon) {
            this.weapon = weapon;
        }

        public float getRegen() {
            return regen;
        }

        public void setRegen(float regen) {
            this.regen = regen;
        }

        public float getRange() {
            return range;
        }

        public void setRange(float range) {
            this.range = range;
        }

        public List<Vector2f> getBoundLocation() {
            return boundLocation;
        }
    }

    public static class HWIMGSystemDataProvider{
        public static Map<String,HWIMGSystemData> data = new HashMap<>();
        static{
            data.put("HWI_Duality",new HWIMGSystemData(2f,4,"HWI_MGSystem_M",0.15f,1100f,new ArrayList<Vector2f>(Arrays.asList(new Vector2f(-6,6)))));
        }
    }

}
