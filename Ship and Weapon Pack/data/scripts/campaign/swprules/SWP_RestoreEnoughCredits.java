package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

public class SWP_RestoreEnoughCredits extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$swpRestoreTarget")) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }
        
        String targetId = Global.getSector().getMemoryWithoutUpdate().getString("$swpRestoreTarget");
        FleetMemberAPI targetMember = null;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.getId().contentEquals(targetId)) {
                targetMember = member;
                break;
            }
        }
        if (targetMember == null) {
            return false;
        }

        int numDMods = 0;
        for (String modId : targetMember.getVariant().getHullMods()) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
            if ((modSpec != null) && modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                numDMods++;
            }
        }
        long startingDMods = 7;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpStartingDMods")) {
            startingDMods = Global.getSector().getMemoryWithoutUpdate().getLong("$swpStartingDMods");
        }
        if (numDMods == 0) {
            return false;
        }

        float needed = targetMember.getHullSpec().getBaseValue() * Global.getSettings().getFloat("baseRestoreCostMult");
        for (int i = 0; i < numDMods; i++) {
            needed *= Global.getSettings().getFloat("baseRestoreCostMultPerDMod");
        }
        needed /= numDMods;
        needed *= 7f / (float) startingDMods;
        if (needed > 0) {
            needed = Math.max(1, Math.round(needed));
        }

        float credits = playerFleet.getCargo().getCredits().get();
        return credits >= needed;
    }
}
