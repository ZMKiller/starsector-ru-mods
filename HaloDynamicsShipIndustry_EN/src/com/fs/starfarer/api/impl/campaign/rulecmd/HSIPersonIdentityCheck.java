package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc.Token;

import data.kit.HSIIds;

public class HSIPersonIdentityCheck extends BaseCommandPlugin{
    protected PersonAPI person;
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		String command = params.get(0).getString(memoryMap);
        person = dialog.getInteractionTarget().getActivePerson();
        switch (command) {
            case "PersonIsHPSIDOperator":
                return person.hasTag("HPSID");
            case "PersonIsTheCommissioner":
                return person.getId().equals(HSIIds.PERSON.THE_COMMISSIONER);

            case "PersonIsStalkerContact":
                return person.hasTag("HSISS");

            case "PersonIsHSIForgeContact":
                return person.hasTag("HSIForge");

            case "PlayerLevelAbove5":
                return Global.getSector().getPlayerStats().getLevel()>=5;
            case "PlayerLevelAbove10":
                return Global.getSector().getPlayerStats().getLevel()>=10;
            case "PlayerLevelAbove15":
                return Global.getSector().getPlayerStats().getLevel()>=15;
            case "PlayerLevelBelow5":
                return Global.getSector().getPlayerStats().getLevel()<5;
            case "SkyeRandom1":
                return Global.getSector().getClock().getDay()%10 == 1;
            case "isTraitor":
                return Global.getSector().getFaction("HSI")!=null&&Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().contains("$HSIisTraitor")&&Global.getSector().getFaction("HSI").getMemoryWithoutUpdate().getBoolean("$HSIisTraitor");
            default:
                break;
        }
        return false;
    }
}
