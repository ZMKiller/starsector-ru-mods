package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kit.HSII18nUtil;

import java.awt.*;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.List;

public class HSIContact {
    protected PersonAPI contactPerson;

    protected String personId;

    public HSIContact(String personId) {
        this.personId = personId;
    }

    protected static PersonAPI getPerson(String id){
        if(Global.getSector()!=null&&Global.getSector().getImportantPeople()!=null){
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            return ip.getPerson(id);
        }
        return null;
    }

    public static List<PersonAPI> getFactionPerson(FactionAPI faction){
        List<PersonAPI> people = new ArrayList<>();
        if(Global.getSector()!=null&&Global.getSector().getImportantPeople()!=null&&faction!=null){
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        }
        return people;
    }

    public static class HSIContactFilter implements ImportantPeopleAPI.PersonFilter{
        @Override
        public boolean accept(ImportantPeopleAPI.PersonDataAPI personData) {
            return  personData.getPerson().getFaction()!=null&&personData.getPerson().getFaction().getId().equals("HSI");
        }
    }



    public TooltipMakerAPI createIconImageForPanel(CustomPanelAPI panel) {
        TooltipMakerAPI image = panel.createUIElement(60, 60, false);
        image.addImage(contactPerson.getPortraitSprite(), 60, 0);
        return image;
    }

    public CustomPanelAPI drawItem(CustomPanelAPI panel) {
        CustomPanelAPI item = panel.createCustomPanel(150, 64, null);
        TooltipMakerAPI image = createIconImageForPanel(item);
        item.addUIElement(image).inTL(2f, 2f);
        TooltipMakerAPI text = item.createUIElement(86, 21, false);
        text.addPara("" , Misc.getHighlightColor(), 0);
        item.addUIElement(text).inTL(66f, 0f);
        FactionAPI fc = Global.getSector().getFaction("HSI");
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();

        List<Object> param0 = new ArrayList<Object>();
        param0.add(this);
        param0.add(5);
        param0.add(1);
        TooltipMakerAPI btnholder1 = item.createUIElement(40, 21, false);
        btnholder1.addAreaCheckbox("+5", param0, base, bg, bright,
                40, 21, 0);
        item.addUIElement(btnholder1).inTL(66f, 21f);
        TooltipMakerAPI btnholder2 = item.createUIElement(40, 21, false);
        List<Object> param1 = new ArrayList<Object>();
        param1.add(this);
        param1.add(50);
        param1.add(1);
        btnholder2.addAreaCheckbox("+50", param1, base, bg, bright,
                40, 21, 0);
        item.addUIElement(btnholder2).inTL(108f, 21f);
        List<Object> param2 = new ArrayList<Object>();
        param2.add(this);
        param2.add(-1);
        param2.add(1);
        TooltipMakerAPI btnholder3 = item.createUIElement(40, 21, false);
        btnholder3.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Reset"), param2, base, bg, bright,
                82, 21, 0);
        item.addUIElement(btnholder3).inTL(66f, 42f);
        /*
         * TooltipMakerAPI btnholder4 = panel.createUIElement(40, 21, false);
         * param0.set(1, -2);
         * btnholder4.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Confirm"),
         * param0, base, bg, bright,
         * 25, 21, 0);
         * item.addUIElement(btnholder4).inTL(108f, 42f);
         */
        return item;
    }


}
