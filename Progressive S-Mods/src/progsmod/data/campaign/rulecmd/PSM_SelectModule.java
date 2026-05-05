package progsmod.data.campaign.rulecmd;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import progsmod.data.campaign.rulecmd.delegates.SelectShip;
import progsmod.data.campaign.rulecmd.ui.PanelCreator;
import progsmod.data.campaign.rulecmd.ui.plugins.ModuleSelector;
import progsmod.util.SModUtils;

import java.util.ArrayList;
import java.util.List;

public class PSM_SelectModule {
    private static int currentVariantIndex;

    public static void createPanel(final FleetMemberAPI fleetMember, final ShipVariantAPI selectedVariant,
            final InteractionDialogAPI dialog, final float shipScrollPanelY) {
        // Guaranteed to not be empty since the button is only created when a ship has multiple modules
        final List<ShipVariantAPI> modulesWithOP = SModUtils.getModuleVariantsWithOP(
                fleetMember.getVariant());

        final List<String> moduleNameStrings = new ArrayList<>();
        int maxSMods = SModUtils.getMaxSMods(fleetMember);
        moduleNameStrings.add("Base ship" +
                              String.format("  (%s/%s S-mods)", fleetMember.getVariant().getSMods().size(),
                                      maxSMods));
        currentVariantIndex = 0;

        for (int i = 0; i < modulesWithOP.size(); i++) {
            ShipVariantAPI moduleVariant = modulesWithOP.get(i);
            moduleNameStrings.add("Module: " + moduleVariant.getHullSpec().getHullName() +
                                  String.format("  (%s/%s S-mods)", moduleVariant.getSMods().size(),
                                          maxSMods));
            if (selectedVariant == moduleVariant) {
                currentVariantIndex = i + 1;
            }
        }

        final ModuleSelector plugin = new ModuleSelector();

        dialog.showCustomDialog(SelectShip.getWidth(), SelectShip.getHeight(), new CustomDialogDelegate() {
            @Override
            public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                float titleHeight = 25f;
                PanelCreator.createTitle(panel, "Select a module", titleHeight);
                plugin.init(PanelCreator.createButtonList(panel, moduleNameStrings, 45f, 10f, titleHeight),
                        fleetMember, modulesWithOP, dialog, callback, shipScrollPanelY);
                plugin.disableItem(currentVariantIndex);
            }

            @Override
            public void customDialogConfirm() {
                // Logic is in plugin.onSelected() without needing to press confirm.
                // Confirm is used as return button, just recreate the hullmod manage panel with the
                // previously selected variant
                plugin.recreateHullmodPanel(selectedVariant);
            }

            @Override
            public void customDialogCancel() {
                // Called when user hits escape even if button is disabled. Same handling as confirm
                plugin.recreateHullmodPanel(selectedVariant);
            }

            @Override
            public String getCancelText() {
                return "Cancel";
            }

            @Override
            public String getConfirmText() {
                return "Return";
            }

            @Override
            public CustomUIPanelPlugin getCustomPanelPlugin() {
                return plugin;
            }

            @Override
            public boolean hasCancelButton() {
                return false;
            }
        });
    }
}
