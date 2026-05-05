package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class ButtonWithImage extends Button {

    protected final String spriteName;
    protected final String titleText;
    protected final String descriptionText; 
    protected final String defaultDescription;
    protected final Color titleColor;

    public LabelAPI title, description;

    public ButtonWithImage(
            String spriteName,
            String titleText,
            String descriptionText,
            Color baseColor,
            Color darkColor,
            Color brightColor,
            Color titleColor,
            float width,
            float height,
            float pad) {
        super("", baseColor, darkColor, brightColor, width, height, pad);
        this.spriteName = spriteName;
        this.titleText = titleText;
        this.descriptionText = descriptionText;
        defaultDescription = descriptionText;
        this.titleColor = titleColor;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    /** If [reason] is null, will not change the description. */
    public void enable(String reason) {
        button.setEnabled(true);
        button.setClickable(true);
        if (reason != null) {
            description.setText(reason);
            description.setHighlight(reason);
            description.setHighlightColor(Color.WHITE);
            title.setHighlightColor(Color.WHITE);
        }
    }

    /** If [reason] is null, will not change the description.
     *  If [highlight] is set, text color will be set to orange.
     *  Otherwise, it is set to gray. */
    public void disable(String reason, boolean highlight) {
        button.setEnabled(false);
        if (reason != null) {
            description.setText(reason);
            description.setHighlight(reason);
            description.setHighlightColor(highlight ? Misc.getNegativeHighlightColor() : Color.GRAY);
            title.setHighlightColor(Color.GRAY);
        }
    }

    @Override
    public void disable() {
        disable(null, false);
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
        TooltipMakerAPI imageAndText = tooltip.beginImageWithText(spriteName, height);
        imageAndText.setTitleOrbitronLarge();
        title = imageAndText.addTitle(titleText);
        title.setHighlight(titleText);
        title.setHighlightColor(titleColor);
        imageAndText.setParaFontOrbitron();
        description = imageAndText.addPara(descriptionText, 0f);
        tooltip.addImageWithText(-height);
    }

    @Override
    public PositionAPI position() {
        return title.getPosition();
    }
}