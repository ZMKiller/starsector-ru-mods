package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.submarkets.BlackMarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import data.scripts.UnderworldModPlugin;
import org.apache.log4j.Level;

public class UW_CabalMarketPlugin extends BlackMarketPlugin {

    private static final RepLevel MIN_STANDING = RepLevel.INHOSPITABLE;

    private boolean playerPaidToUnlock = false;
    private boolean boughtBPMiniPackage = false;
    private boolean boughtAlphaCore = false;
    private boolean boughtNanoforge = false;
    private boolean boughtBPPackage = false;
    private float sinceLastUnlock = 0f;

    @Override
    protected Object readResolve() {
        if ((Global.getSector() == null) || (Global.getSector().getMemoryWithoutUpdate() == null)) {
            return this;
        }
        if (boughtBPMiniPackage) {
            Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtBPMiniPackage", true);
        }
        if (boughtAlphaCore) {
            Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtAlphaCore", true);
        }
        if (boughtNanoforge) {
            Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtNanoforge", true);
        }
        if (boughtBPPackage) {
            Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtBPPackage", true);
        }
        return this;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        float days = Global.getSector().getClock().convertToDays(amount);
        sinceLastUnlock += days;
        if (sinceLastUnlock > 7f) {
            playerPaidToUnlock = false;
        }
    }

    @Override
    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    @Override
    public DialogOption[] getDialogOptions(CoreUIAPI ui) {
        if (canPlayerAffordUnlock()) {
            return new DialogOption[]{
                new DialogOption("Pay", () -> {
                    CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                    playerFleet.getCargo().getCredits().subtract(getUnlockCost());
                    playerPaidToUnlock = true;
                    sinceLastUnlock = 0f;
                }),
                new DialogOption("Never mind", null)
            };
        } else {
            return new DialogOption[]{
                new DialogOption("Never mind", null)
            };
        }
    }

    @Override
    public String getDialogText(CoreUIAPI ui) {
        if (canPlayerAffordUnlock()) {
            return "\"We might consider letting you in today, so long as you pay the "
                    + Misc.getWithDGS(getUnlockCost()) + "-credit fee.\"";
        } else {
            return "\"You can't even pay the " + Misc.getWithDGS(getUnlockCost()) + "-credit fee? Get lost.\"";
        }
    }

    @Override
    public Highlights getDialogTextHighlights(CoreUIAPI ui) {
        Highlights h = new Highlights();
        h.setText("" + getUnlockCost());
        if (canPlayerAffordUnlock()) {
            h.setColors(Misc.getHighlightColor());
        } else {
            h.setColors(Misc.getNegativeHighlightColor());
        }
        return h;
    }

    @Override
    public OnClickAction getOnClickAction(CoreUIAPI ui) {
        if (playerPaidToUnlock || submarket.getFaction().getRelToPlayer().isAtWorst(RepLevel.FRIENDLY)) {
            return OnClickAction.OPEN_SUBMARKET;
        }
        return OnClickAction.SHOW_TEXT_DIALOG;
    }

    @Override
    public float getTariff() {
        float fudge;
        switch (submarket.getFaction().getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
            case HOSTILE:
            case INHOSPITABLE:
                fudge = 2f;
                break;
            case SUSPICIOUS:
                fudge = 1.5f;
                break;
            case NEUTRAL:
                fudge = 1f;
                break;
            case FAVORABLE:
                fudge = 0.75f;
                break;
            case WELCOMING:
                fudge = 0.5f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                fudge = 0f;
                break;
        }

        return fudge;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return "Requires: " + submarket.getFaction().getDisplayName() + " - "
                    + MIN_STANDING.getDisplayName().toLowerCase();
        }
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return "Requires: a clandestine approach (transponder off)";
        }
        return super.getTooltipAppendix(ui);
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return false;
        }

        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public void updateCargoPrePlayerInteraction() {
        sinceLastCargoUpdate = 0f;
        boolean okToAdd = false;

        if (okToUpdateShipsAndWeapons()) {
            sinceSWUpdate = 0f;
            okToAdd = true;

            pruneWeapons(0f);

            int weapons = 30;
            int fighters = 8;

            addWeapons(weapons, weapons + 2, 4, submarket.getFaction().getId());
            addWeapons(weapons, weapons + 2, 4, Factions.MERCENARY);
            addFighters(fighters, fighters + 2, 3, submarket.getFaction().getId());
            addFighters(fighters, fighters + 2, 3, Factions.MERCENARY);

            getCargo().getMothballedShips().clear();

            FactionDoctrineAPI doctrineOverride = submarket.getFaction().getDoctrine().clone();
            doctrineOverride.setShipSize(5);
            doctrineOverride.setCombatFreighterProbability(1f);
            addShips(submarket.getFaction().getId(),
                    175f, // combat
                    25f, // freighter 
                    0f, // tanker
                    0f, // transport
                    0f, // liner
                    0f, // utilityPts
                    2f, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    doctrineOverride
            );
            doctrineOverride = Global.getSector().getFaction(Factions.MERCENARY).getDoctrine().clone();
            doctrineOverride.setShipSize(5);
            doctrineOverride.setCombatFreighterProbability(1f);
            addShips(Factions.MERCENARY,
                    175f, // combat
                    25f, // freighter 
                    0f, // tanker
                    0f, // transport
                    0f, // liner
                    0f, // utilityPts
                    2f, // qualityOverride
                    0f, // qualityMod
                    ShipPickMode.PRIORITY_THEN_ALL,
                    doctrineOverride
            );

            addHullMods(2, 3, submarket.getFaction().getId());
            addHullMods(2, 3, Factions.MERCENARY);
        }

        getCargo().sort();

        if (okToAdd) {
            addSpecialItems();
        }
    }

    private void addSpecialItems() {
        boughtBPMiniPackage = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtBPMiniPackage");
        boughtAlphaCore = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtAlphaCore");
        boughtNanoforge = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtNanoforge");
        boughtBPPackage = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtBPPackage");
        boolean boughtVPCLuxuryGoods = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtVPCLuxuryGoods");
        boolean boughtVPCDrugs = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtVPCDrugs");
        boolean boughtTransmitter = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtTransmitter");
        boolean boughtSimulator = Global.getSector().getMemoryWithoutUpdate().contains("$uw_boughtSimulator");

        CargoAPI ourCargo = getCargo();
        for (CargoStackAPI stack : ourCargo.getStacksCopy()) {
            if (!boughtBPPackage && !boughtBPMiniPackage && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_minipackage")) {
                ourCargo.removeStack(stack);
            }
            if (!boughtAlphaCore && (stack.getCommodityId() != null)
                    && stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)) {
                ourCargo.removeStack(stack);
            }
            if (!boughtNanoforge && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals(Items.PRISTINE_NANOFORGE)) {
                ourCargo.removeStack(stack);
            }
            if (!boughtBPPackage && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
                ourCargo.removeStack(stack);
            }
            if (!boughtVPCLuxuryGoods && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_luxury_goods")) {
                ourCargo.removeStack(stack);
            }
            if (!boughtVPCDrugs && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_drugs")) {
                ourCargo.removeStack(stack);
            }
            if (!boughtTransmitter && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_transmitter")) {
                ourCargo.removeStack(stack);
            }
            if (!boughtSimulator && (stack.getSpecialItemSpecIfSpecial() != null)
                    && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_simulator")) {
                ourCargo.removeStack(stack);
            }
        }

        if (!boughtBPPackage && !boughtBPMiniPackage) {
            ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("uw_cabal_minipackage", null), 1);
        }
        if (!boughtAlphaCore) {
            ourCargo.addItems(CargoItemType.RESOURCES, Commodities.ALPHA_CORE, 1);
        }
        if (!boughtNanoforge) {
            ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData(Items.PRISTINE_NANOFORGE, null), 1);
        }
        if (!boughtBPPackage) {
            ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("uw_cabal_package", null), 1);
        }
        if (UnderworldModPlugin.hasIndEvo) {
            try {
                if (!boughtVPCLuxuryGoods) {
                    ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("IndEvo_vpc_luxury_goods", null), 1);
                }
                if (!boughtVPCDrugs) {
                    ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("IndEvo_vpc_drugs", null), 1);
                }
                if (!boughtTransmitter) {
                    ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("IndEvo_transmitter", null), 1);
                }
                if (!boughtSimulator) {
                    ourCargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("IndEvo_simulator", null), 1);
                }
            } catch (Exception e) {
                Global.getLogger(UW_CabalMarketPlugin.class).log(Level.ERROR, "Exception when adding IndEvo items: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return true;
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_minipackage")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.FAVORABLE);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getCommodityId() != null)
                && stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.WELCOMING);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals(Items.PRISTINE_NANOFORGE)) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.FRIENDLY);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.COOPERATIVE);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_luxury_goods")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.FAVORABLE);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_drugs")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.WELCOMING);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_transmitter")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.FRIENDLY);
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_simulator")) {
            return !submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER)).isAtWorst(RepLevel.COOPERATIVE);
        }

        if (!playerPaidToUnlock && submarket.getFaction().getRelToPlayer().isAtBest(RepLevel.WELCOMING)) {
            return true;
        }

        return super.isIllegalOnSubmarket(stack, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        return !level.isAtWorst(MIN_STANDING);
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return true;
        }
        if (Global.getSector().getPlayerFleet().isTransponderOn()) {
            return true;
        }
        RepLevel level = submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction(Factions.PLAYER));
        if (!level.isAtWorst(MIN_STANDING)) {
            return true;
        }
        return super.isIllegalOnSubmarket(commodityId, action);
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, TransferAction action) {
        if (action == TransferAction.PLAYER_SELL) {
            return "No refunds!";
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_minipackage")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.FAVORABLE.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getCommodityId() != null)
                && stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.WELCOMING.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals(Items.PRISTINE_NANOFORGE)) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.FRIENDLY.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.COOPERATIVE.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_luxury_goods")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.FAVORABLE.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_drugs")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.WELCOMING.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_transmitter")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.FRIENDLY.getDisplayName().toLowerCase();
        }
        if ((action == TransferAction.PLAYER_BUY) && (stack.getSpecialItemSpecIfSpecial() != null)
                && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_simulator")) {
            return "Req: " + submarket.getFaction().getDisplayName() + " - " + RepLevel.COOPERATIVE.getDisplayName().toLowerCase();
        }

        if (!playerPaidToUnlock && submarket.getFaction().getRelToPlayer().isAtBest(RepLevel.WELCOMING)) {
            return "Requires: paid access";
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
    protected Object writeReplace() {
        if (okToUpdateShipsAndWeapons()) {
            pruneWeapons(0f);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }

    @Override
    public boolean isParticipatesInEconomy() {
        return false;
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_minipackage")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtBPMiniPackage", true);
            }
            if ((stack.getCommodityId() != null) && stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtAlphaCore", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals(Items.PRISTINE_NANOFORGE)) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtNanoforge", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("uw_cabal_package")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtBPPackage", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_luxury_goods")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtVPCLuxuryGoods", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_vpc_drugs")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtVPCDrugs", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_transmitter")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtTransmitter", true);
            }
            if ((stack.getSpecialItemSpecIfSpecial() != null) && stack.getSpecialItemSpecIfSpecial().getId().contentEquals("IndEvo_simulator")) {
                Global.getSector().getMemoryWithoutUpdate().set("$uw_boughtSimulator", true);
            }
        }
    }

    private boolean canPlayerAffordUnlock() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        int credits = (int) playerFleet.getCargo().getCredits().get();
        return credits >= getUnlockCost();
    }

    private int getUnlockCost() {
        float fudge;
        switch (submarket.getFaction().getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
            case HOSTILE:
            case INHOSPITABLE:
                fudge = 2f;
                break;
            case SUSPICIOUS:
                fudge = 1.5f;
                break;
            case NEUTRAL:
                fudge = 1f;
                break;
            case FAVORABLE:
                fudge = 0.75f;
                break;
            case WELCOMING:
                fudge = 0.5f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                fudge = 0f;
                break;
        }

        return Math.round(fudge * 30000f);
    }
}
