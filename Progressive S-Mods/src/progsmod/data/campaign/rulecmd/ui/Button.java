package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.Color;

public class Button implements UIElement, Selectable {

    protected final String text;
    protected final Color baseColor;
    protected final Color darkColor;
    protected final Color brightColor;
    protected final float width;
    protected final float height;
    protected final float pad;
    
    public ButtonAPI button;
    // Add a hidden labelAPI in order to
    // track position
    private LabelAPI hidden;

    public Button(
            String text, 
            Color baseColor, 
            Color darkColor,
            Color brightColor,
            float width,
            float height,
            float pad) {
        this.text = text;
        this.baseColor = baseColor;
        this.darkColor = darkColor;
        this.brightColor = brightColor;
        this.width = width;
        this.height = height;
        this.pad = pad;
    }

    public void disable() {
        button.setEnabled(false);
    }

    @Override
    public void init(TooltipMakerAPI tooltip) {
        hidden = tooltip.addPara("", height / 2);
        // WTF is computeTextHeight doing, it's probably bugged
        // giving way too large values
        // For now just assume the height is 10 idk
        float textHeight = 10; 
        button = tooltip.addAreaCheckbox(
            text, 
            "temp",
            baseColor, 
            darkColor, 
            brightColor,
            width,
            height,
            pad - textHeight - height / 2
        );
    }

    @Override
    public PositionAPI position() {
        return hidden.getPosition();
    }

    @Override
    public boolean isSelected() {
        return button.isChecked();
    }
    
    /** use Selector.forceSelect instead */
    @Override
    public void select() {
        button.setChecked(true);
    }

    /** use Selector.forceDeselect instead */
    @Override
    public void deselect() {
        button.setChecked(false);
    }
}
