package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/**
 * UW_AdjustRepCustom <factionId> <delta> <limit> <ensureAtBest> <ensureAtWorst> <requireAtBest> <requireAtWorst>
 */
public class UW_AdjustRepCustom extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        String factionId = params.get(0).getString(memoryMap);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity.getActivePerson() != null) {
            CustomRepImpact impact = new CustomRepImpact();
            if (params.size() >= 2 && !params.get(1).getString(memoryMap).contentEquals("null")) {
                impact.delta = 2f * params.get(1).getFloat(memoryMap);
            }
            if (params.size() >= 3 && !params.get(2).getString(memoryMap).contentEquals("null")) {
                impact.limit = RepLevel.valueOf(RepLevel.class, params.get(2).getString(memoryMap));
            }
            if (params.size() >= 4 && !params.get(3).getString(memoryMap).contentEquals("null")) {
                impact.ensureAtBest = RepLevel.valueOf(RepLevel.class, params.get(3).getString(memoryMap));
            }
            if (params.size() >= 5 && !params.get(4).getString(memoryMap).contentEquals("null")) {
                impact.ensureAtWorst = RepLevel.valueOf(RepLevel.class, params.get(4).getString(memoryMap));
            }
            if (params.size() >= 6 && !params.get(5).getString(memoryMap).contentEquals("null")) {
                impact.requireAtBest = RepLevel.valueOf(RepLevel.class, params.get(5).getString(memoryMap));
            }
            if (params.size() >= 7 && !params.get(6).getString(memoryMap).contentEquals("null")) {
                impact.requireAtWorst = RepLevel.valueOf(RepLevel.class, params.get(6).getString(memoryMap));
            }

            RepActionEnvelope envelope = new RepActionEnvelope(RepActions.CUSTOM, impact, dialog.getTextPanel());
            Global.getSector().adjustPlayerReputation(envelope, entity.getActivePerson());
        }

        CustomRepImpact impact = new CustomRepImpact();
        if (params.size() >= 2 && !params.get(1).getString(memoryMap).contentEquals("null")) {
            impact.delta = params.get(1).getFloat(memoryMap);
        }
        if (params.size() >= 3 && !params.get(2).getString(memoryMap).contentEquals("null")) {
            impact.limit = RepLevel.valueOf(RepLevel.class, params.get(2).getString(memoryMap));
        }
        if (params.size() >= 4 && !params.get(3).getString(memoryMap).contentEquals("null")) {
            impact.ensureAtBest = RepLevel.valueOf(RepLevel.class, params.get(3).getString(memoryMap));
        }
        if (params.size() >= 5 && !params.get(4).getString(memoryMap).contentEquals("null")) {
            impact.ensureAtWorst = RepLevel.valueOf(RepLevel.class, params.get(4).getString(memoryMap));
        }
        if (params.size() >= 6 && !params.get(5).getString(memoryMap).contentEquals("null")) {
            impact.requireAtBest = RepLevel.valueOf(RepLevel.class, params.get(5).getString(memoryMap));
        }
        if (params.size() >= 7 && !params.get(6).getString(memoryMap).contentEquals("null")) {
            impact.requireAtWorst = RepLevel.valueOf(RepLevel.class, params.get(6).getString(memoryMap));
        }

        RepActionEnvelope envelope = new RepActionEnvelope(RepActions.CUSTOM, impact, dialog.getTextPanel());
        ReputationAdjustmentResult result = Global.getSector().adjustPlayerReputation(envelope, factionId);
        return result.delta != 0;
    }
}
