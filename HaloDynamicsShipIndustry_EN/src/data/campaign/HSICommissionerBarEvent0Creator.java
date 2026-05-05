package data.campaign;


import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;


public class HSICommissionerBarEvent0Creator extends BaseBarEventCreator {

    public HSICommissionerBarEvent0 createBarEvent() {
        return new HSICommissionerBarEvent0();
    }

    public boolean isPriority() {
        return true;
    }

    public float getBarEventFrequencyWeight() {
        return 100000f;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 10000000000f; // will reset when intel ends... or not, if keeping this one-time-only
    }
}