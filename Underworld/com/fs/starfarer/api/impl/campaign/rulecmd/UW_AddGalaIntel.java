package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.intel.UW_StarlightGala;
import java.util.List;
import java.util.Map;

// UW_AddGalaIntel
public class UW_AddGalaIntel extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (Global.getSector().getIntelManager().getFirstIntel(UW_StarlightGala.class) == null) {
            Global.getSector().getIntelManager().addIntel(new UW_StarlightGala(dialog), false, (dialog == null) ? null : dialog.getTextPanel());
            return true;
        }
        return false;
    }
}
