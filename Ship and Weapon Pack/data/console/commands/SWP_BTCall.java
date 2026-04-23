package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class SWP_BTCall implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty()) {
            return CommandResult.BAD_SYNTAX;
        }

        RuleBasedInteractionDialogPluginImpl dialog = new RuleBasedInteractionDialogPluginImpl("SWP_BTCallDialog");
        boolean result = Global.getSector().getCampaignUI().showInteractionDialog(dialog, Global.getSector().getPlayerFleet());
        if (!result) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        result = (new data.scripts.campaign.swprules.SWP_BTCall()).execute("ConsoleCommand",
                Global.getSector().getCampaignUI().getCurrentInteractionDialog(), Misc.tokenize(args), dialog.getMemoryMap());
        if (!result) {
            return CommandResult.ERROR;
        } else {
            return CommandResult.SUCCESS;
        }
    }
}
