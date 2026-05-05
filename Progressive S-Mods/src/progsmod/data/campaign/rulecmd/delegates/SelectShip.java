package progsmod.data.campaign.rulecmd.delegates;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import progsmod.data.campaign.rulecmd.ui.PanelCreator;
import progsmod.data.campaign.rulecmd.ui.ShipButton;
import progsmod.data.campaign.rulecmd.ui.plugins.ShipSelector;
import progsmod.data.campaign.rulecmd.util.ShipButtonData;

import java.util.ArrayList;
import java.util.List;

public class SelectShip implements CustomDialogDelegate {
    final public InteractionDialogAPI dialog;
    final String titleString = "Select a ship";
    final float titleHeight = 30;
    final List<ShipButtonData> buttonData = new ArrayList<>();
    final FleetDataAPI fleet;
    final ShipSelector plugin;
    public float scrollPanelY;
    public CustomDialogDelegate.CustomDialogCallback callback;


    public SelectShip(InteractionDialogAPI dialog) {
        this(dialog, 0f);
    }

    public SelectShip(InteractionDialogAPI dialog, float scrollPanelY) {
        this.dialog = dialog;
        this.plugin = new ShipSelector(this);
        this.fleet = Global.getSector().getPlayerFleet().getFleetData();
        this.scrollPanelY = scrollPanelY;
        final List<FleetMemberAPI> ships = fleet.getMembersInPriorityOrder();

        for (FleetMemberAPI ship : ships) {
            buttonData.add(new ShipButtonData(ship));
        }
    }

    public static float getWidth() {
        return 450f; // Should be wide enough for anything
    }

    public static float getHeight() {
        float screenHeight = Global.getSettings().getScreenHeight(); // Already has UI scaling applied to it
        // 75% of the screen should be good for all monitors, except for really small ones
        float MIN_HEIGHT = 700f;
        return Math.max(screenHeight * 0.75f, MIN_HEIGHT);
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.callback = callback;
        PanelCreator.createTitle(panel, titleString, titleHeight);
        PanelCreator.PanelCreatorData<List<ShipButton>> createdButtonsData =
                PanelCreator.createShipButtonList(
                        panel, buttonData, 65f, 10f, titleHeight, false, scrollPanelY);
        plugin.init(createdButtonsData, dialog, createdButtonsData.tooltipMaker.getExternalScroller());
    }

    @Override
    public void customDialogConfirm() {
        // Just a back button, logic is in ShipSelector.onSelected()
    }

    @Override
    public boolean hasCancelButton() {
        return false;
    }

    @Override
    public String getConfirmText() {
        // There's no way to disable the confirm button, so use it as cancel
        return "Return";
    }

    @Override
    public String getCancelText() {
        return null;
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return plugin;
    }
}
