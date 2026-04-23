package shmoneelse.sicdustkeeper.skills.dustkeeper;

import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;


public class FocusedOptimization extends SCBaseSkillPlugin {
    @Override
    public String getAffectsString() {
        return "all ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+33%% EMP damage resistance on ships with the Rugged Construction hullmod", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+10%% hardflux dissipation while shields are active on other ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        //if (!Misc.isAutomated(stats)) return;
        if (variant.getHullMods().contains("rugged")) { // If we're rugged, boost armor
            stats.getEmpDamageTakenMult().modifyMult(id, .67f);
        } else {
            stats.getHardFluxDissipationFraction().modifyFlat(id, 0.1f);
        }
    }

}
