package data.shipsystems.scripts;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.PhaseCloakSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.HSIEGO.HSIEGOStats;
import data.kit.HSII18nUtil;
import data.scripts.HSIRenderer.HSIButterflyRenderObject;
import data.scripts.HSIRenderer.HSICombatRendererV2;

public class HSIButterflyDive extends BaseShipSystemScript {

	public static Color JITTER_COLOR = new Color(245, 245, 245, 255);
	public static float JITTER_FADE_TIME = 0.5f;

	public static float SHIP_ALPHA_MULT = 0.25f;
	// public static float VULNERABLE_FRACTION = 0.875f;
	public static float VULNERABLE_FRACTION = 0f;
	public static float INCOMING_DAMAGE_MULT = 0.25f;

	public static float MAX_TIME_MULT = 3f;

	public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
	public static float MIN_SPEED_MULT = 0.66f;
	public static float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;

	protected Object STATUSKEY1 = new Object();
	protected Object STATUSKEY2 = new Object();
	protected Object STATUSKEY3 = new Object();
	protected Object STATUSKEY4 = new Object();
	private boolean unapplyed = true;
	protected IntervalUtil butterflyTimer = new IntervalUtil(0.5f, 0.5f);
	protected float level = 0;
	protected CombatEngineAPI engine = Global.getCombatEngine();

	public static float getMaxTimeMult(MutableShipStatsAPI stats) {
		return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
	}

	protected boolean isDisruptable(ShipSystemAPI cloak) {
		return cloak.getSpecAPI().hasTag(Tags.DISRUPTABLE);
	}

	protected float getDisruptionLevel(ShipAPI ship) {
		if (FLUX_LEVEL_AFFECTS_SPEED) {
			float threshold = ship.getMutableStats().getDynamic().getMod(
					Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
			if (threshold <= 0)
				return 1f;
			float level = ship.getHardFluxLevel() / threshold;
			if (level > 1f)
				level = 1f;
			return level;
		}
		return 0f;
	}

	protected void maintainStatus(ShipAPI playerShip, State state, float effectLevel) {
		float level = effectLevel;
		float f = VULNERABLE_FRACTION;

		ShipSystemAPI cloak = playerShip.getPhaseCloak();
		if (cloak == null)
			cloak = playerShip.getSystem();
		if (cloak == null)
			return;

		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (level > f) {
				if (getDisruptionLevel(playerShip) <= 0f) {
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(), HSII18nUtil.getShipSystemString("HSIPhaseText0"), HSII18nUtil.getShipSystemString("HSIPhaseText2")+"100%", false);
				} else {
					String speedPercentStr = (int) Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
					Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY3,
							cloak.getSpecAPI().getIconSpriteName(),
							// "phase coils at " + disruptPercent,
							HSII18nUtil.getShipSystemString("HSIPhaseText1"), HSII18nUtil.getShipSystemString("HSIPhaseText2") + speedPercentStr, true);
				}
			}
		}
	}

	public float getSpeedMult(ShipAPI ship, float effectLevel) {
		if (getDisruptionLevel(ship) <= 0f)
			return 1f;
		return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel);
	}

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		if (Global.getCurrentState() != GameState.COMBAT)
			return;
		if (player) {
			maintainStatus(ship, state, effectLevel);
		}

		if (Global.getCombatEngine().isPaused()) {
			return;
		}

		if (ship.hasListenerOfClass(HSIEGOStats.class)) {
			level = ship.getListeners(HSIEGOStats.class).get(0).getLevel();
		}
		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null)
			cloak = ship.getSystem();
		if (cloak == null)
			return;

		if (FLUX_LEVEL_AFFECTS_SPEED) {
			if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
				float mult = getSpeedMult(ship, effectLevel);
				if (mult < 1f) {
					stats.getMaxSpeed().modifyMult(id + "_2", mult);
				} else {
					stats.getMaxSpeed().unmodifyMult(id + "_2");
				}
				((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(getDisruptionLevel(ship));
			}
		}

		if (state == State.COOLDOWN || state == State.IDLE) {
			if(!unapplyed)
			unapply(stats, id);
			return;
		}

		float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
		float accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
		stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
		stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
		stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);

		float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
		float accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
		stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
		stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
		stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);

		float level = effectLevel;
		// float f = VULNERABLE_FRACTION;

		float jitterLevel = 0f;
		float jitterRangeBonus = 0f;
		float levelForAlpha = level;

		// ShipSystemAPI cloak = ship.getPhaseCloak();
		// if (cloak == null) cloak = ship.getSystem();

		if (state == State.IN || state == State.ACTIVE) {
			ship.setPhased(true);
			unapplyed = false;
			levelForAlpha = level;
			if (effectLevel >= 1) {
				float factor = 1;
				if (level >= 6) {
					factor *= 1.66f;
				} else if (level >= 2) {
					factor *= 1.33f;
				}
				butterflyTimer.advance(engine.getElapsedInLastFrame() * factor);
				float seed = (float) Math.random();
				if (butterflyTimer.intervalElapsed()) {
					MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "HSI_ButterFly",
							Misc.getPointAtRadius(ship.getLocation(),
									effectLevel * ship.getCollisionRadius() * 0.8f),
							seed * 360f,
							null);
					if (level >= 4) {
						proj.getDamage().getModifier().modifyMult("HSIEGOButterFly", 1.5f);
					}
					HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(proj,7,15));
					proj.setSpriteAlphaOverride(0f);
				}
			} else {
				float factor = 2;
				if (level >= 6) {
					factor *= 1.66f;
				} else if (level >= 2) {
					factor *= 1.33f;
				}
				butterflyTimer.advance(engine.getElapsedInLastFrame() * factor);
				float seed = (float) Math.random();
				if (butterflyTimer.intervalElapsed()) {
					MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "HSI_ButterFly",
							Misc.getPointAtRadius(ship.getLocation(),
									effectLevel * ship.getCollisionRadius() * 0.8f),
							seed * 360f,
							null);
					if (level >= 4) {
						proj.getDamage().getModifier().modifyMult("HSIEGOButterFly", 1.5f);
					}
					HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(proj,7,15));
					proj.setSpriteAlphaOverride(0f);
				}
			}
		} else if (state == State.OUT) {
			if (level > 0.5f) {
				ship.setPhased(true);
			} else {
				ship.setPhased(false);
			}
			levelForAlpha = level;
			unapplyed = false;
		}

		// ship.setJitter(JITTER_COLOR, jitterLevel, 1, 0, 0 + jitterRangeBonus);
		// ship.setJitterUnder(JITTER_COLOR, jitterLevel, 11, 0f, 7f +
		// jitterRangeBonus);
		// ship.getEngineController().fadeToOtherColor(this, spec.getEffectColor1(), new
		// Color(0,0,0,0), jitterLevel, 1f);
		// ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

		ship.setExtraAlphaMult(Math.max(0,1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha) );
		ship.setApplyExtraAlphaToEngines(true);

		// float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha;
		float extra = 0f;
		// if (isDisruptable(cloak)) {
		// extra = disruptionLevel;
		// }
		float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha * (1f - extra);
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}

	}

	public void unapply(MutableShipStatsAPI stats, String id) {

		ShipAPI ship = null;
		// boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			// player = ship == Global.getCombatEngine().getPlayerShip();
			// id = id + "_" + ship.getId();
		} else {
			return;
		}

		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);

		stats.getMaxSpeed().unmodify(id);
		stats.getMaxSpeed().unmodifyMult(id + "_2");
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);

		ship.setPhased(false);
		ship.setExtraAlphaMult(1f);

		ShipSystemAPI cloak = ship.getPhaseCloak();
		if (cloak == null)
			cloak = ship.getSystem();
		if (cloak != null) {
			((PhaseCloakSystemAPI) cloak).setMinCoilJitterLevel(0f);
		}
		unapplyed = true;
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		// if (index == 0) {
		// return new StatusData("time flow altered", false);
		// }
		// float percent = (1f - INCOMING_DAMAGE_MULT) * effectLevel * 100;
		// if (index == 1) {
		// return new StatusData("damage mitigated by " + (int) percent + "%", false);
		// }
		return null;
	}
}
