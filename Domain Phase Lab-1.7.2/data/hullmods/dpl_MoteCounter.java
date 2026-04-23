package data.hullmods;

import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_MoteCounter extends BaseHullMod {

	public static int MAX_MOTES = 36;
	
	public static float FLUX_COEFFICIENT = 5f;
	
	public static Object STATUS_KEY = new Object();
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(FLUX_COEFFICIENT) + "%";
		if (index == 1) return "300";
		if (index == 2) return "" + (int)Math.round(MAX_MOTES);
		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null;
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		return super.getUnapplicableReason(ship);
	}

	
	public static String dpl_MoteCount_DATA_KEY = "dpl_mote_count_data_key";
	public static class dpl_MoteCountData {
		float flux = -1f;
		float flux_gen = 0;
		public int motes = 0;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = dpl_MoteCount_DATA_KEY + "_" + ship.getId();
		dpl_MoteCountData data = (dpl_MoteCountData) engine.getCustomData().get(key);
		if (data == null) {
			data = new dpl_MoteCountData();
			engine.getCustomData().put(key, data);
			if (data.flux<0) {
				data.flux = ship.getFluxTracker().getCurrFlux();
			}
		}
		
		data.flux_gen += Math.max(0f, ship.getFluxTracker().getCurrFlux() - data.flux);
		
		if (data.flux_gen >= ship.getFluxTracker().getMaxFlux() * FLUX_COEFFICIENT * 0.01f) {
			int num_motes = (int) Math.floor((data.flux_gen)/(ship.getFluxTracker().getMaxFlux() * FLUX_COEFFICIENT * 0.01f));
			if (data.motes < MAX_MOTES) {
				data.motes += Math.min(num_motes, MAX_MOTES - data.motes);
			}
			data.flux_gen = data.flux_gen - num_motes * ship.getFluxTracker().getMaxFlux() * FLUX_COEFFICIENT * 0.01f;
		}
		
		data.flux = ship.getFluxTracker().getCurrFlux();
		
		boolean playerShip = ship == Global.getCombatEngine().getPlayerShip();
		
		if (playerShip) {
			String icon = Global.getSettings().getSpriteName("warroom", "icon_waypoint");
			String num_motes = "" + (int) data.motes + "";
			Global.getCombatEngine().maintainStatusForPlayerShip(
					STATUS_KEY, icon, "Currently you have", num_motes + " anomalies available", false);
		}
		
	}
	
}














