package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.core.util.WeakCache;
import org.lazywizard.lazylib.MathUtils;

public class HSIDreadnoughtCharge extends BaseShipSystemScript {
    private static float SPEED_BOOST = 250f;

    private ShipAPI ship;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(stats.getEntity() instanceof ShipAPI){
            ship =(ShipAPI) stats.getEntity();
        }else{
            return;
        }

        if(ship.getShipTarget()!=null){
            float diff = MathUtils.getShortestRotation(ship.getFacing(), Misc.getAngleInDegrees(ship.getLocation(),ship.getShipTarget().getLocation()));
            if(Math.abs(diff)>5f){
                if(diff<0){
                    ship.giveCommand(ShipCommand.TURN_LEFT,null,0);
                }else{
                    ship.giveCommand(ShipCommand.TURN_RIGHT,null,0);
                }
            }
        }
        stats.getMaxSpeed().modifyFlat(id,SPEED_BOOST*effectLevel);
        stats.getAcceleration().modifyPercent(id,SPEED_BOOST*effectLevel);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }
}
