package data.scripts.campaign.intel.bar;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class CabalBarEventCreator extends BaseBarEventCreator {
	
	@Override
	public PortsideBarEvent createBarEvent() {
		return new CabalBarEvent();
	}
	
	@Override
	public float getBarEventTimeoutDuration() {
		return super.getBarEventTimeoutDuration() * 2f;
	}

	@Override
	public float getBarEventAcceptedTimeoutDuration() {
		return super.getBarEventAcceptedTimeoutDuration() * 2f;
	}
}
