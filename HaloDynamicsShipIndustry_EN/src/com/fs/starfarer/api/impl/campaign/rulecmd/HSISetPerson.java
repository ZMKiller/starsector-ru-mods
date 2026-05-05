package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc.Token;

import data.kit.HSIIds;
import data.scripts.HSIGenerator.HSIIPManager;

public class HSISetPerson extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        String command = params.get(0).getString(memoryMap);
        switch (command) {
            case "TheCommissioner":
                PersonAPI commissioner = Global.getSector().getImportantPeople()
                        .getPerson(HSIIds.PERSON.THE_COMMISSIONER);
                if (commissioner != null) {
                    dialog.getInteractionTarget().setActivePerson(commissioner);
                    if (dialog.getPlugin() instanceof RuleBasedDialog) {
                        ((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
                    }
                }
                break;
            case "ShowCommissionerTTMissionBar":
                dialog.getVisualPanel().showImagePortion("illustrations", "HSI_TTMission_Bar", 640, 400, 0, 0, 480, 300);
                break;
            case "ShowCommissionerTTMissionDepartment":
                dialog.getVisualPanel().showImagePortion("illustrations", "HSI_TTMission_Department", 640, 400, 0, 0, 480, 300);
                break;
            case "Knight":
                PersonAPI Knight = Global.getSector().getImportantPeople()
                        .getPerson(HSIIds.PERSON.THE_KNIGHT);
                if (Knight != null) {
                    dialog.getInteractionTarget().setActivePerson(Knight);
                    if (dialog.getPlugin() instanceof RuleBasedDialog) {
                        ((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
                    }
                }
                break;
            case "RemoveTraitor":
                PersonAPI person = Global.getSector().getPlayerPerson();
                Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().unset("$HSIisTraitor");
                Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().unset("$HSIFLLostToPlayer");
                Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().set("$HSIforgiven",true);
                if(Global.getSector().getFaction("HSI")!=null)  Global.getSector().getFaction("HSI").setRelationship(Factions.PLAYER,Math.max(0,Global.getSector().getFaction("HSI").getRelToPlayer().getRel()));
                break;

            case "Skye":
                PersonAPI Skye = HSIIPManager.getSkye();
                dialog.getInteractionTarget().setActivePerson(Skye);
                if (dialog.getPlugin() instanceof RuleBasedDialog) {
                    ((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
                }
                break;
            case "Salvation":
                PersonAPI Salvation = HSIIPManager.getSalvation();
                dialog.getInteractionTarget().setActivePerson(Salvation);
                if (dialog.getPlugin() instanceof RuleBasedDialog) {
                    ((RuleBasedDialog) dialog.getPlugin()).notifyActivePersonChanged();
                }
            default:
                break;
        }
        return false;
    }
}
