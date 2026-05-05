package progsmod.data.campaign.rulecmd.delegates;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface BuildInSModDelegate extends CustomDialogDelegate {
    void showRecentPressed(CustomPanelAPI panel, TooltipMakerAPI tooltipMaker);
}
