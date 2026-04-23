package data.scripts.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class SWP_NoPackage extends SWP_BasePackage {

    @Override
    protected String getHullModId() {
        return SWP_NO_PACKAGE;
    }

    @Override
    protected String getFlavorText() {
        return "This hull is already too specialized to be upgraded with an Imperial modification suite.";
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }
}
