package data.scripts.campaign.swprules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// SWP_AddConfirmation <optionId> <text> <optionalYes> <optionalNo>
public class SWP_AddConfirmation extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        String option = params.get(0).getString(memoryMap);
        String text = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);

        String yes = "Yes";
        String no = "Never mind";
        if (params.size() > 2) {
            yes = params.get(2).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
            no = params.get(3).getStringWithTokenReplacement(ruleId, dialog, memoryMap);
        }

        dialog.getOptionPanel().addOptionConfirmation(option, text, yes, no);
        return true;
    }
}
