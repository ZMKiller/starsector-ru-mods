package org.amazigh.foundry.shipsystems.scripts;

//import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ASF_temporalBurn_nebelStats extends BaseShipSystemScript {

	public static final float TIME_MULT = 1.5f; // add one to get the actual timescale multiplier
	public static final float SPEED_BOOST = 280f;
	public static final float ACCEL_BOOST = 420f;
	public static final float TURN_MALUS = 0.25f;
	public static final float TURN_A_MALUS = 0.20f;
	public static final float ROF_BONUS = 10.0f;
	public static final float REGEN_BONUS = 40.0f; // so this version of temporal burn also boosts ammo regen "slightly" (spicy!)
	
	public static final float DAMAGE_REDUCTION = 0.35f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		
		float shipTimeMult = 1f + (TIME_MULT * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			
			stats.getEnergyRoFMult().unmodify(id); // so we don't get a loader overdrive style RoF burst, and just forcibly accelerate weapon reloads
			stats.getBallisticRoFMult().unmodify(id);
			stats.getMissileRoFMult().unmodify(id);
			stats.getEnergyAmmoRegenMult().unmodify(id);
			stats.getBallisticAmmoRegenMult().unmodify(id);
			stats.getMissileAmmoRegenMult().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST * effectLevel);
			stats.getAcceleration().modifyFlat(id, ACCEL_BOOST * effectLevel);
			
			stats.getEnergyRoFMult().modifyMult(id, ROF_BONUS);
			stats.getBallisticRoFMult().modifyMult(id, ROF_BONUS);
			stats.getMissileRoFMult().modifyMult(id, ROF_BONUS);
			stats.getEnergyAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
			stats.getBallisticAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
			stats.getMissileAmmoRegenMult().modifyPercent(id, REGEN_BONUS);
		}
		
		stats.getTurnAcceleration().modifyMult(id, 1f - (TURN_A_MALUS * effectLevel));
		stats.getMaxTurnRate().modifyMult(id, 1f - (TURN_MALUS * effectLevel));
		
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (DAMAGE_REDUCTION * effectLevel));
			// secret "hidden" (up to) 35% damage resist while system is active :)
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);

		stats.getEnergyRoFMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
		stats.getEnergyAmmoRegenMult().unmodify(id);
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getMissileAmmoRegenMult().unmodify(id);

		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);
		
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		if (index == 1) {
			return new StatusData("time flow altered", false);
		}
		return null;
	}
}
