package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class SWP_SurveyAll implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (!args.isEmpty()) {
            return CommandResult.BAD_SYNTAX;
        }

        Set<String> surveyed = new LinkedHashSet<>();
        List<String> surveyedList = new ArrayList<>();
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            surveyed.clear();
            surveyedList.clear();

            for (SectorEntityToken entity : location.getAllEntities()) {
                if (entity.getMarket() != null) {
                    if (entity.getMarket().getSurveyLevel() != SurveyLevel.FULL) {
                        entity.getMarket().setSurveyLevel(SurveyLevel.FULL);
                        surveyed.add(entity.getName());
                    }
                    for (MarketConditionAPI condition : entity.getMarket().getConditions()) {
                        if (!condition.isSurveyed()) {
                            condition.setSurveyed(true);
                            surveyed.add(entity.getName());
                        }
                    }
                }
            }

            for (String str : surveyed) {
                surveyedList.add(str);
            }
            if (!surveyed.isEmpty()) {
                Console.showMessage("Surveyed " + Misc.getAndJoined(surveyedList) + " in " + location.getName() + ".");
            }
        }

        return CommandResult.SUCCESS;
    }
}
