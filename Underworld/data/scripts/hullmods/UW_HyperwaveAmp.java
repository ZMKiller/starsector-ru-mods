package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class UW_HyperwaveAmp extends BaseHullMod {

    private Object loop1;
    private Object loop2;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if ((ship == null) || !ship.isAlive()) {
            return;
        }

        Global.getSoundPlayer().playLoop("uw_palace_wubwub", loop1, 1f, 1f, ship.getLocation(), Misc.ZERO);

        float vol;
        if (ship.getSystem().isActive()) {
            vol = Math.max(0.01f, ship.getSystem().getEffectLevel());
        } else {
            vol = 0.01f;
        }
        Global.getSoundPlayer().playLoop("uw_palace_bitcruncher", loop2, 1f, vol, ship.getLocation(), Misc.ZERO);
    }
}
