package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

public class RapidPursuit extends SCBaseSkillPlugin {
    @Override
    public String getAffectsString() {
        return "all automated ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("The zero-flux boost can trigger as long as the ship is below 10%% of its flux capacity*", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Increases the ship's zero-flux speed boost by 40%%/40%%/30%%/20%% based on its hullsize", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

        tooltipMakerAPI.addSpacer(10f);
        tooltipMakerAPI.addPara("*The effect can stack with others of the same kind, two of them would add up to a minimum of 20%%", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "20%");
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, HullSize hullSize, String id) {
        float val = 20f;

        if(hullSize == HullSize.FRIGATE) val = 40f;
        else if(hullSize == HullSize.DESTROYER) val = 40f;
        else if(hullSize == HullSize.CRUISER) val = 30f;
        //else if(hullSize == HullSize.CAPITAL_SHIP) val = 20f; # Redundant
        
        stats.getZeroFluxSpeedBoost().modifyPercent(id, val);

        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 0.1f);
    }
}
