package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

// UW_UpgradeInstalled <upgradeId>
public class UW_UpgradeInstalled extends UW_RestoreOrUpgradeAvailable {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (!anyUpgradeAvailable(ruleId, dialog, params, memoryMap)) {
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

        EnumSet<UpgradeType> currentUpgrades = UpgradeType.getCurrentUpgrades(targetMember.getVariant());
        return currentUpgrades.contains(upgrade);
    }
}
