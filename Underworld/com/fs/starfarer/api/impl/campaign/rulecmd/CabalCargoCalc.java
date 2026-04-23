package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import static com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickExtortionMethod.extortionAmount;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.UW_Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

/**
 * CabalCargoCalc
 */
public class CabalCargoCalc extends BaseCommandPlugin {

    public static CargoAPI createCargoCopyWithCombinedStacks(CargoAPI cargo) {
        CargoAPI newCargo = Global.getFactory().createCargo(true);
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            newCargo.addFromStack(stack);
        }
        return newCargo;
    }

    public static float totalCargoValue(CargoAPI cargo, float shieldedFraction) {
        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        Collections.shuffle(allMarkets);

        float totalCreditsValue = 0f;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (stack.isCommodityStack() && !stack.isCrewStack() && !stack.isMarineStack()) {
                boolean isCore = false;
                if (stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.BETA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.GAMMA_CORE)) {
                    isCore = true;
                }

                float unadjustedValue = 0f;
                for (MarketAPI market : allMarkets) {
                    float thisUnadjustedValue = market.getDemandPrice(stack.getCommodityId(), stack.getSize(), false);
                    if (isCore) {
                        float mult;
                        try {
                            mult = market.getFaction().getCustomFloat("AICoreValueMult");
                        } catch (Exception e) {
                            mult = 1f;
                        }
                        thisUnadjustedValue *= mult;
                    }
                    unadjustedValue = Math.max(thisUnadjustedValue, unadjustedValue);
                }
                totalCreditsValue += unadjustedValue;
            } else if (stack.isSpecialStack() && (stack.getSpecialItemSpecIfSpecial() != null)) {
                if (stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.MISSION_ITEM)
                        || stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.STORY_CRITICAL)) {
                    continue;
                }
                float value = stack.getSpecialItemSpecIfSpecial().getBasePrice() * stack.getSize();
                totalCreditsValue += value;
            }
        }

        return totalCreditsValue * (1f - (0.75f * shieldedFraction));
    }

    public static float valueOfBiggestStack(CargoAPI cargo, float shieldedFraction) {
        CargoAPI newCargo = createCargoCopyWithCombinedStacks(cargo);
        float supplyConsumptionPerDeploymentAndOneMonth = 0f;
        float fuelConsumptionIn20LY = 0f;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            supplyConsumptionPerDeploymentAndOneMonth += member.getDeploymentCostSupplies();
            supplyConsumptionPerDeploymentAndOneMonth += member.getStats().getSuppliesPerMonth().getModifiedValue();
            fuelConsumptionIn20LY += member.getFuelUse() * 20f;
        }

        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        Collections.shuffle(allMarkets);

        float biggestCreditsValue = 0f;
        for (CargoStackAPI stack : newCargo.getStacksCopy()) {
            if (stack.isCommodityStack() && !stack.isCrewStack() && !stack.isMarineStack()) {
                float effectiveSize = stack.getSize();
                if (stack.isSupplyStack()) {
                    effectiveSize = Math.max(0f, effectiveSize - supplyConsumptionPerDeploymentAndOneMonth);
                }
                if (stack.isFuelStack()) {
                    effectiveSize = Math.max(0f, effectiveSize - fuelConsumptionIn20LY);
                }

                boolean isCore = false;
                if (stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.BETA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.GAMMA_CORE)) {
                    isCore = true;
                }

                float value = 0f;
                for (MarketAPI market : allMarkets) {
                    float thisValue = market.getDemandPrice(stack.getCommodityId(), effectiveSize, false);
                    if (isCore) {
                        float mult;
                        try {
                            mult = market.getFaction().getCustomFloat("AICoreValueMult");
                        } catch (Exception e) {
                            mult = 1f;
                        }
                        thisValue *= mult;
                    }
                    value = Math.max(thisValue, value);
                }
                if (value > biggestCreditsValue) {
                    biggestCreditsValue = value;
                }
            } else if (stack.isSpecialStack() && (stack.getSpecialItemSpecIfSpecial() != null)) {
                if (stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.MISSION_ITEM)
                        || stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.STORY_CRITICAL)) {
                    continue;
                }
                float value = stack.getSpecialItemSpecIfSpecial().getBasePrice() * stack.getSize();
                if (value > biggestCreditsValue) {
                    biggestCreditsValue = value;
                }
            }
        }

        return biggestCreditsValue * (1f - (0.75f * shieldedFraction));
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

        float supplyConsumptionPerDeploymentAndOneMonth = 0f;
        float fuelConsumptionIn20LY = 0f;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            supplyConsumptionPerDeploymentAndOneMonth += member.getDeploymentCostSupplies();
            supplyConsumptionPerDeploymentAndOneMonth += member.getStats().getSuppliesPerMonth().getModifiedValue();
            fuelConsumptionIn20LY += member.getFuelUse() * 20f;
        }

        CargoAPI cargo = createCargoCopyWithCombinedStacks(Global.getSector().getPlayerFleet().getCargo());
        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCreditsValue = totalCargoValue(cargo, shieldedFraction);
        double valueToTake = extortionAmount(totalCreditsValue);
        float powerLevel = UW_Util.calculatePowerLevel(fleet);
        valueToTake = Math.min(valueToTake, powerLevel * 2500.0);

        List<MarketAPI> allMarkets = Global.getSector().getEconomy().getMarketsCopy();
        Collections.shuffle(allMarkets);

        MarketAPI location = null;
        float biggestCreditsValue = 0f;
        float creditsPerBiggest = 0f;
        CargoStackAPI biggestStack = null;
        List<CargoExtortionInfo> multipleChoice = new ArrayList<>(2);
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (stack.isCommodityStack() && !stack.isCrewStack() && !stack.isMarineStack()) {
                float effectiveSize = stack.getSize();
                if (stack.isSupplyStack()) {
                    effectiveSize = Math.max(0f, effectiveSize - supplyConsumptionPerDeploymentAndOneMonth);
                }
                if (stack.isFuelStack()) {
                    effectiveSize = Math.max(0f, effectiveSize - fuelConsumptionIn20LY);
                }
                effectiveSize = (int) Math.min(effectiveSize, fleet.getCargo().getSpaceLeft());

                boolean isCore = false;
                if (stack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.BETA_CORE)
                        || stack.getCommodityId().contentEquals(Commodities.GAMMA_CORE)) {
                    isCore = true;
                }

                float value = 0f;
                MarketAPI bestPriceAt = null;
                for (MarketAPI market : allMarkets) {
                    float thisValue = market.getDemandPrice(stack.getCommodityId(), effectiveSize, false);
                    if (isCore) {
                        float mult;
                        try {
                            mult = market.getFaction().getCustomFloat("AICoreValueMult");
                        } catch (Exception e) {
                            mult = 1f;
                        }
                        thisValue *= mult;
                    }
                    if (thisValue > value) {
                        bestPriceAt = market;
                        value = thisValue;
                    }
                }
                if (value > biggestCreditsValue) {
                    biggestCreditsValue = value;
                    creditsPerBiggest = value / effectiveSize;
                    biggestStack = stack;
                    location = bestPriceAt;
                }
                if (value > valueToTake) {
                    multipleChoice.add(new CargoExtortionInfo(stack, value * (1f - (0.75f * shieldedFraction)), value / effectiveSize, bestPriceAt));
                }
            } else if (stack.isSpecialStack()) {
                float value = stack.getSize();
                if (stack.getSpecialItemSpecIfSpecial() != null) {
                    if (stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.MISSION_ITEM)
                            || stack.getSpecialItemSpecIfSpecial().getTags().contains(Tags.STORY_CRITICAL)) {
                        continue;
                    }
                    value *= stack.getSpecialItemSpecIfSpecial().getBasePrice();
                }
                float stability = 0f;
                MarketAPI bestPriceAt = null;
                for (MarketAPI market : allMarkets) {
                    float thisStability = market.getStabilityValue();
                    if (thisStability > stability) {
                        bestPriceAt = market;
                    }
                }
                if (value > biggestCreditsValue) {
                    biggestCreditsValue = value;
                    creditsPerBiggest = value / stack.getSize();
                    biggestStack = stack;
                    location = bestPriceAt;
                }
                if (value > valueToTake) {
                    multipleChoice.add(new CargoExtortionInfo(stack, value * (1f - (0.75f * shieldedFraction)), value / stack.getSize(), bestPriceAt));
                }
            }
        }

        if (!multipleChoice.isEmpty()) {
            int index = MathUtils.getRandomNumberInRange(0, multipleChoice.size() - 1);
            biggestStack = multipleChoice.get(index).stack;
            biggestCreditsValue = multipleChoice.get(index).value;
            creditsPerBiggest = multipleChoice.get(index).valuePerUnit;
            location = multipleChoice.get(index).location;
        }
        if (biggestStack == null || location == null) {
            return false;
        }

        float effectiveSize = Math.round(biggestStack.getSize() * (1f - (0.75f * shieldedFraction)));
        if (biggestStack.isSupplyStack()) {
            effectiveSize = Math.max(0f, effectiveSize - supplyConsumptionPerDeploymentAndOneMonth);
        }
        if (biggestStack.isFuelStack()) {
            effectiveSize = Math.max(0f, effectiveSize - fuelConsumptionIn20LY);
        }
        effectiveSize = (int) Math.min(effectiveSize, fleet.getCargo().getSpaceLeft());

        int amountToTake;
        if (biggestStack.isSpecialStack()) {
            amountToTake = (int) Math.round(Math.min(effectiveSize, valueToTake / creditsPerBiggest));
        } else {
            amountToTake = (int) UW_Util.roundToSignificantFiguresLong(Math.min(effectiveSize, valueToTake / creditsPerBiggest), 2);
        }
        int amountLeft = (int) (biggestStack.getSize() - amountToTake);
        if (amountToTake < 1) {
            amountToTake = 1;
        }
        if (effectiveSize < 1) {
            effectiveSize = 1;
        } else {
            effectiveSize = Math.round(effectiveSize);
        }

        float finalValue = amountToTake * (biggestCreditsValue / effectiveSize);
        float repImpact;
        if (finalValue <= 25000f) {
            repImpact = 0.02f;
        } else if (finalValue <= 50000f) {
            repImpact = 0.04f;
        } else if (finalValue <= 75000f) {
            repImpact = 0.06f;
        } else if (finalValue <= 125000f) {
            repImpact = 0.08f;
        } else if (finalValue <= 200000f) {
            repImpact = 0.10f;
        } else if (finalValue <= 325000f) {
            repImpact = 0.12f;
        } else if (finalValue <= 525000f) {
            repImpact = 0.14f;
        } else if (finalValue <= 850000f) {
            repImpact = 0.16f;
        } else if (finalValue <= 1375000f) {
            repImpact = 0.18f;
        } else {
            repImpact = 0.20f;
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

        boolean isCore = false;
        if (biggestStack.isCommodityStack() && (biggestStack.getCommodityId().contentEquals(Commodities.ALPHA_CORE)
                || biggestStack.getCommodityId().contentEquals(Commodities.BETA_CORE)
                || biggestStack.getCommodityId().contentEquals(Commodities.GAMMA_CORE))) {
            isCore = true;
        }

        String plural = "";
        if (amountToTake > 1) {
            plural = "s";
        }
        if (biggestStack.isSpecialStack() || isCore) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_name", biggestStack.getDisplayName().toLowerCase() + plural, 0);
        } else {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_name", biggestStack.getDisplayName().toLowerCase(), 0);
        }
        if (biggestStack.isSpecialStack() && (biggestStack.getSpecialItemSpecIfSpecial() != null)) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_id", biggestStack.getSpecialItemSpecIfSpecial().getId(), 0);
        } else if (biggestStack.isCommodityStack()) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_id", biggestStack.getCommodityId(), 0);
        }
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_amount", amountToTake, 0);
        if (biggestStack.isSpecialStack() || isCore) {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_amount_string", Misc.getWithDGS(amountToTake), 0);
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_amount_left_string", Misc.getWithDGS(amountLeft), 0);
        } else {
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_amount_string", Misc.getWithDGS(amountToTake) + " unit" + plural + " of", 0);
            memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_amount_left_string", Misc.getWithDGS(amountLeft) + " unit" + plural + " of", 0);
        }
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_best_location", location.getName(), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_cargo_final_value", Misc.getWithDGS(
                UW_Util.roundToSignificantFiguresLong(finalValue, 2)), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repImpact", repImpact, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repNegImpact", -repNegImpact, 0);
        return true;
    }

    private static class CargoExtortionInfo {

        MarketAPI location;
        CargoStackAPI stack;
        float value;
        float valuePerUnit;

        CargoExtortionInfo(CargoStackAPI stack, float value, float valuePerUnit, MarketAPI location) {
            this.stack = stack;
            this.value = value;
            this.valuePerUnit = valuePerUnit;
            this.location = location;
        }
    }
}
