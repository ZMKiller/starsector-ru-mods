package data.scripts.campaign.swprules;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.scripts.util.SWP_Util;
import java.util.List;
import java.util.Map;

public class SWP_RestoreAvailable extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$swpRestoreTarget")) {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return false;
            }

            boolean found = false;
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (SWP_Util.getNonDHullId(member.getHullSpec()).startsWith("swp_notredame")) {
                    Global.getSector().getMemoryWithoutUpdate().set("$swpRestoreTarget", member.getId());
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
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
        memoryMap.get(MemKeys.GLOBAL).set("$swpRestoreShip", targetMember.getShipName(), 0);
        if (numDMods > 1) {
            memoryMap.get(MemKeys.GLOBAL).set("$swpRestoreModsStr", "one random", 0);
        } else {
            memoryMap.get(MemKeys.GLOBAL).set("$swpRestoreModsStr", "the last", 0);
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

        memoryMap.get(MemKeys.GLOBAL).set("$swpRestoreCostStr", Misc.getWithDGS(needed), 0);

        SectorEntityToken entity = dialog.getInteractionTarget();
        if ((entity.getMarket() != null) && !entity.getMarket().hasSpaceport()) {
            return false;
        }

        RepLevel level = entity.getFaction().getRelationshipLevel(Factions.PLAYER);
        if (!level.isAtWorst(RepLevel.SUSPICIOUS)) {
            return false;
        }

        return needed > 0;
    }
}
