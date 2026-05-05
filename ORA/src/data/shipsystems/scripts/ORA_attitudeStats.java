package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.ORA_txt.txt;

public class ORA_attitudeStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        
        if(state == State.ACTIVE){
            //only increase the max speed while the system is ON
            stats.getMaxSpeed().modifyFlat(id, 100 * effectLevel);
        }
        
        stats.getAcceleration().modifyPercent(id, 100f * effectLevel);
        stats.getDeceleration().modifyPercent(id, 100f * effectLevel);
        stats.getTurnAcceleration().modifyFlat(id, 30f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
        stats.getMaxTurnRate().modifyFlat(id, 15f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
        
    }
    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        int speed = 0;
        if(state == State.ACTIVE)speed =100;
        
        if (index == 0) {
                return new StatusData(txt("sstem_aj0"), false);
        } else if (index == 1) {
                return new StatusData(txt("+")+ speed +txt("sstem_aj1"), false);
        }
        return null;
    }
}
