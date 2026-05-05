package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.campaign.HSITC1Items.HSITC1Generator;
import data.campaign.HSITC1Items.HSITC1Intel;
import data.kit.HSII18nUtil;
import data.kit.HSIIds;
import data.scripts.HSIGenerator.HSIIPManager;

public class HSITC1Orders extends BaseCommandPlugin {
    public static final String HSI_KNIGHT_SP = "$HSI_Knight_SP";

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        String command = params.get(0).getString(memoryMap);
        boolean success = false;
        PersonAPI knight = Global.getSector().getImportantPeople()
                .getPerson(HSIIds.PERSON.THE_KNIGHT);
        if(knight==null){HSIIPManager.createKnight(Global.getSector());
        knight = Global.getSector().getImportantPeople()
                .getPerson(HSIIds.PERSON.THE_KNIGHT);
        }
        switch (command) {
            case "ShowMap":
                // CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
                TextPanelAPI text = dialog.getTextPanel();

                PlanetAPI planet = HSITC1Generator.getPlanet();

                if (planet != null) {
                    HSITC1Intel intel = new HSITC1Intel();
                    if (!intel.isDone()) {
                        Global.getSector().getIntelManager().addIntel(intel, false, text);
                        success = true;
                    }
                }
                String icon = Global.getSettings().getSpriteName("intel", "red_planet");
                Set<String> tags = new LinkedHashSet<String>();
                tags.add(Tags.INTEL_MISSIONS);
                dialog.getVisualPanel().showMapMarker(planet,
                        HSII18nUtil.getCampaignString("HSI_Campaign_Destination") + planet.getName(),
                        planet.getFaction().getBaseUIColor(),
                        true, icon, null, tags);
                break;
            case "GenKnight":
                knight.getStats().increaseSkill("HSI_Knight");
                knight.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_LEVEL,8);
                knight.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_ELITE_SKILLS,4);
                Global.getSector().getPlayerFleet().getFleetData().addOfficer(knight);
                break;
            case "GenKnight_SP":
                knight.getStats().increaseSkill("HSI_Knight_SP");
                knight.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_LEVEL,9);
                knight.getMemoryWithoutUpdate().set(MemFlags.OFFICER_MAX_ELITE_SKILLS,5);
                Global.getSector().getPlayerFleet().getFleetData().addOfficer(knight);
                break;
            case "ClearImportant":
                String reason = "";
                if(params.size()>1) reason = params.get(1).getString(memoryMap);
                Misc.makeUnimportant(dialog.getInteractionTarget(),reason);
            default:
        }
        return success;
    }

}
