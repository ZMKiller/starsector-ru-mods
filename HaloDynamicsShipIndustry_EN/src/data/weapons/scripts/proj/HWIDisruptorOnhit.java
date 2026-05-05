package data.weapons.scripts.proj;

import java.awt.Color;
import java.util.List;

import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class HWIDisruptorOnhit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
            if(target instanceof ShipAPI){
                ShipAPI ship = (ShipAPI)target;
                
                MutableShipStatsAPI stats = ship.getMutableStats();
                float flux = ship.getMaxFlux();
                if(flux == 0) flux =1;
                float time = 10000f/flux*5f;
                if(time>11f) time = 11f;
                if(time<5f) time = 5f;
                ship.fadeToColor(ship,new Color(100, 65, 245, 75), time/2f, time/2f, 2f);
                engine.addPlugin(new HWIDisruptorEffectPlugin(ship,time));
            }
    }

    public static class HWIDisruptorEffectPlugin extends BaseEveryFrameCombatPlugin{
        private ShipAPI ship;
        private float time;
        private IntervalUtil timer = new IntervalUtil(5f, 5f);
        private MutableShipStatsAPI stats;
        private String id = "HWIDisruptorMatrix";
        public HWIDisruptorEffectPlugin(ShipAPI ship,float time){
            this.ship = ship;
            this.time = time;
            timer = new IntervalUtil(time, time);
            stats = ship.getMutableStats();
            stats.getEnergyRoFMult().modifyMult(id, 0.5f);
            stats.getMissileRoFMult().modifyMult(id, 0.5f);
            stats.getBallisticRoFMult().modifyMult(id, 0.5f);
        }

        public void advance(float amount,List<InputEventAPI> events){
            if(!ship.isAlive()){
                stats.getEnergyRoFMult().unmodify(id);
                stats.getMissileRoFMult().unmodify(id);
                stats.getBallisticRoFMult().unmodify(id);
                Global.getCombatEngine().removePlugin(this);
                return;
            }
            if(Global.getCombatEngine().isPaused()) return;
            time-=amount;
            if(time<=0){
                stats.getEnergyRoFMult().unmodify(id);
                stats.getMissileRoFMult().unmodify(id);
                stats.getBallisticRoFMult().unmodify(id);
                Global.getCombatEngine().removePlugin(this);
            }else{
                ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                if(ship.getSystem()!=null&&ship.getSystem().isActive()){
                    ship.getSystem().deactivate();
                }
            }

            if(ship.equals(Global.getCombatEngine().getPlayerShip())){
                addStatus(time);
            }
        }

        protected void addStatus(float time) {
            String content = HSII18nUtil.getHullModString("HWIDisruptorWeapon")+String.format("%.1f",time)+HSII18nUtil.getHullModString("HSIUnitSec");
            Global.getCombatEngine().maintainStatusForPlayerShip("HSICombatAutomateRepirSystemManager_Key0",
                    "graphics/icons/hullsys/HSI_Lock.png", HSII18nUtil.getHullModString("HWIDisruptorTitle"), content,
                    true);
        }
    }
}
