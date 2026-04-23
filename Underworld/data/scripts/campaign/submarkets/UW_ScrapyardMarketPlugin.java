package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.impl.campaign.submarkets.OpenMarketPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.UW_Defs;
import java.util.Random;

public class UW_ScrapyardMarketPlugin extends BlackMarketPlugin {

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "No refunds!";
        }

        return super.getIllegalTransferText(stack, action);
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "No refunds!";
        }

        return super.getIllegalTransferText(member, action);
    }

    @Override
    public float getTariff() {
        return -0.3f;
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (market.hasCondition(Conditions.FREE_PORT)) {
            return false;
        }
        return submarket.getFaction().isIllegal(commodityId);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (!stack.isCommodityStack()) {
            return false;
        }
        return isIllegalOnSubmarket((String) stack.getData(), action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        return action == TransferAction.PLAYER_SELL;
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(sinceLastCargoUpdate);
        addAndRemoveStockpiledResources(seconds, false, true, true);
        sinceLastCargoUpdate = 0f;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;

            pruneWeapons(0f);

            WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
            int index = 0;
            for (String item : UW_Defs.SCRAPYARD_FACTIONS.getItems()) {
                FactionAPI f;
                try {
                    f = Global.getSector().getFaction(item);
                } catch (Exception e) {
                    f = null;
                }
                if (f != null) {
                    factionPicker.add(f.getId(), UW_Defs.SCRAPYARD_FACTIONS.getWeight(index));
                }
                index++;
            }

            int weapons = 8 + (market.getSize() * 3);
            int fighters = market.getSize();

            addWeapons(weapons, weapons + 2, 1, factionPicker);
            addFighters(fighters, fighters + 2, 1, factionPicker);

            getCargo().getMothballedShips().clear();
            float pOther = 0.1f;

            for (int i = 0; i < 5; i++) {
                FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
                doctrineOverride.setWarships(4);
                doctrineOverride.setPhaseShips(1);
                doctrineOverride.setCarriers(2);
                doctrineOverride.setCombatFreighterProbability(1f);
                doctrineOverride.setShipSize(4);
                addShips(factionPicker.pick(itemGenRandom),
                        50f, // combat
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // freighter 
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // tanker
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // transport
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // liner
                        itemGenRandom.nextFloat() > pOther ? 0f : 10f, // utilityPts
                        0f,
                        0f, // qualityMod
                        ShipPickMode.IMPORTED,
                        doctrineOverride);
            }

            addHullMods(1, 1 + itemGenRandom.nextInt(3));
        }

        getCargo().sort();
    }

    @Override
    public int getStockpileLimit(CommodityOnMarketAPI com) {
        float limit = OpenMarketPlugin.getBaseStockpileLimit(com);

        Random random = new Random(market.getId().hashCode() + submarket.getSpecId().hashCode() + Global.getSector().getClock().getMonth() * 170000);
        limit *= 0.9f + 0.2f * random.nextFloat();

        if (com.getCommodity().getId().equals(Commodities.SUPPLIES)
                || com.getCommodity().getId().equals(Commodities.FUEL)) {
            limit *= 1.25f;
        } else {
            limit *= 0.5f;
        }

        if (limit < 0) {
            limit = 0;
        }

        return (int) limit;
    }

    @Override
    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }
}
