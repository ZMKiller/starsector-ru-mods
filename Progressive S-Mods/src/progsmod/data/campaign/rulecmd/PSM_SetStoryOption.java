package progsmod.data.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI.OptionTooltipCreator;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import progsmod.util.SModUtils;


/** ProgSModSetStoryOption [fleetMember] [n] [r] [x] makes a story option costing n story points and x ship XP,
 *   and giving m% bonus character XP.
 *  Uses default sound. Modified from SetStoryOption in the base game. */
public class PSM_SetStoryOption extends SetStoryOption {

    private static class ProgSModStoryPointActionDelegate extends BaseOptionStoryPointActionDelegate {

        private final int reqShipXP;
        private final FleetMemberAPI fleetMember;

        ProgSModStoryPointActionDelegate(InteractionDialogAPI dialog, FleetMemberAPI fleetMember, String optionId, int reqShipXP, int cost, int bonusPercent) {
            super(dialog, new StoryOptionParams(optionId, cost, "", "general", null));
            this.reqShipXP = reqShipXP;
            this.fleetMember = fleetMember;
            bonusXPFraction = (float) bonusPercent / 100f;
        }

        String getOptionId() {
            return (String) optionId;
        }

        int getCost() {
            return numPoints;
        }

        int getXPCost() {
            return reqShipXP;
        }

        FleetMemberAPI getFleetMember() {
            return fleetMember;
        }

        String getSoundId() {
            return soundId;
        }

        @Override
        protected void addActionCostSection(TooltipMakerAPI info) {
            super.addActionCostSection(info);
            int xp = (int) SModUtils.getXP(fleetMember.getId());
            if (SModUtils.Constants.DEPLOYMENT_COST_PENALTY >= 0f) {
                info.addPara(
                        "Ships above their standard S-mod limit incur a stacking %s deployment cost increase for each additional S-mod.",
                        10f,
                        Misc.getNegativeHighlightColor(),
                        Misc.getHighlightColor(),
                        (int) (100f * SModUtils.Constants.DEPLOYMENT_COST_PENALTY) + "%");
            }
            if (xp < reqShipXP) {
                info.addPara("Ship has %s XP", 10f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getFormat().format(xp));
            }
        }
    }


    @Override
    public boolean execute(String ruleId, final InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;

        FleetMemberAPI fleetMember = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(params.get(0).string);
        String optionId = params.get(1).string;
        int numPoints = params.get(2).getInt(memoryMap);
        int bonusXPPercent = params.get(3).getInt(memoryMap);
        int reqShipXP = params.get(4).getInt(memoryMap);

        return set(dialog, fleetMember, numPoints, reqShipXP, optionId, bonusXPPercent);
    }

    private boolean set(InteractionDialogAPI dialog, FleetMemberAPI fleetMember, int numPoints, int reqShipXP, String optionId, int bonusXPPercent) {
        return set(dialog, new ProgSModStoryPointActionDelegate(dialog, fleetMember, optionId, reqShipXP, numPoints, bonusXPPercent));
    }

    private ProgSModStoryPointActionDelegate delegate = null;

    private boolean set(InteractionDialogAPI dialog,
            ProgSModStoryPointActionDelegate del) {
        delegate = del;
        int cost = delegate.getCost();
        final int xpCost = delegate.getXPCost();
        final FleetMemberAPI fleetMember = del.getFleetMember();
        String optionId = delegate.getOptionId();
        float bonusXPFraction = delegate.getBonusXPFraction();
        dialog.makeStoryOption(optionId, cost, bonusXPFraction, delegate.getSoundId());
        int nSModsLimit = SModUtils.getMaxSMods(fleetMember);
        dialog.getOptionPanel().setOptionText(
            String.format("Increase this ship's built-in hull mod limit from %s to %s [%s SP; %s ship XP; %s%% bonus character XP]",
                nSModsLimit,
                nSModsLimit + 1,
                cost,
                Misc.getFormat().format(xpCost),
                (int) (100 * bonusXPFraction)
            ), 
        optionId);
        
        if (cost > Global.getSector().getPlayerStats().getStoryPoints()
            || xpCost > SModUtils.getXP(fleetMember.getId())) {
            dialog.getOptionPanel().setEnabled(optionId, false);
        }
        
        dialog.getOptionPanel().addOptionTooltipAppender(optionId, new OptionTooltipCreator() {
            public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                float opad = 10f;
                float initPad = 0f;
                if (hadOtherText) initPad = opad;
                tooltip.addStoryPointUseInfo(initPad, delegate.getCost(), delegate.getBonusXPFraction(), true);
                int xp = (int) SModUtils.getXP(fleetMember.getId());
                if (xp < xpCost) {
                    tooltip.addPara("Ship has %s XP", 10f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getFormat().format(xp));
                }
                int sp = Global.getSector().getPlayerStats().getStoryPoints();
                String points = "points";
                if (sp == 1) points = "point";
                tooltip.addPara("You have %s " + Misc.STORY + " " + points + ".", opad, 
                        Misc.getStoryOptionColor(), "" + sp);
            }
        });
        
        dialog.getOptionPanel().addOptionConfirmation(optionId, delegate);
        
        return true;
    }
}