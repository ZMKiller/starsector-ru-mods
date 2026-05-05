package data.scripts;

import java.awt.Color;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.campaign.econ.jydr_Items;

import data.plugins.JYDRPlugin;
import java.util.List;

public class PlatinumForge extends BaseToggleAbility {
	public static final Color CONTRAIL_COLOR = new Color(255, 97, 27, 80);

    public float getPlatinumPerOre() {
        return (Global.getSettings().getFloat("MF_OreConversionRate")
                * (Global.getSector().getEconomy().getCommoditySpec(Commodities.HEAVY_MACHINERY).getBasePrice() + 5*Global.getSector().getEconomy().getCommoditySpec(Commodities.ORE).getBasePrice())
                / Global.getSector().getEconomy().getCommoditySpec(jydr_Items.PLATINUM).getBasePrice())
                ;//* ;
    }
    public int MoreCoom() {
        return Global.getSettings().getInt("MFUseExtraCommodities");
    }
    float OreCost = Global.getSettings().getFloat("MF_OreCost");
    float HeavyMachineryCost = Global.getSettings().getFloat("MF_HeavyMachineryCost");
    float CorruptedOreMultiplier = Global.getSettings().getFloat("MF_CorruptedOre");
    float PristineOreMultiplier = Global.getSettings().getFloat("MF_PristineOre");
    float SalvageModifier = Global.getSettings().getFloat("MF_SalvageGantry");
    boolean affectInput = Global.getSettings().getBoolean("MF_Input");
    boolean affectOutput = Global.getSettings().getBoolean("MF_Output");
    
    @Override
    protected String getActivationText() {
        /*        if (Commodities.HEAVY_MACHINERY != null
        && Commodities.ORE != null
        && getFleet() != null
        || (getFleet().getCargo().getCommodityQuantity(Commodities.ORE) <= 0
        || getFleet().getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0
        || getFleet().getCargo().getPLATINUM() >= getFleet().getCargo().getMaxCapacity())) {
        return null;
        } else */return null;
    }

    @Override
    protected void activateImpl() { }

    @Override
    public boolean showActiveIndicator() { return isActive(); }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        //Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
                status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        float pad = 10f;
        tooltip.addPara("Refine Ore using Heavy Machinery forging it into Platinum bars.", pad);
            String Supply = Misc.getRoundedValueMaxOneAfterDecimal(getPlatinumPerOre());
            float iCoom = iCalculateBonus();
            if (iCoom > 1) {Supply = Misc.getRoundedValueMaxOneAfterDecimal(getPlatinumPerOre()*iCoom);}
            String canOrIs = isActive() ? "are smelting" : "can smelt";
            String Based = iCoom > 1 ? "Nanoforges in your inventory and ships with Salvage Gantry are improving the process of forging Platinum by": "You do not possess a nanoforge or a ship with Salvage Gantry that can hasten the process.";
            String Based2 = iCoom > 1 ? Misc.getRoundedValue((iCoom-1)*100) + "%." : "";
            tooltip.addPara("Your fleet's autoforges " + canOrIs + " %s units of Ore with %s units of Heavy Machinery to create %s Platinum on a daily basis.",
                        pad, Misc.getTextColor(), Misc.getRoundedValueMaxOneAfterDecimal(OreCost*iCoom), Misc.getRoundedValueMaxOneAfterDecimal(HeavyMachineryCost*iCoom), Supply);
            if (MoreCoom() > 0) {
                for (int i = 0; i < MoreCoom(); i++) {
                    tooltip.addPara("Additionally using %s " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName() + ".",
                    pad*0.2f, Misc.getTextColor(), Misc.getRoundedValueMaxOneAfterDecimal((Global.getSettings().getFloat("ExtraCommoditiesCost" + i))*iCoom));
                }
            };
            tooltip.addPara("%s %s", pad, highlight, Based, Based2);
            tooltip.addPara("Increases the range at which the fleet can be detected by %s.",
                        pad, Misc.getNegativeHighlightColor(), (int)JYDRPlugin.SENSOR_PROFILE_INCREASE_PERCENT + "%");


        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        
        if(!isActive()) return;
        
        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), JYDRPlugin.SENSOR_PROFILE_INCREASE_PERCENT, "Platinum Forging");

        float days = Global.getSector().getClock().convertToDays(amount);
        float cost = days;
        float supply = fleet.getCargo().getCommodityQuantity(jydr_Items.PLATINUM);
        if (MoreCoom() > 0) {
            if(fleet.getCargo().getCommodityQuantity(Commodities.ORE) <= 0 || fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0) {
                fleet.addFloatingText("Out of Resources", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate(); 
            } else if(supply >= fleet.getCargo().getMaxCapacity()) {
                fleet.addFloatingText("Full of Platinum", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate();
            } else {
                float basedmodifier = iCalculateBonus();
                if (affectInput) {
                    for (int i = 0; i < MoreCoom(); i++) {
                        if (fleet.getCargo().getCommodityQuantity(Global.getSettings().getString("ExtraCommodities" + i)) <= 0) {
                            fleet.addFloatingText("Out of " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f); deactivate();  break;
                        }
                        fleet.getCargo().removeCommodity(Global.getSettings().getString("ExtraCommodities" + i), cost*Global.getSettings().getFloat("ExtraCommoditiesCost" + i)*basedmodifier);
                    }
                fleet.getCargo().removeCommodity(Commodities.ORE, cost*OreCost*basedmodifier);
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost*basedmodifier);
                } else {
                    for (int i = 0; i < MoreCoom(); i++) {
                        if (fleet.getCargo().getCommodityQuantity(Global.getSettings().getString("ExtraCommodities" + i)) <= 0) {
                            fleet.addFloatingText("Out of " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f); deactivate();  break;
                        }
                        fleet.getCargo().removeCommodity(Global.getSettings().getString("ExtraCommodities" + i), cost*Global.getSettings().getFloat("ExtraCommoditiesCost" + i));
                    }
                    fleet.getCargo().removeCommodity(Commodities.ORE, cost*OreCost);
                    fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost);
                }
                if (affectOutput) {fleet.getCargo().addCommodity(jydr_Items.PLATINUM, cost*getPlatinumPerOre()*basedmodifier);} else {fleet.getCargo().addCommodity(jydr_Items.PLATINUM, cost*getPlatinumPerOre());}
                for (FleetMemberViewAPI view : getFleet().getViews()) {
                    view.getContrailColor().shift("timidhavenoidea", CONTRAIL_COLOR, getActivationDays(), 2, 1f);
                    view.getContrailWidthMult().shift("timidhavenoidea", 6, getActivationDays(), 2, 1f);
                }
            }
        } else{
            if(fleet.getCargo().getCommodityQuantity(Commodities.ORE) <= 0 || fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0) {
                fleet.addFloatingText("Out of Ore or Heavy Machinery", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate(); 
            } else if(supply >= fleet.getCargo().getMaxCapacity()) {
                fleet.addFloatingText("Full of Platinum", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate();
            } else {
                float basedmodifier = iCalculateBonus();
                if (affectInput) {fleet.getCargo().removeCommodity(Commodities.ORE, cost*OreCost*basedmodifier);fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost*basedmodifier);} else {fleet.getCargo().removeCommodity(Commodities.ORE, cost*OreCost);fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost);}
                if (affectOutput) {fleet.getCargo().addCommodity(jydr_Items.PLATINUM, cost*getPlatinumPerOre()*basedmodifier);} else {fleet.getCargo().addCommodity(jydr_Items.PLATINUM, cost*getPlatinumPerOre());}
                for (FleetMemberViewAPI view : getFleet().getViews()) {
                    view.getContrailColor().shift("timidhavenoidea", CONTRAIL_COLOR, getActivationDays(), 2, 1f);
                    view.getContrailWidthMult().shift("timidhavenoidea", 6, getActivationDays(), 2, 1f);
                }
            }
        }
    }

    @Override
    public boolean isUsable() {
        //return isActive();
        return true;
    }
    
    public float iCalculateBonus() {
        float iCorrupted = getFleet().getCargo().getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
        float iPristine = getFleet().getCargo().getQuantity(CargoItemType.SPECIAL, new SpecialItemData(Items.PRISTINE_NANOFORGE, null));
        float iSalvageCoomer = 0f;
        List<FleetMemberAPI> playerFleetList = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
        int iShipSize = playerFleetList.size();
        for (FleetMemberAPI member : playerFleetList) {
            if (member.isMothballed()) continue;
            if (member.getVariant().hasHullMod("repair_gantry")) {
                iSalvageCoomer = iSalvageCoomer+1;
            }
        }
        float iMaxBonus = PristineOreMultiplier*iShipSize+SalvageModifier*iSalvageCoomer;
        if (iCorrupted > iShipSize) {
            iCorrupted = iShipSize;
        };
        float iBonus = CorruptedOreMultiplier*iCorrupted+PristineOreMultiplier*iPristine+SalvageModifier*iSalvageCoomer;
        if (iBonus > iMaxBonus) {
            iBonus = iMaxBonus;
        };
        return iBonus+1;
    }

    @Override
    protected void deactivateImpl() { cleanupImpl(); }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
    }
}