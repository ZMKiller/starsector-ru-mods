package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.Iterator;
import java.util.Random;

public class HSIMatrixTargeting extends BaseHullMod {

    public static class HSIMatrixTargetingBuff implements AdvanceableListener{
        private ShipAPI ship;
        public TimeoutTracker<ShipAPI> source = new TimeoutTracker<>();
        private static final String KEY = "HSI_Matrix_Targeting_buff";
        public static final float RANGE = 2000f;
        public static final float RANGE_BONUS = 100f;
        public static HSIMatrixTargetingBuff getInstance(ShipAPI ship){
            if(ship.hasListenerOfClass(HSIMatrixTargetingBuff.class)){
                HSIMatrixTargetingBuff buff = ship.getListeners(HSIMatrixTargetingBuff.class).get(0);
                return buff;
            }else{
                HSIMatrixTargetingBuff buff = new HSIMatrixTargetingBuff(ship);
                ship.addListener(buff);
                return buff;
            }
        }

        public HSIMatrixTargetingBuff(ShipAPI ship){
            this.ship = ship;
        }


        public void advance(float amount){
            source.advance(amount);
            if(Math.random()>0.2f) return;
            if(source.getItems().isEmpty()){
                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify(KEY);
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify(KEY);
                ship.removeListener(this);
                return;
            }else{
                float minDist = RANGE*1.5f;
                for(ShipAPI s:source.getItems()){
                    float d = MathUtils.getDistance(s.getLocation(),ship.getLocation());
                    if(d<minDist) minDist = d;
                    if(d<RANGE*1.5f){
                        source.set(s,3f);
                    }
                }
                float m = 1f;
                if(minDist>RANGE){
                    m = 1f-(minDist/RANGE)/1000f;
                }
                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(KEY,RANGE_BONUS*m);
                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(KEY,RANGE_BONUS*m);
                if(ship == Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().maintainStatusForPlayerShip("HSI_MatrixTargeting_Buff",
                            "graphics/icons/hullsys/lidar_barrage.png",
                            Global.getSettings().getHullModSpec("HSI_MatrixTargeting").getDisplayName(), "+" +(int)RANGE_BONUS*m+" "+HSII18nUtil.getHullModString("HSIMatrixTargetingBuff"), false);
                }
            }
        }

    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id,HSIMatrixTargetingBuff.RANGE_BONUS*2f);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id,HSIMatrixTargetingBuff.RANGE_BONUS*2f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //timeShieild
        MutableStat timemult = ship.getMutableStats().getTimeMult();
        timemult.unmodify("HSITimeShieldSeldFix");
        if(timemult.getModifiedValue()<1){
            float mult = timemult.getModifiedValue();
            if(mult<=0) mult = 0.001f;
            timemult.modifyMult("HSITimeShieldSeldFix",1f/mult);
        }



        //matrix
        if(Math.random()>0.2f) return;
        for(ShipAPI f: AIUtils.getNearbyAllies(ship,HSIMatrixTargetingBuff.RANGE*1.5f)){
            if(f.getFleetMember()==null||f.isAlly()) continue;
            if(ship.getFleetMember()==null||ship.getFleetMember().getDeploymentPointsCost()>f.getFleetMember().getDeploymentPointsCost()){
                HSIMatrixTargetingBuff.getInstance(f).source.add(ship,3f);
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HSIMatrixTargetingRangeBalancer());
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index==0) return ""+(int) HSIMatrixTargetingBuff.RANGE;
        if(index == 1) return ""+(int)HSIMatrixTargetingBuff.RANGE_BONUS;
        if(index == 2) return HSII18nUtil.getHullModString("HSIMatrixTargetingSelfBonus");
        return null;
    }

    public static class HSIMatrixTargetingRangeBalancer implements WeaponRangeModifier {

        @Override
        public float getWeaponRangePercentMod(ShipAPI shipAPI, WeaponAPI weaponAPI) {
            return 0;
        }

        @Override
        public float getWeaponRangeMultMod(ShipAPI shipAPI, WeaponAPI weaponAPI) {
            return 1f;
        }

        @Override
        public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            float x = weapon.getSlot().getLocation().getX();
            return Math.max(0,143-x);
        }
    }
}
