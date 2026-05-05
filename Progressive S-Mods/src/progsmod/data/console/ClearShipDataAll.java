package progsmod.data.console;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import progsmod.util.SModUtils;

public class ClearShipDataAll implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!Global.getCurrentState().equals(GameState.CAMPAIGN)) {
            Console.showMessage("This command only works in the campaign.");
            return CommandResult.WRONG_CONTEXT;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (FleetMemberAPI fm : playerFleet.getFleetData().getMembersListCopy()) {
            if (fm.getVariant().hasHullMod("progsmod_xptracker")) {
                fm.getVariant().removePermaMod("progsmod_xptracker");
            }
            SModUtils.deleteXPData(fm.getId());
        }
        if (SModUtils.forceUpdater != null) {
            SModUtils.forceUpdater.resetXP();
        }
        return CommandResult.SUCCESS;
    }
}
