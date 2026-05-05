package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;

public class HSIDogFightAI extends BaseHullMod {


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }



    public static class HSIDogFightAIReplacer implements AdvanceableListener{
        private ShipAPI ship;

        private ShipAPI source;

        private CombatEntityAPI target;

        private IntervalUtil targetTimer = new IntervalUtil(4f,6f);
        public HSIDogFightAIReplacer(ShipAPI ship){
            this.ship = ship;

            if(ship.getWing()!=null&&ship.getWing().getSourceShip()!=null){
                source = ship.getWing().getSourceShip();
            }
        }

        @Override
        public void advance(float amount) {
            if(!ship.isAlive()){
                ship.removeListener(this);
                return;
            }
            if(source!=null&&source.isPullBackFighters()){
                //do nothing
            }else{
                if(target == null||target.getHitpoints()<=0||target.isExpired()|| !Global.getCombatEngine().isEntityInPlay(target)){
                    repickTarget();
                }

            }
        }

        public void repickTarget(){
            for(ShipAPI t:AIUtils.getNearbyEnemies((source!=null)?source:ship,(source!=null)?(ship.getWing().getRange()):8000f)){
                
            }
        }
    }
}
