package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.PositionAPI;

public interface Selectable {
    boolean isSelected();
    void select();
    void deselect();
    PositionAPI position();
}
