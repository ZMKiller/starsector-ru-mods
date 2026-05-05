package data.campaign;


import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;


public class HSICommissionerBarEvent1Creator extends BaseBarEventCreator {

    public HSICommissionerBarEvent1 createBarEvent() {
        return new HSICommissionerBarEvent1();
    }

    public boolean isPriority() {
        return true;
    }

    public float getBarEventFrequencyWeight() {
        return 1000f;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 10000000000f; // will reset when intel ends... or not, if keeping this one-time-only
    }
}