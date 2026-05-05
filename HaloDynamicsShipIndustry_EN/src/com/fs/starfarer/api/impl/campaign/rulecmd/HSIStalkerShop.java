package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HSIStalkerShop extends BaseCommandPlugin{

    public static Map<HSISAShopComm, Integer> COMM = new HashMap<>();
    static {
        COMM.put(new HSISAShopComm("STK_FRG_1_1", "HSI_Scarab_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 2);
        COMM.put(new HSISAShopComm("STK_FRG_1_2", "HSI_TianGuang_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 2);
        COMM.put(new HSISAShopComm("STK_DD", "HSI_Medusa_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 0);
        COMM.put(new HSISAShopComm("STK_CA", "HSI_Eagle_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 0);
        COMM.put(new HSISAShopComm("STK_BB", "HSI_Oath_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 0);
        COMM.put(new HSISAShopComm("STK_BC", "HSI_ShanYu_Stalker", HSISAShopComm.Type.SHIP, 0, 1), 0);
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {
        CargoAPI Cargo = Global.getFactory().createCargo(false);
        createCargo(Cargo);
        dialog.showCargoPickerDialog(HSII18nUtil.getCampaignString("HSISAShop1"),
                HSII18nUtil.getCampaignString("HSISAShop2"),
                HSII18nUtil.getCampaignString("HSISAShop3"),
                false,
                310f,
                Cargo,
                new HSIStalkerShopListener(dialog, memoryMap));
        return true;
    }

    public void createCargo(CargoAPI cargo) {
        MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
        Iterator<HSISAShopComm> comms = COMM.keySet().iterator();
        while (comms.hasNext()) {
            HSISAShopComm c = comms.next();
            if (global.contains("$HSISS_" + c.id)) {
                checkUpdates(global,c);
                int num = global.getInt("$HSISS_" + c.id);
                if (num > 0) {
                    switch (c.type) {
                        case RESOURCE:
                            cargo.addItems(CargoAPI.CargoItemType.RESOURCES, c.spec, num);
                            break;
                        case SHIP:
                            cargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("HSISA_ShipProvider", c.spec),
                                    num);
                            break;
                        case WEAPON:
                            cargo.addItems(CargoAPI.CargoItemType.WEAPONS, c.spec, num);
                            break;
                        default:
                            break;

                    }
                }
            }else{
                int num = COMM.get(c);
                global.set("$HSISS_" + c.id, num);
                checkUpdates(global,c);
                num = global.getInt("$HSISS_" + c.id);
                if (num > 0) {
                    switch (c.type) {
                        case RESOURCE:
                            cargo.addItems(CargoAPI.CargoItemType.RESOURCES, c.spec, num);
                            break;
                        case SHIP:
                            cargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("HSISA_ShipProvider", c.spec),
                                    num);
                            break;
                        case WEAPON:
                            cargo.addItems(CargoAPI.CargoItemType.WEAPONS, c.spec, num);
                            break;
                        default:
                            break;

                    }
                }
            }
        }
    }

    private void checkUpdates(MemoryAPI memory,HSISAShopComm comm){
        switch (comm.id){
            case "STK_FRG_1_1":
                if(!memory.contains("$STK_FRG_1_1_P")&&memory.contains("$HSI_Stalker_1")&&memory.getBoolean("$HSI_Stalker_1")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+2);
                    memory.set("$STK_FRG_1_1_P",true);
                }
                break;
            case "STK_FRG_1_2":
                if(!memory.contains("$STK_FRG_1_2_P")&&memory.contains("$HSI_Stalker_1")&&memory.getBoolean("$HSI_Stalker_1")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+2);
                    memory.set("$STK_FRG_1_2_P",true);
                }
                break;
            case "STK_DD":
                if(!memory.contains("$STK_DD_P")&&memory.contains("$HSI_Stalker_2")&&memory.getBoolean("$HSI_Stalker_2")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+2);
                    memory.set("$STK_DD_P",true);
                }
                break;
            case "STK_CA":
                if(!memory.contains("$STK_CA_P")&&memory.contains("$HSI_Stalker_3")&&memory.getBoolean("$HSI_Stalker_3")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+2);
                    memory.set("$STK_CA_P",true);
                }
                break;
            case "STK_BB":
                if(!memory.contains("$STK_BB_P")&&memory.contains("$HSI_Stalker_4")&&memory.getBoolean("$HSI_Stalker_4")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+1);
                    memory.set("$STK_BB_P",true);
                }
                break;
            case "STK_BC":
                if(!memory.contains("$STK_BC_P")&&memory.contains("$HSI_Stalker_5")&&memory.getBoolean("$HSI_Stalker_5")){
                    memory.set("$HSISS_" + comm.id, memory.getInt("$HSISS_" + comm.id)+1);
                    memory.set("$STK_BC_P",true);
                }
                break;
        }
    }

    public static HSISAShopComm getComm(CargoStackAPI stack) {
        Iterator<HSISAShopComm> comms = COMM.keySet().iterator();
        while (comms.hasNext()) {
            HSISAShopComm c = comms.next();
            switch (c.type) {
                case RESOURCE:
                    if (stack.isCommodityStack()) {
                        if (stack.getCommodityId().equals(c.spec)) {
                            return c;
                        }
                    }
                    break;
                case SHIP:
                    if (stack.isSpecialStack()) {
                        if (stack.getSpecialDataIfSpecial().getData().equals(c.spec)) {
                            return c;
                        }
                    }
                    break;
                case WEAPON:
                    if (stack.isWeaponStack()) {
                        if (stack.getWeaponSpecIfWeapon().getWeaponId().equals(c.spec)) {
                            return c;
                        }
                    }
                    break;
                default:
                    break;

            }

        }
        return null;
    }


    public static class HSIStalkerShopListener implements CargoPickerListener {

        protected final InteractionDialogAPI dialog;
        protected final Map<String, MemoryAPI> memorymap;

        public HSIStalkerShopListener(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
            this.dialog = dialog;
            this.memorymap = memoryMap;
        }

        private static float getValueForComm(HSISAShopComm c){
            switch (c.type) {
                case RESOURCE:
                    CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(c.spec);
                    if(spec!=null){
                        return spec.getBasePrice();
                    }
                    break;
                case SHIP:
                    ShipHullSpecAPI s = Global.getSettings().getHullSpec(c.spec);
                    if(s!=null){
                        return s.getBaseValue();
                    }
                    break;
                case WEAPON:
                    WeaponSpecAPI w = Global.getSettings().getWeaponSpec(c.spec);
                    if(w!=null){
                        return w.getBaseValue();
                    }
                    break;
                default:
                    break;

            }
            return 0;
        }
        @Override
        public void pickedCargo(CargoAPI cargo) {
            float sa_cr = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
            //int sa_cr = 10000;//for test
            final CargoAPI cargo1 = Global.getSector().getPlayerFleet().getCargo();
            cargo.sort();

            for (CargoStackAPI stack : cargo.getStacksCopy()) {
                HSISAShopComm c = getComm(stack);
                if (c!=null&&sa_cr >= getValueForComm(c)* stack.getSize()) {
                    TextPanelAPI text = dialog.getTextPanel();
                    text.setFontSmallInsignia();
                    String str = Misc.getDGSCredits(getValueForComm(c) * stack.getSize());
                    text.addParagraph("- " + str + "", Misc.getNegativeHighlightColor());
                    text.highlightInLastPara(Misc.getHighlightColor(), str);
                    text.setFontInsignia();
                    //cargo1.addFromStack(stack);
                    ShipVariantAPI v = Global.getSettings().createEmptyVariant(c.spec+"_hull",Global.getSettings().getHullSpec(c.spec));
                    for(int i = 0;i<stack.getSize();i++){
                        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP,v));
                        AddRemoveCommodity.addFleetMemberGainText(v,text);
                    }
                    Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(stack.getSize()*getValueForComm(c));
                }

                    int num = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISS_" + c.id);
                    Global.getSector().getMemoryWithoutUpdate().set("$HSISS_" + c.id, num-(int)(stack.getSize()));

            }
            FireAll.fire(null, dialog, memorymap, "PopulateOptions");
        }

        @Override
        public void cancelledCargoSelection() {

        }

        public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp,
                                      boolean pickedUpFromSource, CargoAPI combined) {
            float pad = 3f;
            float small = 5f;
            float opad = 10f;
            final float width = 310f;
            FactionAPI faction = Global.getSector().getFaction("HSI");
            //panel.setParaFontOrbitron();
            //panel.addPara(Misc.ucFirst(HSII18nUtil.getCampaignString("HSISAShopName")), faction.getBaseUIColor(), 1f);
            panel.setParaFontDefault();

            panel.addImage(faction.getLogo(), width * 1f, 3f);
            panel.beginGridFlipped(width, 1, 80, 80);
            int cost = 0;
            int index = 0;
            for (CargoStackAPI stack : combined.getStacksCopy()) {
                HSISAShopComm c = getComm(stack);
                float p = 0;
                if(c!=null) p = getValueForComm(c);
                panel.addToGrid(0, index, stack.getDisplayName(),Misc.getDGSCredits(p*stack.getSize()));
                index++;
            }
            panel.addGrid(pad);

            // panel.addPara("Bounty: %s", opad, Misc.getHighlightColor(),
            // Misc.getWithDGS(bounty) + Strings.C);
            // panel.addPara("Reputation: %s", pad, Misc.getHighlightColor(), "+12");
        }
    }
}
