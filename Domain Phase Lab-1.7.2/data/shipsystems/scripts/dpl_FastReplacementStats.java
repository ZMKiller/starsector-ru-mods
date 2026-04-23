package data.shipsystems.scripts;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;

public class dpl_FastReplacementStats extends BaseShipSystemScript {
	
	public static String RD_NO_EXTRA_CRAFT = "rd_no_extra_craft";
	
	public static float RATE_COST = 0f;
	public static float RATE_COST_1_BAY = 0f;
	public static float BASE_WAIT = 5f;
	
	public static float getRateCost(int bays) {
		if (bays <= 1) return RATE_COST_1_BAY;
		return RATE_COST;
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		if (effectLevel == 1) {
			
			float minRate = Global.getSettings().getFloat("minFighterReplacementRate");
			
			int bays = ship.getLaunchBaysCopy().size();
			float cost = getRateCost(bays);
			
			boolean Used = false;
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null) continue;
				
				if (!bay.getWing().getSpec().hasTag("dpl_anomaly_drone")) continue;
				
				float rate = Math.max(minRate, bay.getCurrRate() - cost);
				bay.setCurrRate(rate);
				
				bay.makeCurrentIntervalFast();
				
				List<ShipAPI> fighters = bay.getWing().getWingMembers();
				int MaxNum = bay.getWing().getSpec().getNumFighters();
				int num = MaxNum - fighters.size();
				
				if (num > 0) {
					bay.setFastReplacements(num);
					Used = true;
				}
			}
			if (!Used) {
				ship.getSystem().setCooldownRemaining(BASE_WAIT);
			}
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}


	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
}








