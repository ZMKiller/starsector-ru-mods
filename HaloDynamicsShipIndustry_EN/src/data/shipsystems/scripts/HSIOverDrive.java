package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.kit.HSII18nUtil;

import java.awt.Color;

public class HSIOverDrive extends BaseShipSystemScript {

    protected static float MANUVER_BOOST = 100f;

    protected static float SPEED_BOOST = 75f;
    private Color color = new Color(240,200,220,255);
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getMaxSpeed().modifyFlat(id,SPEED_BOOST*effectLevel);
        stats.getAcceleration().modifyPercent(id,MANUVER_BOOST*effectLevel);
        stats.getDeceleration().modifyPercent(id,MANUVER_BOOST*effectLevel);
        stats.getMaxTurnRate().modifyPercent(id,MANUVER_BOOST*effectLevel);
        stats.getTurnAcceleration().modifyPercent(id,MANUVER_BOOST*effectLevel);
        /*if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
            ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);
        }*/
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSIOD0"), false);
        } else if (index == 1) {
            return new StatusData("+" + (int)SPEED_BOOST + HSII18nUtil.getShipSystemString("HSIOD1"), false);
        }
        return null;
    }
}
