package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kit.HSII18nUtil;
import data.kit.HSIIds;
import org.apache.log4j.Logger;

public class HSISABattle extends BaseCommandPlugin {

    private static final Logger LOG = Global.getLogger(HSISABattle.class);
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null)
            return true;
        final SectorEntityToken entity = dialog.getInteractionTarget();
        final MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
        final Map<String, MemoryAPI> memoryM = memoryMap;
        final CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        String command = params.get(0).getString(memoryMap);
        switch (command) {
            case "Training":
            {
                SDMParams p = new SDMParams();
                p.entity = entity;
                WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
                for (FactionAPI faction : Global.getSector().getAllFactions()) {
                    // if(faction.isNeutralFaction()) continue;
                    if (faction.isShowInIntelTab()) {
                        factionPicker.add(faction.getId());//选择生成舰队的势力
                    }
                }

                String factionId = factionPicker.pickAndRemove();
                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISALevelTraining");
                float battleSize = Global.getSettings().getBattleSize()*0.2f;//战场规模-以50%折算FP
                float combat = battleSize*(1+(level/10));//基础fp
                combat+=(level%10*battleSize*0.1f);//每10层中的每层增加10%FP基数
                if(level%3==0){
                    combat*=1.5f;//每10层大幅增加一次难度
                }
                if(factionId.equals(Factions.DIKTAT)){
                    combat*=0.9f;
                }
                FleetParamsV3 fp = new FleetParamsV3(
                        null,
                        entity.getLocation(),
                        factionId,
                        null,
                        FleetTypes.PATROL_LARGE,
                        combat, // combatPts
                        0, // freighterPts
                        0, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        level / 3f // qualityMod
                );
                fp.averageSMods = (level / 10);
                fp.maxOfficersToAdd = (level / 3) + 1;
                fp.officerLevelBonus = (level / 10) - 2;
                fp.maxNumShips = (25 + level*3);
                CampaignFleetAPI Bdefenders = FleetFactoryV3.createFleet(fp); // 创建一个舰队
                while (Bdefenders.isEmpty()) {
                    factionId = factionPicker.pickAndRemove();
                    if(factionId.equals(Factions.DIKTAT)){
                        combat*=0.9f;
                    }
                    fp = new FleetParamsV3(
                            null,
                            entity.getLocation(),
                            factionId,
                            null,
                            FleetTypes.PATROL_LARGE,
                            combat, // combatPts
                            0, // freighterPts
                            0, // tankerPts
                            0f, // transportPts
                            0f, // linerPts
                            0f, // utilityPts
                            level / 3f // qualityMod
                    );
                    Bdefenders = FleetFactoryV3.createFleet(fp);
                }
                final CampaignFleetAPI defenders = Bdefenders;
                FleetFactoryV3.addCommanderAndOfficers(defenders, fp, new Random());
                defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                defenders.setName("T-" + level); // 切换名字
                defenders.setNoFactionInName(false); // 不显示势力

                if (defenders.getInflater() instanceof DefaultFleetInflater) { // 不知道干啥 但是原版舰队有
                    DefaultFleetInflater dfi = (DefaultFleetInflater) defenders.getInflater();
                    ((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true; //
                }
                for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队成员
                    ShipVariantAPI copy = member.getVariant().clone(); // 获取装配的复制寄存到copy
                    member.setVariant(copy, false, false); // 设置装配（不更新的）
                    //copy.addTag(Tags.SHIP_LIMITED_TOOLTIP); // 将装配的复制添加tag：SHIP_LIMITED_TOOLTIP//不知道干啥 但是原版舰队有
                }
                for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                    member.getStats().getCrewLossMult().modifyMult("HSISA", 0f);
                    member.getStats().getDynamic().getMod(
                            Stats.DMOD_AVOID_PROB_MOD).modifyFlat("HSISA", 10000f);
                    member.updateStats();
                }
                dialog.setInteractionTarget(defenders);

                final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig(); // 获取一个原生的防卫舰队config
                config.firstTimeEngageOptionText = "NO VOICE"; // 开始定义一些tag用作后来的逻辑，虽然我这里一个都没有用到
                config.afterFirstTimeEngageOptionText = null;
                config.leaveAlwaysAvailable = true;
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
                config.straightToEngage = false;
                config.dismissOnLeave = false;
                config.printXPToDialog = true;
                config.withSalvage = false;
                //config.impactsEnemyReputation = false;
                //config.impactsAllyReputation = false;
                final HSISAFID plugin = new HSISAFID(config);
                final InteractionDialogPlugin originalPlugin = dialog.getPlugin(); // 不知道干啥但是原版有
                final int crewSize = player.getCargo().getCrew();
                final int marineSize = player.getCargo().getMarines();
                config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                    public void notifyLeave(InteractionDialogAPI dialog) { // 设置战斗结束的互动
                        defenders.getMemoryWithoutUpdate().clear();
                        defenders.clearAssignments();
                        defenders.deflate();
                        if (player.getCargo().getCrew() < crewSize) {
                            player.getCargo().addCrew(crewSize - player.getCargo().getCrew());
                        }
                        if (player.getCargo().getMarines() < marineSize) {
                            player.getCargo().addMarines(marineSize - player.getCargo().getMarines());
                        }
                        // dialog.setPlugin(originalPlugin);
                        if (plugin.getContext() instanceof HSISAFEContext) {
                            HSISAFEContext context = (HSISAFEContext) plugin.getContext(); // 取战斗结果
                            if (context.didPlayerWinEncounterOutright()) {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                List<FleetMemberAPI> lossM = new ArrayList<>();
                                for (FleetMemberData m : loss) {
                                    // if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                    lossM.add(m.getMember());
                                    // }
                                }
                                HSISAFEContext.recoverShips(lossM, context, player, defenders);
                                ListenerUtil.reportShipsRecovered(lossM, dialog);
                                for (FleetMemberAPI member : player
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                    member.updateStats();
                                }

                                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISALevelTraining");
                                Global.getSector().getMemoryWithoutUpdate().set("$HSISALevelTraining", level + 1);

                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                                int cr = 0;
                                if (level % 3 == 0) {
                                    cr = 1;
                                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(level * 5000);
                                    AddRemoveCommodity.addCreditsGainText(level * 5000, dialog.getTextPanel());
                                }
                                int sa_cr = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISACredits");
                                Global.getSector().getMemoryWithoutUpdate().set("$HSISACredits",
                                        sa_cr + cr);
                                TextPanelAPI text = dialog.getTextPanel();
                                text.setFontSmallInsignia();
                                String str = Misc.getWithDGS(cr) + "pt";
                                text.addParagraph("+ " + str + "", Misc.getNegativeHighlightColor());
                                text.highlightInLastPara(Misc.getHighlightColor(), str);
                                text.setFontInsignia();
                            } else {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                for (FleetMemberData m : loss) {
                                    if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                        m.getMember().getStatus().repairDisabledABit();
                                    }
                                }
                                for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                    member.updateStats();
                                }
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                            }

                        }
                        // plugin.optionSelected("", OptionId.ENGAGE);
                    }

                    public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) { // 下面都不知道什么用，但是得要有
                        bcc.aiRetreatAllowed = false;
                        bcc.enemyDeployAll = true;
                        // bcc.objectivesAllowed = true;
                    }

                    public void postPlayerSalvageGeneration(InteractionDialogAPI dialog,
                                                            FleetEncounterContext context, CargoAPI salvage) {
                    }
                };
                dialog.setPlugin(plugin); // 特别是这两句，否则会一直显示玩家舰队炸完了的互动。
                plugin.init(dialog);
                plugin.setPlayerFleet(player);
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet != null) {
                    playerFleet.getFleetData().ensureHasFlagship();
                }
                plugin.showFleetInfo();
        }
                break;
            case "Hard":
            {
                SDMParams p = new SDMParams();
                p.entity = entity;
                WeightedRandomPicker<String> factionPicker = new WeightedRandomPicker<>();
                for (FactionAPI faction : Global.getSector().getAllFactions()) {
                    // if(faction.isNeutralFaction()) continue;
                    if (faction.isShowInIntelTab()) {
                        factionPicker.add(faction.getId());//选择生成舰队的势力
                    }
                }
                String factionId = factionPicker.pickAndRemove();
                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISALevelHard");
                float battleSize = Global.getSettings().getBattleSize()*0.3f;//战场规模-以50%折算FP
                float combat = battleSize*(1+(level*0.2f));//基础fp
                combat+=(level*battleSize*0.1f);//每10层中的每层增加10%FP基数
                if(factionId.equals(Factions.DIKTAT)){
                    combat*=0.9f;
                }
                FleetParamsV3 fp = new FleetParamsV3(
                        null,
                        entity.getLocation(),
                        factionId,
                        null,
                        FleetTypes.PATROL_LARGE,
                        combat, // combatPts
                        0, // freighterPts
                        0, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        2 // qualityMod
                );
                fp.averageSMods = (level);
                fp.maxOfficersToAdd = (level*5) + 1;
                fp.officerLevelBonus = (level*2) ;
                fp.maxNumShips = (30 + level*5);
                CampaignFleetAPI Bdefenders = FleetFactoryV3.createFleet(fp); // 创建一个舰队
                while (Bdefenders.isEmpty()) {
                    factionId = factionPicker.pickAndRemove();
                    if(factionId.equals(Factions.DIKTAT)){
                        combat*=0.9f;
                    }
                    fp = new FleetParamsV3(
                            null,
                            entity.getLocation(),
                            factionId,
                            null,
                            FleetTypes.PATROL_LARGE,
                            combat, // combatPts
                            0, // freighterPts
                            0, // tankerPts
                            0f, // transportPts
                            0f, // linerPts
                            0f, // utilityPts
                            level / 3f // qualityMod
                    );
                    Bdefenders = FleetFactoryV3.createFleet(fp);
                }
                final CampaignFleetAPI defenders = Bdefenders;
                FleetFactoryV3.addCommanderAndOfficers(defenders, fp, new Random());
                defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                //FactionAPI faction = Global.getSector().getFaction(factionId);
                //String source = "";
                //if(faction!=null) source = faction.getDisplayName();
                defenders.setName("TH-" + level); // 切换名字
                defenders.setNoFactionInName(false); // 不显示势力

                if (defenders.getInflater() instanceof DefaultFleetInflater) { // 不知道干啥 但是原版舰队有
                    DefaultFleetInflater dfi = (DefaultFleetInflater) defenders.getInflater();
                    ((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true; //
                }
                for (FleetMemberAPI member : defenders.getFleetData().getMembersListCopy()) { // 遍历舰队成员
                    if(member.isFighterWing()) continue;
                    ShipVariantAPI copy = member.getVariant().clone(); // 获取装配的复制寄存到copy
                    copy.setSource(VariantSource.REFIT);
                    if(level>3){
                        copy.addPermaMod(HullMods.AUTOREPAIR,true);
                    }
                    if(level>4){
                        switch (member.getHullSpec().getHullSize()){
                            case CRUISER:
                            case CAPITAL_SHIP:
                                copy.addPermaMod(HullMods.TURRETGYROS,true);
                                break;
                            case DEFAULT:
                                break;
                        }
                    }
                    if(level>5){
                        if(member.getCaptain()!=null&&!member.getCaptain().isDefault()){
                            boolean hasEliteDC = false;
                            boolean hasEliteHels = false;
                            for(MutableCharacterStatsAPI.SkillLevelAPI skill:member.getCaptain().getStats().getSkillsCopy()){
                                if(skill.getSkill().getId().equals(Skills.DAMAGE_CONTROL)){
                                    if(skill.getLevel()<2){
                                        skill.setLevel(2);
                                    }
                                    hasEliteDC = true;
                                }
                                if(skill.getSkill().getId().equals(Skills.HELMSMANSHIP)){
                                    if(skill.getLevel()<2){
                                        skill.setLevel(2);
                                    }
                                    hasEliteHels = true;
                                }
                            }
                            if(!hasEliteDC){
                                member.getCaptain().getStats().setSkillLevel(Skills.DAMAGE_CONTROL,2);
                            }
                            if(level>6&&!hasEliteHels){
                                member.getCaptain().getStats().setSkillLevel(Skills.DAMAGE_CONTROL,2);
                            }
                        }
                    }

                    copy.addTag(Tags.SHIP_LIMITED_TOOLTIP); // 将装配的复制添加tag：SHIP_LIMITED_TOOLTIP//不知道干啥 但是原版舰队有

                    member.setVariant(copy, false, false); // 设置装配（不更新的）
                    member.updateStats();
                }
                for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                    member.getStats().getCrewLossMult().modifyMult("HSISA", 0f);
                    member.getStats().getDynamic().getMod(
                            Stats.DMOD_AVOID_PROB_MOD).modifyFlat("HSISA", 10000f);
                    member.updateStats();
                }
                dialog.setInteractionTarget(defenders);

                final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig(); // 获取一个原生的防卫舰队config
                config.firstTimeEngageOptionText = "NO VOICE"; // 开始定义一些tag用作后来的逻辑，虽然我这里一个都没有用到
                config.afterFirstTimeEngageOptionText = null;
                config.leaveAlwaysAvailable = true;
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
                config.straightToEngage = false;
                config.dismissOnLeave = false;
                config.printXPToDialog = true;
                config.withSalvage = false;
                //config.impactsEnemyReputation = false;
                //config.impactsAllyReputation = false;
                final HSISAFID plugin = new HSISAFID(config);
                final InteractionDialogPlugin originalPlugin = dialog.getPlugin(); // 不知道干啥但是原版有
                final int crewSize = player.getCargo().getCrew();
                final int marineSize = player.getCargo().getMarines();
                config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                    public void notifyLeave(InteractionDialogAPI dialog) { // 设置战斗结束的互动
                        defenders.getMemoryWithoutUpdate().clear();
                        defenders.clearAssignments();
                        defenders.deflate();
                        if (player.getCargo().getCrew() < crewSize) {
                            player.getCargo().addCrew(crewSize - player.getCargo().getCrew());
                        }
                        if (player.getCargo().getMarines() < marineSize) {
                            player.getCargo().addMarines(marineSize - player.getCargo().getMarines());
                        }
                        // dialog.setPlugin(originalPlugin);
                        if (plugin.getContext() instanceof HSISAFEContext) {
                            HSISAFEContext context = (HSISAFEContext) plugin.getContext(); // 取战斗结果
                            if (context.didPlayerWinEncounterOutright()) {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                List<FleetMemberAPI> lossM = new ArrayList<>();
                                for (FleetMemberData m : loss) {
                                    // if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                    lossM.add(m.getMember());
                                    // }
                                }
                                HSISAFEContext.recoverShips(lossM, context, player, defenders);
                                ListenerUtil.reportShipsRecovered(lossM, dialog);
                                for (FleetMemberAPI member : player
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                    member.updateStats();
                                }

                                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISALevelHard");
                                Global.getSector().getMemoryWithoutUpdate().set("$HSISALevelHard", level + 1);

                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                                int cr = 1;
                                Global.getSector().getPlayerFleet().getCargo().getCredits().add(level * 5000);
                                AddRemoveCommodity.addCreditsGainText(level * 5000, dialog.getTextPanel());
                                int sa_cr = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISACredits");
                                Global.getSector().getMemoryWithoutUpdate().set("$HSISACredits",
                                        sa_cr + cr);
                                TextPanelAPI text = dialog.getTextPanel();
                                text.setFontSmallInsignia();
                                String str = Misc.getWithDGS(cr) + "pt";
                                text.addParagraph("+ " + str + "", Misc.getNegativeHighlightColor());
                                text.highlightInLastPara(Misc.getHighlightColor(), str);
                                text.setFontInsignia();
                            } else {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                for (FleetMemberData m : loss) {
                                    if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                        m.getMember().getStatus().repairDisabledABit();
                                    }
                                }
                                for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                    member.updateStats();
                                }
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                            }

                        }
                        // plugin.optionSelected("", OptionId.ENGAGE);
                    }

                    public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) { // 下面都不知道什么用，但是得要有
                        bcc.aiRetreatAllowed = false;
                        bcc.enemyDeployAll = true;
                        // bcc.objectivesAllowed = true;
                    }

                    public void postPlayerSalvageGeneration(InteractionDialogAPI dialog,
                                                            FleetEncounterContext context, CargoAPI salvage) {
                    }
                };
                dialog.setPlugin(plugin); // 特别是这两句，否则会一直显示玩家舰队炸完了的互动。
                plugin.init(dialog);
                plugin.setPlayerFleet(player);
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet != null) {
                    playerFleet.getFleetData().ensureHasFlagship();
                }
                plugin.showFleetInfo();
            }
            break;
            case "Adventure":
            {
                SDMParams p = new SDMParams();
                p.entity = entity;
                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISA_LastAdventureReached");
                if(level == 0){
                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureReached",1);
                    level = 1;
                }
                //List<String> upgrades = Arrays.asList(Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().getString("$HSISA_Upgrades").split(","));
                final CampaignFleetAPI defenders;
                HSISAAdvLoader.HSIAdvEnemySpec enemySpec = HSISAAdvLoader.getRandomEnemyByLevel(level);
                if(enemySpec == null){
                    LOG.error("No enemy for level "+level);
                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureReached", 1);
                    FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                    break;
                }
                defenders = HSISAAdvLoader.createFleet(enemySpec,level);
                defenders.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
                defenders.setNoFactionInName(true);
                defenders.addTag("HSISA_AdventureModeFleet");

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
                    member.getStats().getCrewLossMult().modifyMult("HSISA", 0f);
                    member.getStats().getDynamic().getMod(
                            Stats.DMOD_AVOID_PROB_MOD).modifyFlat("HSISA", 10000f);
                }

                dialog.setInteractionTarget(defenders);
                dialog.getVisualPanel().showPersonInfo(enemySpec.createPersonForDisplay(),true);
                dialog.getTextPanel().addPara(enemySpec.getName(),Misc.getHighlightColor());
                dialog.getTextPanel().addPara(enemySpec.getDesc(),Misc.getHighlightColor());


                final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig(); // 获取一个原生的防卫舰队config
                config.firstTimeEngageOptionText = "NO VOICE"; // 开始定义一些tag用作后来的逻辑，虽然我这里一个都没有用到
                config.afterFirstTimeEngageOptionText = null;
                config.leaveAlwaysAvailable = true;
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
                config.straightToEngage = false;
                config.dismissOnLeave = false;
                config.printXPToDialog = true;
                config.withSalvage = false;
                //config.impactsEnemyReputation = false;
                //config.impactsAllyReputation = false;
                final HSISAFID plugin = new HSISAFID(config);

                final InteractionDialogPlugin originalPlugin = dialog.getPlugin(); // 不知道干啥但是原版有
                final int crewSize = player.getCargo().getCrew();
                final int marineSize = player.getCargo().getMarines();
                config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                    public void notifyLeave(InteractionDialogAPI dialog) { // 设置战斗结束的互动
                        defenders.getMemoryWithoutUpdate().clear();
                        defenders.clearAssignments();
                        defenders.deflate();
                        if (player.getCargo().getCrew() < crewSize) {
                            player.getCargo().addCrew(crewSize - player.getCargo().getCrew());
                        }
                        if (player.getCargo().getMarines() < marineSize) {
                            player.getCargo().addMarines(marineSize - player.getCargo().getMarines());
                        }
                        // dialog.setPlugin(originalPlugin);
                        if (plugin.getContext() instanceof HSISAFEContext) {
                            HSISAFEContext context = (HSISAFEContext) plugin.getContext(); // 取战斗结果
                            if (context.didPlayerWinEncounterOutright()) {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                List<FleetMemberAPI> lossM = new ArrayList<>();
                                for (FleetMemberData m : loss) {
                                    // if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                    lossM.add(m.getMember());
                                    // }
                                }
                                HSISAFEContext.recoverShips(lossM, context, player, defenders);
                                ListenerUtil.reportShipsRecovered(lossM, dialog);
                                for (FleetMemberAPI member : player
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                }

                                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISA_LastAdventureReached");
                                if(level<15) {
                                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureReached", level + 1);
                                }else{
                                    //winning
                                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureReached", 1);
                                }

                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                                int cr = (level==15)?1:0;
                                Global.getSector().getPlayerFleet().getCargo().getCredits().add(level * 5000);
                                AddRemoveCommodity.addCreditsGainText(level * 5000, dialog.getTextPanel());
                                int sa_cr = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISACredits");
                                Global.getSector().getMemoryWithoutUpdate().set("$HSISACredits",
                                        sa_cr + cr);
                                TextPanelAPI text = dialog.getTextPanel();
                                text.setFontSmallInsignia();
                                String str = Misc.getWithDGS(cr) + "pt";
                                text.addParagraph("+ " + str + "", Misc.getNegativeHighlightColor());
                                text.highlightInLastPara(Misc.getHighlightColor(), str);
                                text.setFontInsignia();
                            } else {
                                memory.unset("$hasDefenders");
                                memory.unset("$defenderFleet");
                                memory.set("$defenderFleetDefeated", true);
                                DataForEncounterSide data = context.getDataFor(Global.getSector().getPlayerFleet());
                                List<FleetMemberData> loss = data.getOwnCasualties();
                                for (FleetMemberData m : loss) {
                                    if (m.getMember().getStatus().getNumStatuses() <= 1) {
                                        m.getMember().getStatus().repairDisabledABit();
                                    }
                                }
                                for (FleetMemberAPI member : Global.getSector().getPlayerFleet()
                                        .getMembersWithFightersCopy()) {
                                    member.getStatus().repairFully();
                                    member.getStats().getCrewLossMult().unmodify("HSISA");
                                    member.getStats().getDynamic().getMod(
                                            Stats.DMOD_AVOID_PROB_MOD).unmodify("HSISA");
                                }
                                dialog.setPlugin(originalPlugin);
                                dialog.setInteractionTarget(entity);
                                originalPlugin.init(dialog);
                                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                            }

                        }
                        // plugin.optionSelected("", OptionId.ENGAGE);
                    }

                    public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) { // 下面都不知道什么用，但是得要有
                        bcc.aiRetreatAllowed = false;
                        bcc.enemyDeployAll = true;
                        // bcc.objectivesAllowed = true;
                    }

                    public void postPlayerSalvageGeneration(InteractionDialogAPI dialog,
                                                            FleetEncounterContext context, CargoAPI salvage) {
                    }
                };
                dialog.setPlugin(plugin); // 特别是这两句，否则会一直显示玩家舰队炸完了的互动。
                plugin.init(dialog);
                plugin.setPlayerFleet(player);
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet != null) {
                    playerFleet.getFleetData().ensureHasFlagship();
                }
                //plugin.showFleetInfo();
            }
            break;

            case "ShowAdventureStatus":
                int level = Global.getSector().getMemoryWithoutUpdate().getInt("$HSISA_LastAdventureReached");
                String buffList = "";
                if(Global.getSector().getMemoryWithoutUpdate().contains("$HSISA_LastAdventureBuffs")) {
                    buffList =  Global.getSector().getMemoryWithoutUpdate().getString("$HSISA_LastAdventureBuffs");
                }else{
                    Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureBuffs",buffList);
                }
                List<String> buffs = Arrays.asList(buffList.split(","));

                StringBuilder text = new StringBuilder();
                for(String buff:buffs){
                    if(HSISAAdvLoader.buffs.containsKey(buff)){
                        if(!text.toString().equals("")){
                            text.append(",");
                        }
                        text.append(HSISAAdvLoader.buffs.get(buff).getName());
                        LOG.info("Has buff as "+buff+"||"+HSISAAdvLoader.buffs.get(buff).getName());
                    }
                }
                dialog.getTextPanel().addPara(HSII18nUtil.getCampaignString("HSISAAdvLevel") +level);
                dialog.getTextPanel().addPara(HSII18nUtil.getCampaignString("HSISAAdvBuff")+ text);
                if(buffs.size()<level-1){//should pick buff
                    if((buffs.size()+1)%4!=0) {
                        WeightedRandomPicker<HSISAAdvLoader.HSIAdvBuffSpec> buffToPick = new WeightedRandomPicker<>();
                        for(String id:HSISAAdvLoader.buffs.keySet()){
                            HSISAAdvLoader.HSIAdvBuffSpec buff = HSISAAdvLoader.buffs.get(id);
                            if(buff.getTags().contains(HSIIds.ADVENTURE.ELITE_BUFF)) continue;
                            if(buffs.contains(buff.getId())) continue;
                            buffToPick.add(buff);
                        }
                        if(buffToPick.isEmpty()){
                            addBuff("");
                        }else{
                            dialog.getOptionPanel().clearOptions();
                            for(int i = 0;i<2;i++){
                                if(buffToPick.isEmpty()) break;
                                HSISAAdvLoader.HSIAdvBuffSpec optionBuff = buffToPick.pickAndRemove();
                                if(optionBuff == null) break;
                                dialog.getOptionPanel().addOption(HSII18nUtil.getCampaignString("HSISAAdvPickBuff")+optionBuff.getName(),"HSISA_Adventure_Buff_"+optionBuff.getId());
                            }
                        }
                    }else{
                        WeightedRandomPicker<HSISAAdvLoader.HSIAdvBuffSpec> buffToPick = new WeightedRandomPicker<>();
                        for(String id:HSISAAdvLoader.buffs.keySet()){
                            HSISAAdvLoader.HSIAdvBuffSpec buff = HSISAAdvLoader.buffs.get(id);
                            if(!buff.getTags().contains(HSIIds.ADVENTURE.ELITE_BUFF)) continue;
                            if(buffs.contains(buff.getId())) continue;
                            buffToPick.add(buff);
                        }
                        if(buffToPick.isEmpty()){
                            addBuff("");
                        }else{
                            dialog.getOptionPanel().clearOptions();
                            for(int i = 0;i<2;i++){
                                if(buffToPick.isEmpty()){
                                    break;
                                }else{
                                HSISAAdvLoader.HSIAdvBuffSpec optionBuff = buffToPick.pickAndRemove();
                                if(optionBuff == null){
                                    break;
                                }else{
                                    dialog.getTextPanel().addPara(optionBuff.getName()+":"+optionBuff.getDesc());
                                    dialog.getOptionPanel().addOption(HSII18nUtil.getCampaignString("HSISAAdvPickBuff")+optionBuff.getName()+"("+optionBuff.getDesc()+")","HSISA_Adventure_Buff_"+optionBuff.getId());
                                }
                                }
                            }
                        }
                    }
                }else{
                    dialog.getOptionPanel().clearOptions();
                    dialog.getOptionPanel().addOption(HSII18nUtil.getCampaignString("HSISAAdvStartBattle"),"HSISA_BattleStartAdventure");
                }
                break;

            case "PickedBuffOption":
                return memory.contains("$option") && (memory.getString("$option").startsWith("HSISA_Adventure_Buff_"));
            case "AddBuff":
                if(memory.contains("$option")&&(memory.getString("$option").startsWith("HSISA_Adventure_Buff_"))){
                    String buff = memory.getString("$option").substring(21);
                    LOG.info("Current Buff to Add:"+buff);
                    if(HSISAAdvLoader.buffs.containsKey(buff)){
                        addBuff(buff);
                    }
                }
                FireAll.fire(null, dialog, memoryM, "HSISAOptions");
                break;
        }


        // Global.getLogger(this.getClass()).info((HSISAFEContext)(plugin.getContext())==null);
        // dialog.getOptionPanel().addOption("Leave.", OptionId.LEAVE);
        return true;
    }

    protected static void addBuff(String id){
        if(!Global.getSector().getMemoryWithoutUpdate().contains("$HSISA_LastAdventureBuffs")){
            Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureBuffs",id);
        }else{
            Global.getSector().getMemoryWithoutUpdate().set("$HSISA_LastAdventureBuffs",","+id);
        }
        LOG.info("Current Buff to Check:"+Global.getSector().getMemoryWithoutUpdate().getString("$HSISA_LastAdventureBuffs"));
    }
}
