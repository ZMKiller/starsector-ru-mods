package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HSIModularArmorSpecial extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0.01f);
        stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0.01f);
    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if(ship.getParentStation()!=null) {
                float mult = ship.getArmorGrid().getArmorRating() / ship.getParentStation().getArmorGrid().getArmorRating();
                MutableShipStatsAPI stats = ship.getMutableStats();
                if (mult > 1) mult = 1;
                stats.getArmorDamageTakenMult().modifyMult(id, mult);
                stats.getShieldDamageTakenMult().modifyMult(id, (1f - (1f - mult) * 2f));
                stats.getHullDamageTakenMult().modifyMult(id, mult);
            }
    }
}
