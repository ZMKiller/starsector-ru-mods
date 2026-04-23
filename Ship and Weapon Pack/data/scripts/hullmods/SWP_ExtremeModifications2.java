package data.scripts.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class SWP_ExtremeModifications2 extends SWP_ExtremeModifications {

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("efficiency_overhaul");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                SWP_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }

        int maxSMods = Misc.getMaxPermanentMods(ship);
        int currSMods = Misc.getCurrSpecialMods(ship.getVariant());
        if ((currSMods < maxSMods) || (maxSMods <= 0)) {
            ship.getVariant().removePermaMod(spec.getId());
            ship.getVariant().addMod("swp_extrememods");
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
            return "Can not be installed on civilian ships";
        }
        if ((ship != null) && ship.getVariant().hasHullMod("efficiency_overhaul")) {
            return "Incompatible with Efficiency Overhaul";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
            return false;
        }
        return !((ship != null) && ship.getVariant().hasHullMod("efficiency_overhaul"));
    }

    @Override
    public Color getNameColor() {
        return new Color(255, 100, 100);
    }
}
