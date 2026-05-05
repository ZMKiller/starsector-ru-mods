package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.input.InputEventAPI;
import data.hullmods.HSIDeflectionShield;
import data.scripts.HSIGenerator.HSIDreadnought;

import java.util.List;

public class HSIDeepDiveEnginePlugin extends BaseEveryFrameCombatPlugin {

    protected static final String KEY = "HSI_DeepDive_Plugin";

    public static HSIDeepDiveEnginePlugin getInstance(CombatEngineAPI engine){
        if(engine==null) return null;
        if(engine.getCustomData().containsKey(KEY)){
            return (HSIDeepDiveEnginePlugin) engine.getCustomData().get(KEY);
        }else{
            HSIDeepDiveEnginePlugin plugin = new HSIDeepDiveEnginePlugin();
            engine.addPlugin(plugin);
            engine.getCustomData().put(KEY,plugin);
            return plugin;
        }
    }


    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);
        if(Math.random()<0.1){
            for(ShipAPI ship:Global.getCombatEngine().getShips()){
                if(ship.getOwner()==100) continue;
                HSIDeepDiveNoLockListener.getInstance(ship);
            }
        }
    }

    public static class HSIDeepDiveNoLockListener implements AdvanceableListener{
        private ShipAPI ship;

        public HSIDeepDiveNoLockListener(ShipAPI ship){
            this.ship = ship;
        }

        public static HSIDeepDiveNoLockListener getInstance(ShipAPI ship){
            if(ship.hasListenerOfClass(HSIDeepDiveNoLockListener.class)){
                return ship.getListeners(HSIDeepDiveNoLockListener.class).get(0);
            }else{
                HSIDeepDiveNoLockListener l = new HSIDeepDiveNoLockListener(ship);
                ship.addListener(l);
                return l;
            }
        }
        @Override
        public void advance(float amount) {
            if(ship.getShipTarget()!=null&&ship.getShipTarget().getCustomData().containsKey(HSIDeepDive.DEEP_DIVE)&&(boolean)ship.getShipTarget().getCustomData().get(HSIDeepDive.DEEP_DIVE)){
                ship.setShipTarget(null);
            }
            for(WeaponGroupAPI group:ship.getWeaponGroupsCopy()){
                if(group.isAutofiring()){
                    for(AutofireAIPlugin ai: group.getAIPlugins()){
                        if(ai.getTargetShip()!=null&&ai.getTargetShip().getCustomData().containsKey(HSIDeepDive.DEEP_DIVE)&&(boolean)ai.getTargetShip().getCustomData().get(HSIDeepDive.DEEP_DIVE)){
                            ai.forceOff();
                        }
                    }
                }
            }
        }
    }
}
