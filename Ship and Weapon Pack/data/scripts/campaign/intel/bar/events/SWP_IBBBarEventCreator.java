package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import data.scripts.campaign.intel.SWP_IBBTracker;

public class SWP_IBBBarEventCreator extends BaseBarEventCreator {

    public boolean createInstantly = false;
    public boolean debug = false;

    @Override
    public PortsideBarEvent createBarEvent() {
        return new SWP_IBBBarEvent();
    }

    @Override
    public float getBarEventFrequencyWeight() {
        /* Exponentially less likely to spawn additional bounties when you leave them laying around */
        if (createInstantly || debug) {
            return 1000f;
        }
        if (SWP_IBBTracker.getTracker().allStagesComplete()) {
            return 0f;
        }
        float giveNextWeight = (SWP_IBBTracker.getTracker().getNumCompletedStages() > 0) ? 3f : 1.5f;
        return giveNextWeight * super.getBarEventFrequencyWeight() / (float) Math.pow(2, Math.max(0, SWP_IBBTracker.getTracker().getPendingStages()));
    }

    @Override
    public float getBarEventTimeoutDuration() {
        if (createInstantly || debug) {
            return 0f;
        }
        return 10f;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        /* Short timeout with few active bounties, increases quickly with more pending bounties 
        NOTE: when this function is called, getPendingStages() has NOT been incremented yet! */
        if (createInstantly || debug) {
            return 0f;
        }
        return Math.max(0f, (20f * Math.max(0, 1 + SWP_IBBTracker.getTracker().getPendingStages())) + 10f);
    }

    @Override
    public boolean isPriority() {
        if (SWP_IBBTracker.getTracker().allStagesComplete()) {
            return false;
        }
        return createInstantly || ((SWP_IBBTracker.getTracker().getNumCompletedStages() > 0)
                && (SWP_IBBTracker.getTracker().getPendingStages() == 0));
    }
}
