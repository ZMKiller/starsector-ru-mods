package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * SWP_CanAfford <price>
 */
public class SWP_CanAfford extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }

        float price = params.get(0).getFloat(memoryMap);

        final float credits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        final boolean canAccept = (int) price <= (int) credits;

        return canAccept;
    }
}
