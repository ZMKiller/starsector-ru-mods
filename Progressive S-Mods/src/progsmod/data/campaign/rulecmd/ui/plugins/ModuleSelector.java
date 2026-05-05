package progsmod.data.campaign.rulecmd.ui.plugins;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import progsmod.data.campaign.rulecmd.PSM_BuildInHullMod;
import progsmod.data.campaign.rulecmd.ui.Button;
import progsmod.data.campaign.rulecmd.ui.PanelCreator;

import java.util.List;

public class ModuleSelector extends Selector<Button> {
    public boolean shouldRecreateHullmodPanel = true;
    private FleetMemberAPI fleetMember;
    private List<ShipVariantAPI> modulesWithOP;
    private InteractionDialogAPI dialog;
    private CustomDialogDelegate.CustomDialogCallback callback;
    private float scrollPanelY;

    public void init(
            PanelCreator.PanelCreatorData<List<Button>> data,
            FleetMemberAPI fleetMember,
            List<ShipVariantAPI> modulesWithOP,
            InteractionDialogAPI dialog,
            CustomDialogDelegate.CustomDialogCallback callback,
            float scrollPanelY
    ) {
        super.init(data.created);
        this.fleetMember = fleetMember;
        this.modulesWithOP = modulesWithOP;
        this.dialog = dialog;
        this.callback = callback;
        this.scrollPanelY = scrollPanelY;
    }

    public void recreateHullmodPanel(ShipVariantAPI selectedVariant) {
        // Needed because dismissCustomDialog() triggers customDialogCancel(), which is supposed to
        // recreate the hullmod panel if the user presses escape
        if (!shouldRecreateHullmodPanel) {
            return;
        }
        shouldRecreateHullmodPanel = false;
        callback.dismissCustomDialog(1);
        PSM_BuildInHullMod.createPanel(fleetMember, selectedVariant, dialog, scrollPanelY, callback, null);
    }

    @Override
    protected void onSelected(int index) {
        ShipVariantAPI variant = fleetMember.getVariant();
        if (index > 0 && modulesWithOP.size() >= index) {
            variant = modulesWithOP.get(index - 1);
        }
        recreateHullmodPanel(variant);
    }

    @Override
    protected void onDeselected(int index) {
    }

    public void disableItem(int index) {
        items.get(index).disable();
    }
}
