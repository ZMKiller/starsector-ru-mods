package org.xhan.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class XHAN_MicroBurnStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else if (state == State.ACTIVE) {
            stats.getMaxSpeed().modifyFlat(id, 600f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 1200f * effectLevel);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (state == State.ACTIVE && index == 0) {
            return new StatusData("full speed ahead", false);
        } else if (state == State.OUT && index == 0) {
            return new StatusData("redistributing power for combat", false);
        } else if (index == 0) {
            return new StatusData("redirecting power to engines", false);
        }
        return null;
    }
}
