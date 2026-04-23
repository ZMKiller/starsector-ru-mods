package org.xhan.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

/*
code by Tomatopaste
*/

public class XHAN_DroneshipEliteProductionListener implements CampaignEventListener {
    private static final String HULLMOD_ID = "XHAN_Elite_PlasteenProduction";
    private static final String SHIP_VARIANT_ID = "XHAN_Hassak_Occisor";
    private static final int COMMODITY_COST = 280;

    @Override
    public void reportEconomyMonthEnd() {
        FleetDataAPI player = Global.getSector().getPlayerFleet().getFleetData();
        int count = 0;
        for (FleetMemberAPI member : player.getMembersListCopy()) {
            if (member.isFighterWing()) continue;

            if (member.getVariant().hasHullMod(HULLMOD_ID)) {
                count++;
            }
        }

        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        for (int i = 1; i <= count; i++) {
            Global.getLogger(XHAN_DroneshipEliteProductionListener.class).info("Executing loop step " + i + " cargo " + cargo.getCommodityQuantity(Commodities.ORGANICS));

            if (cargo.getCommodityQuantity(Commodities.ORGANICS) >= COMMODITY_COST) {
                cargo.removeCommodity(Commodities.ORGANICS, COMMODITY_COST);

                Global.getSector().getPlayerFleet().getFleetData().addFleetMember(SHIP_VARIANT_ID);

                Global.getLogger(XHAN_DroneshipEliteProductionListener.class).info("Consuming commmodities");
            }
        }

        Global.getLogger(XHAN_DroneshipEliteProductionListener.class).info("Economy month ended producing " + count + "ship with variant id " + SHIP_VARIANT_ID);
    }

    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {

    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {

    }

    @Override
    public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {

    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {

    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportPlayerEngagement(EngagementResultAPI result) {

    }

    @Override
    public void reportFleetDespawned(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {

    }

    @Override
    public void reportFleetReachedEntity(CampaignFleetAPI fleet, SectorEntityToken entity) {

    }

    @Override
    public void reportFleetJumped(CampaignFleetAPI fleet, SectorEntityToken from, JumpPointAPI.JumpDestination to) {

    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {

    }

    @Override
    public void reportPlayerReputationChange(String faction, float delta) {

    }

    @Override
    public void reportPlayerReputationChange(PersonAPI person, float delta) {

    }

    @Override
    public void reportPlayerActivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDeactivatedAbility(AbilityPlugin ability, Object param) {

    }

    @Override
    public void reportPlayerDumpedCargo(CargoAPI cargo) {

    }

    @Override
    public void reportPlayerDidNotTakeCargo(CargoAPI cargo) {

    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }
}