package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class ASF_TemporalCoreStats extends BaseShipSystemScript {
	
	public static final float DAMAGE_REDUCTION = 0.2f; // yes it gives a "damper" when unregulated, but it's *weakish*
	public static final float TIME_MULT = 4f;
	public static final float VEL_MULT = 20f;
	public Color ENGINE_COLOR = new Color(165,90,255,55);
	
    private final Object STATUSKEY1 = new Object();
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = null;
		boolean player = false;
        CombatEngineAPI engine = Global.getCombatEngine();
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
		}
		
		float shipTimeMult = TIME_MULT;

		float TRUE_REDUCTION = DAMAGE_REDUCTION;
		
		if (ship.getVariant().getHullMods().contains("ASF_PhantasmagoriaRegulator_off")) {
			shipTimeMult = 1f + ((TIME_MULT - 1f) * effectLevel);
		} else {
			TRUE_REDUCTION *= 1.5f; // stronger damper when regulated!
			shipTimeMult = 1f + ((TIME_MULT - 2.5f) * effectLevel); // weaker timescale when regulated, one of the *big* nerfs that you get from regulating
		}
		
		float RED = 165f - (115f * effectLevel);
		float GREEN = 90f + (160f * effectLevel);
		float BLUE = 255f - (155f * effectLevel);

		if (effectLevel < 0.99f) {
			TRUE_REDUCTION *= 0.5f;
		}
		
		
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (TRUE_REDUCTION * effectLevel));
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (TRUE_REDUCTION * effectLevel));
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (TRUE_REDUCTION * effectLevel));
		
		stats.getEnergyProjectileSpeedMult().modifyPercent(id, (effectLevel * VEL_MULT));
		
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		
		ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, new Color(15,0,30,40), effectLevel, 0.6f);
		ship.getEngineController().extendFlame(this, 0.2f, 0.2f, 0.2f);
		
		float FLUX_USAGE = ship.getFluxLevel();
		float ALPHA_1 = (FLUX_USAGE * 5) + 15f + (10f * effectLevel);
		float ALPHA_2 = (FLUX_USAGE * 10) + 20f + (15f * effectLevel);
		
		Color JITTER_COLOR = new Color((int)RED,(int)GREEN,(int)BLUE,(int)ALPHA_1);
		Color JITTER_UNDER_COLOR = new Color((int)RED,(int)GREEN,(int)BLUE,(int)ALPHA_2);
		
		float jitterRangeBonus_1 = (FLUX_USAGE * 3f) * (1f + effectLevel);
		float jitterRangeBonus_2 = (FLUX_USAGE * 9f) * (1f + effectLevel);
		
		float jitterLevel = ( (float) Math.sqrt(FLUX_USAGE) * 0.35f ) + 0.85f;
		
		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 15f + jitterRangeBonus_1);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 30f + jitterRangeBonus_2);
		
		
		// repair section
		
		if (ship == engine.getPlayerShip()) {
            engine.maintainStatusForPlayerShip(STATUSKEY1, "graphics/icons/hullsys/temporal_shell.png",
                    "Temporal Core", "time flow altered", true);
        }
		
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getHullDamageTakenMult().unmodify(id);
		stats.getArmorDamageTakenMult().unmodify(id);
		stats.getEmpDamageTakenMult().unmodify(id);

		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("time flow altered", false);
		}
		if (index == 1) {
			return new StatusData("damage taken reduced", false);
		}
		return null;
	}
}
