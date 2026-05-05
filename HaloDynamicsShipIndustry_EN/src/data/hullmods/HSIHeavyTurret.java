package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HSIHeavyTurret extends BaseHullMod{
    public void applyEffectsBeforeShipCreation(HullSize hullSize,
											   MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyPercent(id, 40f);
        stats.getHullBonus().modifyPercent(id, 50f);
	}
}
