package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemQuantity;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import static com.fs.starfarer.api.impl.campaign.rulecmd.CabalPickExtortionMethod.extortionAmount;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
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
 * CabalWeaponCalc
 */
public class CabalWeaponCalc extends BaseCommandPlugin {

    public static float bestWeaponFanciness(CampaignFleetAPI fleet, float shieldedFraction) {
        Collection<WeaponExtortionInfo> weapons = getFleetWeapons(fleet, true, shieldedFraction);
        float fanciestWeaponFanciness = 0f;
        for (WeaponExtortionInfo weaponInfo : weapons) {
            int maximum;
            switch (weaponInfo.weaponSpec.getSize()) {
                case SMALL:
                    maximum = 15;
                    break;
                case MEDIUM:
                    maximum = 10;
                    break;
                case LARGE:
                default:
                    maximum = 5;
                    break;
            }

            int effectiveCount = Math.min(Math.round(weaponInfo.count), maximum);

            float baseValue = weaponInfo.weaponSpec.getBaseValue();
            int tier = weaponInfo.weaponSpec.getTier();
            float fanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;

            if (fanciness > fanciestWeaponFanciness) {
                fanciestWeaponFanciness = fanciness;
            }
        }
        return fanciestWeaponFanciness;
    }

    public static float totalWeaponsValue(CampaignFleetAPI fleet, boolean stripFleet, float shieldedFraction) {
        Collection<WeaponExtortionInfo> weaponsList = getFleetWeapons(fleet, stripFleet, shieldedFraction);
        float totalWeaponsValue = 0f;
        for (WeaponExtortionInfo weaponInfo : weaponsList) {
            float value = weaponInfo.weaponSpec.getBaseValue() * weaponInfo.count * Global.getSettings().getFloat(
                    "nonEconItemSellPriceMult");
            totalWeaponsValue += value;
        }
        return totalWeaponsValue;
    }

    private static Collection<WeaponExtortionInfo> getFleetWeapons(CampaignFleetAPI fleet, boolean stripFleet, float shieldedFraction) {
        LinkedHashMap<String, WeaponExtortionInfo> weaponInfoMap = new LinkedHashMap<>(100);
        CargoAPI cargo = fleet.getCargo();
        for (CargoItemQuantity<String> weaponStack : cargo.getWeapons()) {
            WeaponExtortionInfo playerWeapon = weaponInfoMap.get(weaponStack.getItem());
            if (playerWeapon == null) {
                WeaponSpecAPI weaponSpec = Global.getSettings().getWeaponSpec(weaponStack.getItem());
                if (weaponSpec != null) {
                    playerWeapon = new WeaponExtortionInfo(weaponSpec, 0);
                    weaponInfoMap.put(weaponStack.getItem(), playerWeapon);
                }
            }
            if (playerWeapon != null) {
                playerWeapon.count += weaponStack.getCount() * (1f - (0.75f * shieldedFraction));
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
                ShipHullSpecAPI hullSpec = member.getHullSpec();
                for (WeaponSlotAPI slot : hullSpec.getAllWeaponSlotsCopy()) {
                    if (slot.isBuiltIn() || slot.isDecorative() || slot.isSystemSlot()) {
                        continue;
                    }
                    WeaponSpecAPI weaponSpec = variant.getWeaponSpec(slot.getId());
                    if (weaponSpec != null) {
                        WeaponExtortionInfo playerWeapon = weaponInfoMap.get(weaponSpec.getWeaponId());
                        if (playerWeapon == null) {
                            playerWeapon = new WeaponExtortionInfo(weaponSpec, 0);
                            weaponInfoMap.put(weaponSpec.getWeaponId(), playerWeapon);
                        }
                        playerWeapon.count += 1f * shieldedMult;
                    }
                }
            }
        }
        return weaponInfoMap.values();
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

        float shieldedFraction = Misc.getShieldedCargoFraction(Global.getSector().getPlayerFleet());
        float totalCreditsValue = totalWeaponsValue(Global.getSector().getPlayerFleet(), true, shieldedFraction);
        double valueToTake = extortionAmount(totalCreditsValue);
        float powerLevel = UW_Util.calculatePowerLevel(fleet);
        valueToTake = Math.min(valueToTake, powerLevel * 350.0);

        Collection<WeaponExtortionInfo> playerWeapons = getFleetWeapons(Global.getSector().getPlayerFleet(), true, shieldedFraction);
        String fanciestWeaponId = null;
        String fanciestWeaponName = null;
        int fanciestWeaponCount = 0;
        float fanciestWeaponStackValue = 0f;
        float fanciestWeaponFanciness = 0f;
        List<WeaponExtortionInfo> multipleChoice = new ArrayList<>(2);
        for (WeaponExtortionInfo weaponInfo : playerWeapons) {
            int cargoSizePerUnit;
            int maximum;
            switch (weaponInfo.weaponSpec.getSize()) {
                case SMALL:
                    cargoSizePerUnit = 2;
                    maximum = 15;
                    break;
                case MEDIUM:
                    cargoSizePerUnit = 4;
                    maximum = 10;
                    break;
                case LARGE:
                default:
                    cargoSizePerUnit = 8;
                    maximum = 5;
                    break;
            }

            int effectiveCount = Math.min(Math.min(Math.round(weaponInfo.count), maximum), (int) (fleet.getCargo().getSpaceLeft() / cargoSizePerUnit));

            float value = weaponInfo.weaponSpec.getBaseValue() * effectiveCount * Global.getSettings().getFloat(
                    "nonEconItemSellPriceMult");
            float baseValue = weaponInfo.weaponSpec.getBaseValue();
            int tier = weaponInfo.weaponSpec.getTier();
            float fanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;

            if (fanciness > fanciestWeaponFanciness) {
                fanciestWeaponId = weaponInfo.weaponSpec.getWeaponId();
                fanciestWeaponName = weaponInfo.weaponSpec.getWeaponName();
                fanciestWeaponCount = effectiveCount;
                fanciestWeaponStackValue = value;
                fanciestWeaponFanciness = fanciness;
            }
            if (value > valueToTake) {
                multipleChoice.add(weaponInfo);
            }
        }

        if (!multipleChoice.isEmpty()) {
            int index = MathUtils.getRandomNumberInRange(0, multipleChoice.size() - 1);
            int cargoSizePerUnit;
            switch (multipleChoice.get(index).weaponSpec.getSize()) {
                case SMALL:
                    cargoSizePerUnit = 2;
                    break;
                case MEDIUM:
                    cargoSizePerUnit = 4;
                    break;
                case LARGE:
                default:
                    cargoSizePerUnit = 8;
                    break;
            }

            int effectiveCount = Math.min(Math.round(multipleChoice.get(index).count), (int) (fleet.getCargo().getSpaceLeft() / cargoSizePerUnit));
            float baseValue = multipleChoice.get(index).weaponSpec.getBaseValue();
            int tier = multipleChoice.get(index).weaponSpec.getTier();

            fanciestWeaponId = multipleChoice.get(index).weaponSpec.getWeaponId();
            fanciestWeaponName = multipleChoice.get(index).weaponSpec.getWeaponName();
            fanciestWeaponCount = effectiveCount;
            fanciestWeaponStackValue = (multipleChoice.get(index).weaponSpec.getBaseValue() * effectiveCount
                    * Global.getSettings().getFloat("nonEconItemSellPriceMult"));
            fanciestWeaponFanciness = (float) Math.sqrt(baseValue / 1000f) * (float) Math.pow(tier, 2) * effectiveCount;
        }
        if (fanciestWeaponId == null) {
            return false;
        }

        float creditsPer = fanciestWeaponStackValue / fanciestWeaponCount;
        int amountToTake = (int) UW_Util.roundToSignificantFiguresLong(Math.min(fanciestWeaponCount, valueToTake
                / creditsPer), 2);
        if (amountToTake < 1) {
            return false;
        }

        float finalFanciness = amountToTake * (fanciestWeaponFanciness / fanciestWeaponCount);
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

        float collectorValue = (amountToTake * (fanciestWeaponStackValue / fanciestWeaponCount)
                / Global.getSettings().getFloat("nonEconItemSellPriceMult"))
                * Global.getSettings().getFloat("nonEconItemBuyPriceMult") + (finalFanciness * 500f);

        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_name", fanciestWeaponName, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_names", fanciestWeaponName + (amountToTake > 1 ? "s" : ""), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_id", fanciestWeaponId, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_amount", amountToTake, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_amount_string", Misc.getWithDGS(amountToTake), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_weapon_collector_value_string", Misc.getWithDGS(
                UW_Util.roundToSignificantFiguresLong(collectorValue, 2)), 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repImpact", repImpact, 0);
        memoryMap.get(MemKeys.LOCAL).set("$Cabal_repNegImpact", -repNegImpact, 0);
        return true;
    }

    private static class WeaponExtortionInfo {

        float count;
        transient WeaponSpecAPI weaponSpec;

        WeaponExtortionInfo(WeaponSpecAPI weaponSpec, int count) {
            this.weaponSpec = weaponSpec;
            this.count = count;
        }
    }
}
