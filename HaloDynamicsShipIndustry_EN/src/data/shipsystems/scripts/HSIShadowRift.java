package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;

import java.util.*;

public class HSIShadowRift extends BaseShipSystemScript {

    //private Set<ShipAPI> blinded = new HashSet<>();

    private ShipAPI ship;

    //private IntervalUtil timer = new IntervalUtil(0.2f,0.3f);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(ship==null) {
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            }
        }
        /*timer.advance(Global.getCombatEngine().getElapsedInLastFrame());
        if(timer.intervalElapsed()) {
            for (ShipAPI e : Global.getCombatEngine().getShips()) {
                if (e.getOwner() == ship.getOwner()) continue;
                if (!e.isAlive() || e.isHulk()) continue;
                float sightRadius = e.getMutableStats().getSightRadiusMod().computeEffective(3000f);
                float dist = Misc.getDistance(e.getLocation(), ship.getLocation()) - ship.getCollisionRadius();
                e.getMutableStats().getSightRadiusMod().modifyMult("HSI_MoonShadow_"+ship.getId(),dist/sightRadius*0.9f*(2f-effectLevel));
                blinded.add(e);
            }
        }*/
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if(ship==null) {
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            }
        }
        /*Iterator<ShipAPI> iter = blinded.iterator();
        while (iter.hasNext()){
            ShipAPI s = iter.next();
            s.getMutableStats().getSightRadiusMod().unmodify("HSI_MoonShadow_"+ship.getId());
            iter.remove();
        }*/
    }



    public static class HSIShadowRiftPlugin extends BaseEveryFrameCombatPlugin{

    }
}
