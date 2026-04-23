package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.campaign.fleet.FleetMember;
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


public class NetworkDepth  extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "all automated ships with AI cores";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltip) {
        tooltip.addPara("AI cores have a reduced multiplier to their automated ship point cost", 0f,Misc.getHighlightColor(),Misc.getHighlightColor());
        tooltip.addPara("+10%% deployment costs for ships with AI cores", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
        /*tooltip.addSpacer(10f);
        tooltip.addPara("" +
                        "The increase in deployment points depends on the installed AI cores - " +
                        "%s for an Alpha Core, " +
                        "%s for an Beta Core, " +
                        "%s for a Gamma Core. Does not apply to automated ships that do not require the automated ships skill.", 0f,
                Misc.getGrayColor(), Misc.getHighlightColor(), "18%", "12%", "6%");*/
        tooltip.addSpacer(10f);
        tooltip.addPara("" +
                        "The new multiplier is equal to %s, or " +
                        "%s for an Alpha Core, " +
                        "%s for an Beta Core, " +
                        "%s for a Gamma Core. This skill does not affect automated ships that do not require the automated ships skill.", 0f,
                Misc.getGrayColor(), Misc.getHighlightColor(), "1 + (AI core multiplier - 1) / 3","2x", "1.66x", "1.33x");
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        if(!Misc.isAutomated(stats)) return;
        if(variant.getHullSpec().hasTag(Tags.TAG_AUTOMATED_NO_PENALTY)) return; // We don't need to bother if the ship doesn't have AI mults anyway

        if(stats.getFleetMember() == null) return;
        if(stats.getFleetMember().getCaptain() == null) return;

        if(!stats.getFleetMember().getCaptain().isAICore()) return;

        float getmult = stats.getFleetMember().getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT);
        float mult = 1f; // Default values if we're under 1 for an AI multiplier - no change
        //float dpmult = 0f;
        if(getmult >= 1f) { // Correcting for an issue with Adaptive Tactical Cores
            // They have a mult of 0f, and 1f / 0f means you start getting near floatmax due to how floating points work. This just sanity checks that the AI mult is non-zero.
            float cmult = stats.getFleetMember().getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT);
            mult = (1f + (cmult - 1f) / 3f) / cmult;
            //dpmult = stats.getFleetMember().getCaptain().getMemoryWithoutUpdate().getFloat(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT) - 1f; // Remove the base cost
            //dpmult *= 6f;
        }

        stats.getDynamic().getStat("sc_auto_points_mult").modifyMult("network_depth", mult);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyPercent(id, 10f);
    }

    @Override
    public void onDeactivation(SCData data)
    {
        if(data.isNPC()) return;

        List<FleetMemberAPI> fleet = data.getFleet().getFleetData().getMembersListCopy();
        for(FleetMemberAPI curr : fleet)
            curr.getStats().getDynamic().getMod("sc_auto_points_mult").unmodify("network_depth");
    }
}
