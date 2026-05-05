package progsmod.data.campaign.rulecmd.ui.plugins;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.CampaignUIAPI.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.*;
import com.fs.starfarer.api.loading.*;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.*;
import progsmod.data.campaign.rulecmd.PSM_BuildInHullMod.*;
import progsmod.data.campaign.rulecmd.delegates.*;
import progsmod.data.campaign.rulecmd.ui.*;
import progsmod.data.campaign.rulecmd.ui.PanelCreator.*;
import progsmod.data.campaign.rulecmd.util.*;
import progsmod.util.*;

import java.util.*;

public class BuildInSelector extends Selector<HullModButton> {

    // 1 variable: XP
    private XPHelper xpHelper;
    // 2 variables: # selected, max allowed
    private LabelWithVariables<Integer> countLabel;
    private Button showRecentButton;
    private FleetMemberAPI fleetMember;
    private ShipVariantAPI checkerVariant;
    private ShipVariantAPI originalVariant;

    private ManageSMods delegate;
    private TooltipMakerAPI tooltipMaker;
    private CustomPanelAPI panel;

    private SelectorContainer container;

    public boolean needRemoveText = true;
    public boolean needEnhanceText = true;
    public boolean needBuildInText = true;

    public void init(ManageSMods delegate, PanelCreatorData<List<HullModButton>> data,
                     Button showRecentButton, FleetMemberAPI fleetMember, ShipVariantAPI variant,
                     SelectorContainer container) {
        super.init(data.created);
        this.delegate = delegate;
        this.panel = data.panel;
        this.tooltipMaker = data.tooltipMaker;
        this.xpHelper = container.xpHelper;
        this.countLabel = container.countLabel;
        this.showRecentButton = showRecentButton;
        this.originalVariant = variant;
        checkerVariant = variant.clone();
        this.fleetMember = fleetMember;
        this.container = container;
        container.register(this);
        SModUtils.forceUpdater = new SModUtils.ForceUpdater() {
            @Override
            public void addXP(int xp) {
                if (xp >= 0) {
                    xpHelper.increaseXPLabel(xp);
                } else {
                    int minXP = xpHelper.getXP() + xpHelper.getReserveXP();
                    xp = Math.min(minXP, -xp);
                    xpHelper.decreaseXPLabel(xp);
                }
                updateItems();
            }

            @Override
            public void addReserveXP(int xp) {
                if (xp >= 0) {
                    xpHelper.addReserveXP(xp);
                } else {
                    xp = Math.min(xpHelper.getReserveXP(), -xp);
                    xpHelper.reduceReserveXP(xp);
                }
                updateItems();
            }

            @Override
            public void resetXP() {
                // Deselect all buttons in reverse order, so S-mod removal is deselected last
                for (int i = items.size() - 1; i >= 0; i--) {
                    if (items.get(i).isSelected()) {
                        items.get(i).deselect();
                    }
                }
                xpHelper.clear();
                updateItems();
            }
        };
        updateItems();
    }

    public void addNewItems(List<HullModButton> items) {
        this.items.addAll(items);
        updateItems();
    }

    public void updateItems() {
        container.updateAll();
    }

    @Override
    public void update() {
        // Disable all of the unapplicable entries
        BitSet unapplicable = disableUnapplicable();
        int count = countLabel.getVar(0);
        int maxCount = countLabel.getVar(1);
        for (int i = 0; i < items.size(); i++) {
            HullModButton button = items.get(i);
            // TODO figure out how this works with removal
            //  probably fine since there is no logic in RemoveSelector
            if (unapplicable.get(i)) {
                continue;
            }

            if (button.data.isBuiltIn) {
                // If the S-mod isn't selected, check if it can be removed
                if (!button.isSelected()) {
                    if (SModUtils.canModifyHullMod(button.data)) {
                        enable(i, button.getDefaultDescription());
                    } else {
                        disable(i, "Requires docking at a spaceport or orbital station", true);
                    }
                    continue;
                }

                // Undoing this refund would result in negative pending xp
                if (xpHelper.canAfford(button.data.cost) == XPHelper.Affordable.NO) {
                    disable(i, button.getDefaultDescription(), true, true);
                }
                // Undoing this refund would result in negative S-mod slots
                if (count >= maxCount) {
                    disable(i, button.getDefaultDescription(), false, true);
                }
                // Otherwise it's possible to undo the refund
                else {
                    enable(i, button.getDefaultDescription());
                }
            } else {
                // Costs too much
                if (xpHelper.canAfford(button.data.cost) == XPHelper.Affordable.NO) {
                    disable(i, button.getDefaultDescription(), true);
                }
                // Already at limit
                else if (count >= maxCount && !button.data.isEnhanceOnly) {
                    disable(i, button.getDefaultDescription(), false);
                }
                // No reason to be disabled; enable it
                else {
                    enable(i, button.getDefaultDescription());
                }
            }
        }
    }

    /**
     * Disable an item, but only if it isn't already selected.
     */
    private void disable(int index, String reason, boolean highlight, boolean alwaysDisable) {
        HullModButton item = items.get(index);
        // Ignore already selected items
        if (item.isSelected() && !alwaysDisable) {
            return;
        }
        item.disable(reason, highlight);
    }

    private void disable(int index, String reason, boolean highlight) {
        disable(index, reason, highlight, false);
    }

    /**
     * Enable an item.
     */
    private void enable(int index, String reason) {
        items.get(index).enable(reason);
    }

    /**
     * Returns a set of indices of buttons that were disabled by this
     * function.
     */
    private BitSet disableUnapplicable() {
        boolean checkedEntriesChanged = true;
        BitSet disabledIndices = new BitSet();
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        SectorEntityToken interactionTarget = dialog == null ? null : dialog.getInteractionTarget();
        while (checkedEntriesChanged) {
            ShipAPI checkerShip = TempShipMaker.makeShip(checkerVariant, fleetMember);
            // Since hull mods may have dependencies, some checked entries may
            // need to be unchecked.
            // Since dependencies can be chained, we need to do this in a loop.
            // (# of loops is bounded by # of checked entries as well as
            // longest hull mod dependency chain)
            checkedEntriesChanged = false;
            for (int i = 0; i < items.size(); i++) {
                HullModButton button = items.get(i);
                if (button.data.isEnhanceOnly) {
                    continue;
                }
                HullModSpecAPI hullMod = Global.getSettings().getHullModSpec(button.data.id);
                boolean shouldDisable = false;
                String disableText = null;
                if (!hullMod.getEffect().isApplicableToShip(checkerShip) && !button.data.isBuiltIn && !originalVariant.hasHullMod(button.data.id)) {
                    String reason = hullMod.getEffect().getUnapplicableReason(checkerShip);
                    // Can build in any number of logistics hull mods
                    // Don't use s-mods in the check ship as we want to be able to tell when
                    // incompatibilities arise
                    // via forcible removing of non s-mods
                    if (reason != null && !reason.startsWith("Maximum of 2 non-built-in \"Logistics\"")) {
                        disableText = SModUtils.shortenText(reason, button.description);
                        shouldDisable = true;
                    }

                } else if (interactionTarget != null && interactionTarget.getMarket() != null) {
                    String tradeModeString = interactionTarget.getMemory().getString("$tradeMode");
                    CoreUITradeMode tradeMode = tradeModeString == null ? CoreUITradeMode.NONE : CoreUITradeMode.valueOf(tradeModeString);
                    if (!originalVariant.hasHullMod(button.data.id) && !hullMod.getEffect().canBeAddedOrRemovedNow(checkerShip,
                            interactionTarget.getMarket(), tradeMode)) {
                        String reason = hullMod.getEffect().getCanNotBeInstalledNowReason(checkerShip,
                                interactionTarget.getMarket(), tradeMode);
                        shouldDisable = true;
                        if (reason != null && !button.data.isBuiltIn) {
                            // getCanNotBeInstalledNowReason() returns a weird message when trying to build in logistic
                            //  hullmods while not at a spaceport or station
                            reason = reason.replace("Can only be removed at", "Can only be built in at");
                        }
                        disableText = SModUtils.shortenText(reason, button.description);
                    }
                }
                if (hullMod.hasTag("no_build_in") && !SModUtils.Constants.IGNORE_NO_BUILD_IN) {
                    shouldDisable = true;
                    disableText = hullMod.getDisplayName() + " can't be built in";
                }
                if (shouldDisable) {
                    if (button.isSelected()) {
                        forceDeselect(i);
                        checkedEntriesChanged = true;
                    }
                    if (disableText == null) {
                        disableText = "Can't build in (no reason given, default message)";
                    }
                    disable(i, disableText, true);
                    disabledIndices.set(i);
                }
            }
        }
        return disabledIndices;
    }

    @Override
    public void advance(float amount) {
        // Check if the show all button has been pressed
        if (showRecentButton != null && showRecentButton.isSelected()) {
            showRecentButton.deselect();
            showRecentButton.disable();
            delegate.showRecentPressed(panel, tooltipMaker);
        }
    }

    @Override
    protected void onSelected(int index) {
        HullModButton hullModButton = items.get(index);
        int cost = hullModButton.data.cost;
        if (hullModButton.data.isBuiltIn) {
            xpHelper.increaseXPLabel(cost);
            // No need to check isEnhanceOnly because those aren't in the list in the first place
            countLabel.changeVar(0, countLabel.getVar(0) - 1);
            updateItems();
        } else {
            xpHelper.decreaseXPLabel(cost);
            if (!hullModButton.data.isEnhanceOnly) {
                countLabel.changeVar(0, countLabel.getVar(0) + 1);
            }
            String hullModId = items.get(index).data.id;
            checkerVariant.addMod(hullModId);
            if (testForDesync()) {
                forceDeselect(index);
                Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
                        "Can't build in due to custom hull mod incompatibility",
                        Misc.getNegativeHighlightColor());
            } else {
                updateItems();
            }
        }
    }

    /**
     * Tests if a ship made from checkerVariant still has all the hull mods that checkerVariant does.
     * If not, this likely means that a mod forcibly removed some other hull mod(s) due to mod
     * incompatibilities,
     * so adding this hull mod would not be safe.
     */
    private boolean testForDesync() {
        ShipVariantAPI cloneVariant = checkerVariant.clone();
        TempShipMaker.makeShip(cloneVariant, fleetMember);
        if (cloneVariant.hasHullMod("ML_incompatibleHullmodWarning")) {
            return true;
        }
        for (String id : checkerVariant.getHullMods()) {
            if (!cloneVariant.hasHullMod(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDeselected(int index) {
        HullModButton hullModButton = items.get(index);
        int cost = hullModButton.data.cost;
        if (hullModButton.data.isBuiltIn) {
            xpHelper.decreaseXPLabel(cost);
            // No need to check isEnhanceOnly because those aren't in the list in the first place
            countLabel.changeVar(0, countLabel.getVar(0) + 1);
        } else {
            xpHelper.increaseXPLabel(cost);
            if (!hullModButton.data.isEnhanceOnly) {
                countLabel.changeVar(0, countLabel.getVar(0) - 1);
            }
            String hullModId = items.get(index).data.id;
            // Don't remove hull mods that were already on the ship
            if (!originalVariant.hasHullMod(hullModId)) {
                checkerVariant.removeMod(hullModId);
            }
        }
        updateItems();
    }

    @Override
    protected void forceDeselect(int index) {
        super.forceDeselect(index);
        HullModButton button = items.get(index);
        xpHelper.increaseXPLabel(items.get(index).data.cost);
        if (!button.data.isEnhanceOnly) {
            countLabel.changeVar(0, countLabel.getVar(0) - 1);
        }
        String hullModId = items.get(index).data.id;
        // Don't remove hull mods that were already on the ship
        if (!originalVariant.hasHullMod(hullModId)) {
            checkerVariant.removeMod(hullModId);
        }
    }
}