package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.UW_RestoreOrUpgradeAvailable.UpgradeType;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

// UW_UpgradeEnoughCredits <upgradeId>
public class UW_UpgradeEnoughCredits extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains("$uwTIMMember")) {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        String targetId = Global.getSector().getMemoryWithoutUpdate().getString("$uwTIMMember");
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

        String upgradeId = params.get(0).getString(memoryMap);
        UpgradeType upgrade = UpgradeType.getUpgrade(upgradeId);
        if (upgrade == null) {
            return false;
        }

        float needed = UpgradeType.getCost(upgrade, targetMember.getVariant());
        float credits = playerFleet.getCargo().getCredits().get();
        return credits >= needed;
    }
}
