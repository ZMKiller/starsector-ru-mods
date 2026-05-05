package progsmod.data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.util.Misc.Token;
import progsmod.data.campaign.rulecmd.ui.HullModButton;
import progsmod.data.campaign.rulecmd.ui.LabelWithVariables;
import progsmod.data.campaign.rulecmd.ui.PanelCreator;
import progsmod.data.campaign.rulecmd.ui.plugins.RemoveSelector;
import progsmod.data.campaign.rulecmd.util.HullModButtonData;
import progsmod.util.SModUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** ProgSModRemove [fleetMember] [selectedVariant] [trigger] -- shows the built-in hull mods for 
 *  the module of [fleetMember] whose variant is [selectedVariant].
 *  Remove the selected built-in hull mods. 
 *  Fire [trigger] upon confirmation. */
@SuppressWarnings("unused")
public class PSM_RemoveSMod extends BaseCommandPlugin {

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.isEmpty()) {
            return false;
        }

        final String titleString = "Choose built-in hull mods to remove";
        final float titleHeight = 50f;
        final List<HullModButtonData> buttonData = new ArrayList<>();
        final FleetMemberAPI fleetMember = (FleetMemberAPI) memoryMap.get(MemKeys.LOCAL).get(params.get(0).string);
        final ShipVariantAPI selectedVariant = (ShipVariantAPI) memoryMap.get(MemKeys.LOCAL).get(params.get(1).string);
        
        Collection<String> builtInIds = selectedVariant.getSMods();
        for (String id : builtInIds) {
            HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(id);
            int amt = (int) (
                SModUtils.Constants.XP_REFUND_FACTOR 
                * SModUtils.getBuildInCost(hullMod, selectedVariant.getHullSize(), fleetMember.getUnmodifiedDeploymentPointsCost()));
            buttonData.add(
                new HullModButtonData(
                    hullMod.getId(),
                    hullMod.getDisplayName(),
                    hullMod.getSpriteName(),
                    String.format("Refunds %s XP", amt),
                    hullMod.getDescription(selectedVariant.getHullSize()),
                    hullMod.getEffect(),
                    selectedVariant.getHullSize(),
                    amt,
                    false)
                );
        }

        final RemoveSelector plugin = new RemoveSelector();
        dialog.showCustomDialog(500f, 500f,
            new CustomDialogDelegate() {

                @Override
                public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                    PanelCreator.createTitle(panel, titleString, titleHeight);
                    List<HullModButton> buttons = 
                        PanelCreator.createHullModButtonList(panel, buttonData, 45f, 10f, titleHeight, true).created;
                    LabelWithVariables<Integer> xpLabel = 
                        PanelCreator.createLabelWithVariables(panel, "XP: %s", Color.WHITE, 30f, Alignment.MID, (int) SModUtils.getXP(fleetMember.getId())).created;
                    plugin.init(buttons, xpLabel);
                    for (int i = 0; i < buttonData.size(); i++) {
                        if (!SModUtils.canModifyHullMod(Global.getSettings().getHullModSpec(buttonData.get(i).id), dialog.getInteractionTarget())) {
                            plugin.disableItem(i, "Requires docking at a spaceport or orbital station", true);
                        }
                    }
                }

                @Override
                public void customDialogCancel() {}

                @Override
                public void customDialogConfirm() {
                    boolean removedAtLeastOne = false;
                    
                    float xpGained = 0;

                    for (int i : plugin.getSelectedIndices()) {
                        HullModButtonData data = buttonData.get(i);
                        selectedVariant.removePermaMod(data.id);
                        xpGained += data.cost;
                        dialog.getTextPanel().addPara("Removed " + data.name).setHighlight(data.name);
                        removedAtLeastOne = true;
                    }

                    if (removedAtLeastOne) {
                        Global.getSoundPlayer().playUISound("ui_objective_constructed", 1f, 1f);
                        SModUtils.giveXP(fleetMember, xpGained);
                        SModUtils.displayXP(dialog, fleetMember);
                        FireAll.fire(ruleId, dialog, memoryMap, params.get(2).getString(memoryMap));
                    }
                }

                @Override
                public String getCancelText() {
                    return "Cancel";
                }

                @Override
                public String getConfirmText() {
                    return "Remove";
                }

                @Override
                public CustomUIPanelPlugin getCustomPanelPlugin() {
                    return plugin;
                }

                @Override
                public boolean hasCancelButton() {
                    return true;
                }

            }
        );

        return true;
    }
}
