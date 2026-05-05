package data.shipsystems.scripts;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ORA_longRangeStats extends BaseShipSystemScript {

    private final float RANGE_BOOST=3000, SPEED_DAMPENING=-75f;	

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(effectLevel>0){            
            stats.getFighterWingRange().modifyFlat(id, RANGE_BOOST*effectLevel);
            stats.getMaxSpeed().modifyPercent(id, effectLevel*SPEED_DAMPENING);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {        
        stats.getFighterWingRange().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
    }	

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("+"+(int)(RANGE_BOOST * effectLevel)+" fighters range.", false);
        }
        if (index == 1) {
            return new StatusData(""+(int)(SPEED_DAMPENING * effectLevel)+"% carrier speed.", true);
        }
        return null;
    }
}