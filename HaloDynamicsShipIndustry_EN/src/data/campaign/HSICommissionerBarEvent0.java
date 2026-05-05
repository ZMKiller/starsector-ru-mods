package data.campaign;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;
import data.kit.HSIIds;

public class HSICommissionerBarEvent0 extends BaseBarEventWithPerson {
    public static final Color TEXT = new Color(255, 140, 200, 255);
    public static final Color T = Misc.getTextColor();
    protected boolean battle_occured = false;
    protected boolean once = true;

    public enum OptionId {
        START, CHAT1_1, CHAT1_2, CHAT2, CHAT3_1, CHAT3_2, BATTLE_HARD, FINISH_BATTLE, LEAVE, END;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (!super.shouldShowAtMarket(market))
            return false;
        // 获取派系id
        if (market.getFaction().isHostileTo("HSI")) {
            return false;
        }

        return Global.getSector().getPlayerStats().getLevel() >= 0
                && (Global.getSector().getFaction("HSI") != null
                        && Global.getSector().getFaction("HSI").getRelToPlayer().getRel() >= 0.7f);
        // &&
        // Global.getSector().getMemoryWithoutUpdate().contains("$gaATG_missionCompleted");
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

        text.addPara(HSII18nUtil.getCampaignString("HSITC0_start"), TEXT);

        dialog.getOptionPanel().addOption(HSII18nUtil.getCampaignString("HSITC0_startopt"), this,
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
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_start"), T,
                        Global.getSector().getPlayerPerson().getName().getFirst());
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt1_1"), OptionId.CHAT1_1);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt1_2"), OptionId.CHAT1_2);
                break;
            case CHAT1_1:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text1_1"), T,
                        Global.getSector().getPlayerPerson().getName().getFirst());
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt2"), OptionId.CHAT2);
                Global.getSector().getMemoryWithoutUpdate().set("$HSITC0Naughty", true);
                break;
            case CHAT1_2:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text1_2"), T,
                        Global.getSector().getPlayerPerson().getName().getFirst());
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt2"), OptionId.CHAT2);
                Global.getSector().getMemoryWithoutUpdate().set("$HSITC0Slience", true);
                break;
            case CHAT2:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text2"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt3_2"), OptionId.CHAT3_2);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt3_1"), OptionId.CHAT3_1);
                break;
            case CHAT3_1:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text3_1"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_1"), OptionId.BATTLE_HARD);
                // options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_2"),
                // OptionId.BATTLE_EASY);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_3"), OptionId.LEAVE);
                break;
            case CHAT3_2:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text3_2"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_1"), OptionId.BATTLE_HARD);
                // options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_2"),
                // OptionId.BATTLE_EASY);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_opt4_3"), OptionId.LEAVE);
                break;
            case FINISH_BATTLE:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text4_1"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_end"), OptionId.END);
                break;
            case BATTLE_HARD:
                if (dialog == null)
                    break;
                Global.getLogger(this.getClass()).info("Battle Occured " + battle_occured);
                if (!battle_occured) {
                    // if(battle_occured) optionSelected("", OptionId.FINISH_BATTLE);
                    Global.getLogger(this.getClass()).info("Battle hard");
                    final SectorEntityToken entity = dialog.getInteractionTarget();
                    final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
                    SDMParams p = new SDMParams();
                    p.entity = entity;
                    final CampaignFleetAPI defenders = FleetFactoryV3.createEmptyFleet("HSI",
                            FleetTypes.PATROL_SMALL, null); // 创建一个舰队
                    if (!defenders.isEmpty()) {
                        defenders.clearAbilities();
                        defenders.getFleetData().sort();
                    }
                    defenders.getFleetData().clear(); // 清除舰队
                    defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                    defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                    defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                    defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                    defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                    defenders.setName("猎手"); // 切换名字
                    defenders.setNoFactionInName(true); // 不显示势力
                    for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队，取舰船
                        PersonAPI ps = Global.getFactory().createPerson();
                        ps.setGender(FullName.Gender.FEMALE);
                        ps.getName().setFirst("??");
                        ps.getName().setLast(" ");
                        ps.setPersonality(Personalities.AGGRESSIVE);
                        ps.setId("Tamamo");
                        ps.getStats().setLevel(8);
                        ps.getStats().setSkillLevel("helmsmanship", 2);
                        ps.getStats().setSkillLevel("combat_endurance", 2);
                        ps.getStats().setSkillLevel("target_analysis", 2);
                        ps.getStats().setSkillLevel("systems_expertise", 2);
                        ps.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Hostile.png"); // 定义person
                        member.setCaptain(ps);
                        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR()); // 为所有成员回满cr
                    }
                    if (defenders.getInflater() instanceof DefaultFleetInflater) { // 不知道干啥 但是原版舰队有
                        DefaultFleetInflater dfi = (DefaultFleetInflater) defenders.getInflater();
                        ((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true; //
                    }
                    for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队成员
                        ShipVariantAPI copy = member.getVariant().clone(); // 获取装配的复制寄存到copy
                        member.setVariant(copy, false, false); // 设置装配（不更新的）
                        copy.addTag(Tags.SHIP_LIMITED_TOOLTIP); // 将装配的复制添加tag：SHIP_LIMITED_TOOLTIP//不知道干啥 但是原版舰队有
                    }
                    for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                        member.getRepairTracker().setMothballed(true);
                    }
                    final FleetMemberAPI extra1 = Global.getSector().getPlayerFleet().getFleetData()
                            .addFleetMember("HSI_T_01_68_Elite");
                    Global.getLogger(this.getClass()).info("AddShip");
                    PersonAPI ps1 = Global.getFactory().createPerson();
                    ps1.setGender(FullName.Gender.FEMALE);
                    ps1.getName().setFirst("??");
                    ps1.getName().setLast(" ");
                    ps1.setPersonality(Personalities.AGGRESSIVE);
                    ps1.setId("Tamamo");
                    ps1.getStats().setLevel(8);
                    ps1.getStats().setSkillLevel("helmsmanship", 2);
                    ps1.getStats().setSkillLevel("combat_endurance", 2);
                    ps1.getStats().setSkillLevel("target_analysis", 2);
                    ps1.getStats().setSkillLevel("systems_expertise", 2);
                    ps1.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Hostile.png"); // 定义person
                    ShipVariantAPI copy1 = extra1.getVariant().clone(); // 获取装配的复制寄存到copy
                    extra1.setVariant(copy1, false, false); // 设置装配（不更新的）
                    extra1.setCaptain(ps1);
                    extra1.getRepairTracker().setCR(1f); // 为所有成员回满cr

                    dialog.setInteractionTarget(defenders);

                    final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig(); // 获取一个原生的防卫舰队config
                    config.firstTimeEngageOptionText = "VOICE ONLY"; // 开始定义一些tag用作后来的逻辑，虽然我这里一个都没有用到
                    config.afterFirstTimeEngageOptionText = null;
                    config.leaveAlwaysAvailable = false;
                    config.showCommLinkOption = false;
                    config.showEngageText = true;
                    config.showFleetAttitude = false;
                    config.showTransponderStatus = false;
                    config.showWarningDialogWhenNotHostile = false;
                    config.alwaysAttackVsAttack = true;
                    config.impactsAllyReputation = false;
                    config.impactsEnemyReputation = false;
                    config.pullInAllies = false;
                    config.pullInEnemies = false;
                    config.pullInStations = false;
                    config.lootCredits = false;
                    config.straightToEngage = true;
                    config.dismissOnLeave = false;
                    config.printXPToDialog = true;
                    config.withSalvage = false;
                    final HSIForceCombat plugin = new HSIForceCombat(config); // 实例化一个插件
                    final InteractionDialogPlugin originalPlugin = dialog.getPlugin(); // 不知道干啥但是原版有
                    final HSICommissionerBarEvent0 event = this;
                    config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                        public void notifyLeave(InteractionDialogAPI dialog) { // 设置战斗结束的互动
                            defenders.getMemoryWithoutUpdate().clear();
                            defenders.clearAssignments();
                            defenders.deflate();
                            // dialog.setPlugin(originalPlugin);
                            if (plugin.getContext() instanceof FleetEncounterContext) {
                                FleetEncounterContext context = (FleetEncounterContext) plugin.getContext(); // 取战斗结果
                                if (context.didPlayerWinEncounterOutright()) {
                                    memory.unset("$hasDefenders");
                                    memory.unset("$defenderFleet");
                                    memory.set("$defenderFleetDefeated", true);
                                    // Global.getLogger(this.getClass()).info("End Win");
                                    // event.optionSelected("", OptionId.FINISH_BATTLE);
                                    Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra1);
                                    //Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra2);
                                    for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                            .getMembersWithFightersCopy()) {
                                        member.getRepairTracker().setMothballed(false);
                                        member.getRepairTracker().setCR(0.7f);
                                    }
                                    // battle_occured = true;
                                    // event.optionSelected("", OptionId.END);
                                    // FireBest.fire(null, dialog, memoryMap, "BeatDefendersContinue"); //
                                    // dialog.dismiss();
                                    // 从rules触发"BeatDefendersContinue"
                                    // optionSelected("", FleetInteractionDialogPluginImpl.OptionId.LEAVE);
                                    dialog.setPlugin(originalPlugin);
                                    dialog.setInteractionTarget(entity);
                                    // originalPlugin.init(dialog);
                                    event.optionSelected("", OptionId.FINISH_BATTLE);
                                } else {
                                    memory.unset("$hasDefenders");
                                    memory.unset("$defenderFleet");
                                    memory.set("$defenderFleetDefeated", true);
                                    // Global.getLogger(this.getClass()).info("End defeat");
                                    Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra1);
                                    //Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra2);
                                    for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                            .getMembersWithFightersCopy()) {
                                        member.getRepairTracker().setMothballed(false);
                                        member.getRepairTracker().setCR(0.7f);
                                    }
                                    // optionSelected("", FleetInteractionDialogPluginImpl.OptionId.LEAVE);
                                    // FireBest.fire(null, dialog, memoryMap, "BeatDefendersContinue");
                                    // context.getBattle().finish(context.getBattle().pickSide(Global.getSector().getPlayerFleet()));
                                    // battle_occured = true;
                                    dialog.setPlugin(originalPlugin);
                                    dialog.setInteractionTarget(entity);
                                    // originalPlugin.init(dialog);
                                    event.optionSelected("", OptionId.FINISH_BATTLE);
                                    // dialog.dismiss();
                                    // 从rules触发"BeatDefendersContinue"
                                    // dialog.getPlugin().optionSelected("", OptionId.LEAVE);
                                }

                            }
                        }

                        public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) { // 下面都不知道什么用，但是得要有
                            bcc.aiRetreatAllowed = false;
                            bcc.enemyDeployAll = true;
                        }

                        public void postPlayerSalvageGeneration(InteractionDialogAPI dialog,
                                FleetEncounterContext context, CargoAPI salvage) {
                        }
                    };
                    dialog.setPlugin(plugin); // 特别是这两句，否则会一直显示玩家舰队炸完了的互动。
                    plugin.init(dialog);
                    battle_occured = true;
                } else {
                    // text.addPara(HSII18nUtil.getCampaignString("HSITC0_text4_1"), T);
                    // options.addOption(HSII18nUtil.getCampaignString("HSITC0_end"), OptionId.END);
                }
                break;
            case LEAVE:
                text.addPara(HSII18nUtil.getCampaignString("HSITC0_text4_2"), T);
                options.addOption(HSII18nUtil.getCampaignString("HSITC0_end"), OptionId.END);
                Global.getSector().getMemoryWithoutUpdate().set("$HSITC0UnFinished", true);
                break;
            case END:
                noContinue = true;
                done = true;
                CustomRepImpact impact = new CustomRepImpact();
                impact.limit = RepLevel.COOPERATIVE;
                impact.delta = 0.1f;
                Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact,
								null, text, true),
						person);
                BarEventManager.getInstance().notifyWasInteractedWith(this);
                if (!Global.getSector().getMemoryWithoutUpdate().contains("$HSITC0UnFinished")) {
                    Global.getSector().getMemoryWithoutUpdate().set("$HSITC0Finished", true);
                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(150000);
                    text.addPara(HSII18nUtil.getCampaignString("HSITC0_text4_2"), T);
                    AddRemoveCommodity.addCreditsGainText(150000, text);
                    BarEventManager.getInstance().notifyWasInteractedWith(this);
                    // Global.getLogger(this.getClass()).info("HSITC0-end");
                }
                // Global.getLogger(this.getClass()).info("HSITC0-end");
                break;
            default:
                break;

        }
    }

}
