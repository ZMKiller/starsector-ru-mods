package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class sfcunshieldable extends BaseHullMod {


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.setShield(ShieldType.NONE, 0f, 1f, 1f);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(HullMods.CIVGRADE)) {
            return "A shield cannot be installed on this ship.";
        }

        return null;
    }
}