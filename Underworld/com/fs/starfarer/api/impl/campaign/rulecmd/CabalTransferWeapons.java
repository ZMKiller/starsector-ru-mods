package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * CabalTransferWeapons
 */
public class CabalTransferWeapons extends BaseCommandPlugin {

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
        int qty = Math.round(params.get(1).getFloat(memoryMap));

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        int currQty = cargo.getNumWeapons(id);
        if (currQty >= qty) {
            cargo.removeItems(CargoItemType.WEAPONS, id, qty);
            fleet.getCargo().addWeapons(id, qty);
            return true;
        } else if (currQty > 0) {
            cargo.removeItems(CargoItemType.WEAPONS, id, currQty);
            cargo.removeEmptyStacks();
            qty -= currQty;
        }
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.isFighterWing()) {
                continue;
            }
            ShipHullSpecAPI hullSpec = member.getHullSpec();
            ShipVariantAPI variant = member.getVariant();
            boolean changed = false;
            for (WeaponSlotAPI slot : hullSpec.getAllWeaponSlotsCopy()) {
                if (slot.isBuiltIn() || slot.isDecorative() || slot.isSystemSlot()) {
                    continue;
                }
                String weaponId = variant.getWeaponId(slot.getId());
                if (weaponId != null && weaponId.contentEquals(id)) {
                    changed = true;
                    if (variant.getSource() == VariantSource.STOCK || variant.getSource() == VariantSource.HULL) {
                        variant = variant.clone();
                        variant.setSource(VariantSource.REFIT);
                    }
                    variant.clearSlot(slot.getId());
                    qty--;
                    if (qty <= 0) {
                        member.setVariant(variant, false, false);
                        fleet.getCargo().addWeapons(id, qty);
                        return true;
                    }
                }
            }
            if (changed) {
                member.setVariant(variant, false, false);
            }
        }

        fleet.getCargo().addWeapons(id, qty);
        return true;
    }
}
