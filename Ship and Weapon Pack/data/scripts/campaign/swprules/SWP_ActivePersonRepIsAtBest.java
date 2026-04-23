package data.scripts.campaign.swprules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * SWP_ContactRepIsAtBest <RepLevel>
 */
public class SWP_ActivePersonRepIsAtBest extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity.getActivePerson() == null) {
            return false;
        }

        String repLevelStr = params.get(0).getString(memoryMap);
        RepLevel repLevel = RepLevel.valueOf(repLevelStr);

        return entity.getActivePerson().getRelToPlayer().isAtBest(repLevel);
    }
}
