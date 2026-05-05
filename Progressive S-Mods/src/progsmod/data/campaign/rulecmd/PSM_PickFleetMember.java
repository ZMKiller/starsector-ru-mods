package progsmod.data.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import progsmod.data.campaign.rulecmd.delegates.SelectShip;

import java.util.*;
import java.util.List;

/**
 * ProgSModPickFleetMember [selected ship key] [selected variant key] [first time opened] [trigger] [menuId]
 * -- afterwards sets [selected ship key] to the picked ship
 * -- also sets [selected variant key] to the picked ship's variant
 * -- sets [first time opened] to true
 * -- fires [trigger] and changes $menuState to [menuId]
 * upon successful selection of a ship
 */
@SuppressWarnings("unused")
public class PSM_PickFleetMember extends BaseCommandPlugin {
    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.isEmpty()) {
            return false;
        }

        dialog.showCustomDialog(SelectShip.getWidth(), SelectShip.getHeight(), new SelectShip(dialog));
        return true;
    }
}


//    @Override
//    public boolean execute(final String ruleId, final InteractionDialogAPI dialog, final List<Token> params, final Map<String, MemoryAPI> memoryMap)  {
//        if (dialog == null) return false;
//        if (params.size() < 4) return false;
//
//        final String selectedShipKey = params.get(0).string;
//        final String selectedVariantKey = params.get(1).string;
//        final String firstTimeOpenedKey = params.get(2).string;
//
//        // This function excludes fighters
//        List<FleetMemberAPI> selectFrom =  Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
//        dialog.showFleetMemberPickerDialog("Select a ship", "Ok", "Cancel", 3, 7, 58f, true, false, selectFrom,
//            new FleetMemberPickerListener() {
//
//                @Override
//                public void cancelledFleetMemberPicking() {}
//
//                @Override
//                public void pickedFleetMembers(List<FleetMemberAPI> fleetMembers) {
//                    if (fleetMembers == null || fleetMembers.size() == 0) {
//                        return;
//                    }
//
//                    FleetMemberAPI picked = fleetMembers.get(0);
//                    memoryMap.get(MemKeys.LOCAL).set(selectedShipKey, picked, 0f);
//                    memoryMap.get(MemKeys.LOCAL).set(selectedVariantKey, picked.getVariant(), 0f);
//                    memoryMap.get(MemKeys.LOCAL).set(firstTimeOpenedKey, true, 0f);
//
//                    // Wait for player to finish picking a ship before messing with
//                    // the menu states.
//                    dialog.getVisualPanel().showFleetMemberInfo(picked, false);
//                    memoryMap.get(MemKeys.LOCAL).set("$menuState", params.get(4).getString(memoryMap), 0f);
//                    FireAll.fire(ruleId, dialog, memoryMap, params.get(3).getString(memoryMap));
//                }
//
//            }
//        );
//
//        return true;
//    }
    

