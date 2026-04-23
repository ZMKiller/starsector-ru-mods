package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class ASF_PhantasmagoriaCurtain extends BaseHullMod {
    @Override
    public int getDisplaySortOrder() {
        return 2223;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "Curtain";
    	return null;
        // <Illusory: CURTAIN>
    }
}