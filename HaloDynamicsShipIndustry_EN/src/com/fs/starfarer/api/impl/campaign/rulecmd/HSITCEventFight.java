package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.util.Misc.Token;
import data.campaign.HSIForceCombat;
import data.campaign.HSITTMission.HSITTMissionCombat;

import java.util.List;
import java.util.Map;

import static com.fs.starfarer.api.impl.campaign.rulecmd.AddShip.addShipGainText;

public class HSITCEventFight extends BaseCommandPlugin{
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        String command = params.get(0).getString(memoryMap);
        switch (command) {
            case "TC0":
            {
                //Global.getLogger(this.getClass()).info("Battle hard");
                final SectorEntityToken entity = dialog.getInteractionTarget();
                final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
                SDMParams p = new SDMParams();
                p.entity = entity;
                final Map<String, MemoryAPI> memCopy = memoryMap;
                final CampaignFleetAPI defenders = FleetFactoryV3.createEmptyFleet("HSI",
                        FleetTypes.PATROL_SMALL, null); // 创建一个舰队
                if (!defenders.isEmpty()) {
                    defenders.clearAbilities();
                    defenders.getFleetData().sort();
                }
                //final int crew = Global.getSector().getPlayerFleet().getCargo().getCrew();
                //final int marine = Global.getSector().getPlayerFleet().getCargo().getMarines();
                defenders.getFleetData().clear(); // 清除舰队
                defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                defenders.getFleetData().addFleetMember("HSI_Weaver_Strike");
                defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                defenders.setName("Unknown"); // 切换名字
                defenders.setNoFactionInName(true); // 不显示势力
                if (defenders.getInflater() instanceof DefaultFleetInflater) { // 不知道干啥 但是原版舰队有
                    DefaultFleetInflater dfi = (DefaultFleetInflater) defenders.getInflater();
                    ((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true; //
                }
                for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队成员
                    ShipVariantAPI copy = member.getVariant().clone(); // 获取装配的复制寄存到copy
                    member.setVariant(copy, false, false); // 设置装配（不更新的）
                    copy.addTag(Tags.SHIP_LIMITED_TOOLTIP); // 将装配的复制添加tag：SHIP_LIMITED_TOOLTIP//不知道干啥 但是原版舰队有
                }
                final CampaignFleetAPI temp = Global.getFactory().createEmptyFleet(Factions.PLAYER,"??",false);
                temp.getCargo().addCrew(200);
                temp.addAbility(Abilities.TRANSPONDER);
                final FleetMemberAPI extra1 = temp.getFleetData()
                        .addFleetMember("HSI_T_01_68_Elite");
                PersonAPI ps1 = Global.getFactory().createPerson();
                ps1.setGender(FullName.Gender.FEMALE);
                ps1.getName().setFirst("??");
                ps1.getName().setLast(" ");
                ps1.setPersonality(Personalities.AGGRESSIVE);
                ps1.setId("Tamamo");
                ps1.getStats().setLevel(10);
                ps1.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
                ps1.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
                ps1.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
                ps1.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
                ps1.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
                ps1.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
                ps1.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
                ps1.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
                ps1.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                ps1.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
                ps1.getStats().setSkillLevel(Skills.NAVIGATION, 1);
                ps1.setPortraitSprite("graphics/portraits/IP/HSI_TheCommisioner_Hostile.png"); // 定义person
                ShipVariantAPI copy1 = extra1.getVariant().clone(); // 获取装配的复制寄存到copy
                extra1.setVariant(copy1, false, false); // 设置装配（不更新的）
                extra1.setCaptain(ps1);
                extra1.getRepairTracker().setCR(1f); // 为所有成员回满cr
                extra1.getStats().getHullDamageTakenMult().modifyMult("HSI_TC_EVENT_USE", 0.05f);
                extra1.getStats().getDamageToDestroyers().modifyMult("HSI_TC_EVENT_USE", 2f);
                extra1.updateStats();

                final FleetMemberAPI extra2 = temp.getFleetData()
                        .addFleetMember("HSI_T_01_68_Elite");
                ShipVariantAPI copy2 = extra2.getVariant().clone(); // 获取装配的复制寄存到copy
                extra2.setVariant(copy2, false, false); // 设置装配（不更新的）
                extra2.getRepairTracker().setCR(1f); // 为所有成员回满cr
                extra2.getStats().getHullDamageTakenMult().modifyMult("HSI_TC_EVENT_USE", 0.05f);
                extra2.getStats().getDamageToDestroyers().modifyMult("HSI_TC_EVENT_USE", 2f);
                extra2.updateStats();

                extra2.setFlagship(true);

                final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

                playerFleet.getContainingLocation().spawnFleet(
                        playerFleet.getInteractionTarget(),
                        playerFleet.getLocation().getX() + 100000f,
                        playerFleet.getLocation().getY() + 100000f,
                        temp);

                Global.getSector().setPlayerFleet(temp);
                temp.getCargo().addCrew(300);

                playerFleet.setAI(null);
                playerFleet.setNoEngaging(3f);

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
                //final HSICommissionerBarEvent0 event = this;
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
                                /*for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                        .getMembersWithFightersCopy()) {
                                    member.getRepairTracker().setMothballed(false);
                                    member.getRepairTracker().setCR(1f);
                                }*/
                                /*if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                                    CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
                                    int currMarine = playerCargo.getMarines();
                                    int currCrew = playerCargo.getCrew();
                                    if (currCrew < crew) {
                                        playerCargo.addCrew(crew - currCrew);
                                    }
                                    if (currMarine < marine) {
                                        playerCargo.addMarines(marine - currMarine);
                                    }
                                }*/
                                // battle_occured = true;
                                // event.optionSelected("", OptionId.END);
                                // FireBest.fire(null, dialog, memoryMap, "BeatDefendersContinue"); //
                                // dialog.dismiss();
                                // 从rules触发"BeatDefendersContinue"
                                // optionSelected("", FleetInteractionDialogPluginImpl.OptionId.LEAVE);
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                //Global.getSector().setPlayerFleet(playerFleet);
                                // originalPlugin.init(dialog);
                                //event.optionSelected("", OptionId.FINISH_BATTLE);
                            } else {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                // Global.getLogger(this.getClass()).info("End defeat");
                                Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra1);
                                //Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra2);
                                /*for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                        .getMembersWithFightersCopy()) {
                                    member.getRepairTracker().setMothballed(false);
                                    member.getRepairTracker().setCR(1f);
                                }*/
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                            }
                            /*if (Global.getSector().getPlayerFleet() != null && Global.getSector().getPlayerFleet().getCargo() != null) {
                                CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
                                int currMarine = playerCargo.getMarines();
                                int currCrew = playerCargo.getCrew();
                                if (currCrew < crew) {
                                    playerCargo.addMarines(crew - currCrew);
                                }
                                if (currMarine < marine) {
                                    playerCargo.addMarines(marine - currMarine);
                                }
                            }*/
                            Global.getSector().setPlayerFleet(playerFleet);
                            Global.getSector().getPlayerFleet().getContainingLocation().removeEntity(temp);
                            MemoryAPI m = getEntityMemory(memCopy);
                            memory.set("$option", "HSI_TC0_End");
                            FireBest.fire(null, dialog, memCopy, "DialogOptionSelected");
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
            }
            break;
            case "TTMission": {
                final SectorEntityToken entity = dialog.getInteractionTarget();
                final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
                SDMParams p = new SDMParams();
                p.entity = entity;
                final Map<String, MemoryAPI> memCopy = memoryMap;
                final CampaignFleetAPI defenders = FleetFactoryV3.createEmptyFleet(Factions.TRITACHYON,
                        FleetTypes.PATROL_SMALL, null); // 创建一个舰队
                defenders.getMemoryWithoutUpdate().set("$HSI_TTMISSION",true);
                if (!defenders.isEmpty()) {
                    defenders.clearAbilities();
                    defenders.getFleetData().sort();
                }
                defenders.getFleetData().clear(); // 清除舰队
                defenders.getFleetData().addFleetMember("wolf_Assault");
                defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                defenders.setName(Global.getSector().getFaction(Factions.TRITACHYON).getFleetTypeName(FleetTypes.PATROL_SMALL)); // 切换名字
                if (defenders.getInflater() instanceof DefaultFleetInflater) { // 不知道干啥 但是原版舰队有
                    DefaultFleetInflater dfi = (DefaultFleetInflater) defenders.getInflater();
                    ((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true; //
                }
                for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队成员
                    ShipVariantAPI copy = member.getVariant().clone(); // 获取装配的复制寄存到copy
                    member.setVariant(copy, false, false); // 设置装配（不更新的）
                    copy.addTag(Tags.SHIP_LIMITED_TOOLTIP); // 将装配的复制添加tag：SHIP_LIMITED_TOOLTIP//不知道干啥 但是原版舰队有
                }
                final CampaignFleetAPI temp = Global.getFactory().createEmptyFleet(Factions.PLAYER,"??",false);
                temp.getCargo().addCrew(2);
                temp.addAbility(Abilities.TRANSPONDER);
                final FleetMemberAPI extra1 = temp.getFleetData()
                        .addFleetMember("HSI_Apostle_Elite");
                ShipVariantAPI copy1 = extra1.getVariant().clone(); // 获取装配的复制寄存到copy
                extra1.setVariant(copy1, false, false); // 设置装配（不更新的）
                extra1.getRepairTracker().setCR(1f); // 为所有成员回满cr
                extra1.getStats().getCRLossPerSecondPercent().modifyMult("Event_Use",0f);
                extra1.updateStats();
                extra1.setFlagship(true);

                final CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

                playerFleet.getContainingLocation().spawnFleet(
                        playerFleet.getInteractionTarget(),
                        playerFleet.getLocation().getX() + 100000f,
                        playerFleet.getLocation().getY() + 100000f,
                        temp);

                Global.getSector().setPlayerFleet(temp);

                playerFleet.setAI(null);
                playerFleet.setNoEngaging(3f);

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
                final HSITTMissionCombat plugin = new HSITTMissionCombat(config); // 实例化一个插件
                final InteractionDialogPlugin originalPlugin = dialog.getPlugin(); // 不知道干啥但是原版有
                //final HSICommissionerBarEvent0 event = this;
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
                                Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra1);
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                            } else {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(extra1);
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                            }
                            Global.getSector().setPlayerFleet(playerFleet);
                            MemoryAPI m = getEntityMemory(memCopy);
                            Global.getSector().getPlayerFleet().getContainingLocation().removeEntity(temp);
                            memory.set("$option", "HSI_TTMission_CombatEnd");
                            FireBest.fire(null, dialog, memCopy, "DialogOptionSelected");
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
            }
                //battle_occured = true;
                break;
            case "AddShipTTMission":
                if (dialog == null) return false;
//		ShipVariantAPI variant = (ShipVariantAPI) var.memory.get(var.name);
//		FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
                ShipVariantAPI v = Global.getSettings().createEmptyVariant("HSI_Apostle_Hull",Global.getSettings().getHullSpec("HSI_Apostle")).clone();
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
                Global.getSector().getPlayerFleet().getFleetData().addFleetMember(member);
                addShipGainText(member, dialog.getTextPanel());
                break;
            default:
                break;
        }
        return false;
    }
}
