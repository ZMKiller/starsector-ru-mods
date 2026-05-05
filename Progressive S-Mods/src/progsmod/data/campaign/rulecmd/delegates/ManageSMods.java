package progsmod.data.campaign.rulecmd.delegates;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public abstract class ManageSMods implements CustomDialogDelegate {
    // SelectShip and ManageSMods should have slightly different shapes
    // SelectShip is taller, because there's a long list of ships
    public static float getWidth() {
        return SelectShip.getWidth() + 150;
    }

    public static float getHeight() {
        return SelectShip.getHeight() - 100;
    }


    public abstract void showRecentPressed(CustomPanelAPI panel, TooltipMakerAPI tooltipMaker);
}
