package data.shipsystems.scripts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class SFCBlitzStats extends BaseShipSystemScript {
	public static final Object KEY_JITTER = new Object();

	public static final float DAMAGE_INCREASE_PERCENT = 30;
	public static final float SPEED_INCREASE_PERCENT = 50;
	public static final float SHIP_WEAPONS_DECREASE = 0.5f;

	public static final Color JITTER_UNDER_COLOR = new Color(187, 67, 63, 75);
	public static final Color JITTER_COLOR = new Color(94, 29, 47, 125);


	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		stats.getBallisticWeaponRangeBonus().modifyMult(id, SHIP_WEAPONS_DECREASE);
		stats.getBallisticRoFMult().modifyMult(id, SHIP_WEAPONS_DECREASE);
		stats.getEnergyWeaponRangeBonus().modifyMult(id, SHIP_WEAPONS_DECREASE);
		stats.getEnergyRoFMult().modifyMult(id, SHIP_WEAPONS_DECREASE);
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}


		if (effectLevel > 0) {
			float jitterLevel = effectLevel;
			float maxRangeBonus = 5f;
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			for (ShipAPI fighter : getFighters(ship)) {
				if (fighter.isHulk()) continue;
				MutableShipStatsAPI fStats = fighter.getMutableStats();
//				fStats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);

				fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getBallisticWeaponRangeBonus().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getEnergyWeaponRangeBonus().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMissileWeaponRangeBonus().modifyMult(id, 1f + 0.01f * DAMAGE_INCREASE_PERCENT * effectLevel);
				fStats.getMaxSpeed().modifyMult(id, 1f + 0.01f * SPEED_INCREASE_PERCENT * effectLevel);
				fStats.getAcceleration().modifyMult(id, 1f + 0.01f * SPEED_INCREASE_PERCENT * effectLevel);

				if (jitterLevel > 0) {
					//fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
					fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));

					fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, jitterLevel, 5, 0f, jitterRangeBonus);
					fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 2, 0f, 0 + jitterRangeBonus * 1f);
					Global.getSoundPlayer().playLoop("system_advertisement_blitz", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
				}
			}
		}
	}

	private List<ShipAPI> getFighters(ShipAPI carrier) {
		List<ShipAPI> result = new ArrayList<ShipAPI>();

//		this didn't catch fighters returning for refit
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}

		for (ShipAPI ship : Global.getCombatEngine().getShips()) {
			if (!ship.isFighter()) continue;
			if (ship.getWing() == null) continue;
			if (ship.getWing().getSourceShip() == carrier) {
				result.add(ship);
			}
		}

		return result;
	}


	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		for (ShipAPI fighter : getFighters(ship)) {
			if (fighter.isHulk()) continue;
			MutableShipStatsAPI fStats = fighter.getMutableStats();
			fStats.getBallisticWeaponDamageMult().unmodify(id);
			fStats.getEnergyWeaponDamageMult().unmodify(id);
			fStats.getMissileWeaponDamageMult().unmodify(id);
			fStats.getMaxSpeed().unmodify(id);
			fStats.getAcceleration().unmodify(id);
		}
	}


	public StatusData getStatusData(int index, State state, float effectLevel) {
		float percent1 = DAMAGE_INCREASE_PERCENT * effectLevel;
		if (index == 0) {
			//return new StatusData("+" + (int)percent + "% fighter damage", false);
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + DAMAGE_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter damage", false);
		}
		float percent2 = DAMAGE_INCREASE_PERCENT * effectLevel;
		if (index == 1) {
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + DAMAGE_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter max weapon range", false);
		}
		float percent3 = SPEED_INCREASE_PERCENT * effectLevel;
		if (index == 2) {
			return new StatusData("" + Misc.getRoundedValueMaxOneAfterDecimal(1f + SPEED_INCREASE_PERCENT * effectLevel * 0.01f) + "x fighter max speed and accel", false);
		}
		float percent4 = SHIP_WEAPONS_DECREASE * effectLevel;
		if (index == 3) {
			return new StatusData("decreased ballistics and energy weapon performance", true);
		}
		return null;
	}
}