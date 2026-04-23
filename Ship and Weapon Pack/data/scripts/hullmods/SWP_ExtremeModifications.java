package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.SWP_BlockedHullmodDisplayScript;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class SWP_ExtremeModifications extends BaseHullMod {

    private static final float ENGINE_MALFUNCTION_PROB = 0.03f;
    private static final float WEAPON_MALFUNCTION_PROB = 0.03f;
    private static final float LOW_CR_CRIT_MALFUNCTION_PROB_MAX = 0.5f;
    private static final float REFIT_TIME_PENALTY = 0.20f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("efficiency_overhaul");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getCurrentCR() < 0.4f) {
            ship.getMutableStats().getCriticalMalfunctionChance().modifyFlat("swp_em_critmalfunction",
                    (1f - (0.8f * (ship.getCurrentCR() / 0.4f))) * LOW_CR_CRIT_MALFUNCTION_PROB_MAX);
            ship.getMutableStats().getEngineMalfunctionChance().modifyFlat("swp_em_engmalfunction", ENGINE_MALFUNCTION_PROB);
        } else {
            ship.getMutableStats().getCriticalMalfunctionChance().unmodify("swp_em_critmalfunction");
            ship.getMutableStats().getEngineMalfunctionChance().unmodify("swp_em_engmalfunction");
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                SWP_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }

        /* What have you done?! */
        if (ship.getVariant().getSMods().contains(spec.getId())) {
            ship.getVariant().removePermaMod(spec.getId());
            ship.getVariant().addMod(spec.getId());
        } else {
            int maxSMods = Misc.getMaxPermanentMods(ship);
            int currSMods = Misc.getCurrSpecialMods(ship.getVariant());
            if (currSMods >= maxSMods) {
                ship.getVariant().removeMod(spec.getId());
                ship.getVariant().addPermaMod("swp_extrememods2", false);
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALFUNCTION_PROB);
        stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_PENALTY * 100f);

        /* Show me the money */
        stats.getDynamic().getMod(Stats.MAX_PERMANENT_HULLMODS_MOD).modifyFlat(id, 1);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "one additional built-in hullmod";
        }
        if (index == 1) {
            return "significant chance";
        }
        if (index == 2) {
            return "" + (int) (REFIT_TIME_PENALTY * 100f) + "%";
        }
        if (index == 3) {
            return "40%";
        }
        return null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("swp_extrememods2")) {
            return "Can not stack Extreme Modifications with itself";
        }
        if ((ship != null) && ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
            return "Can not be installed on civilian ships";
        }
        if ((ship != null) && ship.getVariant().hasHullMod("efficiency_overhaul")) {
            return "Incompatible with Efficiency Overhaul";
        }
        if ((ship != null) && !ship.getVariant().hasHullMod(spec.getId())) {
            int maxSMods = Misc.getMaxPermanentMods(ship);
            int currSMods = Misc.getCurrSpecialMods(ship.getVariant());
            if ((currSMods != maxSMods) || (maxSMods <= 0)) {
                return "Ship is not at the built-in hullmod limit";
            }
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod("swp_extrememods2")) {
            return false;
        }
        if ((ship != null) && ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
            return false;
        }
        if ((ship != null) && ship.getVariant().hasHullMod("efficiency_overhaul")) {
            return false;
        }
        if ((ship != null) && !ship.getVariant().hasHullMod(spec.getId())) {
            int maxSMods = Misc.getMaxPermanentMods(ship);
            int currSMods = Misc.getCurrSpecialMods(ship.getVariant());
            if ((currSMods != maxSMods) || (maxSMods <= 0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Color getNameColor() {
        return new Color(150, 150, 150);
    }
}
