package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

import java.awt.*;

public class HSIDayTimeAurora extends BaseShipSystemScript {

    public static final Color DEFAULT_JITTER_COLOR = new Color(100,165,255,75);
    protected ShipAPI ship;
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 400f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 400f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 400f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id, 400f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 400f * effectLevel);
            stats.getTimeMult().modifyMult(id,1f+1.1f*effectLevel);
            stats.getEnergyRoFMult().modifyMult(id,1f/(1f+1.1f*effectLevel));
        }

        if(ship == null) {
            if (stats.getEntity() instanceof ShipAPI) {
                this.ship = (ShipAPI) stats.getEntity();
            }
        }
        if(ship!=null){
            ship.setPhased(true);
            ship.setExtraAlphaMult2(1f-0.5f*effectLevel);
            ship.setJitter("HSI_DayTimeAurora", DEFAULT_JITTER_COLOR, effectLevel, 6, 0f, 15f);
        }

    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTimeMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        if(ship == null) {
            if (stats.getEntity() instanceof ShipAPI) {
                this.ship = (ShipAPI) stats.getEntity();
            }
        }
        if(ship!=null){
            ship.setPhased(false);
            ship.setExtraAlphaMult2(1f);
        }
    }
}
