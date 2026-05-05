package progsmod.data.campaign.rulecmd.ui;

import java.awt.Color;

import progsmod.data.campaign.rulecmd.util.HullModButtonData;

/** A button with image that stores a [HullModButtonData]
 *  object. */
public class HullModButton extends ButtonWithImage {

    public HullModButtonData data;

    public HullModButton(
            HullModButtonData data,
            Color baseColor, 
            Color darkColor,
            Color brightColor, 
            Color titleColor, 
            float width, 
            float height, 
            float pad) {
        super(data.spriteName, data.name, data.defaultDescription, baseColor, darkColor, brightColor, titleColor, width, height, pad);
        this.data = data;
    }
}
