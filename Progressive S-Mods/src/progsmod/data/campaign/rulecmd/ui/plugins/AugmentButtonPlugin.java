package progsmod.data.campaign.rulecmd.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import progsmod.data.campaign.rulecmd.PSM_BuildInHullMod.SelectorContainer;
import progsmod.data.campaign.rulecmd.util.XPHelper;
import progsmod.util.Action;
import progsmod.util.SModUtils;

import java.util.List;

public class AugmentButtonPlugin implements CustomUIPanelPlugin, Updatable {
    private final TooltipMakerAPI augmentElement;
    private final float width;
    public CustomPanelAPI augmentPanel;
    public ButtonAPI button;
    private int xpCost;
    private SModUtils.AugmentSPCost spCost;
    private FleetMemberAPI ship;
    private CustomPanelAPI parentPanel;
    private SelectorContainer container;
    private LabelAPI spInfoLabel;
    private CustomDialogDelegate.CustomDialogCallback callback;
    private ShipVariantAPI selectedVariant;
    private Action doOnConfirm;

    public AugmentButtonPlugin(CustomPanelAPI parentPanel, FleetMemberAPI ship, ShipVariantAPI selectedVariant, SelectorContainer container, CustomDialogDelegate.CustomDialogCallback callback, Action doOnConfirm) {
        container.register(this);
        this.parentPanel = parentPanel;
        this.ship = ship;
        this.container = container;
        this.callback = callback;
        this.selectedVariant = selectedVariant;
        this.doOnConfirm = doOnConfirm;
        width = parentPanel.getPosition().getWidth();
        float textHeight = 20f;
        float buttonHeight = 30f;
        float buttonWidth = width * 0.95f;
        float buttonListHorizontalPadding = 0.5f * (width - buttonWidth - 10f);
        float height = textHeight + buttonHeight;

        augmentPanel = parentPanel.createCustomPanel(width, height, this);

        augmentElement = augmentPanel.createUIElement(width, height, false);
        spInfoLabel = augmentElement.addPara("", 0f, Misc.getStoryBrightColor());

        String buttonKey = "increase_limit_button";
        String buttonText = buttonText();
        button = augmentElement.addButton(buttonText, buttonKey, Misc.getStoryOptionColor(), Misc.getStoryDarkColor(),
                Alignment.MID, CutStyle.ALL, buttonWidth, buttonHeight, 5f);

        update();
        augmentPanel.addUIElement(augmentElement).inTL(buttonListHorizontalPadding, 0);
        parentPanel.addComponent(augmentPanel);
    }

    private void updateSPInfoLabel() {
        String[] highlights = {Integer.toString(Global.getSector().getPlayerStats().getStoryPoints()), "", ""};
        String text = String.format("You have %s story points.", highlights[0]);
        if (spCost.bonusXP > 0f) {
            highlights[1] = Integer.toString(spCost.spCost);
            highlights[2] = (int) (spCost.bonusXP * 100) + "% bonus player XP";
            text += String.format(" The cost is rounded up to to %s, granting %s.", highlights[1], highlights[2]);
        }
        spInfoLabel.setText(text);
        spInfoLabel.setHighlight(highlights);
    }

    private void updateButtonTooltip() {
        String infoString = "Increasing a ship's S-mod limit with story points grants no bonus experience";
        if (SModUtils.Constants.DEPLOYMENT_COST_PENALTY >= 0f) {
            infoString += " and increases its deployment cost by " +
                          (int) (SModUtils.Constants.DEPLOYMENT_COST_PENALTY * 100) + "%% for each additional S-mod.";
        } else {
            infoString += ".";
        }

        String xpError = "Ship only has " + (container.xpHelper.getXP() + container.xpHelper.getReserveXP()) +
                         " of the required " + xpCost + " XP";
        String spError =
                "You only have " + Global.getSector().getPlayerStats().getStoryPoints() + " of the " + "required " +
                spCost + " SP";
        if (spCost.bonusXP > 0f) {
            spError += " (rounded up to " + spCost.spCost + ")";
        }

        final String[] highlights = {xpError, spError};
        if (!enoughXP()) {
            infoString += "\n\n" + xpError;
        }
        if (!enoughSP()) {
            infoString += "\n\n" + spError;
        }

        final String finalInfoString = infoString;
        augmentElement.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return width;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara(finalInfoString, 0f, Misc.getNegativeHighlightColor(), highlights);
            }
        }, button, TooltipMakerAPI.TooltipLocation.BELOW);

    }

    private String buttonText() {
        return String.format("Increase this ship's S-mod limit from %s to %s  [%s SP; %s ship XP]",
                SModUtils.getMaxSMods(ship), SModUtils.getMaxSMods(ship) + 1, spCost, xpCost);
    }

    public boolean buttonEnabled() {
        // If augmenting can only be afforded with the XP from an S-mod removal, it is very messy to implement.
        // Better to just disable the button as it's not useful anyway
        boolean enoughXPWithoutSModRemoval = enoughXP() && SModUtils.enoughXP(ship.getId(), xpCost - SModUtils.getReserveXP(ship));
        return enoughSP() && enoughXPWithoutSModRemoval;
    }

    private boolean enoughXP() {
        return container.xpHelper.canAfford(xpCost) != XPHelper.Affordable.NO;
    }

    private boolean enoughSP() {
        return spCost.spCost <= Global.getSector().getPlayerStats().getStoryPoints();
    }

    public void update() {
        xpCost = SModUtils.getAugmentXPCost(ship);
        spCost = SModUtils.getAugmentSPCost(ship);
        button.setEnabled(buttonEnabled());
        updateSPInfoLabel();
        updateButtonTooltip();
        button.setText(buttonText());
    }

    public void doAugment() {
        if (buttonEnabled()) {
            Global.getSector().getPlayerStats().spendStoryPoints(spCost.spCost, true,
                    Global.getSector().getCampaignUI().getCurrentInteractionDialog().getTextPanel(), true,
                    spCost.bonusXP, "");
            Global.getSoundPlayer().playUISound("ui_char_spent_story_point_combat", 1f, 1f);
            // xpLabel is the "tentative" value shared across all three separate panels (augment button,
            //   remove section and buildin section)
            container.xpHelper.decreaseXPLabel(xpCost);
            // Use reserve XP if required
            container.xpHelper.useReserveXPIfNeeded(ship);
            SModUtils.incrementSModLimit(ship);
            SModUtils.addTrackerHullMod(selectedVariant);
            container.countLabel.changeVar(1, SModUtils.getMaxSMods(ship));
            container.updateAll();

            if (doOnConfirm != null) {
                doOnConfirm.perform();
            }
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (buttonEnabled()) {
            // Dismissing an arbitrary callback allows for stacking custom dialogs
            // This looks strange but dialog.showCustomDialog does nothing otherwise
            callback.dismissCustomDialog(1);
            InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
            float height = 50f;
            if (SModUtils.Constants.DEPLOYMENT_COST_PENALTY > 0f) {
                height += 30f;
            }
            // Should look like the vanilla story point confirmation dialog
            dialog.showCustomDialog(500f, height, new BaseCustomDialogDelegate() {
                @Override
                public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                    TooltipMakerAPI element = panel.createUIElement(panel.getPosition().getWidth(),
                            panel.getPosition().getHeight(), false);
                    // This is the font used by the vanilla dialog, will need to adjust panel size if it is used
                    // element.setParaFont("graphics/fonts/insignia21LTaa.fnt");

                    String[] highlights = {
                            spCost.spCost + " story point" + (spCost.spCost > 1 ? "s" : ""),
                            (int) (spCost.bonusXP * 100) + "%"};
                    String infoString = String.format(
                            "Increasing this ship's S-mod limit from %s to %s requires %s and grants %s bonus player " +
                            "experience.", SModUtils.getMaxSMods(ship), SModUtils.getMaxSMods(ship) + 1, highlights[0],
                            highlights[1]);
                    element.addPara(infoString.replace("%", "%%"), 0f, Misc.getStoryBrightColor(), highlights);

                    if (SModUtils.Constants.DEPLOYMENT_COST_PENALTY > 0f) {
                        String penalty = (int) (SModUtils.Constants.DEPLOYMENT_COST_PENALTY * 100) + "%";
                        element.addPara(String.format("\nIt will also increase its base deployment cost by %s " +
                                                      "once the S-mod is installed.", penalty).replace("%", "%%"), 0f,
                                Misc.getNegativeHighlightColor(), penalty);
                    }

                    CustomPanelAPI spCountPanel = panel.createCustomPanel(panel.getPosition().getWidth(), 20f, null);
                    TooltipMakerAPI spCountElement = spCountPanel.createUIElement(spCountPanel.getPosition().getWidth(),
                            spCountPanel.getPosition().getHeight(), false);
                    String numSP = Global.getSector().getPlayerStats().getStoryPoints() + "";
                    spCountElement.addPara(String.format("\nStory points: %s", numSP), 0f, Misc.getGrayColor(),
                            Misc.getStoryBrightColor(), numSP);
                    spCountPanel.addUIElement(spCountElement);
                    panel.addComponent(spCountPanel).inBL(0f, -30f);

                    panel.addUIElement(element);
                }

                @Override
                public void customDialogConfirm() {
                    doAugment();
                }

                @Override
                public boolean hasCancelButton() {
                    return true;
                }
            });
        }
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }


    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }
}
