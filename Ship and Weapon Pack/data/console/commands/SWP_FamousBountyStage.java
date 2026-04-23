package data.console.commands;

import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator;
import data.scripts.campaign.intel.SWP_IBBIntel;
import data.scripts.campaign.intel.SWP_IBBIntel.FamousBountyStage;
import data.scripts.campaign.intel.SWP_IBBTracker;
import data.scripts.campaign.intel.bar.events.SWP_IBBBarEventCreator;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class SWP_FamousBountyStage implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty()) {
            return CommandResult.BAD_SYNTAX;
        }

        int stageNum;

        try {
            stageNum = Integer.parseInt(args);
        } catch (NumberFormatException ex) {
            Console.showMessage("Error: stage must be a number!");
            return CommandResult.BAD_SYNTAX;
        }

        if (stageNum < 0 || stageNum > SWP_IBBIntel.MAX_BOUNTY_STAGE) {
            Console.showMessage("Error: stage must be between 0 and " + SWP_IBBIntel.MAX_BOUNTY_STAGE + "!");
            return CommandResult.ERROR;
        }

        FamousBountyStage stage = SWP_IBBTracker.getStage(stageNum);
        if (stage != null && !stage.mod.isLoaded()) {
            switch (stage.mod) {
                case JUNK_PIRATES:
                    Console.showMessage("Error: selected stage requires Junk Pirates!");
                    break;
                case TIANDONG:
                    Console.showMessage("Error: selected stage requires Tiandong Heavy Industries!");
                    break;
                case SHADOWYARDS:
                    Console.showMessage("Error: selected stage requires Shadowyards Reconstruction Authority!");
                    break;
                case IMPERIUM:
                    Console.showMessage("Error: selected stage requires Interstellar Imperium!");
                    break;
                case TEMPLARS:
                    Console.showMessage("Error: selected stage requires The Knights Templar!");
                    break;
                case BLACKROCK:
                    Console.showMessage("Error: selected stage requires Blackrock Drive Yards!");
                    break;
                case CABAL:
                case CURSED:
                    Console.showMessage("Error: selected stage requires Underworld!");
                    break;
                case DIABLE:
                    Console.showMessage("Error: selected stage requires Diable Avionics!");
                    break;
                case EXIGENCY:
                    Console.showMessage("Error: selected stage requires Exigency!");
                    break;
                case ORA:
                    Console.showMessage("Error: selected stage requires Outer Rim Alliance!");
                    break;
                case SCY:
                    Console.showMessage("Error: selected stage requires Scy Nation!");
                    break;
                case TYRADOR:
                    Console.showMessage("Error: selected stage requires Tyrador Systems Coalition!");
                    break;
                case DME:
                    Console.showMessage("Error: selected stage requires Dassault-Mikoyan Engineering!");
                    break;
                case ICE:
                    Console.showMessage("Error: selected stage requires Idoneous Citadel Exiles!");
                    break;
                case BORKEN:
                    Console.showMessage("Error: selected stage requires Foundation of Borken!");
                    break;
                case SCALARTECH:
                    Console.showMessage("Error: selected stage requires ScalarTech!");
                    break;
                case ARKGNEISIS:
                    Console.showMessage("Error: selected stage requires Legacy of Arkgneisis!");
                    break;
                default:
                    break;
            }
            return CommandResult.ERROR;
        }
        if (stage == null || !SWP_IBBTracker.getTracker().isStageAllowed(stage)) {
            Console.showMessage("Error: invalid stage!");
            return CommandResult.ERROR;
        }

        SWP_IBBTracker.getTracker().reset();
        for (int i = 0; i < stageNum; i++) {
            FamousBountyStage s = SWP_IBBTracker.getStage(i);
            SWP_IBBTracker.getTracker().reportStageCompleted(s);
        }

        /* Make it available instantly */
        if (BarEventManager.getInstance() != null) {
            BarEventManager.getInstance().setTimeout(SWP_IBBBarEventCreator.class, 0f);
            for (GenericBarEventCreator creator : BarEventManager.getInstance().getCreators()) {
                if (creator instanceof SWP_IBBBarEventCreator) {
                    ((SWP_IBBBarEventCreator) creator).createInstantly = true;
                }
            }
        }

        Console.showMessage("Famous bounty stage set to " + stageNum + " (" + stage.firstName + " " + stage.lastName + ").");
        return CommandResult.SUCCESS;
    }
}
