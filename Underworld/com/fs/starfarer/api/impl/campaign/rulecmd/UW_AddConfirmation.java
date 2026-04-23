package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// UW_AddConfirmation <optionId> <text>
public class UW_AddConfirmation extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        String option = params.get(0).getString(memoryMap);
        String text = params.get(1).getStringWithTokenReplacement(ruleId, dialog, memoryMap);

        dialog.getOptionPanel().addOptionConfirmation(option, text, "Yes", "Never mind");
        return true;
    }
}
