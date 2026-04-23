package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class XHAN_PsyShield extends BaseHullMod {

    private final String INNERLARGE = "graphics/fx/Xhan_PsyShield_inner.png";
    private final String OUTERLARGE = "graphics/fx/Xhan_PsyShield_Rim.png";
    public static final float SHIELD_BONUS_TURN = 20f;
    public static final float SHIELD_BONUS_UNFOLD = 9200f;
    public static final float SHIELD_HE_REDUCTION = -50f;
    public static final float SHIELD_KE_REDUCTION = 200f;
    public static final float SHIELD_FRAG_REDUCTION = -50f;

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_BONUS_TURN);
        stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);
        stats.getHighExplosiveShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_HE_REDUCTION / 100f);
        stats.getHighExplosiveShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_KE_REDUCTION / 100f);
        stats.getHighExplosiveShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_FRAG_REDUCTION / 100f);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) SHIELD_BONUS_TURN + "%";
        if (index == 1) return "" + (int) SHIELD_BONUS_UNFOLD + "%";
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getShield() != null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return "Ship has no shields";
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getShield().setRadius(ship.getShieldRadiusEvenIfNoShield(), INNERLARGE, OUTERLARGE);
        ship.getShield().setInnerRotationRate(0.1f);
    }
}
