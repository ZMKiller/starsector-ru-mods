package org.xhan.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;


public class XHAN_Elite_PlasteenProduction extends BaseHullMod {


    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "280 Organics";
        if (index == 1) return "1 Hassak";
        return null;
    }

}
