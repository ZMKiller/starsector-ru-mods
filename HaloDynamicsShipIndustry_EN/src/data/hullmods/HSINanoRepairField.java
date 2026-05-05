package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.combat.AIUtils;

import java.awt.*;

public class HSINanoRepairField extends BaseHullMod {

    protected static final float REPAIR_RANGE = 2000f;

    protected static final float RATE = 0.005f;

    protected static final float SELF_MULT = 2f;

    public static final Color JITTER_UNDER_COLOR = new Color(59, 255, 59,95);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Math.random()>1f-(0.15f*amount*60f)){
            for(ShipAPI s: AIUtils.getNearbyAllies(ship,REPAIR_RANGE)){
                HSINanoRepairListener.getInstance(s,ship,false);
            }
            for(ShipAPI s:ship.getChildModulesCopy()){
                HSINanoRepairListener.getInstance(s,ship,true);
            }
            HSINanoRepairListener.getInstance(ship,ship,true);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)REPAIR_RANGE+"su";
        if(index == 1) return RATE*100f+"%";
        if(index == 2) return (int)(SELF_MULT*100f)+"%";
        return null;
    }

    public static class HSINanoRepairListener implements AdvanceableListener{
        private ShipAPI ship;
        private TimeoutTracker<ShipAPI> source = new TimeoutTracker<>();

        private boolean self = false;

        public HSINanoRepairListener(ShipAPI ship,boolean self){
            this.ship = ship;
            this.self = self;
        }

        @Override
        public void advance(float amount) {
            source.advance(amount);
            if(!ship.isAlive()||ship.isHulk()){
                ship.removeListener(this);
                return;
            }
            if(source.getItems().isEmpty()&&!self){
                ship.removeListener(this);
            }else{
                //Global.getLogger(this.getClass()).info("Repairing");
                float repairLevel = 0;
                float repairRate = amount*((self)?SELF_MULT:1f)*RATE;
                if(ship.getHitpoints()<ship.getMaxHitpoints()){
                    float loss = ship.getMaxHitpoints()-ship.getHitpoints();
                    float repair = Math.max(loss*repairRate,repairRate*ship.getMaxHitpoints()*0.3f);
                    ship.setHitpoints(Math.min(ship.getHitpoints()+repair,ship.getMaxHitpoints()));
                    repairLevel+=2f;
                }
                ArmorGridAPI aromr = ship.getArmorGrid();
                int y = 0;
                for (float[] row : aromr.getGrid()) {
                    int x = 0;
                    for (float col : row) {
                        if (col < aromr.getMaxArmorInCell()) {
                            float loss = aromr.getMaxArmorInCell()-col;
                            float repair =  Math.max(loss*repairRate,repairRate*aromr.getMaxArmorInCell()*0.3f);
                            aromr.setArmorValue(x,y,Math.min(aromr.getMaxArmorInCell(),col+repair));
                            repairLevel+=0.1f;
                        }
                        x++;
                    }
                    y++;
                }
                if(repairLevel>=4f){
                    repairLevel = 4;
                }
                if(repairLevel>0){
                    ship.syncWithArmorGridState();
                    ship.setJitterUnder(this, JITTER_UNDER_COLOR, repairLevel/4f, (int)(3*repairLevel), 0f, 3f);
                    if(ship == Global.getCombatEngine().getPlayerShip()){
                        addStatus();
                    }
                }
            }

        }

        public void updateSource(ShipAPI s){
            if(source.contains(s)){
                source.set(s,2f);
            }else{
                source.add(s,2f);
            }
        }

        public static HSINanoRepairListener getInstance(ShipAPI ship,ShipAPI source,boolean self){
            if(!ship.isAlive()){
                return null;
            }
            HSINanoRepairListener listener;
            if(ship.hasListenerOfClass(HSINanoRepairListener.class)){
                listener = ship.getListeners(HSINanoRepairListener.class).get(0);
            }else{
                listener = new HSINanoRepairListener(ship,self);
                ship.addListener(listener);
            }
            listener.updateSource(source);
            return listener;
        }

        protected void addStatus() {
            String content = HSII18nUtil.getHullModString("HSINanoRepairContent");
            Global.getCombatEngine().maintainStatusForPlayerShip("HSINanoRepairContent",
                    "graphics/icons/hullsys/damper_field.png", Global.getSettings().getHullModSpec("HSI_NanoRepairField").getDisplayName(), content,
                    false);
        }

    }


}
