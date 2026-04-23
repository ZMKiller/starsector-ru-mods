package org.amazigh.foundry.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class ASF_amitaDriveStats extends BaseShipSystemScript {

	public static final float SPEED_BOOST = 60f;
	public static final float OTHER_BOOST = 90f;
	
	public static final float SECRET_BOOST = 15f;

	public static final float TIME_MULT = 1.6f;
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST);
		}
		stats.getAcceleration().modifyPercent(id, OTHER_BOOST * effectLevel);
		stats.getDeceleration().modifyPercent(id, OTHER_BOOST * effectLevel);
		stats.getTurnAcceleration().modifyPercent(id, OTHER_BOOST * effectLevel);
		stats.getMaxTurnRate().modifyPercent(id, OTHER_BOOST * effectLevel * 0.8f); //this is not quite as high, because it's a "main" stat, not an accel stat.
		
 		stats.getDamageToTargetShieldsMult().modifyPercent(id, SECRET_BOOST * effectLevel);
 		stats.getHitStrengthBonus().modifyPercent(id, SECRET_BOOST * effectLevel);
 			// shh, secret 15% "damage performance" bonus >_>
 		stats.getArmorDamageTakenMult().modifyPercent(id, -SECRET_BOOST * effectLevel);
 		stats.getBeamDamageTakenMult().modifyPercent(id, -SECRET_BOOST * effectLevel);
 			// also minor armour+beam damage resistance
		
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		float shipTimeMult = 1f + ((TIME_MULT - 1f) * effectLevel);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		
 		stats.getDamageToTargetShieldsMult().unmodify(id);
 		stats.getHitStrengthBonus().unmodify(id);
 		stats.getArmorDamageTakenMult().unmodify(id);
 		stats.getBeamDamageTakenMult().unmodify(id);
		
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("+" + (int)SPEED_BOOST + " top speed", false);
		}
		return null;
	}
}