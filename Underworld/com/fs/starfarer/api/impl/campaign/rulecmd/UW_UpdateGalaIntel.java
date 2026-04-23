package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.campaign.intel.UW_StarlightGala;
import java.util.List;
import java.util.Map;

// UW_UpdateGalaIntel
public class UW_UpdateGalaIntel extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        UW_StarlightGala starlightGala = (UW_StarlightGala) Global.getSector().getIntelManager().getFirstIntel(UW_StarlightGala.class);
        if (starlightGala != null) {
            starlightGala.sendUpdate(params, (dialog == null) ? null : dialog.getTextPanel());
            return true;
        }
        return false;
    }
}
