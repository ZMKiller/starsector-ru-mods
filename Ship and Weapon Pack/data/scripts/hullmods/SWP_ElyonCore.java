package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

public class SWP_ElyonCore extends BaseHullMod {

    public static final float HULL_BONUS = 20f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        float capBonus = (ship.getMaxHitpoints() - ship.getHitpoints()) * 2f;
        ship.getMutableStats().getFluxCapacity().modifyFlat("swp_elyoncore_fluxbonus", capBonus);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHullBonus().modifyPercent(id, HULL_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "two times";
        }
        if (index == 1) {
            return "" + (int) HULL_BONUS + "%";
        }
        return null;
    }

    @Override
    public Color getBorderColor() {
        return new Color(124, 230, 184);
    }

    @Override
    public Color getNameColor() {
        return new Color(51, 193, 94);
    }

    @Override
    public int getDisplaySortOrder() {
        return 0;
    }
}
