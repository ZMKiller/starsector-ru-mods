package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import progsmod.data.campaign.rulecmd.util.ShipButtonData;
import progsmod.util.SModUtils;

import java.awt.*;
import java.util.Collections;

public class ShipButton extends Button {

    public ShipButtonData data;
    public CustomPanelAPI panel;

    public ShipButton(
            ShipButtonData data,
            Color baseColor,
            Color darkColor,
            Color brightColor,
            float width,
            float height,
            float pad,
            CustomPanelAPI panel) {
        super("", baseColor, darkColor, brightColor, width, height, pad);
        this.data = data;
        this.panel = panel;
    }

    @Override
    public void init(TooltipMakerAPI tooltip) {
        button = tooltip.addAreaCheckbox(
                text,
                "temp",
                baseColor,
                darkColor,
                brightColor,
                width,
                height,
                pad
        );
        float innerHeight = height * 0.9f;
        float xPad = 10f;

        float xpHeight = 20f;
        float nameHeight = innerHeight - xpHeight;
        float textWidth = width - innerHeight - xPad * 2;

        CustomPanelAPI customPanel = panel.createCustomPanel(width, height, null);
        TooltipMakerAPI shipElement = customPanel.createUIElement(innerHeight, nameHeight, false);
        TooltipMakerAPI textElement = customPanel.createUIElement(textWidth, nameHeight,
                false);
        shipElement.addShipList(1, 1, innerHeight, Misc.getBasePlayerColor(),
                Collections.singletonList(data.fleetMember), 0f);
        String shipName = data.fleetMember.getShipName() + " (" +
                          data.fleetMember.getHullSpec().getNameWithDesignationWithDashClass() + ")";
        textElement.setParaFontOrbitron();
        LabelAPI text = textElement.addPara(shipName, Misc.getBasePlayerColor(), 5f);
        text.setHighlightColor(Color.WHITE);
        text.setHighlight(data.fleetMember.getHullSpec().getHullName());

        TooltipMakerAPI xpElement = customPanel.createUIElement(textWidth, xpHeight, false);
        String xpString = "XP: " + (int) SModUtils.getXP(data.fleetMember.getId());
        int reserveXP = (int) SModUtils.getReserveXP(data.fleetMember);
        if (reserveXP > 0) {
            xpString += "  (" + reserveXP + " in the reserve pool)";
        }
        xpElement.addPara(xpString, 0);

        customPanel.addUIElement(shipElement).inLMid(0);
        customPanel.addUIElement(textElement).rightOfTop(shipElement, xPad);
        customPanel.addUIElement(xpElement).rightOfBottom(shipElement, xPad);
        tooltip.addCustom(customPanel, -height);

        button.setEnabled(true);
    }
}
