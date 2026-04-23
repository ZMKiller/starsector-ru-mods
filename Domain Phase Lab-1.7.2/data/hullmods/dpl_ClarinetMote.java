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

public class dpl_ClarinetMote extends BaseHullMod {

	public static int MAX_MOTES = 12;
	public static int BASE_MOTES = 6;
	
	public static float CD_Time = 10f;
	
	public static Object STATUS_KEY = new Object();

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(CD_Time);
		if (index == 1) return "" + (int)Math.round(BASE_MOTES);
		if (index == 2) return "" + (int)Math.round(MAX_MOTES);
		if (index == 3) return "350";
		if (index == 4) return "350";
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

	
	public static String dpl_clarinetMote_DATA_KEY = "dpl_clarinet_mote_data_key";
	public static class dpl_ClarinetMoteData {
		public IntervalUtil interval = new IntervalUtil(CD_Time - 0.1f, CD_Time + 0.1f);;
		public int count = BASE_MOTES;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = dpl_clarinetMote_DATA_KEY + "_" + ship.getId();
		dpl_ClarinetMoteData data = (dpl_ClarinetMoteData) engine.getCustomData().get(key);
		if (data == null) {
			data = new dpl_ClarinetMoteData();
			engine.getCustomData().put(key, data);
			if (data.count != BASE_MOTES) {
				data.count = BASE_MOTES; //Just here for safety reasons.
			}
		}
		
		data.interval.advance(amount);
		if (data.count > MAX_MOTES) {
			data.count = MAX_MOTES; //It cannot exceed MAX_MOTES.
		}
		
		if (data.interval.intervalElapsed()) {
			int num_motes = data.count;
			if (num_motes > BASE_MOTES) {
				data.count -= 1;
			} else if (num_motes < BASE_MOTES) {
				data.count += 1;
			}
		}
		
		boolean playerShip = ship == Global.getCombatEngine().getPlayerShip();
		
		if (playerShip) {
			String icon = Global.getSettings().getSpriteName("warroom", "icon_waypoint");
			String num_motes = "" + (int) data.count + "";
			Global.getCombatEngine().maintainStatusForPlayerShip(
					STATUS_KEY, icon, "Currently you have", num_motes + " anomalies available", false);
		}
		
	}
	
}














