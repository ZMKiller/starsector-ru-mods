package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class HSIDayTimeCore extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToMissiles().modifyMult(id,2f);
        stats.getDamageToFighters().modifyMult(id,2f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id,0.1f);
        stats.getCombatEngineRepairTimeMult().modifyMult(id,0.1f);
        stats.getOverloadTimeMod().modifyMult(id,0.2f);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(ship.getFluxTracker().isOverloaded()){
            ship.getMutableStats().getFluxDissipation().modifyMult(spec.getId(),5f);
        }else{
            ship.getMutableStats().getFluxDissipation().unmodify(spec.getId());
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if(ship.getCaptain()==null||ship.getCaptain().isDefault()){
            ship.setCaptain(new AICoreOfficerPluginImpl().createPerson(Commodities.BETA_CORE, Factions.NEUTRAL,null));
        }
    }
}
