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

public class CompvForge extends BaseToggleAbility {
	public static final Color CONTRAIL_COLOR = new Color(255, 97, 27, 80);
	

    public float getCOMPVPerVOLATILES() {
        return (Global.getSettings().getFloat("MF_VOLATILESConversionRate")
                * (Global.getSector().getEconomy().getCommoditySpec(Commodities.HEAVY_MACHINERY).getBasePrice() + 5*Global.getSector().getEconomy().getCommoditySpec(Commodities.VOLATILES).getBasePrice())
                / Global.getSector().getEconomy().getCommoditySpec(jydr_Items.COMPV).getBasePrice())
                ;//* ;				
    }
    public int MVOLATILESCoom() {
        return Global.getSettings().getInt("MFUseExtraCommodities");
    }
    float VOLATILESCost = Global.getSettings().getFloat("MF_VOLATILESCost");
    float HeavyMachineryCost = Global.getSettings().getFloat("MF_HeavyMachineryCost");
    float CorruptedVOLATILESMultiplier = Global.getSettings().getFloat("MF_CorruptedVOLATILES");
    float PristineVOLATILESMultiplier = Global.getSettings().getFloat("MF_PristineVOLATILES");
    float SalvageModifier = Global.getSettings().getFloat("MF_SalvageGantry");
    boolean affectInput = Global.getSettings().getBoolean("MF_Input");
    boolean affectOutput = Global.getSettings().getBoolean("MF_Output");
    
    @Override
    protected String getActivationText() {
        /*        if (Commodities.HEAVY_MACHINERY != null
        && Commodities.VOLATILES != null
        && getFleet() != null
        || (getFleet().getCargo().getCommodityQuantity(Commodities.VOLATILES) <= 0
        || getFleet().getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0
        || getFleet().getCargo().getRARE_METALS() >= getFleet().getCargo().getMaxCapacity())) {
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
        tooltip.addPara("\u041f\u0435\u0440\u0435\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0435\u0442 \u043b\u0435\u0442\u0443\u0447\u0438\u0435 \u0432\u0435\u0449\u0435\u0441\u0442\u0432\u0430 \u0441 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u0442\u044f\u0436\u0435\u043b\u043e\u0439 \u0442\u0435\u0445\u043d\u0438\u043a\u0438, \u0441\u0436\u0438\u043c\u0430\u044f \u0438\u0445 \u0432 \u043a\u0430\u043d\u0438\u0441\u0442\u0440\u044b CompV.", pad);
            String Supply = Misc.getRoundedValueMaxOneAfterDecimal(getCOMPVPerVOLATILES());
            float iCoom = iCalculateBonus();
            if (iCoom > 1) {Supply = Misc.getRoundedValueMaxOneAfterDecimal(getCOMPVPerVOLATILES()*iCoom);}
            String canOrIs = isActive() ? "are smelting" : "can smelt";
            String Based = iCoom > 1 ? "Nanoforges in your inventory and ships with Salvage Gantry are improving the process of forging CompV by": "You do not possess a nanoforge or a ship with Salvage Gantry that can hasten the process.";
            String Based2 = iCoom > 1 ? Misc.getRoundedValue((iCoom-1)*100) + "%." : "";
            tooltip.addPara("\u0410\u0432\u0442\u043e\u0444\u0430\u0431\u0440\u0438\u043a\u0438 \u0432\u0430\u0448\u0435\u0433\u043e \u0444\u043b\u043e\u0442\u0430 " + canOrIs + " %s \u0435\u0434. \u043b\u0435\u0442\u0443\u0447\u0438\u0445 \u0432\u0435\u0449\u0435\u0441\u0442\u0432 \u0441 %s \u0435\u0434. \u0442\u044f\u0436\u0435\u043b\u043e\u0439 \u0442\u0435\u0445\u043d\u0438\u043a\u0438, \u0441\u043e\u0437\u0434\u0430\u0432\u0430\u044f %s CompV \u0435\u0436\u0435\u0434\u043d\u0435\u0432\u043d\u043e.",
                        pad, Misc.getTextColor(), Misc.getRoundedValueMaxOneAfterDecimal(VOLATILESCost*iCoom), Misc.getRoundedValueMaxOneAfterDecimal(HeavyMachineryCost*iCoom), Supply);
            if (MVOLATILESCoom() > 0) {
                for (int i = 0; i < MVOLATILESCoom(); i++) {
                    tooltip.addPara("\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442\u0441\u044f %s " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName() + ".",
                    pad*0.2f, Misc.getTextColor(), Misc.getRoundedValueMaxOneAfterDecimal((Global.getSettings().getFloat("ExtraCommoditiesCost" + i))*iCoom));
                }
            };
            tooltip.addPara("%s %s", pad, highlight, Based, Based2);
            tooltip.addPara("\u0423\u0432\u0435\u043b\u0438\u0447\u0438\u0432\u0430\u0435\u0442 \u0434\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c, \u0441 \u043a\u043e\u0442\u043e\u0440\u043e\u0439 \u0444\u043b\u043e\u0442 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d, \u043d\u0430 %s.",
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
        
        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), JYDRPlugin.SENSOR_PROFILE_INCREASE_PERCENT, "COMPV Forging");

        float days = Global.getSector().getClock().convertToDays(amount);
        float cost = days;
        float supply = fleet.getCargo().getCommodityQuantity(jydr_Items.COMPV);
        if (MVOLATILESCoom() > 0) {
            if(fleet.getCargo().getCommodityQuantity(Commodities.VOLATILES) <= 0 || fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0) {
                fleet.addFloatingText("Out of Resources", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate(); 
            } else if(supply >= fleet.getCargo().getMaxCapacity()) {
                fleet.addFloatingText("Full of CompV", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate();
            } else {
                float basedmodifier = iCalculateBonus();
                if (affectInput) {
                    for (int i = 0; i < MVOLATILESCoom(); i++) {
                        if (fleet.getCargo().getCommodityQuantity(Global.getSettings().getString("ExtraCommodities" + i)) <= 0) {
                            fleet.addFloatingText("Out of " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f); deactivate();  break;
                        }
                        fleet.getCargo().removeCommodity(Global.getSettings().getString("ExtraCommodities" + i), cost*Global.getSettings().getFloat("ExtraCommoditiesCost" + i)*basedmodifier);
                    }
                fleet.getCargo().removeCommodity(Commodities.VOLATILES, cost*VOLATILESCost*basedmodifier);
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost*basedmodifier);
                } else {
                    for (int i = 0; i < MVOLATILESCoom(); i++) {
                        if (fleet.getCargo().getCommodityQuantity(Global.getSettings().getString("ExtraCommodities" + i)) <= 0) {
                            fleet.addFloatingText("Out of " + Global.getSettings().getCommoditySpec(Global.getSettings().getString("ExtraCommodities" + i)).getName(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f); deactivate();  break;
                        }
                        fleet.getCargo().removeCommodity(Global.getSettings().getString("ExtraCommodities" + i), cost*Global.getSettings().getFloat("ExtraCommoditiesCost" + i));
                    }
                    fleet.getCargo().removeCommodity(Commodities.VOLATILES, cost*VOLATILESCost);
                    fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost);
                }
                if (affectOutput) {fleet.getCargo().addCommodity(jydr_Items.COMPV, cost*getCOMPVPerVOLATILES()*basedmodifier);} else {fleet.getCargo().addCommodity(jydr_Items.COMPV, cost*getCOMPVPerVOLATILES());}
                for (FleetMemberViewAPI view : getFleet().getViews()) {
                    view.getContrailColor().shift("timidhavenoidea", CONTRAIL_COLOR, getActivationDays(), 2, 1f);
                    view.getContrailWidthMult().shift("timidhavenoidea", 6, getActivationDays(), 2, 1f);
                }
            }
        } else{
            if(fleet.getCargo().getCommodityQuantity(Commodities.VOLATILES) <= 0 || fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) <= 0) {
                fleet.addFloatingText("Out of Volatiles or Heavy Machinery", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate(); 
            } else if(supply >= fleet.getCargo().getMaxCapacity()) {
                fleet.addFloatingText("Full of CompV", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
                deactivate();
            } else {
                float basedmodifier = iCalculateBonus();
                if (affectInput) {fleet.getCargo().removeCommodity(Commodities.VOLATILES, cost*VOLATILESCost*basedmodifier);fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost*basedmodifier);} else {fleet.getCargo().removeCommodity(Commodities.VOLATILES, cost*VOLATILESCost);fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, cost*HeavyMachineryCost);}
                if (affectOutput) {fleet.getCargo().addCommodity(jydr_Items.COMPV, cost*getCOMPVPerVOLATILES()*basedmodifier);} else {fleet.getCargo().addCommodity(jydr_Items.COMPV, cost*getCOMPVPerVOLATILES());}
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
        float iMaxBonus = PristineVOLATILESMultiplier*iShipSize+SalvageModifier*iSalvageCoomer;
        if (iCorrupted > iShipSize) {
            iCorrupted = iShipSize;
        };
        float iBonus = CorruptedVOLATILESMultiplier*iCorrupted+PristineVOLATILESMultiplier*iPristine+SalvageModifier*iSalvageCoomer;
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