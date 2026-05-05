package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

import data.scripts.HSIContrail.HSIContrailEntityPlugin;


public class HSIContrailMod extends BaseHullMod{
    
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        HSIContrailEntityPlugin.getInstance(ship);
    }
}
