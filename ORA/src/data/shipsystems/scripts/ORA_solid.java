package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.ORA_txt.txt;

public class ORA_solid extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {            
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - 0.66f * effectLevel);		
        stats.getShieldUpkeepMult().modifyMult(id, 1f - 0.66f * effectLevel);
        
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if(ship!=null && ship.getFluxTracker()!= null){
            float softFlux = ship.getFluxTracker().getCurrFlux()-ship.getFluxTracker().getHardFlux();
            softFlux = ship.getFluxTracker().getHardFlux()+softFlux/3;
            ship.getFluxTracker().setCurrFlux(softFlux);        
            ship.getFluxTracker().setHardFlux(softFlux);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getShieldUpkeepMult().unmodify(id);
    }

    private final String DESC0 = txt("sstm_solid0");
    private final String DESC1 = txt("sstm_solid1");
    
    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
                return new StatusData(DESC0, false);
        }
        else if (index == 1) {
                return new StatusData(DESC1, false);
        }
        return null;
    }
}
