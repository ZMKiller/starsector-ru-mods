package data.scripts.campaign;

import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;

import data.scripts.crisis.dpl_ActivityCause;
import data.scripts.crisis.dpl_HostileActivityFactor;

public class dpl_crisis_plugin implements EconomyTickListener {
    @Override
    public void reportEconomyTick(int iterIndex) {
        addDPLColonyCrisis(); // HACK: More convenient to add the crisis mid-game using an existing listener
    }

    public static void addDPLColonyCrisis() {
        HostileActivityEventIntel intel = HostileActivityEventIntel.get();
        if (intel != null && intel.getActivityOfClass(dpl_HostileActivityFactor.class) == null)
            intel.addActivity(new dpl_HostileActivityFactor(intel), new dpl_ActivityCause(intel));
    }

	@Override
	public void reportEconomyMonthEnd() {
		// TODO Auto-generated method stub
		
	}
}
