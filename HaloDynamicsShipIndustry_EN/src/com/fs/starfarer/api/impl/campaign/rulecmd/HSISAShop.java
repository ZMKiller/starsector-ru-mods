package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAShopComm.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.kit.HSII18nUtil;

import java.util.*;

public class HSISAShop extends BaseCommandPlugin {

    public static Map<HSISAShopComm, Integer> COMM = new HashMap<>();
    static {
        COMM.put(new HSISAShopComm("ButterFly", "HSI_T_01_68", Type.SHIP, 9, 1), 1);
        COMM.put(new HSISAShopComm("FM", "HWI_FirstMantitude", Type.WEAPON, 3, 1), 2);
        //COMM.put(new HSISAShopComm("POS", "HWI_POS_Pod", Type.WEAPON, 1, 2), 2);
        COMM.put(new HSISAShopComm("DAYTIME","HSI_DayTime_wing",Type.FIGHTER,5,1),1);
        COMM.put(new HSISAShopComm("AB", "HWI_ArcBlaster", Type.WEAPON, 2, 1), 1);
        //COMM.put(new HSISAShopComm("Apostle", "HSI_Apostle", Type.SHIP, 0, 1,"$HSI_TTMission_Finished"), 1);
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        CargoAPI Cargo = Global.getFactory().createCargo(false);
        createCargo(Cargo);
        dialog.showCargoPickerDialog(HSII18nUtil.getCampaignString("HSISAShop1"),
                HSII18nUtil.getCampaignString("HSISAShop2"),
                HSII18nUtil.getCampaignString("HSISAShop3"),
                false,
                310f,
                Cargo,
                new HSISAShopListener(dialog, memoryMap));
        return true;
    }

    public void createCargo(CargoAPI cargo) {
        MemoryAPI global = Global.getSector().getMemoryWithoutUpdate();
        Iterator<HSISAShopComm> comms = COMM.keySet().iterator();
        while (comms.hasNext()) {
            HSISAShopComm c = comms.next();
            if(!Objects.equals(c.globalFlag, "NAN")){
                if(!global.contains(c.globalFlag)){
                    continue;
                }
            }
            if (global.contains("$HSISA_" + c.id)) {
                int num = global.getInt("$HSISA_" + c.id);
                if (num > 0) {
                    switch (c.type) {
                        case RESOURCE:
                            cargo.addItems(CargoItemType.RESOURCES, c.spec, num);
                            break;
                        case SHIP:
                            cargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("HSISA_ShipProvider", c.spec),
                                    num);
                            break;
                        case WEAPON:
                            cargo.addItems(CargoItemType.WEAPONS, c.spec, num);
                            break;
                        case FIGHTER:
                            cargo.addItems(CargoItemType.FIGHTER_CHIP,c.spec,num);
                            break;
                        default:
                            break;

                    }
                }
            }else{
                int num = COMM.get(c);
                global.set("$HSISA_" + c.id, num);
                if (num > 0) {
                    switch (c.type) {
                        case RESOURCE:
                            cargo.addItems(CargoItemType.RESOURCES, c.spec, num);
                            break;
                        case SHIP:
                            cargo.addItems(CargoItemType.SPECIAL, new SpecialItemData("HSISA_ShipProvider", c.spec),
                                    num);
                            break;
                        case WEAPON:
                            cargo.addItems(CargoItemType.WEAPONS, c.spec, num);
                            break;
                        case FIGHTER:
                            cargo.addItems(CargoItemType.FIGHTER_CHIP,c.spec,num);
                            break;
                        default:
                            break;

                    }
                }
            }
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
                case FIGHTER:
                    if (stack.isFighterWingStack()) {
                        if (stack.getFighterWingSpecIfWing().getId().equals(c.spec)) {
                            return c;
                        }
                    }
                default:
                    break;

            }

        }
        return null;
    }

    public static class HSISAShopListener implements CargoPickerListener {

        protected final InteractionDialogAPI dialog;
        protected final Map<String, MemoryAPI> memorymap;

        public HSISAShopListener(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
            this.dialog = dialog;
            this.memorymap = memoryMap;
        }

        @Override
        public void pickedCargo(CargoAPI cargo) {
            int sa_cr = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISACredits");
            //int sa_cr = 10000;//for test
            final CargoAPI cargo1 = Global.getSector().getPlayerFleet().getCargo();
            cargo.sort();
            for (CargoStackAPI stack : cargo.getStacksCopy()) {
                HSISAShopComm c = getComm(stack);
                if (c != null && sa_cr >= c.cost * stack.getSize()) {
                    TextPanelAPI text = dialog.getTextPanel();
                    text.setFontSmallInsignia();
                    String str = Misc.getWithDGS(c.cost * stack.getSize()) + "pt";
                    text.addParagraph("- " + str + "", Misc.getNegativeHighlightColor());
                    text.highlightInLastPara(Misc.getHighlightColor(), str);
                    text.setFontInsignia();
                    cargo1.addFromStack(stack);
                    AddRemoveCommodity.addStackGainText(stack, dialog.getTextPanel(), false);
                    sa_cr-=c.cost*(int)(stack.getSize());
                    int num = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISA_" + c.id);
                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_" + c.id, num-(int)(stack.getSize()));
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set("$HSISACredits", sa_cr);
            // memorymap.get(MemKeys.LOCAL).set("$option", "contact_accept", 0);
            FireAll.fire(null, dialog, memorymap, "HSISAOptions");
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
						panel.setParaFontOrbitron();
						panel.addPara(Misc.ucFirst(HSII18nUtil.getCampaignString("HSISAShopName")), faction.getBaseUIColor(), 1f);
						panel.setParaFontDefault();

						panel.addImage(faction.getLogo(), width * 1f, 3f);
                        panel.beginGridFlipped(width, 1, 80, 80);
                        int cost = 0;
                        int index = 0;
                        for (CargoStackAPI stack : combined.getStacksCopy()) {
                            HSISAShopComm c = getComm(stack);
                            if (c != null ) {
                                panel.addToGrid(0, index, stack.getDisplayName(),""+c.cost+"pt");
                                cost+=c.cost*(int)(stack.getSize());
                                index++;
                            }
                        }
                        panel.addGrid(pad);        
                        int sa_cr = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISACredits");
						panel.addPara(HSII18nUtil.getCampaignString("HSISAShopPicked"),
								opad * 1f, Misc.getHighlightColor(),
								""+cost+" pt",
								"" + (int) sa_cr+"pt");

						// panel.addPara("Bounty: %s", opad, Misc.getHighlightColor(),
						// Misc.getWithDGS(bounty) + Strings.C);
						// panel.addPara("Reputation: %s", pad, Misc.getHighlightColor(), "+12");
					}
            }
    }

