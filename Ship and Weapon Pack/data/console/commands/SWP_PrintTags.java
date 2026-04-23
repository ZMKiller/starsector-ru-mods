package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class SWP_PrintTags implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (!args.isEmpty()) {
            return CommandResult.BAD_SYNTAX;
        }

        List<String> tagStrs = new ArrayList<>();
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            tagStrs.clear();

            if ((location.getTags()) != null && !location.getTags().isEmpty()) {
                tagStrs.addAll(location.getTags());
                Console.showMessage(Misc.ucFirst(location.getName()) + ": " + Misc.getAndJoined(tagStrs) + ".");
            }
        }

        return CommandResult.SUCCESS;
    }
}
