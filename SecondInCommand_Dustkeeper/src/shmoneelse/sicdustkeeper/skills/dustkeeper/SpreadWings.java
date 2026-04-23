package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import com.fs.starfarer.combat.entities.Ship;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class SpreadWings extends SCBaseSkillPlugin {
    int PDBONUS = 100;
    int FIREBONUS = 10;
    int SPEEDBONUS = 15;

    @Override
    public String getAffectsString() {
        return "all drone fighters";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+%s point defense weapon range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), String.valueOf(PDBONUS));
        tooltipMakerAPI.addPara("+%s%% weapon fire rate and non-missile ammunition recharge rate", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), String.valueOf(FIREBONUS));
        tooltipMakerAPI.addPara("+%s%% top speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), String.valueOf(SPEEDBONUS));

    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(SCData data, ShipAPI fighter, ShipAPI ship, String id) {
        boolean isAlwaysValid = ship.getVariant().hasHullMod("SKR_remote") || ship.getVariant().hasHullMod("rat_autonomous_bays");
        if (isAlwaysValid || fighter.getHullSpec().getMinCrew() == 0) {
            MutableShipStatsAPI stats = fighter.getMutableStats();
            stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PDBONUS);
            stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, PDBONUS);

            stats.getMaxSpeed().modifyPercent(id, SPEEDBONUS);
            stats.getAcceleration().modifyPercent(id, SPEEDBONUS * 2f);
            stats.getDeceleration().modifyPercent(id, SPEEDBONUS * 2f);

            stats.getBallisticRoFMult().modifyPercent(id, FIREBONUS);
            stats.getEnergyRoFMult().modifyPercent(id, FIREBONUS);
            stats.getMissileRoFMult().modifyPercent(id, FIREBONUS);

            stats.getEnergyAmmoRegenMult().modifyPercent(id, FIREBONUS);
            stats.getBallisticAmmoRegenMult().modifyPercent(id, FIREBONUS);
        }
    }
}
