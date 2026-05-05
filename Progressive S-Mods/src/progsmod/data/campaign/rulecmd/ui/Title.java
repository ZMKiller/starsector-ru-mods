package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class Title implements UIElement {

    protected final String text;
    
    public LabelAPI title;

    public Title(String text) {
        this.text = text;
    }

    @Override
    public void init(TooltipMakerAPI tooltip) {
        tooltip.setTitleOrbitronLarge();
        title = tooltip.addTitle(text);
        title.setAlignment(Alignment.MID);
    }   
}
