package progsmod.data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import progsmod.data.campaign.rulecmd.delegates.ManageSMods;
import progsmod.data.campaign.rulecmd.delegates.SelectShip;
import progsmod.data.campaign.rulecmd.ui.Button;
import progsmod.data.campaign.rulecmd.ui.HullModButton;
import progsmod.data.campaign.rulecmd.ui.LabelWithVariables;
import progsmod.data.campaign.rulecmd.ui.PanelCreator;
import progsmod.data.campaign.rulecmd.ui.PanelCreator.PanelCreatorData;
import progsmod.data.campaign.rulecmd.ui.plugins.BuildInSelector;
import progsmod.data.campaign.rulecmd.ui.plugins.Updatable;
import progsmod.data.campaign.rulecmd.util.HullModButtonData;
import progsmod.data.campaign.rulecmd.util.XPHelper;
import progsmod.util.Action;
import progsmod.util.RecentBuildInTracker;
import progsmod.util.SModUtils;

import java.awt.*;
import java.util.List;
import java.util.*;


public class PSM_BuildInHullMod {

    public static boolean shouldRecreateShipPanel = true;
    public static boolean fromLunaButton = false;

    // returnBuiltIn = true -> returns list of built in S-mods
    // returnBuiltIn = false -> returns list of hullmods that can be built in (including ones that are
    // enhanceOnly)
    public static List<HullModButtonData> listHullMods(FleetMemberAPI ship, ShipVariantAPI selectedVariant,
            boolean returnBuiltIn) {
        Collection<String> enhancedAlready = selectedVariant.getSModdedBuiltIns();
        List<String> ids = new ArrayList<>();
        int firstIndexToBeCounted = 0;
        if (returnBuiltIn) {
            ids = new ArrayList<>(selectedVariant.getSMods());
        } else {
            if (selectedVariant.getHullSpec() != null) {
                for (String id : selectedVariant.getHullSpec().getBuiltInMods()) {
                    HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(id);
                    HullModEffect effect = hullMod.getEffect();
                    if (effect.hasSModEffect() && !effect.isSModEffectAPenalty() &&
                        !enhancedAlready.contains(id)) {
                        ids.add(id);
                    }
                }
            }

            firstIndexToBeCounted = ids.size();

            for (String id : selectedVariant.getNonBuiltInHullmods()) {
                HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(id);
                if (!hullMod.isHidden() && !hullMod.isHiddenEverywhere()) {
                    ids.add(id);
                }
            }
        }

        List<HullModButtonData> buttonData = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(id);
            int cost = SModUtils.getBuildInCost(hullMod, selectedVariant.getHullSize(),
                    ship.getUnmodifiedDeploymentPointsCost());
            String description = cost + " XP";
            boolean isEnhanceOnly = i < firstIndexToBeCounted;
            if (returnBuiltIn) {
                cost = (int) (cost * SModUtils.Constants.XP_REFUND_FACTOR);
                if (cost != 0) {
                    description = "Refunds " + cost + " XP";
                } else {
                    description = "";
                }
            }
            buttonData.add(
                    new HullModButtonData(
                            hullMod.getId(),
                            hullMod.getDisplayName() + (isEnhanceOnly ? "*" : ""),
                            hullMod.getSpriteName(),
                            description,
                            hullMod.getDescription(selectedVariant.getHullSize()),
                            hullMod.getEffect(),
                            selectedVariant.getHullSize(),
                            cost,
                            isEnhanceOnly,
                            returnBuiltIn)
            );
        }
        return buttonData;
    }

    public static void createPanel(final FleetMemberAPI fleetMember, final ShipVariantAPI selectedVariant,
                                   final InteractionDialogAPI dialog, final float shipScrollPanelY,
                                   final CustomDialogDelegate.CustomDialogCallback originalCallback, final Action doOnConfirm) {
        final String titleString = "Choose hull mods to build in";
        final float titleHeight = 50f;

        // First generate HullModButtonData for all built in S-mods (so they can be removed)
        final List<HullModButtonData> buttonData = listHullMods(fleetMember, selectedVariant, true);
        // Then add the HullModButtonData for all not built hullmods (so they can be added)
        buttonData.addAll(listHullMods(fleetMember, selectedVariant, false));

        final BuildInSelector plugin = new BuildInSelector();
        // Convenient way of synchronizing state across the various panels on the screen
        final SelectorContainer container = new SelectorContainer();
        final XPHelper xpHelper = new XPHelper();
        final CustomDialogDelegate.CustomDialogCallback[] callback_ =
                new CustomDialogDelegate.CustomDialogCallback[1];
        final float dWidth = ManageSMods.getWidth();
        final float dHeight = ManageSMods.getHeight();
        final float shipInfoPanelPad = 10f;
        dialog.showCustomDialog(dWidth, dHeight,
                new ManageSMods() {
                    @Override
                    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                        // Set the callback function, so customDialogConfirm can call it and create the
                        // ship list again
                        // Without calling the callback, it's not possible to create the ship list in
                        // customDialogConfirm (I think)
                        callback_[0] = callback;
                        shouldRecreateShipPanel = true;

                        // Initialize the S-mod and XP counters at the top. These track the "pending"
                        // values that will be
                        //   set when the player hits confirm.
                        LabelWithVariables<Integer> countLabel =
                                PanelCreator.createLabelWithVariables(panel, "S-mods: %s/%s", Color.WHITE,
                                        30f, Alignment.LMID, selectedVariant.getSMods().size(),
                                        SModUtils.getMaxSMods(fleetMember)).created;
                        int reserveXP = (int) SModUtils.getReserveXP(
                                fleetMember.getHullSpec().getBaseHullId());
                        String xpLabelString = reserveXP > 0 ? "XP: %s  (%s shared reserves)" : "XP: %s";
                        LabelWithVariables<Integer> xpLabel =
                                PanelCreator.createLabelWithVariables(panel, xpLabelString, Color.WHITE, 30f,
                                        Alignment.RMID,
                                        (int) SModUtils.getXP(fleetMember.getId()), reserveXP).created;
                        xpHelper.init(xpLabel);
                        container.init(xpHelper, countLabel);

                        // Static text at the top of the screen. The function positions it at the top of panel
                        CustomPanelAPI titlePanel = PanelCreator.createTitle(panel, titleString,
                                titleHeight).panel;

                        // Panel with the ship picture and name - (class)
                        CustomPanelAPI shipInfoPanel = PanelCreator.createShipInfoPanel(panel, fleetMember,
                                selectedVariant, callback, shipScrollPanelY);
                        shipInfoPanel.getPosition().belowMid(titlePanel, shipInfoPanelPad);

                        // Button for increasing the S-mod limit. Only created if the setting is enabled
                        CustomPanelAPI lastPanel = shipInfoPanel;
                        float augmentPanelHeight = 0f;
                        if (SModUtils.Constants.ALLOW_INCREASE_SMOD_LIMIT) {
                            float augmentPanelPad = 5f;
                            CustomPanelAPI augmentPanel = PanelCreator.createAugmentPanel(panel, fleetMember, selectedVariant,
                                    container, originalCallback == null ? callback : originalCallback, doOnConfirm);
                            augmentPanel.getPosition().belowMid(shipInfoPanel, augmentPanelPad);
                            lastPanel = augmentPanel;
                            augmentPanelHeight = augmentPanel.getPosition().getHeight() + augmentPanelPad;
                        }

                        // A scrollable list of hullmods. First the mods that are built in (for removal),
                        // then the mods
                        //  that are not built in (so they can be selected for building in)
                        float hullModPanelHeight =
                                panel.getPosition().getHeight() - shipInfoPanel.getPosition().getHeight()
                                - titlePanel.getPosition().getHeight() - augmentPanelHeight -
                                shipInfoPanelPad - 5f;


                        PanelCreatorData<List<HullModButton>> createdButtonsData =
                                PanelCreator.createHullModPanel(panel, hullModPanelHeight, dWidth, buttonData,
                                        plugin);
                        createdButtonsData.panel.getPosition().belowMid(lastPanel, 5f);

                        // Show recent button
                        Button showAllButton = PanelCreator.createButton(panel, "Show recent", 100f, 25f, 10f,
                                panel.getPosition().getHeight() + 6f).created;

                        plugin.init(this, createdButtonsData, showAllButton, fleetMember, selectedVariant,
                                container);
                    }

                    @Override
                    public void customDialogCancel() {
                        SModUtils.forceUpdater = null;
                        recreateShipPanel();
                    }

                    @Override
                    public void customDialogConfirm() {
                        boolean addedAtLeastOne = false;
                        boolean removedAtLeastOne = false;

                        String fmId = fleetMember.getId();

                        xpHelper.useReserveXPIfNeeded(fleetMember);

                        List<String> addToRecent = new ArrayList<>();
                        for (HullModButton button : plugin.getSelected()) {
                            if (button.data.isBuiltIn) {
                                selectedVariant.removePermaMod(button.data.id);
                                SModUtils.giveXP(fleetMember, button.data.cost);
                                dialog.getTextPanel().addPara("Removed " + button.data.name).setHighlight(
                                        button.data.name);
                                removedAtLeastOne = true;
                            } else {
                                if (SModUtils.spendXP(fmId, button.data.cost)) {
                                    String hullModName = Global.getSettings().getHullModSpec(
                                            button.data.id).getDisplayName();

                                    if (button.data.isEnhanceOnly) {
                                        selectedVariant.getSModdedBuiltIns().add(button.data.id);
                                        dialog.getTextPanel().addPara("Enhanced " + hullModName)
                                                .setHighlight(hullModName);
                                    } else {
                                        selectedVariant.addPermaMod(button.data.id, true);
                                        dialog.getTextPanel().addPara("Built in " + hullModName)
                                                .setHighlight(hullModName);
                                        addToRecent.add(button.data.id);

                                    }
                                    addedAtLeastOne = true;
                                }
                            }
                        }

                        // Add in reverse order
                        for (int i = addToRecent.size() - 1; i >= 0; i--) {
                            RecentBuildInTracker.addToRecentlyBuiltIn(addToRecent.get(i));
                        }

                        String sound = "ui_acquired_hullmod";
                        if (addedAtLeastOne || removedAtLeastOne) {/**/
                            Global.getSoundPlayer().playUISound(sound, 1f, 1f);
                            SModUtils.displayXP(dialog, fleetMember);
                            SModUtils.addTrackerHullMod(selectedVariant);
                        }

                        SModUtils.forceUpdater = null;
                        recreateShipPanel();

                        if (doOnConfirm != null) {
                            doOnConfirm.perform();
                        }
                    }

                    @Override
                    public String getCancelText() {
                        return "Cancel";
                    }

                    @Override
                    public String getConfirmText() {
                        return "Confirm";
                    }

                    @Override
                    public CustomUIPanelPlugin getCustomPanelPlugin() {
                        return plugin;
                    }

                    @Override
                    public boolean hasCancelButton() {
                        return true;
                    }

                    @Override
                    public void showRecentPressed(CustomPanelAPI panel, TooltipMakerAPI tooltipMaker) {
                        List<HullModButtonData> newButtonData = generateNewButtonData(selectedVariant, fleetMember);

                        if (plugin.needBuildInText) {
                            plugin.needBuildInText = false;
                            PanelCreator.addSelectHullModsText(tooltipMaker);
                        }

                        PanelCreatorData<List<HullModButton>> newButtons =
                                PanelCreator.addToHullModButtonList(panel, tooltipMaker, newButtonData, 45f,
                                        10f, 0, false);

                        tooltipMaker.getExternalScroller().setYOffset(
                                Math.max(0f, tooltipMaker.getHeightSoFar() - panel.getPosition().getHeight()));
                        plugin.addNewItems(newButtons.created);
                    }

                    private void recreateShipPanel() {
                        // Don't recreate ship selection dialog if switching to module selection dialog
                        if (shouldRecreateShipPanel && !fromLunaButton) {
                            shouldRecreateShipPanel = false;
                            callback_[0].dismissCustomDialog(1);
                            dialog.showCustomDialog(SelectShip.getWidth(), SelectShip.getHeight(),
                                    new SelectShip(dialog, shipScrollPanelY));
                        }
                    }
                }
        );
    }

    // Contains a list of things that want to be updated whenever a ship's tentative XP changes
    public static class SelectorContainer {
        public List<Updatable> updateList = new ArrayList<>();
        public XPHelper xpHelper;
        public LabelWithVariables<Integer> countLabel;

        public void init(XPHelper xp, LabelWithVariables<Integer> countLabel) {
            this.xpHelper = xp;
            this.countLabel = countLabel;
        }

        public void register(Updatable item) {
            updateList.add(item);
        }

        public void updateAll() {
            for (Updatable updatable : updateList) {
                updatable.update();
            }
        }
    }


    static List<HullModButtonData> generateNewButtonData(ShipVariantAPI selectedVariant, FleetMemberAPI fleetMember) {
        List<HullModButtonData> newButtonData = new ArrayList<>();
        List<String> recentlyBuiltIn = new ArrayList<>(RecentBuildInTracker.getRecentlyBuiltIn());
        Collections.sort(recentlyBuiltIn, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                HullModSpecAPI spec1 = Global.getSettings().getHullModSpec(s1);
                String name1 = spec1 == null ? "zzzunknown" : spec1.getDisplayName();
                HullModSpecAPI spec2 = Global.getSettings().getHullModSpec(s2);
                String name2 = spec2 == null ? "zzzunknown" : spec2.getDisplayName();
                return name1.compareTo(name2);
            }
        });

        for (String id : recentlyBuiltIn) {
            if (selectedVariant.hasHullMod(id)) {
                continue;
            }
            HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(id);
            int cost = SModUtils.getBuildInCost(hullMod, selectedVariant.getHullSize(), fleetMember.getUnmodifiedDeploymentPointsCost());
            newButtonData.add(
                    new HullModButtonData(
                            hullMod.getId(),
                            hullMod.getDisplayName(),
                            hullMod.getSpriteName(),
                            cost + " XP",
                            hullMod.getDescription(selectedVariant.getHullSize()),
                            hullMod.getEffect(),
                            selectedVariant.getHullSize(),
                            cost,
                            false)
            );
        }
        return newButtonData;
    }
}
