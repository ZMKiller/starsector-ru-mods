package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import com.fs.starfarer.api.ui.LabelAPI;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import java.awt.*;
import java.util.List;


public class WarmindCooperation extends SCBaseSkillPlugin{
    @Override
    public String getAffectsString() {
        return "all ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Ships are almost always recoverable if lost in combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        //tooltipMakerAPI.addPara("Provides bonuses which scale based on the amount of automated and crewed ships in the fleet", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

        tooltipMakerAPI.addPara("%s repair rate out of combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), "+25%");
        tooltipMakerAPI.addPara("Ships lost in combat without the %s hullmod have a %s chance to avoid d-mods", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), "Rugged Construction", "50%");
        //tooltipMakerAPI.addPara("   - Other ships lost in combat are up to %s less likely to acquire d-mods", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "66%");
        //tooltipMakerAPI.addPara("   - The bonuses reach their maximum when your fleet contains at least %s ships of their ship type", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "5");
    }

    /*private float getEffect(SCData data, boolean auto) {
        float effectStrength = 0f;

        List<FleetMemberAPI> fleet = data.getFleet().getFleetData().getMembersListCopy();
        for(FleetMemberAPI ship : fleet)
        {
            if(Misc.isAutomated((ship)) == auto)
                effectStrength += .2f;
        }
        if(effectStrength > 1f) // Maximum effect strength = 100%
            effectStrength = 1f;

        return effectStrength;
    }*/

/*
    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {

        if (variant.getHullMods().contains("rugged"))  // If we're rugged, increase dmod chance
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, rDMod);
        else // Otherwise, decrease it
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, oDMod);
        if (!variant.getHullMods().contains("rugged"))
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, oDMod);
    }*/

    @Override
    public void advance(SCData data, Float amunt) {
        data.getFleet().getStats().getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).modifyFlat("sc_warmind_cooperation", 2f);
        float rBonus = 25f;
        data.getCommander().getStats().getRepairRateMult().modifyPercent("sc_warmind_cooperation", rBonus);
    }

    @Override
    public void onActivation(SCData data) {
        data.getFleet().getStats().getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).modifyFlat("sc_warmind_cooperation", 2f);
        float rBonus = 25f;
        data.getCommander().getStats().getRepairRateMult().modifyPercent("sc_warmind_cooperation", rBonus);
    }

    @Override
    public void onDeactivation(SCData data) {
        data.getFleet().getStats().getDynamic().getMod(Stats.SHIP_RECOVERY_MOD).unmodify("sc_warmind_cooperation");
        data.getCommander().getStats().getRepairRateMult().unmodify("sc_warmind_cooperation");
    }

    @Override
    public void callEffectsFromSeparateSkill(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if (stats.getVariant().getHullMods().contains("rugged")) return;

        //float effectStrength = getEffect(data, false);

        //float oDMod = 1f - effectStrength * .67f; // max .33
        float oDMod = 1f - .67f;
        stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, oDMod);
    }
}
