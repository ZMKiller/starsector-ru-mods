package data.hullmods;

import org.magiclib.util.MagicIncompatibleHullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import data.kit.HSII18nUtil;

public class HSIFreischutz extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(HullSize hullSize,
            MutableShipStatsAPI stats, String id) {
        if(stats.getVariant().hasHullMod("magazines"))
        MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "magazines", HSII18nUtil.getHullModString("HSIFreischutzWarning"));
    }
}
