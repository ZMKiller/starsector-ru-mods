package data.scripts.world.underworld;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import data.scripts.world.underworld.UW_DickersonFleetManager.DickersonFleetData;

public class UW_DickersonAssignmentAI implements EveryFrameScript {

    private final DickersonFleetData data;
    private float daysTotal = 0f;
    private final CampaignFleetAPI fleet;
    private boolean orderedReturn = false;

    public UW_DickersonAssignmentAI(CampaignFleetAPI fleet, DickersonFleetData data) {
        this.fleet = fleet;
        this.data = data;
        giveInitialAssignment();
    }

    @Override
    public void advance(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        daysTotal += days;

        if (daysTotal > 120f) {
            giveReturnToSourceOrders();
            return;
        }

        if (fleet.getAI().getCurrentAssignment() != null) {
            float fp = fleet.getFleetPoints();
            if (fp < data.startingFleetPoints / 3 || !fleet.getMemoryWithoutUpdate().getBoolean("$stillAlive")) {
                giveReturnToSourceOrders();
            }
        } else {
            fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, data.source, 1000, "defending "
                    + data.sourceMarket.getName());
        }
    }

    @Override
    public boolean isDone() {
        return !fleet.isAlive();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    private float getDaysToOrbit() {
        return 1f;
    }

    private void giveInitialAssignment() {
        float daysToOrbit = getDaysToOrbit();
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, data.source, daysToOrbit, "preparing for duty at "
                + data.source.getName());
    }

    private void giveReturnToSourceOrders() {
        if (!orderedReturn) {
            orderedReturn = true;

            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, data.source, 1000);
        }
    }
}
