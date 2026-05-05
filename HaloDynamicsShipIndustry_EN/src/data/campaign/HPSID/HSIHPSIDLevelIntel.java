package data.campaign.HPSID;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.ProductionReportIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSIForgeIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.HSIIds;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI.MessageClickAction;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.CargoPodsEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import data.kit.HSII18nUtil;

public class HSIHPSIDLevelIntel extends BaseEventIntel implements FleetEventListener {
    public enum Stage {
        START,
        JUNIOR_COOP,
        SENIOR_COOP,
        DEEPSPACE_SUPPLY,
        SENIOR_SHIP_PROVIDER
    }

    public enum Tab {
        PROGRESS,
        SUPPLY,
        TALES,
        CONTRACTS
    }

    public static class HSIShopRequirement {
        protected CargoAPI cargo = Global.getFactory().createCargo(true);
        protected float daysDelay = 0;
        protected long timestamp;
        protected String descStr = "";

        public HSIShopRequirement() {
            this.timestamp = Global.getSector().getClock().getTimestamp();
        }

        public CargoAPI getCargo() {
            return cargo;
        }

        public float getDaysDelay() {
            return daysDelay;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getDescStr() {
            return descStr;
        }

        public void setCargo(CargoAPI cargo) {
            this.cargo = cargo;
        }

        public void setDaysDelay(float daysDelay) {
            this.daysDelay = daysDelay;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isExpired() {
            return Global.getSector().getClock().getElapsedDaysSince(timestamp) >= daysDelay;
        }

        public void addToString(String suffix) {
            descStr += suffix;
        }
    }

    public static class HSIHPSIDDiscount {
        protected float max;
        protected float current;
        protected long timestamp;

        public HSIHPSIDDiscount() {
            max = 0;
            current = 0;
            timestamp = Global.getSector().getClock().getTimestamp();
        }

        public float getCurrent() {
            return current;
        }

        public float getMax() {
            return max;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public float getRest() {
            return max - current;
        }

        public void refresh() {
            setTimestamp(Global.getSector().getClock().getTimestamp());
            setCurrent(0);
        }

        public void use(float amount) {
            current += max;
            if (current > max)
                current = max;
        }

        public void setCurrent(float current) {
            this.current = current;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static String KEY = "$HPSID_ref";
    public static int TAB_BUTTON_HEIGHT = 20;
    public static int TAB_BUTTON_WIDTH = 180;
    public static int ENTRY_HEIGHT = 80;
    public static int ENTRY_WIDTH = 300;
    public static int IMAGE_WIDTH = 80;
    public static int BUTTON_WIDTH = 120;
    public static int IMAGE_DESC_GAP = 12;

    protected HSIHPSIDDiscount discount = new HSIHPSIDDiscount();

    private static Logger getLogger(){return Global.getLogger(HSIHPSIDLevelIntel.class);}


    public HSIHPSIDLevelIntel() {
        super();
        setup();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        Global.getSector().getIntelManager().addIntel(this);
    }

    public static HSIHPSIDLevelIntel getInstance() {
        if (Global.getSector().getMemoryWithoutUpdate().contains(KEY)) {
            return (HSIHPSIDLevelIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
        } else {
            return new HSIHPSIDLevelIntel();
        }
    }

    protected void setup() {
        factors.clear();
        stages.clear();
        setMaxProgress(1000);
        addStage(Stage.START, 0);
        addStage(Stage.JUNIOR_COOP, 100, StageIconSize.MEDIUM);
        addStage(Stage.SENIOR_COOP, 200, StageIconSize.MEDIUM);
        addStage(Stage.DEEPSPACE_SUPPLY, 400, StageIconSize.MEDIUM);
        addStage(Stage.SENIOR_SHIP_PROVIDER, 700, StageIconSize.MEDIUM);
        this.addFactor(new HSIHPSIDMonthlyFactor());
        // addStage(Stage.DEEPSPACE_SUPPLY, 400, true, StageIconSize.MEDIUM);
    }

    protected Tab currentTab = Tab.PROGRESS;

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        TooltipMakerAPI main = panel.createUIElement(width, height, true);
        if (Global.getSector().getFaction("HSI") == null) {
            main.addTitle(HSII18nUtil.getCampaignString("HSI_Failed"));
        }else if(Global.getSector().getFaction("HSI").getRelToPlayer().isHostile()){
            main.addTitle(HSII18nUtil.getCampaignString("HSI_Hostile"));
        }else{
            if (currentTab == null)
                currentTab = Tab.PROGRESS;
            TooltipMakerAPI buttonHolder = addTabButtons(main, panel, width);
            switch (currentTab) {
                case PROGRESS:
                    drawMainProgressTab(main, panel, width, height);
                    break;
                case SUPPLY:
                    drawSupplyTab(main, panel, width, height);
                    break;
                case TALES:
                    drawTaleTab(main, panel, width, height);
                    break;
                case CONTRACTS:
                    drawContractTab(main,panel,width,height);
                    break;
                default:
                    break;
            }
            panel.addUIElement(main).inTL(6,30);

        }


    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId instanceof Tab) {
            if ((Tab) buttonId == Tab.PROGRESS) {
                currentTab = Tab.PROGRESS;
                ui.updateUIForItem(this);
                return;
            }
            if ((Tab) buttonId == Tab.SUPPLY) {
                currentTab = Tab.SUPPLY;
                ui.updateUIForItem(this);
                return;
            }
            if ((Tab) buttonId == Tab.TALES) {
                currentTab = Tab.TALES;
                ui.updateUIForItem(this);
                return;
            }
            if ((Tab) buttonId == Tab.CONTRACTS) {
                currentTab = Tab.CONTRACTS;
                ui.updateUIForItem(this);
                return;
            }
            /*if ((Tab) buttonId == Tab.SPECIAL_REWARDS) {
                currentTab = Tab.SPECIAL_REWARDS;
                ui.updateUIForItem(this);
                return;
            }*/
        }

        if (buttonId instanceof List) {
            List<Object> params = (ArrayList) buttonId;
            if(params.isEmpty()) return;
            if (params.size() == 3) {
                if(params.get(2) instanceof Integer) {
                    if(params.get(2).equals(1)) {
                        HSIDSSSItem item = (HSIDSSSItem) params.get(0);
                        int delta = 0;
                        if (params.get(1) instanceof Integer) {
                            delta = (Integer) params.get(1);
                        }
                        if (delta == -1) {
                            item.reset();
                        } else {
                            item.setSize(item.getSize() + delta);
                        }
                    }
                }
            }
            if(params.get(0).equals("HPSID_TALES")){
                ACTIVE_TALE = (String)params.get(1);
            }

            if(params.get(0).equals("HPSID_PROD")){
                ACTIVE_CONTRACT_INDEX = (Integer) params.get(1);
            }
            if (params.size() == 1) {
                String cmd = (String) params.get(0);
                if (Objects.equals(cmd, "Confirm")) {
                    processShopList();
                    for (HSIDSSSItem i : supplyitem) {
                        i.reset();
                    }
                    ownerStr = pickRandomOwnerConfirmStr();
                }
                if (Objects.equals(cmd, "Cancel")) {
                    for (HSIDSSSItem i : supplyitem) {
                        i.reset();
                    }
                }
                if (Objects.equals(cmd, "Touch")) {
                    ownerStr = pickRandomOwnerStr();
                }
            }
            ui.updateUIForItem(this);
            return;
        }
    }

    private float FORGE_VALUE = 400000f;

    private int SKIP = 1;

    private static final int MAX_SKIP = 1;
    public static float getCurrentLimForLevel(int level){
        return 400000f+(level)*(level+1)/2f*300000f;
    }

    public float getLeftValue(){
        return FORGE_VALUE;
    }

    public void setLeftValue(float value){
        FORGE_VALUE = value;
    }

    private TimeoutTracker<FactionProductionAPI> productionQueue = new TimeoutTracker<>();


    public TimeoutTracker<FactionProductionAPI> getProductionQueue() {
        return productionQueue;
    }

    private List<HSIShopRequirement> shopRequirements = new ArrayList<>();

    protected void processShopList() {
        HSIShopRequirement req = new HSIShopRequirement();
        int total = 0;
        float fee = 0;
        for (HSIDSSSItem item : supplyitem) {
            if (item.getSize() > 0) {
                item.addItemStack(req.getCargo());
                total += item.getSize();
                req.addToString(Global.getSettings().getCommoditySpec(item.getItemId()).getName() + " x "
                        + item.getSize() + " | ");
                fee += Global.getSettings().getCommoditySpec(item.getItemId()).getBasePrice() * item.getSize();
            }
        }
        float mult = getSupplyMult(total);
        float toDiscount = 0;
        if (discount.getRest() > 0) {
            toDiscount = Math.min(discount.getRest(), fee);
        }
        float num = fee - toDiscount;
        num *= mult;
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(num);
        if (total > 0) {
            float time = 0.5f + (2f * getSupplyMult(total) - 1);
            req.setDaysDelay(time);
            shopRequirements.add(req);
            MessageIntel msg = new MessageIntel();
            String str = String.format("%.1f", time) + "D";
            msg.setIcon("graphics/factions/crest_HPSID.png");
            msg.addLine(HSII18nUtil.getCampaignString("HSI_Shop_Delay"), Misc.getTextColor(),
                    new String[] { str }, Misc.getHighlightColor());
            Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
        }
    }

    protected TooltipMakerAPI addTabButtons(TooltipMakerAPI tm, CustomPanelAPI panel, float width) {
        // CustomPanelAPI row = panel.createCustomPanel(width, TAB_BUTTON_HEIGHT, null);
        // CustomPanelAPI spacer = panel.createCustomPanel(width, TAB_BUTTON_HEIGHT,
        // null);
        FactionAPI fc = getFactionForUIColors();
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();

        TooltipMakerAPI btnHolder1 = generateTabButton(panel, getName(), Tab.PROGRESS,
                base, bg, bright, null);
        if (progress >= getDataFor(Stage.DEEPSPACE_SUPPLY).progress) {
            btnHolder1 =  generateTabButton(panel, HSII18nUtil.getCampaignString("HSI_Shop_name"), Tab.SUPPLY,
                    base, bg, bright, btnHolder1);
        }

        btnHolder1 = generateTabButton(panel, HSII18nUtil.getCampaignString("HPSIDTales"), Tab.TALES,
                base, bg, bright, btnHolder1);

        btnHolder1 = generateTabButton(panel, HSII18nUtil.getCampaignString("HPSIDContracts"), Tab.CONTRACTS,
                base, bg, bright, btnHolder1);
        return btnHolder1;
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

    private String ACTIVE_TALE = "WhiteBookC195";

    protected void drawTaleTab(TooltipMakerAPI main, CustomPanelAPI panel, float width, float height){
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        FactionAPI fc = Global.getSector().getFaction("HSI");
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();
        CustomPanelAPI columnL = panel.createCustomPanel(TAB_BUTTON_WIDTH, height-30f, null);
        CustomPanelAPI columnR = panel.createCustomPanel((width - TAB_BUTTON_WIDTH-18f), height-30f, null);

        //左栏：按钮 显示事件名称
        CustomPanelAPI buttonHolder = columnL.createCustomPanel(120f, height, null);
        TooltipMakerAPI rel = null;
        for(String taleId:HSIHPSIDTaleLoader.TALES.keySet()){//遍历所有待加入的事件
            HSIHPSIDTaleLoader.HSIHPSIDTale tale = HSIHPSIDTaleLoader.TALES.get(taleId);
            List<String> btn_param = new ArrayList<>();
            btn_param.add("HPSID_TALES");
            btn_param.add(taleId);
            TooltipMakerAPI holder = buttonHolder.createUIElement(TAB_BUTTON_WIDTH,
                    TAB_BUTTON_HEIGHT, false);

            ButtonAPI button = holder.addAreaCheckbox(tale.getTitle(), btn_param, base, bg, Color.white,
                    TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 0);
            button.setChecked(Objects.equals(tale.getTitle(), ACTIVE_TALE));
            button.getPosition().inTL(0,0);
            if (rel != null) {
                buttonHolder.addUIElement(holder).belowLeft(rel,4);
            } else {
                buttonHolder.addUIElement(holder).inTL(0, 1);
            }
            rel = holder;
            getLogger().info("Added tale L:"+taleId);
        }
        columnL.addComponent(buttonHolder).inTL(0,0);
        //右栏：显示事件名称 事件内容 可能的插图
        if(ACTIVE_TALE !=null&&!ACTIVE_TALE.equals("") &&HSIHPSIDTaleLoader.TALES.containsKey(ACTIVE_TALE)){
            HSIHPSIDTaleLoader.HSIHPSIDTale tale = HSIHPSIDTaleLoader.TALES.get(ACTIVE_TALE);
            TooltipMakerAPI storyPanel = columnR.createUIElement((width - TAB_BUTTON_WIDTH-18f-2f),30,false);
            //storyPanel.setTitleOrbitronVeryLarge();
            storyPanel.addSectionHeading(tale.getTitle(),Alignment.MID,4);
            columnR.addUIElement(storyPanel).inTL(0,0);
            TooltipMakerAPI image = columnR.createUIElement(200f,200f,false);
            //boolean activeImage = false;
            float iw = 0;
            float ih = 0;
            if(!Objects.equals(tale.getIll(), "")){
                SpriteAPI ill = Global.getSettings().getSprite(tale.getIll());
                if(ill!=null){
                    if(ill.getWidth()<ill.getHeight()){
                        image.addImage(tale.getIll(),ill.getWidth()*200f/ill.getHeight(),0f);
                        iw = ill.getWidth()*200f/ill.getHeight();
                        ih = 200f;
                    }else{
                        image.addImage(tale.getIll(),200f,0f);
                        ih = ill.getHeight()*200f/ill.getWidth();
                        iw = 200f;
                    }
                    //activeImage = true;
                }
            }
            columnR.addUIElement(image).belowMid(storyPanel,4f);
            TooltipMakerAPI descPanel = columnR.createUIElement((width - TAB_BUTTON_WIDTH-18f-2f),30,false);
            for(String descs:tale.getDesc()){
                descPanel.setParaFontVictor14();
                descPanel.addPara(descs,Color.white,pad);
            }
            columnR.addUIElement(descPanel).inTL(1,ih+30f+10f);
            //getLogger().info("Added tale R:"+ACTIVE_TALE);
        }else{
            TooltipMakerAPI storyPanel = columnR.createUIElement((width - 140f),height-8,false);
            storyPanel.addPara(HSII18nUtil.getCampaignString("HPSIDTalesTutorial"),bright,pad);
            //getLogger().info("NO ACTIVE TALE:"+ACTIVE_TALE);
            columnR.addUIElement(storyPanel).inTL(1,4);
        }
        main.addComponent(columnL).inTL(6f, 4f);
        main.addComponent(columnR).inTL(TAB_BUTTON_WIDTH+12f, 4f);
        //main.addSpacer(0).getPosition().inBL(10,-10);
        //List<Object> param = new ArrayList<>();
        //main.addComponent(columnL).inTL(6f, 4f);
        //main.addComponent(columnR).inTL((width - 18f) / 2 + 12f, 4f);
        /*if (0 <= Global.getSector().getPlayerFleet().getCargo().getCredits().get()) {
            param.add("Confirm");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Confirm"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        } else {
            param.add("Cancel");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_ConfirmFail"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        }*/
        List<Object> param1 = new ArrayList<>();
        ButtonAPI b1 = main.addAreaCheckbox("", param1, base,
                bg, bg,
                columnL.getPosition().getWidth(), columnL.getPosition().getHeight(), 0);
        b1.getPosition().inTL(6,4f);
        b1.setClickable(false);
        b1.setEnabled(false);
        ButtonAPI b2 =  main.addAreaCheckbox("", param1, base,
                bg, bg,
                columnR.getPosition().getWidth(), columnR.getPosition().getHeight(), 0);
        b2.getPosition().inTL(TAB_BUTTON_WIDTH+12f, 4f);
        b2.setChecked(true);
        b2.setClickable(false);
        b2.setEnabled(false);
    }

    private int ACTIVE_CONTRACT_INDEX = 0;

    protected void drawContractTab(TooltipMakerAPI main, CustomPanelAPI panel, float width, float height){
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        FactionAPI fc = Global.getSector().getFaction("HSI");
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();
        CustomPanelAPI columnL = panel.createCustomPanel(TAB_BUTTON_WIDTH, height-30f, null);
        CustomPanelAPI columnR = panel.createCustomPanel((width - TAB_BUTTON_WIDTH-18f), height-30f, null);

        //左栏：按钮 显示事件名称
        CustomPanelAPI buttonHolder = columnL.createCustomPanel(120f, height, null);
        TooltipMakerAPI rel = null;
        int index = 0;
        for(FactionProductionAPI prod:productionQueue.getItems()){//遍历所有待加入的事件
            //HSIHPSIDTaleLoader.HSIHPSIDTale tale = HSIHPSIDTaleLoader.TALES.get(taleId);
            List<Object> btn_param = new ArrayList<>();
            btn_param.add("HPSID_PROD");
            btn_param.add(index);
            TooltipMakerAPI holder = buttonHolder.createUIElement(TAB_BUTTON_WIDTH,
                    TAB_BUTTON_HEIGHT, false);

            ButtonAPI button = holder.addAreaCheckbox("#"+index, btn_param, base, bg, Color.white,
                    TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT, 0);
            button.setChecked(Objects.equals(index,ACTIVE_CONTRACT_INDEX));
            button.getPosition().inTL(0,0);
            if (rel != null) {
                buttonHolder.addUIElement(holder).belowLeft(rel,4);
            } else {
                buttonHolder.addUIElement(holder).inTL(0, 1);
            }
            rel = holder;
            index++;
            //getLogger().info("Added tale L:"+taleId);
        }
        columnL.addComponent(buttonHolder).inTL(0,0);
        //右栏：显示事件名称 事件内容 可能的插图
        if(ACTIVE_CONTRACT_INDEX <productionQueue.getItems().size()){
            FactionProductionAPI activeProd = productionQueue.getItems().get(ACTIVE_CONTRACT_INDEX);
            TooltipMakerAPI storyPanel = columnR.createUIElement((width - TAB_BUTTON_WIDTH-18f-2f),30,false);
            //storyPanel.setTitleOrbitronVeryLarge();
            storyPanel.addSectionHeading(HSII18nUtil.getCampaignString("HPSIDContracts_Title") +"#"+ACTIVE_CONTRACT_INDEX+"-"+
                            HSII18nUtil.getCampaignString("HPSIDContracts_TimeLeft")+String.format("%.1f",productionQueue.getRemaining(activeProd)) +" d",Alignment.MID,4);
            columnR.addUIElement(storyPanel).inTL(0,0);
            TooltipMakerAPI descPanel = columnR.createUIElement((width - TAB_BUTTON_WIDTH-18f-2f),height-8,false);
            for (FactionProductionAPI.ItemInProductionAPI item : activeProd.getCurrent()) {
                int count = item.getQuantity();

                if (item.getType() == FactionProductionAPI.ProductionItemType.SHIP) {
                    String hullid = item.getSpecId();
                    ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullid);
                    if(spec==null) continue;
                    TooltipMakerAPI t = descPanel.beginImageWithText(spec.getSpriteName(),32f);
                    t.addPara(spec.getHullNameWithDashClass()+" x"+count,5f);
                    descPanel.addImageWithText(10f);
                } else if (item.getType() == FactionProductionAPI.ProductionItemType.FIGHTER) {
                    String fighterid = item.getSpecId();
                    FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(fighterid);
                    if(spec==null) continue;
                    TooltipMakerAPI t = descPanel.beginImageWithText(spec.getVariant().getHullSpec().getSpriteName(),32f);
                    t.addPara(spec.getWingName()+" x"+count,5f);
                    descPanel.addImageWithText(10f);
                } else if (item.getType() == FactionProductionAPI.ProductionItemType.WEAPON) {
                    String weaponid = item.getSpecId();
                    WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponid);
                    if(spec==null) continue;
                    TooltipMakerAPI t = descPanel.beginImageWithText(spec.getTurretSpriteName(),32f);
                    t.addPara(spec.getWeaponName()+" x"+count,5f);
                    descPanel.addImageWithText(10f);
                }
            }
            columnR.addUIElement(descPanel).inTL(1,34);
            //getLogger().info("Added tale R:"+ACTIVE_TALE);
        }else{
            TooltipMakerAPI storyPanel = columnR.createUIElement((width - 140f),height-8,false);
            storyPanel.addPara(HSII18nUtil.getCampaignString("HPSIDContracts_Tutorial"),bright,pad);
            //getLogger().info("NO ACTIVE TALE:"+ACTIVE_TALE);
            columnR.addUIElement(storyPanel).inTL(1,4);
        }
        main.addComponent(columnL).inTL(6f, 4f);
        main.addComponent(columnR).inTL(TAB_BUTTON_WIDTH+12f, 4f);
        //main.addSpacer(0).getPosition().inBL(10,-10);
        //List<Object> param = new ArrayList<>();
        //main.addComponent(columnL).inTL(6f, 4f);
        //main.addComponent(columnR).inTL((width - 18f) / 2 + 12f, 4f);
        /*if (0 <= Global.getSector().getPlayerFleet().getCargo().getCredits().get()) {
            param.add("Confirm");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Confirm"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        } else {
            param.add("Cancel");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_ConfirmFail"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        }*/
        List<Object> param1 = new ArrayList<>();
        ButtonAPI b1 = main.addAreaCheckbox("", param1, base,
                bg, bg,
                columnL.getPosition().getWidth(), columnL.getPosition().getHeight(), 0);
        b1.getPosition().inTL(6,4f);
        b1.setClickable(false);
        b1.setEnabled(false);
        ButtonAPI b2 =  main.addAreaCheckbox("", param1, base,
                bg, bg,
                columnR.getPosition().getWidth(), columnR.getPosition().getHeight(), 0);
        b2.getPosition().inTL(TAB_BUTTON_WIDTH+12f, 4f);
        b2.setChecked(true);
        b2.setClickable(false);
        b2.setEnabled(false);
    }

    protected List<HSIDSSSItem> supplyitem = new ArrayList<>();
    {
        supplyitem.add(new HSIDSSSItem(Commodities.SUPPLIES));
        supplyitem.add(new HSIDSSSItem(Commodities.FUEL));
        supplyitem.add(new HSIDSSSItem(Commodities.HEAVY_MACHINERY));
    }

    private String ownerStr = pickRandomOwnerStr();

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    protected void drawSupplyTab(TooltipMakerAPI main, CustomPanelAPI panel, float width, float height) {
        float pad = 3;
        float opad = 10;
        Color h = Misc.getHighlightColor();
        FactionAPI fc = Global.getSector().getFaction("HSI");
        Color base = fc.getBaseUIColor(), bg = fc.getDarkUIColor(), bright = fc.getBrightUIColor();
        CustomPanelAPI columnL = panel.createCustomPanel((width - 18f) / 2, height, null);
        CustomPanelAPI columnR = panel.createCustomPanel((width - 18f) / 2, height, null);
        CustomPanelAPI shopOwner = columnR.createCustomPanel((width - 18f) / 2 - 4f, 150f, null);
        TooltipMakerAPI ownerTouch = shopOwner.createUIElement(96f, 96f, false);
        TooltipMakerAPI ownerImage = shopOwner.createUIElement(96f, 96f, false);
        boolean ownerIsScythe = ownerIsScythe();
        List<String> paramOwner = new ArrayList<>();
        paramOwner.add("Touch");
        ownerTouch.addAreaCheckbox("", paramOwner, base,
                bg, bright,
                100f, 100f, 0).getPosition().inTL(2f, -2f);
        ownerImage.addImage(ownerIsScythe?("graphics/portraits/IP/HSI_Scythe.png"):("graphics/portraits/portrait_corporate04.png"), 96f, 0);
        shopOwner.addUIElement(ownerTouch).inTL(2f, 2f);
        shopOwner.addUIElement(ownerImage).inTL(2f, 2f);
        TooltipMakerAPI ownerStrHolder = shopOwner.createUIElement((width - 18f) / 2 - 4f, 48f, false);
        ownerStrHolder.addPara(ownerStr, 0);
        shopOwner.addUIElement(ownerStrHolder).inTL(0, 102f);
        TooltipMakerAPI ownerTitle = shopOwner.createUIElement((width - 18f) / 2 - 104f, 100f, false);
        ownerTitle.addPara(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_Shop_Owner_Name"):HSII18nUtil.getCampaignString("HSI_Shop_Owner_Normal_Name"), 0);
        ownerTitle.addPara(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_Shop_Owner_Rank"):HSII18nUtil.getCampaignString("HSI_Shop_Owner_Normal_Rank"), 0);
        shopOwner.addUIElement(ownerTitle).inTL(102f, 0f);
        CustomPanelAPI total = columnR.createCustomPanel(150f, height - 100f, null);
        TooltipMakerAPI sum = total.createUIElement((width - 18f) / 2 - 4f, height - 260f, false);
        int sizeTotal = 0;
        float fee = 0;
        int index = 0;
        for (HSIDSSSItem i : supplyitem) {
            CustomPanelAPI p = i.drawItem(panel);
            columnL.addComponent(p).inTL(0f, 70f * index);
            sizeTotal += i.getSize();
            if (i.getSize() > 0) {
                sum.addPara(Global.getSettings().getCommoditySpec(i.getItemId()).getName() + " x " + i.getSize(), 0, h,
                        Global.getSettings().getCommoditySpec(i.getItemId()).getName());
                sum.addPara(
                        "= " + Misc.getDGSCredits(Global.getSettings().getCommoditySpec(i.getItemId()).getBasePrice())
                                + " x " + i.getSize(),
                        pad, h,
                        Misc.getDGSCredits(Global.getSettings().getCommoditySpec(i.getItemId()).getBasePrice()));
                fee += Global.getSettings().getCommoditySpec(i.getItemId()).getBasePrice() * i.getSize();
            }
            index++;
        }
        columnR.addComponent(shopOwner).inTL(2f, 2f);
        if (discount.getRest() > 0) {
            sum.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Discount"), opad, h);
            sum.addPara("= " + Misc.getDGSCredits(Math.min(fee, discount.getRest())), opad);
        }
        float num = getSupplyMult(sizeTotal) * Math.max(0, (fee - discount.getRest()));
        sum.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Deliver_Fee"), opad);
        sum.addPara("= " + Misc.getDGSCredits(num - Math.max(0, (fee - discount.getRest()))), opad);
        sum.addPara(HSII18nUtil.getCampaignString("HSI_Shop_Total"), opad, h,
                HSII18nUtil.getCampaignString("HSI_Shop_Total"));
        sum.addPara("= " + Misc.getDGSCredits(num), opad,
                (num > Global.getSector().getPlayerFleet().getCargo().getCredits().get())
                        ? Misc.getNegativeHighlightColor()
                        : h,
                Misc.getDGSCredits(num));
        total.addUIElement(sum).inTL(2f, 0f);
        columnR.addComponent(total).inTL(2f, 150f);
        List<Object> param = new ArrayList<>();
        main.addComponent(columnL).inTL(6f, 4f);
        main.addComponent(columnR).inTL((width - 18f) / 2 + 12f, 4f);
        if (num <= Global.getSector().getPlayerFleet().getCargo().getCredits().get()) {
            param.add("Confirm");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Confirm"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        } else {
            param.add("Cancel");
            main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_ConfirmFail"), param, base,
                    bg, bright,
                    width / 2 - 180, 30, 0).getPosition().inTL(60f, height - 60f);
        }
        List<Object> param1 = new ArrayList<>();
        param1.add("Cancel");
        main.addAreaCheckbox(HSII18nUtil.getCampaignString("HSI_Shop_Cancel"), param1, base,
                bg, bright,
                width / 2 - 180, 30, 0).getPosition().inTL(width / 2 - 60f, height - 60f);
    }

    protected boolean ownerIsScythe(){
        return Global.getSector().getMemoryWithoutUpdate().contains("$HSI_Scythe_Saved");
    }


    protected String pickRandomOwnerStr() {
        boolean ownerIsScythe = ownerIsScythe();
        WeightedRandomPicker<String> pool = new WeightedRandomPicker<>();
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatTouch0"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatTouch0"));
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatTouch1"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatTouch1"));
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatTouch2"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatTouch2"));
        return pool.pick();
    }

    protected String pickRandomOwnerConfirmStr() {
        boolean ownerIsScythe = ownerIsScythe();
        WeightedRandomPicker<String> pool = new WeightedRandomPicker<>();
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatConfirm0"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatConfirm0"));
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatConfirm1"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatConfirm1"));
        pool.add(ownerIsScythe?HSII18nUtil.getCampaignString("HSI_ShopOwner_ChatConfirm2"):HSII18nUtil.getCampaignString("HSI_ShopOwner_Normal_ChatConfirm2"));
        return pool.pick();
    }

    protected float getSupplyMult(int sizeTotal) {
        float m = 1;
        float dist = Misc.getDistance(Global.getSector().getPlayerFleet().getLocationInHyperspace(),
                new Vector2f(580f, -17370f));
        m = (float) Math.max(1, Math.min(3f, Math.sqrt(dist * sizeTotal / 10000f)));
        return m;
    }

    protected void drawMainProgressTab(TooltipMakerAPI main, CustomPanelAPI panel, float width, float height) {

        float opad = 10f;
        uiWidth = width;

        EventProgressBarAPI bar = main.addEventProgressBar(this, 100f);
        TooltipCreator barTC = getBarTooltip();
        if (barTC != null) {
            main.addTooltipToPrevious(barTC, TooltipLocation.BELOW, false);
        }

        for (EventStageData curr : stages) {
            if (curr.progress <= 0)
                continue; // no icon for "starting" stage
            // if (curr.rollData == null || curr.rollData.equals(RANDOM_EVENT_NONE))
            // continue;
            if (RANDOM_EVENT_NONE.equals(curr.rollData))
                continue;
            if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable)
                continue;

            if (curr.hideIconWhenPastStageUnlessLastActive &&
                    curr.progress <= progress &&
                    getLastActiveStage(true) != curr) {
                continue;
            }

            EventStageDisplayData data = createDisplayData(curr.id);
            UIComponentAPI marker = main.addEventStageMarker(data);
            float xOff = bar.getXCoordinateForProgress(curr.progress) - bar.getPosition().getX();
            marker.getPosition().aboveLeft(bar, data.downLineLength).setXAlignOffset(xOff - data.size / 2f - 1);

            TooltipCreator tc = getStageTooltip(curr.id);
            if (tc != null) {
                main.addTooltipTo(tc, marker, TooltipLocation.LEFT, false);
            }
        }
        // progress indicator
        {
            UIComponentAPI marker = main.addEventProgressMarker(this);
            float xOff = bar.getXCoordinateForProgress(progress) - bar.getPosition().getX();
            marker.getPosition().belowLeft(bar, -getBarProgressIndicatorHeight() * 0.5f - 2)
                    .setXAlignOffset(xOff - getBarProgressIndicatorWidth() / 2 - 1);
        }

        main.addSpacer(opad);
        main.addSpacer(opad);
        for (EventStageData curr : stages) {
            if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable)
                continue;
            addStageDescriptionWithImage(main, curr.id);
        }
        {
            String icon = getIcon();
            float imageSize = getImageSizeForStageDesc(Stage.START);
            // float opad = 10f;
            float indent = 0;
            indent = 10f;
            indent += getImageIndentForStageDesc(Stage.START);
            // float Dwidth = getBarWidth() - indent * 2f;

            TooltipMakerAPI info = main.beginImageWithText(icon, imageSize, width, true);
            // TooltipMakerAPI info =
            // main.beginImageWithText("graphics/icons/missions/ga_intro.png", 64);
            Color h = Misc.getHighlightColor();
            info.addPara(HSII18nUtil.getCampaignString("HPSIDIntelGuidence"), 0f, h,
                    HSII18nUtil.getCampaignString("HPSIDIntelGuidenceHL"));
            if (info.getHeightSoFar() > 0) {
                main.addImageWithText(opad).getPosition().setXAlignOffset(indent);
                main.addSpacer(0).getPosition().setXAlignOffset(-indent);
            }
        }

        float barW = getBarWidth();
        float factorWidth = (barW - opad) / 2f;

        if (withMonthlyFactors() != withOneTimeFactors()) {
            // factorWidth = barW;
            factorWidth = (int) (barW * 0.6f);
        }

        TooltipMakerAPI mFac = main.beginSubTooltip(factorWidth);

        Color c = getFactionForUIColors().getBaseUIColor();
        Color bg = getFactionForUIColors().getDarkUIColor();
        mFac.addSectionHeading("Monthly factors", c, bg, Alignment.MID, opad).getPosition().setXAlignOffset(0);

        float strW = 40f;
        float rh = 20f;
        // rh = 15f;
        mFac.beginTable2(getFactionForUIColors(), rh, false, false,
                "Monthly factors", factorWidth - strW - 3,
                "Progress", strW);

        for (EventFactor factor : factors) {
            if (factor.isOneTime())
                continue;
            if (!factor.shouldShow(this))
                continue;

            String desc = factor.getDesc(this);
            if (desc != null) {
                mFac.addRowWithGlow(Alignment.LMID, factor.getDescColor(this), desc,
                        Alignment.RMID, factor.getProgressColor(this), factor.getProgressStr(this));
                TooltipCreator t = factor.getMainRowTooltip(this);
                if (t != null) {
                    mFac.addTooltipToAddedRow(t, TooltipLocation.RIGHT, false);
                }
            }
            factor.addExtraRows(mFac, this);
        }

        // mFac.addButton("TEST", new String(), factorWidth, 20f, opad);
        mFac.addTable("None", -1, opad);
        mFac.getPrev().getPosition().setXAlignOffset(-5);

        main.endSubTooltip();

        TooltipMakerAPI oFac = main.beginSubTooltip(factorWidth);

        oFac.addSectionHeading("Recent one-time factors", c, bg, Alignment.MID, opad).getPosition().setXAlignOffset(0);

        oFac.beginTable2(getFactionForUIColors(), 20f, false, false,
                "One-time factors", factorWidth - strW - 3,
                "Progress", strW);

        List<EventFactor> reversed = new ArrayList<EventFactor>(factors);
        Collections.reverse(reversed);
        for (EventFactor factor : reversed) {
            if (!factor.isOneTime())
                continue;
            if (!factor.shouldShow(this))
                continue;

            String desc = factor.getDesc(this);
            if (desc != null) {
                oFac.addRowWithGlow(Alignment.LMID, factor.getDescColor(this), desc,
                        Alignment.RMID, factor.getProgressColor(this), factor.getProgressStr(this));
                TooltipCreator t = factor.getMainRowTooltip(this);
                if (t != null) {
                    oFac.addTooltipToAddedRow(t, TooltipLocation.LEFT);
                }
            }
            factor.addExtraRows(oFac, this);
        }

        oFac.addTable("None", -1, opad);
        oFac.getPrev().getPosition().setXAlignOffset(-5);
        main.endSubTooltip();

        float factorHeight = Math.max(mFac.getHeightSoFar(), oFac.getHeightSoFar());
        mFac.setHeightSoFar(factorHeight);
        oFac.setHeightSoFar(factorHeight);

        if (withMonthlyFactors() && withOneTimeFactors()) {
            main.addCustom(mFac, opad * 2f);
            main.addCustomDoNotSetPosition(oFac).getPosition().rightOfTop(mFac, opad);
        } else if (withMonthlyFactors()) {
            main.addCustom(mFac, opad * 2f);
        } else if (withOneTimeFactors()) {
            main.addCustom(oFac, opad * 2f);
        }

        // main.addButton("TEST", new String(), factorWidth, 20f, opad);

    }

    public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
        float opad = 10f;
        float small = 0f;
        Color h = Misc.getHighlightColor();
        EventStageData curr = getCurrentStage();
        if (curr == null) {

        } else {
            if ((Stage) stageId == ((Stage) curr.id)) {
                switch ((Stage)stageId){
                    case SENIOR_SHIP_PROVIDER:
                        info.addPara(HSII18nUtil.getCampaignString("HPSIDStageSSP"), small, h,
                                HSII18nUtil.getCampaignString("HPSIDStageSSPHL"));
                    case DEEPSPACE_SUPPLY:
                        info.addPara(HSII18nUtil.getCampaignString("HPSIDStageDSSS"), small, h,
                                HSII18nUtil.getCampaignString("HPSIDStageDSSSHL"));
                    case SENIOR_COOP:
                        info.addPara(HSII18nUtil.getCampaignString("HPSIDStageSeniorCoop"), small, h,
                                HSII18nUtil.getCampaignString("HPSIDStageSeniorCoopHL1"),
                                HSII18nUtil.getCampaignString("HPSIDStageSeniorCoopHL2"));
                    case JUNIOR_COOP:
                        info.addPara(HSII18nUtil.getCampaignString("HPSIDStageJuniorCoop"), small, h,
                                HSII18nUtil.getCampaignString("HPSIDStageJuniorCoopHL"));
                    case START:
                    default:
                        break;
                }
            }
        }
    }
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("ui", "HSI_HPSID");
    }

    @Override
    protected String getStageIcon(Object stageId) {
        EventStageData esd = getDataFor(stageId);
        if (esd != null) {
            return Global.getSettings().getSpriteName("events", "stage_unknown_neutral");
        }
        return super.getStageIcon(stageId);
    }

    @Override
    protected void advanceImpl(float amount) {
        if(Global.getSector().getFaction("HSI")==null) return;
        List<HSIShopRequirement> toRemove = new ArrayList<>(1);
        if (!shopRequirements.isEmpty()) {
            for (HSIShopRequirement r : shopRequirements) {
                if (r.isExpired()) {
                    if (createCargoPodForReqirementsAndHL(r))
                        toRemove.add(r);
                }
            }
        }
        shopRequirements.removeAll(toRemove);

        productionQueue.advance(Global.getSector().getClock().convertToDays(amount));
        //gger(this.getClass()).info("Production queue:"+productionQueue.getItems().size());
        String msgProd = "";
        for(FactionProductionAPI pro:productionQueue.getItems()){
            msgProd+=productionQueue.getRemaining(pro)+"||";
        }
        //Global.getLogger(this.getClass()).info(msgProd);
        List<FactionProductionAPI> toRemoveProd = new ArrayList<>();
        for(FactionProductionAPI data:productionQueue.getItems()){
            if(productionQueue.getRemaining(data)<=1f){
                toRemoveProd.add(data);
                covertProdToStorage(data);
            }
        }
        for(FactionProductionAPI data:toRemoveProd){
            productionQueue.remove(data);
        }
    }

    protected void covertProdToStorage(FactionProductionAPI prod){
        MarketAPI gatheringPoint =Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market");
        if (gatheringPoint == null) return;

        StoragePlugin plugin = (StoragePlugin) Misc.getStorage(gatheringPoint);
        if (plugin == null) return;
        plugin.setPlayerPaidToUnlock(true);

        Random genRandom = new Random();

        ProductionReportIntel.ProductionData data = new ProductionReportIntel.ProductionData();
        CargoAPI cargo = data.getCargo("Order manifest");

        float quality = ShipQuality.getShipQuality(gatheringPoint, gatheringPoint.getFactionId());

        CampaignFleetAPI ships = Global.getFactory().createEmptyFleet(gatheringPoint.getFactionId(), "temp", true);
        ships.setCommander(Global.getSector().getPlayerPerson());
        ships.getFleetData().setShipNameRandom(genRandom);
        DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
        p.quality = quality;
        p.mode = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        p.persistent = false;
        p.seed = genRandom.nextLong();
        p.timestamp = null;

        FleetInflater inflater = Misc.getInflater(ships, p);
        ships.setInflater(inflater);

        //int totalCost = 0;

        for (FactionProductionAPI.ItemInProductionAPI item : prod.getCurrent()) {
            int count = item.getQuantity();

            if (item.getType() == FactionProductionAPI.ProductionItemType.SHIP) {
                for (int i = 0; i < count; i++) {
                    ships.getFleetData().addFleetMember(item.getSpecId() + "_Hull");
                    //Global.getLogger(this.getClass()).info("Production ship:"+item.getSpecId());
                }
            } else if (item.getType() == FactionProductionAPI.ProductionItemType.FIGHTER) {
                cargo.addFighters(item.getSpecId(), count);
                //Global.getLogger(this.getClass()).info("Production ship:"+item.getSpecId());
            } else if (item.getType() == FactionProductionAPI.ProductionItemType.WEAPON) {
                cargo.addWeapons(item.getSpecId(), count);
                //Global.getLogger(this.getClass()).info("Production ship:"+item.getSpecId());
            }
        }


        // so that it adds d-mods
        ships.inflateIfNeeded();
        for (FleetMemberAPI member : ships.getFleetData().getMembersListCopy()) {
            // it should be due to the inflateIfNeeded() call, this is just a safety check
            if (member.getVariant().getSource() == VariantSource.REFIT) {
                member.getVariant().clear();
            }
            cargo.getMothballedShips().addFleetMember(member);
        }

        CargoAPI c = plugin.getCargo();

        for (CargoAPI curr : data.data.values()) {
            c.addAll(curr, true);
            Global.getLogger(this.getClass()).info("Production added.");
        }
        HSIForgeIntel intel = new HSIForgeIntel(gatheringPoint, data,
                prod.getTotalCurrentCost(), prod.getAccruedProduction(),
                false);
        Global.getSector().getIntelManager().addIntel(intel);
    }


    protected boolean createCargoPodForReqirementsAndHL(HSIShopRequirement req) {
        StarSystemAPI system = null;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player.getContainingLocation() instanceof StarSystemAPI) {
            system = (StarSystemAPI) player.getContainingLocation();
        } else {
            List<StarSystemAPI> sys = Misc.getSystemsInRange(player, null, true, 3000f);
            if (!sys.isEmpty())
                system = sys.get(0);
        }
        if (system != null) {
            JumpPointAPI jp = Misc.getDistressJumpPoint(system);
            if (jp != null) {
                SectorEntityToken pod = system.addCustomEntity(null, HSII18nUtil.getCampaignString("HSI_Shop_Pod_Name"),
                        Entities.CARGO_PODS, Factions.NEUTRAL);
                pod.setCircularOrbit(jp, (float) Math.random() * 100f, 100f + (float) Math.random() * 100f,
                        15f + (float) Math.random() * 10f);
                pod.getCargo().addAll(req.getCargo());
                CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin) pod.getCustomPlugin();
                if (plugin != null)
                    plugin.setNeverExpire(true);
                Misc.makeImportant(pod, getName());
            } else {
                SectorEntityToken pod = system.addCustomEntity(null, HSII18nUtil.getCampaignString("HSI_Shop_Pod_Name"),
                        Entities.CARGO_PODS, Factions.NEUTRAL);
                Vector2f loc = Misc.getPointWithinRadius(player.getLocation(), 2000f);
                pod.setLocation(loc.x, loc.y);
                pod.getCargo().addAll(req.getCargo());
                CargoPodsEntityPlugin plugin = (CargoPodsEntityPlugin) pod.getCustomPlugin();
                if (plugin != null)
                    plugin.setNeverExpire(true);
                Misc.makeImportant(pod, getName());
            }
        } else {
            return false;
        }
        MessageIntel msg = new MessageIntel();
        msg.setIcon("graphics/factions/crest_HPSID.png");
        msg.addLine(HSII18nUtil.getCampaignString("HSI_Shop_Arrival"), Misc.getTextColor(),
                new String[] { req.getDescStr(), system.getName() }, Misc.getHighlightColor());
        Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);

        return true;
    }

    public int getMonthlyProgress() {
        int total = 0;
        float mult = 1f;
        for (EventFactor factor : factors) {
            if (factor.isOneTime())
                continue;
            total += factor.getProgress(this);
            // mult *= factor.getAllProgressMult(this);
        }

        total = Math.min(total, 100);

        return total;
    }

    public EventStageData getCurrentStage() {
        EventStageData toReturn = null;
        for (EventStageData stage : stages) {
            if (toReturn == null)
                toReturn = stage;
            if (progress >= stage.progress && stage.progress > toReturn.progress) {
                toReturn = stage;
            }
        }
        return toReturn;
    }

    protected String getName() {
        return HSII18nUtil.getCampaignString("HPSIDIntelName");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("HSI");
        return tags;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI arg0, CampaignFleetAPI arg1, BattleAPI arg2) {

    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI arg0, FleetDespawnReason arg1, Object arg2) {

    }

    //private int totalProgressFromLastCheck = 0;
    //private int totalMonthFromLastCheck = 0;

    public void addFactor(EventFactor factor) {
        super.addFactor(factor);
        //totalProgressFromLastCheck += factor.getProgress(this);
    }

    protected static final int cost0 = 1;
    protected static final int cost1 = 10;
    protected static final int cost2 = 50;
    protected static final int cost3 = 300;

    public void reportEconomyMonthEnd() {
        if (Global.getSector().getPlayerFleet() == null || Global.getSector().getPlayerFleet().getCargo() == null
                || Global.getSector().getFaction("HSI") == null)
            return;
        /*if (totalMonthFromLastCheck >= 1) {
            if (totalProgressFromLastCheck > 0) {
                MessageIntel msg = new MessageIntel();
                String str = ""+totalProgressFromLastCheck;
                msg.setIcon("graphics/factions/crest_HPSID.png");
                msg.addLine(HSII18nUtil.getCampaignString("HPSIDMonthlyReward"), Misc.getTextColor(),
                        new String[] { "+" + str }, Misc.getHighlightColor());
                Global.getSector().getCampaignUI().addMessage(msg, MessageClickAction.NOTHING);
            }

            while (totalProgressFromLastCheck > 0) {
                if (totalProgressFromLastCheck >= cost3 && getProgress() >= getDataFor(Stage.SENIOR_COOP).progress) {
                    WeightedRandomPicker<CargoStackAPI> p3 = HSIHPSIDMonthlyRewards.getTierPicker(3);
                    CargoStackAPI r = p3.pick();
                    totalProgressFromLastCheck -= cost3;
                    Global.getSector().getPlayerFleet().getCargo().addFromStack(r);
                } else if (totalProgressFromLastCheck >= cost2
                        && getProgress() >= getDataFor(Stage.SENIOR_COOP).progress) {
                    WeightedRandomPicker<CargoStackAPI> p2 = HSIHPSIDMonthlyRewards.getTierPicker(2);
                    CargoStackAPI r = p2.pick();
                    totalProgressFromLastCheck -= cost2;
                    Global.getSector().getPlayerFleet().getCargo().addFromStack(r);
                    continue;
                } else if (totalProgressFromLastCheck >= cost1) {
                    WeightedRandomPicker<CargoStackAPI> p1 = HSIHPSIDMonthlyRewards.getTierPicker(1);
                    CargoStackAPI r = p1.pick();
                    totalProgressFromLastCheck -= cost1;
                    Global.getSector().getPlayerFleet().getCargo().addFromStack(r);
                    continue;
                } else if (totalProgressFromLastCheck >= cost0) {
                    WeightedRandomPicker<CargoStackAPI> p0 = HSIHPSIDMonthlyRewards.getTierPicker(0);
                    CargoStackAPI r = p0.pick();
                    totalProgressFromLastCheck -= cost0;
                    Global.getSector().getPlayerFleet().getCargo().addFromStack(r);
                    continue;
                }
            }
            totalProgressFromLastCheck = 0;
            totalMonthFromLastCheck -=12;
        }
        totalMonthFromLastCheck++;*/
        switch ((Stage) getCurrentStage().id) {
            case DEEPSPACE_SUPPLY:
                discount.setMax(15000f);
                break;
            default:
                discount.setMax(0);
                break;

        }
        discount.refresh();

        if(SKIP>0){
            SKIP--;
        }else{
            FORGE_VALUE = getCurrentLimForLevel(((Stage)(getCurrentStage().id)).ordinal());
            SKIP = MAX_SKIP;
        }

    }

    public void reportEconomyTick(int iterIndex) {
        if (Global.getSector().getFaction("HSI") == null)
            return;
        int credits = computeCreditsPerTick();

        if (credits != 0) {
            FDNode node = getMonthlyReportNode();
            if (credits > 0) {
                node.income += credits;
            } else if (credits < 0) {
                node.upkeep -= credits;
            }
        }
    }

    public FDNode getMonthlyReportNode() {
        MonthlyReport report = SharedData.getData().getCurrentReport();
        FDNode marketsNode = report.getNode(MonthlyReport.EXPORTS);
        if (marketsNode.name == null) {
            marketsNode.name = "Exports";
            marketsNode.custom = MonthlyReport.EXPORTS;
            marketsNode.tooltipCreator = report.getMonthlyReportTooltip();
        }

        FDNode paymentNode = report.getNode(marketsNode, "HPSIDTrade");
        paymentNode.name = "HPSID" + HSII18nUtil.getCampaignString("HPSIDStageSeniorCoopHL2");
        // paymentNode.upkeep += payment;
        // paymentNode.icon = Global.getSettings().getSpriteName("income_report",
        // "generic_expense");
        paymentNode.icon = Global.getSector().getFaction("HSI").getCrest();

        if (paymentNode.tooltipCreator == null) {
            paymentNode.tooltipCreator = new TooltipCreator() {
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                public float getTooltipWidth(Object tooltipParam) {
                    return 450;
                }

                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addPara(HSII18nUtil.getCampaignString("HPSIDFNDesc"), 0f);
                }
            };
        }

        return paymentNode;
    }

    public int computeCreditsPerMonth() {
        int perTick = computeCreditsPerTick();
        float numIter = Global.getSettings().getFloat("economyIterPerMonth");
        return (int) (perTick * numIter);
    }

    public static int computeCreditsPerTick() {
        // float numIter = Global.getSettings().getFloat("economyIterPerMonth");
        // float f = 1f / numIter;

        int payment = 0;
        HSIHPSIDLevelIntel intel = HSIHPSIDLevelIntel.getInstance();
        // EventStageData stage;
        int max = 0;
        for (EventStageData curr : intel.getStages()) {
            if (!curr.wasEverReached) {
                continue;
            } else {
                if (curr.progress > max) {
                    // stage = curr;
                    payment+=1000;
                }
            }
        }
        return payment;
    }

    @Override
    public boolean isHidden() {
        return (Global.getSector().getFaction("HSI")==null)||((Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().contains("$HSIisTraitor")&&Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().getBoolean("$HSIisTraitor")));
    }












}