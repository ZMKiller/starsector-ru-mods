package progsmod.data.console;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import progsmod.util.SModUtils;

public class AddShipXPAll implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!Global.getCurrentState().equals(GameState.CAMPAIGN)) {
            Console.showMessage("This command only works in the campaign.");
            return CommandResult.WRONG_CONTEXT;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        int amount;
        try {
            amount = Integer.parseInt(args);
        } catch (NumberFormatException e) {
            Console.showMessage("Specified XP amount must be an integer.");
            return CommandResult.BAD_SYNTAX;
        }

        for (FleetMemberAPI fm : playerFleet.getFleetData().getMembersListCopy()) {
            SModUtils.giveXP(fm, amount);
        }
        if (SModUtils.forceUpdater != null) {
            SModUtils.forceUpdater.addXP(amount);
        }
        return CommandResult.SUCCESS;
    }
}
