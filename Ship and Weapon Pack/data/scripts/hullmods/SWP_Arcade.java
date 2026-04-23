package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class SWP_Arcade extends BaseHullMod {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    static {
        BLOCKED_HULLMODS.add("safetyoverrides");
        BLOCKED_HULLMODS.add("converted_hangar");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                SWP_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }
}
