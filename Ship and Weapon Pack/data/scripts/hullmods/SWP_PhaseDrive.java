package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.util.HashSet;
import java.util.Set;

public class SWP_PhaseDrive extends BaseHullMod {

    public static float PHASE_COOLDOWN_REDUCTION = 25f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("adaptive_coils");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getPhaseCloakCooldownBonus().modifyMult(id, 1f - (PHASE_COOLDOWN_REDUCTION / 100f));
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

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "" + (int) Math.round(PHASE_COOLDOWN_REDUCTION) + "%";
        }
        return null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("adaptive_coils")) {
            return "Incompatible with Adaptive Phase Coils";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("adaptive_coils")) {
            return false;
        }
        return super.isApplicableToShip(ship);
    }
}
