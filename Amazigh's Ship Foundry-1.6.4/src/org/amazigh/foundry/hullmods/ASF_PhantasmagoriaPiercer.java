package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class ASF_PhantasmagoriaPiercer extends BaseHullMod {
    @Override
    public int getDisplaySortOrder() {
        return 2222;
    }

    @Override
    public int getDisplayCategoryIndex() {
        return 3;
    }
    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 0) return "Piercer";
    	return null;
        // <Barrage: PIERCE>
    }
}