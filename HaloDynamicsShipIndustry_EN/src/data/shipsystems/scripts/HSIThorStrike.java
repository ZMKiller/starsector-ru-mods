package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;

public class HSIThorStrike extends BaseShipSystemScript {
    private ShipAPI ship;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private IntervalUtil bombing = new IntervalUtil(0.15f, 0.15f);

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(ship.getCollisionClass()!=CollisionClass.FIGHTER) ship.setCollisionClass(CollisionClass.FIGHTER);
        ship.turnOnTravelDrive();
        float amount = engine.getElapsedInLastFrame();
        bombing.advance(amount);
        if(bombing.intervalElapsed()){
            bomb();
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(ship.getCollisionClass()==CollisionClass.FIGHTER&&!ship.isFighter()){
            ship.setCollisionClass(CollisionClass.SHIP);
        }
        ship.turnOffTravelDrive();
    }

    private void bomb(){
        float angle = ship.getFacing();
        if(Math.random()>0.5f){
            angle-=100f;
        }else{
            angle+=100f;
        }
        if(angle<0) angle+=360f;
        angle%=360f;
        engine.spawnProjectile(ship, null, "annihilator", ship.getLocation(), angle,ship.getVelocity());
    }
}
