package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * SWP_AdjustRepMarketFaction <RepActions action>
 */
public class SWP_AdjustRepMarketFaction extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (market == null) {
            return false;
        }

        try {
            RepActions action = RepActions.valueOf(params.get(0).getString(memoryMap));
            RepActionEnvelope envelope = new RepActionEnvelope(action, null, dialog.getTextPanel());
            ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, market.getFaction().getId());
            return result.delta != 0;
        } catch (Throwable t) {
            CustomRepImpact impact = new CustomRepImpact();
            impact.limit = RepLevel.valueOf(params.get(0).getString(memoryMap));
            impact.delta = params.get(1).getFloat(memoryMap) * 0.01f;
            ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(
                    new RepActionEnvelope(RepActions.CUSTOM, impact,
                            null, dialog.getTextPanel(), true), market.getFaction().getId());
            return result.delta != 0;
        }
    }
}
