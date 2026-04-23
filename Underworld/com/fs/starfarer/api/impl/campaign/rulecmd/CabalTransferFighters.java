package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * CabalTransferFighters
 */
public class CabalTransferFighters extends BaseCommandPlugin {

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
        int currQty = cargo.getNumFighters(id);
        if (currQty >= qty) {
            cargo.removeItems(CargoItemType.FIGHTER_CHIP, id, qty);
            fleet.getCargo().addFighters(id, qty);
            return true;
        } else if (currQty > 0) {
            cargo.removeItems(CargoItemType.FIGHTER_CHIP, id, currQty);
            cargo.removeEmptyStacks();
            qty -= currQty;
        }
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.isFighterWing()) {
                continue;
            }
            ShipVariantAPI variant = member.getVariant();
            boolean changed = false;
            for (int i = 0; i < 20; i++) {
                FighterWingSpecAPI fighterSpec = variant.getWing(i);
                if (fighterSpec == null) {
                    continue;
                }

                String fighterId = fighterSpec.getId();
                if (fighterId != null && fighterId.contentEquals(id)) {
                    changed = true;
                    if (variant.getSource() == VariantSource.STOCK || variant.getSource() == VariantSource.HULL) {
                        variant = variant.clone();
                        variant.setSource(VariantSource.REFIT);
                    }
                    variant.setWingId(i, "");
                    qty--;
                    if (qty <= 0) {
                        member.setVariant(variant, false, false);
                        fleet.getCargo().addFighters(id, qty);
                        return true;
                    }
                }
            }
            if (changed) {
                member.setVariant(variant, false, false);
            }
        }

        fleet.getCargo().addFighters(id, qty);
        return true;
    }
}
