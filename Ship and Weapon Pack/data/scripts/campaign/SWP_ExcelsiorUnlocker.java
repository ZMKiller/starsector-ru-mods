package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.util.Misc;
import java.util.List;

public class SWP_ExcelsiorUnlocker implements FleetEventListener {

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI nullHere, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (battle == null || !battle.isPlayerInvolved()) {
            return;
        }

        for (CampaignFleetAPI fleet : battle.getNonPlayerSideSnapshot()) {
            List<FleetMemberAPI> members = Misc.getSnapshotMembersLost(fleet);
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (!members.contains(member)) {
                    members.add(member);
                }
            }
            for (FleetMemberAPI member : members) {
                if (member.getHullId().contentEquals("swp_excelsior_boss")) {
                    if (!SharedUnlockData.get().isPlayerAwareOfShip("swp_excelsior")) {
                        SharedUnlockData.get().reportPlayerAwareOfShip("swp_excelsior", true);
                        Global.getSector().getListenerManager().removeListener(this);
                        return;
                    }
                }
            }
        }
    }
}
