package data.campaign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIShopIntel extends BaseIntelPlugin {

    public HSIShopIntel() {
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
    }

    public List<FleetMemberAPI> getAllShips() {
        return Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();
    }

    public float getCredits() {
        return 1;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return "getName";
    }

    protected String getName() {
        if (listInfoParam != null && listInfoParam instanceof List)
            return "titleV2Expire";
        return HSII18nUtil.getCampaignString("HSI_Shop_name");
    }

    @Override
    public boolean hasSmallDescription() {
        return false;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public boolean isPlayerVisible() {
        return true;
    }

    public static int TAB_BUTTON_HEIGHT = 20;
    public static int TAB_BUTTON_WIDTH = 180;
    public static int ENTRY_HEIGHT = 80;
    public static int ENTRY_WIDTH = 300;
    public static int IMAGE_WIDTH = 80;
    public static int BUTTON_WIDTH = 120;
    public static int SMALL_BUTTON_WIDTH = 40;
    public static int IMAGE_DESC_GAP = 12;
    public static int LIST_WIDTH = 200;

    protected transient Tab currentTab;
    protected List<CargoStackAPI> goods = new ArrayList<>();
    protected float forge_limit = 100000;
    protected int refresh = 90;
    protected float elpsed = 0;

    @Override
    public void advance(float amount) {
        if (isEnded())
            return;

        float days = Global.getSector().getClock().convertToDays(amount);
        elpsed += days;
        if (elpsed >= refresh) {
            forge_limit = 100000 * (Global.getSector().getFaction("HSI") == null ? 0
                    : Global.getSector().getFaction("HSI").getRelToPlayer().getRel());
        }

    }

    public TooltipMakerAPI createCargoImageForPanel(CustomPanelAPI panel, String commodityId) {
        TooltipMakerAPI image = panel.createUIElement(80, 80, false);
        image.addImage(Global.getSettings().getCommoditySpec(commodityId).getIconName(), 80,
                0);
        return image;
    }

    public TooltipMakerAPI createGoodsList(CustomPanelAPI panel) {
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        int items = goods.size();
        float height = items * ENTRY_HEIGHT;
        TooltipMakerAPI goodlist = panel.createUIElement(LIST_WIDTH, height, false);
        float sum = 0;
        float cargoSpace = 0;
        for (CargoStackAPI good : goods) {
            goodlist.addPara("+ " + (int) (good.getSize()) + " x " + good.getDisplayName(), opad, h,
                    good.getDisplayName());
            goodlist.addPara(BULLET + " " + Misc.getDGSCredits(good.getBaseValuePerUnit() * good.getSize()) + "C", opad,
                    h, " " + good.getBaseValuePerUnit() * good.getSize());
            sum += good.getBaseValuePerUnit() * good.getSize();
            cargoSpace += (good.getCargoSpace());
        }
        goodlist.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Deliver_Fee"), pad, h);
        goodlist.addPara("+ " + Misc.getDGSCredits(cargoSpace * getDeliveryFee()), pad, h);
        goodlist.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Total"), pad, h, "");
        goodlist.addPara("+ " + Misc.getDGSCredits(sum) + "C", pad, h, "");
        return goodlist;
    }

    public void createGoodsView(CustomPanelAPI panel, TooltipMakerAPI info, float width) {
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        int items = goods.size();
        float height = (items + 5) * ENTRY_HEIGHT;
        CustomPanelAPI goodlist = panel
                .createCustomPanel(LIST_WIDTH + opad + SMALL_BUTTON_WIDTH + opad + SMALL_BUTTON_WIDTH, height, null);
        float sum = 0;
        float cargoSpace = 0;
        float yPos = opad;
        for (CargoStackAPI good : goods) {
            TooltipMakerAPI entry = panel.createUIElement(LIST_WIDTH, ENTRY_HEIGHT, false);
            entry.addPara("+ " + (int) (good.getSize()) + " x " + good.getDisplayName(), opad, h,
                    good.getDisplayName());
            entry.addPara(BULLET + " " + Misc.getDGSCredits(good.getBaseValuePerUnit() * good.getSize()) + "C", opad, h,
                    " " + good.getBaseValuePerUnit() * good.getSize());
            goodlist.addUIElement(entry).inTL(4, yPos);
            sum += good.getBaseValuePerUnit() * good.getSize();
            cargoSpace += (good.getCargoSpace());
            List<Object> params = new ArrayList<>();
            params.add(good);
            params.add(-1);

            TooltipMakerAPI buttonHolder = goodlist.createUIElement(SMALL_BUTTON_WIDTH, 16, false);
            String name = "-1";
            ButtonAPI cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH, yPos + opad);

            params.set(1, -10);
            buttonHolder = goodlist.createUIElement(BUTTON_WIDTH, 16, false);
            name = "-10";
            cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH + opad + SMALL_BUTTON_WIDTH, yPos + opad);

            params.set(1, -100);
            buttonHolder = goodlist.createUIElement(BUTTON_WIDTH, 16, false);
            name = "-100";
            cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH + opad + SMALL_BUTTON_WIDTH + opad + SMALL_BUTTON_WIDTH,
                    yPos + opad);

            params.set(1, 1);
            buttonHolder = goodlist.createUIElement(BUTTON_WIDTH, 16, false);
            name = "+1";
            cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH, yPos + opad + 20 + opad);

            params.set(1, 10);
            buttonHolder = goodlist.createUIElement(BUTTON_WIDTH, 16, false);
            name = "+10";
            cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH, yPos + opad + 20 + opad);

            params.set(1, 100);
            buttonHolder = goodlist.createUIElement(LIST_WIDTH + opad + SMALL_BUTTON_WIDTH, 16, false);
            name = "+100";
            cancelbutton = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
            goodlist.addUIElement(buttonHolder).inTL(LIST_WIDTH + opad + SMALL_BUTTON_WIDTH + opad + SMALL_BUTTON_WIDTH,
                    yPos + opad + 20 + opad);

            yPos += ENTRY_HEIGHT + opad;
        }
        TooltipMakerAPI entry = panel.createUIElement(LIST_WIDTH, ENTRY_HEIGHT * 2, false);
        entry.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Deliver_Fee"), opad, h);
        entry.addPara("+ " + Misc.getDGSCredits(cargoSpace * getDeliveryFee()), opad, h);
        entry.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Total"), opad, h, "");
        entry.addPara("+ " + Misc.getDGSCredits(sum) + "C", opad, h, "");
        goodlist.addUIElement(entry).inTL(4, yPos);
        List<Object> params = new ArrayList<>();
        params.add(true);
        params.add(sum + getDeliveryFee() * cargoSpace);
        TooltipMakerAPI buttonHolder = goodlist.createUIElement(SMALL_BUTTON_WIDTH, 16, false);
        String name = HSII18nUtil.getCampaignString("HSI_Shop_Confirm");
        ButtonAPI confirmbutton = buttonHolder.addButton(name, params, 120, 30, 0);
        goodlist.addUIElement(buttonHolder).inTL(4, yPos + ENTRY_HEIGHT + opad + ENTRY_HEIGHT);

        List<Object> params1 = new ArrayList<>();
        params.add(false);
        TooltipMakerAPI buttonHolder1 = goodlist.createUIElement(SMALL_BUTTON_WIDTH, 16, false);
        String name1 = HSII18nUtil.getCampaignString("HSI_Shop_Confirm");
        ButtonAPI cancelButton = buttonHolder1.addButton(name1, params1, 120, 30, 0);
        goodlist.addUIElement(buttonHolder1).inTL(124+opad, yPos + ENTRY_HEIGHT + opad + ENTRY_HEIGHT);

        info.addCustom(goodlist, 0);
    }

    public void createForgeView(CustomPanelAPI panel, TooltipMakerAPI info, float width) {
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        int itemsW = Global.getSector().getPlayerFaction() == null ? 0
                : Global.getSector().getPlayerFaction().getKnownWeapons().size();
        int itemsF = Global.getSector().getPlayerFaction() == null ? 0
                : Global.getSector().getPlayerFaction().getKnownFighters().size();
        int items = itemsF + itemsW;
        float height = items * 30;
        float yPos = opad;
        CustomPanelAPI itemPanel = panel.createCustomPanel(ENTRY_WIDTH + LIST_WIDTH, height, null);
        if (itemsW > 0) {
            for (String weapon : Global.getSector().getPlayerFaction().getKnownWeapons()) {
                WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weapon);
                TooltipMakerAPI entry = panel.createUIElement(ENTRY_WIDTH / 2, 30, false);
                entry.addPara(spec.getWeaponName(), opad, h);
                itemPanel.addUIElement(entry).inTL(4, yPos);
                TooltipMakerAPI entry2 = panel.createUIElement(ENTRY_WIDTH / 2, 30, false);
                entry2.addPara("" + Misc.getDGSCredits(spec.getBaseValue() * 1.5f) + "C", opad, h);
                itemPanel.addUIElement(entry).inTL(4 + ENTRY_WIDTH / 2, yPos);
                List<Object> params = new ArrayList<>();
                params.add(spec);
                TooltipMakerAPI buttonHolder = panel.createUIElement(SMALL_BUTTON_WIDTH, 16, false);
                String name = "+1";
                ButtonAPI button = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
                itemPanel.addUIElement(buttonHolder).inTL(ENTRY_WIDTH - SMALL_BUTTON_WIDTH, yPos + 5);
            }
        }
        if (itemsF > 0) {
            for (String wing : Global.getSector().getPlayerFaction().getKnownFighters()) {
                FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wing);
                TooltipMakerAPI entry = panel.createUIElement(ENTRY_WIDTH / 2, 30, false);
                entry.addPara(spec.getWingName(), opad, h);
                itemPanel.addUIElement(entry).inTL(4, yPos);
                TooltipMakerAPI entry2 = panel.createUIElement(ENTRY_WIDTH / 2, 30, false);
                entry2.addPara("" + Misc.getDGSCredits(spec.getBaseValue() * 1.5f) + "C", opad, h);
                itemPanel.addUIElement(entry).inTL(4 + ENTRY_WIDTH / 2, yPos);
                List<Object> params = new ArrayList<>();
                params.add(spec);
                TooltipMakerAPI buttonHolder = panel.createUIElement(SMALL_BUTTON_WIDTH, 16, false);
                String name = "+1";
                ButtonAPI button = buttonHolder.addButton(name, params, SMALL_BUTTON_WIDTH, 20, 0);
                itemPanel.addUIElement(buttonHolder).inTL(ENTRY_WIDTH - SMALL_BUTTON_WIDTH, yPos + 5);
            }
        }
        TooltipMakerAPI list = createGoodsList(panel);
        itemPanel.addUIElement(list).inTL(ENTRY_WIDTH, opad);
        info.addCustom(itemPanel, 0);
    }

    protected void createIndexView(CustomPanelAPI panel, TooltipMakerAPI info, float width) {
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player == null)
            return;

        // float itemPanelHeight = 600f;
        CustomPanelAPI itemPanel = panel.createCustomPanel(500f, 600f, null);
        float yPos = opad;

        TooltipMakerAPI image = panel.createUIElement(128, 128, false);
        image.addImage("graphics/portraits/IP/HSI_TheCommisioner_Cooperate.png", 128, 0);

        itemPanel.addUIElement(image).inTL(4, yPos);

        TooltipMakerAPI entry = panel.createUIElement(ENTRY_WIDTH, ENTRY_HEIGHT, false);
        entry.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Owner_Name"), opad, h,
                HSII18nUtil.getCampaignString("HSI_Shop_Owner_Name"));
        entry.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Owner_Rank"), opad, h,
                HSII18nUtil.getCampaignString("HSI_Shop_Owner_Rank"));
        itemPanel.addUIElement(entry).rightOfMid(image, IMAGE_DESC_GAP);

        info.addCustom(itemPanel, 0);
    }

    public TooltipMakerAPI generateTabButton(CustomPanelAPI buttonRow, String nameId, Tab tab,
            Color base, Color bg, Color bright, TooltipMakerAPI rightOf) {
        TooltipMakerAPI holder = buttonRow.createUIElement(TAB_BUTTON_WIDTH,
                TAB_BUTTON_HEIGHT, false);

        ButtonAPI button = holder.addAreaCheckbox(nameId, tab, base, bg, bright,
                TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 0);
        button.setChecked(tab == this.currentTab);

        if (rightOf != null) {
            buttonRow.addUIElement(holder).rightOfTop(rightOf, 4);
        } else {
            buttonRow.addUIElement(holder).inTL(0, 3);
        }

        return holder;
    }

    protected TooltipMakerAPI addTabButtons(TooltipMakerAPI tm, CustomPanelAPI panel, float width) {

        CustomPanelAPI row = panel.createCustomPanel(width, TAB_BUTTON_HEIGHT, null);
        CustomPanelAPI spacer = panel.createCustomPanel(width, TAB_BUTTON_HEIGHT, null);
        FactionAPI fc = getFactionForUIColors();
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();

        TooltipMakerAPI btnHolder1 = generateTabButton(panel, HSII18nUtil.getCampaignString("HSI_Shop_Index"),
                Tab.INDEX,
                base, bg, bright, null);

        TooltipMakerAPI btnHolder2 = generateTabButton(panel, HSII18nUtil.getCampaignString("HSI_Shop_Forge"),
                Tab.FORGE,
                base, bg, bright, null);

        TooltipMakerAPI btnHolder3 = generateTabButton(panel, HSII18nUtil.getCampaignString("HSI_Shop_List"), Tab.GOODS,
                base, bg, bright, null);

        return btnHolder1;
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float opad = 10;
        float pad = 3;
        Color h = Misc.getHighlightColor();

        if (currentTab == null)
            currentTab = Tab.INDEX;

        TooltipMakerAPI info = panel.createUIElement(width, height - TAB_BUTTON_HEIGHT - 4, true);
        FactionAPI faction = Global.getSector().getPlayerFaction();

        TooltipMakerAPI buttonHolder = addTabButtons(info, panel, width);

        info.addSectionHeading(HSII18nUtil.getCampaignString("HSI_Shop_name"), faction.getBaseUIColor(),
                faction.getDarkUIColor(), com.fs.starfarer.api.ui.Alignment.MID, opad);

        switch (currentTab) {
            case INDEX:
                createIndexView(panel, info, width);
                break;
            case FORGE:
                createForgeView(panel, info, width);
                break;
            case GOODS:
                createGoodsView(panel, info, width);
                break;

        }

        panel.addUIElement(info).belowLeft(buttonHolder, pad);
    }

    public enum Tab {
        INDEX, FORGE, GOODS
    }


    private float getDeliveryFee() {
        if (Global.getSector().getPlayerFleet() == null)
            return 1;
        Vector2f ploc = Global.getSector().getPlayerFleet().getLocationInHyperspace();
        if (Global.getSector().getFaction("HSI") == null
                || Global.getSector().getEntityById("HSI_Wanderer_Station") == null)
            return 10f;
        Vector2f hloc = Global.getSector().getEntityById("HSI_Wanderer_Station").getLocationInHyperspace();
        float dist = Misc.getDistance(ploc, hloc);
        return ((float) Math.pow(Math.max(3, dist / 1000f), Math.max(1.5, dist / 5000f)));
    }

}
