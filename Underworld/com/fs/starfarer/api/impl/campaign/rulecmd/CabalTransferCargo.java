package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * CabalTransferCargo
 */
public class CabalTransferCargo extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        CampaignFleetAPI fleet;
        if (dialog.getInteractionTarget() instanceof CampaignFleetAPI campaignFleetAPI) {
            fleet = campaignFleetAPI;
        } else {
            return false;
        }

        String id = params.get(0).getString(memoryMap);
        float qty = params.get(1).getFloat(memoryMap);

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        SpecialItemData specialData = null;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (stack.isSpecialStack() && (stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals(id)) {
                specialData = stack.getSpecialDataIfSpecial();
                break;
            }
        }
        if (specialData != null) {
            fleet.getCargo().addSpecial(specialData, qty);
        } else {
            fleet.getCargo().addCommodity(id, qty);
        }
        if (specialData != null) {
            cargo.removeItems(CargoItemType.SPECIAL, specialData, qty);
        } else {
            cargo.removeItems(CargoItemType.RESOURCES, id, qty);
        }
        cargo.removeEmptyStacks();
        return true;
    }
}
