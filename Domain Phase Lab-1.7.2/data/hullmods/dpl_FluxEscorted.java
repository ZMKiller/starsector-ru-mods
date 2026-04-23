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

public class dpl_FluxEscorted extends BaseHullMod {

	public static float FLUX_BONUS = 200f;
	
	public static float ADDITIONAL_BONUS = 50f;
	
	public static float MAX_BONUS = 2f;
	
	public static float EFFECT_RANGE = 600f;
	public static float EFFECT_FADE = 400f;
	
	public static Object STATUS_KEY = new Object();
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "800";
		if (index == 1) return "" + (int)Math.round(FLUX_BONUS) + "";
		if (index == 2) return "" + (int)Math.round(ADDITIONAL_BONUS) + "";
		if (index == 3) return "" + (int)Math.round(MAX_BONUS * FLUX_BONUS) + "";
		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return getUnapplicableReason(ship) == null;
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && !ship.isCapital()) {
			return "Can only be installed on capital ships";
		}
		return super.getUnapplicableReason(ship);
	}


	
	public static String dpl_FED_DATA_KEY = "dpl_flux_escorted_data_key";
	public static class dpl_FluxEscortedData {
		IntervalUtil interval = new IntervalUtil(0.9f, 1.1f);
		float mag = 0;
	}
	
	public void dpl_applyFEDEffect(ShipAPI ship, ShipAPI other, float mag) {
		String id = "dpl_flux_escorted_bonus" + ship.getId();
		MutableShipStatsAPI selfStats = ship.getMutableStats();
		
		if (mag > 0) {
			selfStats.getFluxDissipation().modifyFlat(id, mag*(FLUX_BONUS));
			
		} else {
			selfStats.getFluxDissipation().unmodify(id);
		}
		
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = dpl_FED_DATA_KEY + "_" + ship.getId();
		dpl_FluxEscortedData data = (dpl_FluxEscortedData) engine.getCustomData().get(key);
		if (data == null) {
			data = new dpl_FluxEscortedData();
			engine.getCustomData().put(key, data);
		}

		boolean playerShip = ship == Global.getCombatEngine().getPlayerShip();
		
		data.interval.advance(amount * 4f);
		if (data.interval.intervalElapsed() || playerShip) {
			float checkSize = EFFECT_RANGE + EFFECT_FADE + ship.getCollisionRadius() + 300f;
			checkSize *= 2f;
			
			Iterator<Object> iter = Global.getCombatEngine().getShipGrid().getCheckIterator(
											ship.getLocation(), checkSize, checkSize);
			
			ShipAPI best = null;
			float bestMag = 0f;
			while (iter.hasNext()) {
				Object next = iter.next();
				if (!(next instanceof ShipAPI)) continue;
				
				ShipAPI other = (ShipAPI) next;
				
				if (ship == other) continue;
				if (other.getOwner() != ship.getOwner()) continue;
				
				if (!other.isFrigate()) continue;
				
				if (!other.getVariant().hasHullMod("dpl_flux_escort")) continue;
				
				if (other.isHulk()) continue;
				
				float radSum = ship.getShieldRadiusEvenIfNoShield() + other.getShieldRadiusEvenIfNoShield();
				radSum *= 0.75f;
				float dist = Misc.getDistance(ship.getShieldCenterEvenIfNoShield(), other.getShieldCenterEvenIfNoShield());
				dist -= radSum;
				
				float mag = 0f;
				if (dist < EFFECT_RANGE) {
					mag = 1f;
				} else if (dist < EFFECT_RANGE + EFFECT_FADE) {
					mag = 1f - (dist - EFFECT_RANGE) / EFFECT_FADE;
				}
				
				if (other.getVariant().getSMods().contains("dpl_flux_escort")) {
					mag *= 1.25f;
				}
				
				if (mag > 0f) {
					best = other;
					bestMag += mag;
				}
			}
			
			float realMag = Math.min(MAX_BONUS, bestMag);
			
			dpl_applyFEDEffect(ship, best, realMag);
			
			data.mag = realMag;
		}
		
		if (playerShip) {
			if (data.mag > 0.005f) {
				String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");
				String percent = "" + (int) Math.round(data.mag * 100f) + "%";
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUS_KEY, icon, "Remote flux projector", percent + " telemetry quality", false);
			} else {
				String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");
				Global.getCombatEngine().maintainStatusForPlayerShip(
						STATUS_KEY, icon, "Remote flux projector", "no connection", true);
			}
		}
		
	}
	
}














