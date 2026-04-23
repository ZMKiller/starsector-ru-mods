package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;

public class dpl_PhaseHanger extends BaseHullMod {
	public static final float MAX_DISTANCE = 50f;
	public static final Object KEY_JITTER = new Object();
	public static final Color JITTER_UNDER_COLOR = new Color(125,0,255,125);
	public static final Color JITTER_COLOR = new Color(125,0,255,75);
	
	public void advanceInCombat(ShipAPI ship, float amount) {
		String id = "dpl_phase_hangar";
		MutableShipStatsAPI stats = ship.getMutableStats();
		if (!ship.isPhased()) {
			stats.getFighterRefitTimeMult().unmodify(id);
			return;
		}
		stats.getFighterRefitTimeMult().modifyMult(id, 0.5f);
		if (ship.getVariant().hasHullMod("phase_anchor")) {
			stats.getFighterRefitTimeMult().modifyMult(id, 0.333f);
		}
		
		List<ShipAPI> theFighters = getFighters(ship);
		
		for (ShipAPI fighter : theFighters) {
			if (fighter.isHulk()) continue;
			float dist = Misc.getDistance(fighter.getLocation(), ship.getLocation());
			float jitterLevel = (float) Math.max(Math.atan((MAX_DISTANCE-dist)*0.06f)*0.8f, 0);
				
			fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, 5f);
			fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + 5f);
			fighter.setExtraAlphaMult(1f - (1f - 0.25f) * jitterLevel);
			fighter.setApplyExtraAlphaToEngines(true);
			fighter.setPhased(true);
			
			if (jitterLevel <= 0.1) {
				fighter.setPhased(false);
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
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








