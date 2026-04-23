package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.util.Misc;

@SuppressWarnings("unchecked")
public class dpl_FluxGate extends BaseHullMod {

	public static final int HARD_FLUX_DISSIPATION_PERCENT = 30;
	public static final float MAX_DISTANCE = 300f;
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_UNDER_COLOR = new Color(155,50,255,125);
	public static final Color JITTER_COLOR = new Color(155,50,255,75);
	
	public void advanceInCombat(ShipAPI ship, float amount) {
		
		List<ShipAPI> theFighters = getFighters(ship);
		if (theFighters.isEmpty()) return;
		String id = "dpl_flux_gate";
		MutableShipStatsAPI stats = ship.getMutableStats();
		ship.getFluxTracker().decreaseFlux(amount*stats.getFluxDissipation().getModifiedValue()*HARD_FLUX_DISSIPATION_PERCENT*0.01f);
			
		for (ShipAPI fighter : theFighters) {
			if (fighter.isHulk()) continue;
			float dist = Misc.getDistance(fighter.getLocation(), ship.getLocation());
			float jitterLevel = (float) Math.max(Math.atan((MAX_DISTANCE-dist)*0.01f)*0.8f, 0);
				
			fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, 5f);
			fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + 5f);
			Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)MAX_DISTANCE;
		if (index == 1) return "" + HARD_FLUX_DISSIPATION_PERCENT;
		return null;
	}
	
	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();
		
		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				float dist = Misc.getDistance(ship.getLocation(), carrier.getLocation());
				if (dist <= MAX_DISTANCE) {
					result.add(ship);
				}
			}
		}
		return result;
	}



}
