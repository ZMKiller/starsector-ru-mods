package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import static com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickExtortionMethod.extortionAmount;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

/**
 * CabalFighterCalc
 */
public class CabalFighterCalc extends BaseCommandPlugin {

    public static float bestFighterFanciness(CampaignFleetAPI fleet, float shieldedFraction) {
        Collection<FighterExtortionInfo> fighters = getFleetFighters(fleet, true, shieldedFraction);
        float fanciestFighterFanciness = 0f;
        for (FighterExtortionInfo fighterInfo : fighters) {
            int maximum = 5;

            int effectiveCount = Math.min(Math.round(fighterInfo.count), maximum);

            float baseValue = fighterInfo.fighterSpec.getBaseValue();
            int tier = fighterInfo.fighterSpec.getTier();
            float fanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;

            if (fanciness > fanciestFighterFanciness) {
                fanciestFighterFanciness = fanciness;
            }
        }
        return fanciestFighterFanciness;
    }

    public static float totalFightersValue(CampaignFleetAPI fleet, boolean stripFleet, float shieldedFraction) {
        Collection<FighterExtortionInfo> fightersList = getFleetFighters(fleet, stripFleet, shieldedFraction);
        float totalFightersValue = 0f;
        for (FighterExtortionInfo fighterInfo : fightersList) {
            float value = fighterInfo.fighterSpec.getBaseValue() * fighterInfo.count * Global.getSettings().getFloat("nonEconItemSellPriceMult");
            totalFightersValue += value;
        }
        return totalFightersValue;
    }

    private static Collection<FighterExtortionInfo> getFleetFighters(CampaignFleetAPI fleet, boolean stripFleet, float shieldedFraction) {
        LinkedHashMap<String, FighterExtortionInfo> fighterInfoMap = new LinkedHashMap<>(100);
        CargoAPI cargo = fleet.getCargo();
        for (CargoItemQuantity<String> fighterStack : cargo.getFighters()) {
            FighterExtortionInfo playerFighter = fighterInfoMap.get(fighterStack.getItem());
            if (playerFighter == null) {
                FighterWingSpecAPI fighterSpec = Global.getSettings().getFighterWingSpec(fighterStack.getItem());
                if (fighterSpec != null) {
                    playerFighter = new FighterExtortionInfo(fighterSpec, 0);
                    fighterInfoMap.put(fighterStack.getItem(), playerFighter);
                }
            }
            if (playerFighter != null) {
                playerFighter.count += fighterStack.getCount() * (1f - (0.75f * shieldedFraction));
            }
        }
        if (stripFleet) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member.isFighterWing()) {
                    continue;
                }
                ShipVariantAPI variant = member.getVariant();
                float shieldedMult = 1f;
                if (variant.hasHullMod(HullMods.SHIELDED_CARGO_HOLDS)) {
                    shieldedMult = 0.5f;
                }
                for (int i = 0; i < 20; i++) {
                    FighterWingSpecAPI fighterSpec = variant.getWing(i);
                    if (fighterSpec != null && variant.getNonBuiltInWings().contains(fighterSpec.getId())) {
                        FighterExtortionInfo playerFighter = fighterInfoMap.get(fighterSpec.getId());
                        if (playerFighter == null) {
                            playerFighter = new FighterExtortionInfo(fighterSpec, 0);
                            fighterInfoMap.put(fighterSpec.getId(), playerFighter);
                        }
                        playerFighter.count += 1f * shieldedMult;
                    }
                }
            }
        }
        return fighterInfoMap.values();
    }

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

        float shieldedFraction = Misc.getShieldedCargoFraction(fleet);
        float totalCreditsValue = totalFightersValue(Global.getSector().getPlayerFleet(), true, shieldedFraction);
        double valueToTake = extortionAmount(totalCreditsValue);
        float powerLevel = UW_Util.calculatePowerLevel(fleet);
        valueToTake = Math.min(valueToTake, powerLevel * 350.0);

        Collection<FighterExtortionInfo> playerFighters = getFleetFighters(Global.getSector().getPlayerFleet(), true, shieldedFraction);
        String fanciestFighterId = null;
        String fanciestFighterName = null;
        int fanciestFighterCount = 0;
        float fanciestFighterStackValue = 0f;
        float fanciestFighterFanciness = 0f;
        List<FighterExtortionInfo> multipleChoice = new ArrayList<>(2);
        for (FighterExtortionInfo fighterInfo : playerFighters) {
            int cargoSizePerUnit = 1;
            int maximum = 5;

            int effectiveCount = Math.min(Math.min(Math.round(fighterInfo.count), maximum), (int) (fleet.getCargo().getSpaceLeft() / cargoSizePerUnit));

            float value = fighterInfo.fighterSpec.getBaseValue() * effectiveCount * Global.getSettings().getFloat(
                    "nonEconItemSellPriceMult");
            float baseValue = fighterInfo.fighterSpec.getBaseValue();
            int tier = fighterInfo.fighterSpec.getTier();
            float fanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;

            if (fanciness > fanciestFighterFanciness) {
                fanciestFighterId = fighterInfo.fighterSpec.getId();
                fanciestFighterName = fighterInfo.fighterSpec.getVariant().getFullDesignationWithHullName();
                fanciestFighterCount = effectiveCount;
                fanciestFighterStackValue = value;
                fanciestFighterFanciness = fanciness;
            }
            if (value > valueToTake) {
                multipleChoice.add(fighterInfo);
            }
        }

        if (!multipleChoice.isEmpty()) {
            int index = MathUtils.getRandomNumberInRange(0, multipleChoice.size() - 1);
            int cargoSizePerUnit = 1;

            int effectiveCount = Math.min(Math.round(multipleChoice.get(index).count), (int) (fleet.getCargo().getSpaceLeft() / cargoSizePerUnit));
            float baseValue = multipleChoice.get(index).fighterSpec.getBaseValue();
            int tier = multipleChoice.get(index).fighterSpec.getTier();

            fanciestFighterId = multipleChoice.get(index).fighterSpec.getId();
            fanciestFighterName = multipleChoice.get(index).fighterSpec.getVariant().getFullDesignationWithHullName();
            fanciestFighterCount = effectiveCount;
            fanciestFighterStackValue = (multipleChoice.get(index).fighterSpec.getBaseValue() * effectiveCount
                    * Global.getSettings().getFloat("nonEconItemSellPriceMult"));
            fanciestFighterFanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;
        }
        if (fanciestFighterId == null) {
            return false;
        }

        float creditsPer = fanciestFighterStackValue / fanciestFighterCount;
        int amountToTake = (int) UW_Util.roundToSignificantFiguresLong(Math.min(fanciestFighterCount, valueToTake
                / creditsPer), 2);
        if (amountToTake < 1) {
            return false;
        }

        float finalFanciness = amountToTake * (fanciestFighterFanciness / fanciestFighterCount);
        float repImpact;
        if (finalFanciness <= 50f) {
            repImpact = 0.02f;
        } else if (finalFanciness <= 100f) {
            repImpact = 0.04f;
        } else if (finalFanciness <= 200f) {
            repImpact = 0.06f;
        } else if (finalFanciness <= 400f) {
            repImpact = 0.08f;
        } else {
            repImpact = 0.1f;
        }
        float repNegImpact = repImpact * 0.5f;
        switch (Global.getSector().getFaction("cabal").getRelToPlayer().getLevel()) {
            default:
            case VENGEFUL:
                repImpact = repImpact * 1.5f;
                repNegImpact = repNegImpact / 1.5f;
                break;
            case HOSTILE:
                break;
            case INHOSPITABLE:
            case SUSPICIOUS:
                repImpact = Math.max(0.01f, repImpact - 0.01f);
                repNegImpact = repNegImpact + 0.01f;
                break;
            case NEUTRAL:
                repImpact = Math.max(0.01f, repImpact - 0.02f);
                repNegImpact = repNegImpact + 0.02f;
                break;
            case FAVORABLE:
                repImpact = Math.max(0.01f, repImpact - 0.03f);
                repNegImpact = repNegImpact + 0.03f;
                break;
            case WELCOMING:
                repImpact = Math.max(0.01f, repImpact - 0.04f);
                repNegImpact = repNegImpact + 0.04f;
                break;
            case FRIENDLY:
            case COOPERATIVE:
                repImpact = Math.max(0.01f, repImpact - 0.05f);
                repNegImpact = repNegImpact + 0.05f;
                break;
        }

        float collectorValue = (amountToTake * (fanciestFighterStackValue / fanciestFighterCount)
                / Global.getSettings().getFloat("nonEconItemSellPriceMult"))
                * Global.getSettings().getFloat("nonEconItemBuyPriceMult") + (finalFanciness * 500f);

        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_name", fanciestFighterName, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_names", fanciestFighterName + (amountToTake > 1 ? "s" : ""), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_id", fanciestFighterId, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_amount", amountToTake, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_amount_string", Misc.getWithDGS(amountToTake), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_fighter_collector_value_string", Misc.getWithDGS(
                UW_Util.roundToSignificantFiguresLong(collectorValue, 2)), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repImpact", repImpact, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repNegImpact", -repNegImpact, 0);
        return true;
    }

    private static class FighterExtortionInfo {

        float count;
        transient FighterWingSpecAPI fighterSpec;

        FighterExtortionInfo(FighterWingSpecAPI fighterSpec, int count) {
            this.fighterSpec = fighterSpec;
            this.count = count;
        }
    }
}
