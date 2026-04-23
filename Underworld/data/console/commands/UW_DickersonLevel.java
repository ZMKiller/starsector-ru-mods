package data.console.commands;

import com.fs.starfarer.api.Global;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class UW_DickersonLevel implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty()) {
            return CommandResult.BAD_SYNTAX;
        }

        int level;

        try {
            level = Integer.parseInt(args);
        } catch (NumberFormatException ex) {
            Console.showMessage("Error: level must be a whole number!");
            return CommandResult.BAD_SYNTAX;
        }

        if (level < 0 || level > 6) {
            Console.showMessage("Error: level must be between 0 and 6!");
            return CommandResult.BAD_SYNTAX;
        }

        Global.getSector().getPersistentData().put("uw_dickerson_level", level);
        Console.showMessage("Set Dickerson level to " + level + ".");
        return CommandResult.SUCCESS;
    }
}
