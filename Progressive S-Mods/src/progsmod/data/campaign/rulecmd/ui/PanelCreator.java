package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;
import progsmod.data.campaign.rulecmd.PSM_BuildInHullMod;
import progsmod.data.campaign.rulecmd.PSM_SelectModule;
import progsmod.data.campaign.rulecmd.ui.plugins.AugmentButtonPlugin;
import progsmod.data.campaign.rulecmd.ui.plugins.BuildInSelector;
import progsmod.data.campaign.rulecmd.util.HullModButtonData;
import progsmod.data.campaign.rulecmd.util.ShipButtonData;
import progsmod.util.Action;
import progsmod.util.SModUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanelCreator {

    private static final String BUTTON_MANAGE_MODULE = "BUTTON_MANAGE_MODULE";
    private static float width, height, buttonWidth, buttonListHorizontalPadding, buttonListHeight;

    public static PanelCreatorData<Title> createTitle(CustomPanelAPI panel, String titleText,
            float titleHeight) {
        float width = panel.getPosition().getWidth();
        CustomPanelAPI titlePanel = panel.createCustomPanel(width, titleHeight, null);
        TooltipMakerAPI titleElement = titlePanel.createUIElement(width - 10f, titleHeight, false);
        Title title = new Title(titleText);
        title.init(titleElement);

        titlePanel.addUIElement(titleElement).inTMid(0f);
        panel.addComponent(titlePanel).inTMid(0);

        return new PanelCreatorData<>(titlePanel, titleElement, title);
    }

    // Create a panel containing the ship image, name, XP, S-mod limit and the button to increase the limit
    // Returns the panel, so the next panel can be positioned below it
    public static CustomPanelAPI createShipInfoPanel(CustomPanelAPI panel, final FleetMemberAPI ship,
            final ShipVariantAPI selectedVariant, final CustomDialogDelegate.CustomDialogCallback callback,
            final float shipScrollPanelY) {
        initVars(panel, 0);
        float shipSize = 80f;
        boolean shipHasModules = !SModUtils.getModuleVariantsWithOP(ship.getVariant()).isEmpty();

        CustomUIPanelPlugin panelPlugin = null;

        // If ship has modules, add a button for switching modules and a listener plugin
        // When the button is pressed, dismiss this dialog and open a new module selection dialog
        // If a module is selected in that dialog, the hullmod panel will be recreated with a different
        //  selectedVariant parameter
        if (shipHasModules) {
            panelPlugin = new BaseCustomUIPanelPlugin() {
                @Override
                public void buttonPressed(Object buttonId) {
                    if (buttonId.equals(BUTTON_MANAGE_MODULE)) {
                        PSM_BuildInHullMod.shouldRecreateShipPanel = false;
                        callback.dismissCustomDialog(1);
                        InteractionDialogAPI dialog =
                                Global.getSector().getCampaignUI().getCurrentInteractionDialog();
                        PSM_SelectModule.createPanel(ship, selectedVariant, dialog, shipScrollPanelY);
                    }
                }
            };
        }

        CustomPanelAPI shipInfoPanel = panel.createCustomPanel(width, shipSize, panelPlugin);

        TooltipMakerAPI shipElement = shipInfoPanel.createUIElement(shipSize, shipSize, false);
        // If editing a non-modular ship or a modular ship's base, add a fancy ship list image thing
        if (ship.getVariant() == selectedVariant) {
            shipElement.addShipList(1, 1, shipSize, Misc.getBasePlayerColor(),
                    Collections.singletonList(ship),
                    0f);
        }
        // Otherwise manually grab the module's image
        else {
            String moduleSprite = selectedVariant.getHullSpec().getSpriteName();
            // This might be rotated the wrong way, but it's hard to fix
            // 1. Determining the correct rotation is tricky, have to iterate over weapon slots
            // 2. Drawing the image rotated is tricky, have to create a temp file and rotate and draw that
            // So the module will just be rotated incorrectly for now
            shipElement.addImage(moduleSprite, shipSize, shipSize, 0f);
        }
        shipInfoPanel.addUIElement(shipElement).inTL(buttonListHorizontalPadding, 0f);

        float namePanelHeight = 30f;
        float infoTextPad = 10f;
        float infoTextWidth = buttonWidth - shipSize - infoTextPad;
        TooltipMakerAPI infoTextElement = shipInfoPanel.createUIElement(infoTextWidth, namePanelHeight,
                false);
        String shipName =
                ship.getShipName() + " (" + ship.getHullSpec().getNameWithDesignationWithDashClass() + ")";
        if (ship.getVariant() != selectedVariant) {
            // If a module of the ship is selected, add it to the name
            shipName += " - " + selectedVariant.getHullSpec().getHullName();
        }
        infoTextElement.setParaOrbitronLarge();
        LabelAPI text = infoTextElement.addPara(shipName,
                                                Misc.getBasePlayerColor(), 0f);
        text.setHighlightColor(Color.WHITE);
        text.setHighlight(ship.getHullSpec().getHullName());

        // If ship has multiple modules, add a button to select a different one
        if (shipHasModules && !PSM_BuildInHullMod.fromLunaButton) {
            float buttonYPad = 5f;
            float buttonHeight = 30f;
            infoTextElement.addButton("Manage a different module", BUTTON_MANAGE_MODULE, infoTextWidth,
                    buttonHeight, buttonYPad);
        }

        shipInfoPanel.addUIElement(infoTextElement).rightOfTop(shipElement, infoTextPad);
        panel.addComponent(shipInfoPanel);

        return shipInfoPanel;
    }

    public static CustomPanelAPI createAugmentPanel(CustomPanelAPI panel, FleetMemberAPI ship,
                                                    ShipVariantAPI selectedVariant, PSM_BuildInHullMod.SelectorContainer container, CustomDialogDelegate.CustomDialogCallback callback, Action doOnConfirm) {
        AugmentButtonPlugin augmentButtonPlugin = new AugmentButtonPlugin(panel, ship, selectedVariant, container, callback, doOnConfirm);
        return augmentButtonPlugin.augmentPanel;
    }

    public static void addSelectHullModsText(TooltipMakerAPI element) {
        element.setParaFontOrbitron();
        element.addSpacer(7f);
        element.addPara("Select hull mods to build in", 3f);
    }

    public static PanelCreatorData<List<HullModButton>> createHullModPanel(CustomPanelAPI panel, float height,
            float width, List<HullModButtonData> hullModButtonData, BuildInSelector plugin) {
        float padding = 10f;
        float buttonHeight = 45f;
        initVars(panel, 0);

        CustomPanelAPI hullmodPanel = panel.createCustomPanel(width, height, plugin);
        TooltipMakerAPI element = hullmodPanel.createUIElement(width, height, true);


        List<HullModButton> buttons = new ArrayList<>();

        for (HullModButtonData buttonData : hullModButtonData) {
            element.setParaFontOrbitron();
            if (plugin.needRemoveText && buttonData.isBuiltIn && !buttonData.isEnhanceOnly) {
                plugin.needRemoveText = false;
                element.addPara("Select built-in hull mods to remove", 3f);
            }
            if (plugin.needEnhanceText && !buttonData.isBuiltIn && buttonData.isEnhanceOnly) {
                plugin.needEnhanceText = false;
                element.addSpacer(7f);
                element.addPara("Select built-in hull mods to enhance (does not contribute to the S-mod limit)", 3f);
            }
            if (plugin.needBuildInText && !buttonData.isBuiltIn && !buttonData.isEnhanceOnly) {
                plugin.needBuildInText = false;
                addSelectHullModsText(element);
            }
            buttons.add(addHullModButtonToElement(hullmodPanel, element, buttonData, buttonHeight, padding, 0,
                    false));
        }

        hullmodPanel.addUIElement(element).inTL(buttonListHorizontalPadding, 0f);
        panel.addComponent(hullmodPanel);

        return new PanelCreatorData<>(hullmodPanel, element, buttons);

    }

    public static PanelCreatorData<List<Button>> createButtonList(CustomPanelAPI panel,
            List<String> buttonText, float buttonHeight, float buttonPadding, float distanceFromTop) {
        initVars(panel, distanceFromTop);
        List<Button> buttons = new ArrayList<>();
        TooltipMakerAPI buttonsElement = panel.createUIElement(width, buttonListHeight, true);
        for (String text : buttonText) {
            Button button = new Button(text, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                    Misc.getBrightPlayerColor(), buttonWidth, buttonHeight, buttonPadding);
            button.init(buttonsElement);
            buttons.add(button);
        }
        panel.addUIElement(buttonsElement).inTL(buttonListHorizontalPadding, distanceFromTop);
        return new PanelCreatorData<List<Button>>(panel, buttonsElement, buttons);
    }


    public static PanelCreatorData<Button> createButton(CustomPanelAPI panel, String text, float width,
            float height, float distanceFromLeft, float distanceFromTop) {
        TooltipMakerAPI buttonElement = panel.createUIElement(width, height, false);
        Button button = new Button(text, Misc.getBasePlayerColor(), Misc.getBasePlayerColor(),
                Misc.getBrightPlayerColor(), width, height, 0f);
        button.init(buttonElement);
        panel.addUIElement(buttonElement).inTL(distanceFromLeft, distanceFromTop);
        return new PanelCreatorData<>(panel, buttonElement, button);
    }

    @SafeVarargs
    public static <T> PanelCreatorData<LabelWithVariables<T>> createLabelWithVariables(CustomPanelAPI panel,
            String text, Color highlightColor, float pad, Alignment align, T... vars) {
        float width = panel.getPosition().getWidth();
        float labelWidth = width * 0.85f;
        TooltipMakerAPI labelElement = panel.createUIElement(labelWidth, 20f, false);
        labelElement.setParaFontOrbitron();
        LabelWithVariables<T> label = new LabelWithVariables<>(text, highlightColor, align, vars);
        label.init(labelElement);
        panel.addUIElement(labelElement).inTMid(pad);
        return new PanelCreatorData<>(panel, labelElement, label);
    }

    public static PanelCreatorData<List<HullModButton>> createHullModButtonList(CustomPanelAPI panel,
            List<HullModButtonData> buttonData, float buttonHeight, float buttonPadding,
            float distanceFromTop, boolean useStoryColor) {
        initVars(panel, distanceFromTop);
        TooltipMakerAPI buttonsElement = panel.createUIElement(width, buttonListHeight, true);
        return addToHullModButtonList(panel, buttonsElement, buttonData, buttonHeight, buttonPadding,
                distanceFromTop, useStoryColor);
    }

    public static PanelCreatorData<List<ShipButton>> createShipButtonList(CustomPanelAPI panel,
            List<ShipButtonData> buttonData, float buttonHeight, float buttonPadding, float distanceFromTop,
            boolean useStoryColor, float scrollPanelY) {
        initVars(panel, distanceFromTop);
        TooltipMakerAPI tooltipMaker = panel.createUIElement(width, buttonListHeight, true);
        List<ShipButton> buttons = new ArrayList<>();
        // No need for an  addToShipButtonList function because the list is immutable
        for (final ShipButtonData data : buttonData) {
            ShipButton button = new ShipButton(data,
                    useStoryColor ? Misc.getStoryOptionColor() : Misc.getBasePlayerColor(),
                    useStoryColor ? Misc.getStoryDarkColor() : Misc.getDarkPlayerColor(),
                    Misc.getBrightPlayerColor(), buttonWidth, buttonHeight, buttonPadding, panel);
            button.init(tooltipMaker);
            // Nothing fancy with hover tooltips yet
            buttons.add(button);
        }
        panel.addUIElement(tooltipMaker).inTL(buttonListHorizontalPadding, distanceFromTop);
        if (scrollPanelY != 0) {
            tooltipMaker.getExternalScroller().setYOffset(scrollPanelY);
        }
        return new PanelCreatorData<>(panel, tooltipMaker, buttons);
    }

    public static HullModButton addHullModButtonToElement(CustomPanelAPI panel, TooltipMakerAPI tooltipMaker,
            final HullModButtonData data, float buttonHeight, float buttonPadding, float distanceFromTop,
            boolean useStoryColor) {
        useStoryColor |= data.isBuiltIn;
        HullModButton button = new HullModButton(data,
                useStoryColor ? Misc.getStoryOptionColor() : Misc.getBasePlayerColor(),
                useStoryColor ? Misc.getStoryDarkColor() : Misc.getDarkPlayerColor(),
                Misc.getBrightPlayerColor(), useStoryColor ? Misc.getStoryOptionColor() : Color.WHITE,
                buttonWidth, buttonHeight, buttonPadding);
        button.init(tooltipMaker);
        // Add the hull mod's effect description to a tooltip on mouse hover
        tooltipMaker.addTooltipToPrevious(new TooltipCreator() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                if (data.hullModEffect.shouldAddDescriptionToTooltip(data.hullSize, null, true)) {
                    List<String> highlights = new ArrayList<>();
                    String descParam;
                    // hard cap at 100 just in case getDescriptionParam for some reason
                    // doesn't default to null
                    for (int i = 0; i < 100 &&
                                    (descParam = data.hullModEffect.getDescriptionParam(i, data.hullSize,
                                            null)) != null; i++) {
                        highlights.add(descParam);
                    }
                    tooltip.addPara(data.tooltipDescription.replaceAll("%", "%%"), 0f,
                            Misc.getHighlightColor(), highlights.toArray(new String[0]));
                }
                data.hullModEffect.addPostDescriptionSection(tooltip, data.hullSize, null,
                        getTooltipWidth(tooltipParam), true);
                if (data.hullModEffect.hasSModEffectSection(data.hullSize, null, false)) {
                    data.hullModEffect.addSModSection(tooltip, data.hullSize, null,
                            getTooltipWidth(tooltipParam), true, true);
                }
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 500f;
            }

            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

        }, TooltipLocation.RIGHT);
        return button;
    }

    /**
     * Returns only the buttons that were added
     */
    public static PanelCreatorData<List<HullModButton>> addToHullModButtonList(CustomPanelAPI panel,
            TooltipMakerAPI tooltipMaker, List<HullModButtonData> buttonData, float buttonHeight,
            float buttonPadding, float distanceFromTop, boolean useStoryColor) {
        initVars(panel, distanceFromTop);
        List<HullModButton> buttons = new ArrayList<>();
        for (final HullModButtonData data : buttonData) {
            buttons.add(addHullModButtonToElement(panel, tooltipMaker, data, buttonHeight, buttonPadding,
                    distanceFromTop, useStoryColor));
        }
        panel.addUIElement(tooltipMaker).inTL(buttonListHorizontalPadding, distanceFromTop);
        return new PanelCreatorData<>(panel, tooltipMaker, buttons);
    }

    private static void initVars(CustomPanelAPI panel, float distanceFromTop) {
        width = panel.getPosition().getWidth();
        height = panel.getPosition().getHeight();
        buttonWidth = width * 0.95f;
        // There seems to be a base horizontal padding of 10f,
        // need to account for this for true centering
        buttonListHorizontalPadding = 0.5f * (width - buttonWidth - 10f);
        buttonListHeight = height - distanceFromTop /* - buttonPadding */;
    }

    public static class PanelCreatorData<T> {
        // The panel that was acted on
        public CustomPanelAPI panel;
        // The TooltipMakerAPI that was used to create the element
        public TooltipMakerAPI tooltipMaker;
        // Whatever [tooltipMaker] created
        public T created;

        private PanelCreatorData(CustomPanelAPI panel, TooltipMakerAPI tooltipMaker, T created) {
            this.panel = panel;
            this.tooltipMaker = tooltipMaker;
            this.created = created;
        }
    }
}
