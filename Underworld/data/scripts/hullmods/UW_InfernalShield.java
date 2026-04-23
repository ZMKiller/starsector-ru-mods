package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.awt.Color;

public class UW_InfernalShield extends BaseHullMod {

    public static final int HARD_FLUX_DISSIPATION_PERCENT = 50;
    public static final float ARC_BONUS = 300f;
    public static final float SHIELD_BONUS_TURN = 100f;
    public static final float SHIELD_BONUS_UNFOLD = 100f;
    public static final float SHIELD_BONUS = 20f;

    private static final Color SHIELD_COLOR = new Color(255, 75, 25, 102);

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHardFluxDissipationFraction().modifyFlat(id, (float) HARD_FLUX_DISSIPATION_PERCENT * 0.01f);
        stats.getShieldArcBonus().modifyMult(id, 1f + (ARC_BONUS * 0.01f));
        stats.getShieldTurnRateMult().modifyMult(id, 1f + (SHIELD_BONUS_TURN * 0.01f));
        stats.getShieldUnfoldRateMult().modifyMult(id, 1f + (SHIELD_BONUS_UNFOLD * 0.01f));
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - (SHIELD_BONUS * 0.01f));
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ShieldAPI shield = ship.getShield();
        if (shield != null) {
            shield.setType(ShieldType.OMNI);
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        ShieldAPI shield = ship.getShield();
        if (shield != null) {
            shield.setInnerColor(SHIELD_COLOR);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + HARD_FLUX_DISSIPATION_PERCENT + "%";
        }
        if (index == 1) {
            return "quadruples";
        }
        if (index == 2) {
            return "Doubles";
        }
        if (index == 3) {
            return "" + (int) SHIELD_BONUS + "%";
        }
        return null;
    }
}
