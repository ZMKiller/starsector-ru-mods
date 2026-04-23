package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SWP_ForceImpederStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        ship.getMutableStats().getTimeMult().modifyMult(id, 1f + effectLevel * 2f);
        Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / (1f + effectLevel * 3f));
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        ship.getMutableStats().getTimeMult().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
    }
}
