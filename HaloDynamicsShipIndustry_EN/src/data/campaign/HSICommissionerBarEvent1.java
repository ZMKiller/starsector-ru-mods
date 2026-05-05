package data.campaign;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.kit.HSIIds;

public class HSICommissionerBarEvent1 extends BaseBarEventWithPerson {
    public static final Color TEXT = new Color(255, 140, 200, 255);
    public static final Color T = Misc.getTextColor();
    protected boolean battle_occured = false;
    protected boolean once = true;

    public enum OptionId {
        START, CHAT1, CHAT2, CHAT3, CHAT4, END;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market))
            return false;
        // 获取派系id
        if (!market.getFactionId().contentEquals("HSI")) {
            return false;
        }

        return Global.getSector().getPlayerStats().getLevel() >= 0
                && (Global.getSector().getFaction("HSI") != null
                        && Global.getSector().getFaction("HSI").getRelToPlayer().getRel() >= 0.5f)
                //&& Global.getSector().getMemoryWithoutUpdate().contains("$gaATG_missionCompleted")
                && !Global.getSector().getMemoryWithoutUpdate().contains("$HSITC0UnFinished")
                && Global.getSector().getMemoryWithoutUpdate().contains("$HSITC0Finished");
    }

    @Override
    protected void regen(MarketAPI market) {
        if (this.market == market)
            return;
        this.market = market;
        // 设置剧情对话人/姓名/性别
        PersonAPI commissioner = Global.getSector().getImportantPeople().getPerson(HSIIds.PERSON.THE_COMMISSIONER);

        if (commissioner != null) {
            person = commissioner;
        }
    }

    // 设置开局对话
    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);

        regen(dialog.getInteractionTarget().getMarket());

        TextPanelAPI text = dialog.getTextPanel();

        text.addPara(HSII18nUtil.getCampaignString("HSITC1_start"), TEXT);

        dialog.getOptionPanel().addOption(HSII18nUtil.getCampaignString("HSITC1_startopt"), this,
                TEXT, null);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        done = false;

        dialog.getVisualPanel().showPersonInfo(person, true);

        optionSelected(null, OptionId.START);
    }

    public void optionSelected(String optionText, Object optionData) {
        if (!(optionData instanceof OptionId)) {
            return;
        }
        OptionId option = (OptionId) optionData;

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();

        // 剧情部分
        switch (option) {
            case START:
                text.addPara(HSII18nUtil.getCampaignString("HSITC1_start"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC1_startopt"), OptionId.CHAT1);
                break;
            case CHAT1:
                text.addPara(HSII18nUtil.getCampaignString("HSI_TC1_text0"), T,
                        Global.getSector().getPlayerPerson().getName().getFirst());
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt1_1"), OptionId.CHAT2);
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt1_2"), OptionId.CHAT2);
                break;
            case CHAT2:
                text.addPara(HSII18nUtil.getCampaignString("HSI_TC1_text1"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt2_1"), OptionId.CHAT3);
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt2_2"), OptionId.CHAT3);
                break;
            case CHAT3:
                text.addPara(HSII18nUtil.getCampaignString("HSI_TC1_text2"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt3_1"), OptionId.CHAT4);
                break;
            case CHAT4:
                text.addPara(HSII18nUtil.getCampaignString("HSI_TC1_text3"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSI_TC1_opt4_1"), OptionId.END);
                break;
            case END:
                noContinue = true;
                done = true;
                final FleetMemberAPI extra1 = Global.getSector().getPlayerFleet().getFleetData()
                        .addFleetMember("HSI_T_01_68_Elite");
                ShipVariantAPI copy1 = extra1.getVariant().clone(); // 获取装配的复制寄存到copy
                extra1.setVariant(copy1, false, false); // 设置装配（不更新的）
                extra1.getRepairTracker().setCR(1f); // 为所有成员回满cr
                text.addPara(HSII18nUtil.getCampaignString("HSI_TC1_getShip"), T, extra1.getShipName());
                BarEventManager.getInstance().notifyWasInteractedWith(this);
                // Global.getLogger(this.getClass()).info("HSITC0-end");
                break;
            default:
                break;

        }
    }

}
