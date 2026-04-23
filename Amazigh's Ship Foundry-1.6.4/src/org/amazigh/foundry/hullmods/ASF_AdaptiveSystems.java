package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class ASF_AdaptiveSystems extends BaseHullMod {

	// Don't fucking look at any of this, it's cursed content.
	
	// Also this is the Lustres secret sauce, so you're spoiling the meme by looking at this!
	// i guess less of a spoiler now that this is "in the open" via hullmod description, but w/e
	
	public static final float HARD_DISS = 15f;
	public static final float TIME_HULL = 45f;
	public static final float SPEED_HULL = 0.2f;
	
	public static final float UPKEEP_MULT = 2f;
	public static final float EFF_DIV = 0.4f;
	
	public static final float TIME_FLUX = 25f;
	public static final float SPEED_FLUX = 0.15f;
	
	public static final Color JITTER_COLOR = new Color(90,165,255,45);
	public static final Color JITTER_UNDER_COLOR = new Color(90,165,255,95);
	
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused()) {
			return;
		}
		if ( !ship.isAlive() || ship.isPiece() ) {
            return;
        }
		MutableShipStatsAPI stats = ship.getMutableStats();
		
		// Stat setup section
		
		float DAMAGE = Math.min((1 - ship.getHullLevel()), 0.6f);
		float HULL_RATIO = DAMAGE / 0.6f;
		
		float HARD_FLUX = Math.min(ship.getHardFluxLevel(), 0.8f);
		float HARD_RATIO = HARD_FLUX / 0.8f;
		
		float FLUX_USAGE = ship.getFluxLevel();
		// Stat setup section
		
		// Hull section
		stats.getHardFluxDissipationFraction().modifyFlat(spec.getId(), HULL_RATIO * HARD_DISS * 0.01f);
		// Hull section
		
		// Hardflux section
		//if (SHIELD_HAVE = true) {
		if (ship.getVariant().hasHullMod("shield_shunt")) {
			//yeah this is empty because fuck shield shunt
		} else {
			stats.getShieldUpkeepMult().modifyMult(spec.getId(), 1f + (HARD_RATIO * UPKEEP_MULT));
			stats.getShieldDamageTakenMult().modifyMult(spec.getId(), 1f - (HARD_RATIO * EFF_DIV));
		}
		// Hardflux section
		
		// Softflux section
		stats.getMaxSpeed().modifyMult(spec.getId(), 1f - ((HULL_RATIO * SPEED_HULL)+(FLUX_USAGE * SPEED_FLUX))); //combined the speed from hull into here to ensure it works "right"
		// Softflux section
		
		// Time section
		float HULL_TIME_BONUS = HULL_RATIO * TIME_HULL;
		float FLUX_TIME_BONUS = FLUX_USAGE * TIME_FLUX;
		float TIME_MULT = HULL_TIME_BONUS + FLUX_TIME_BONUS +10f; // the ship gets a free 10% timescale because if this was at 0 by default it freezes the game because lol divide by zero
		
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        
		if (ship.getSystem() != null) {
			if (player) {
				ship.getMutableStats().getTimeMult().modifyPercent(spec.getId(), TIME_MULT);
				Global.getCombatEngine().getTimeMult().modifyPercent(spec.getId(), 1f / TIME_MULT);
			} else {
				ship.getMutableStats().getTimeMult().modifyPercent(spec.getId(), TIME_MULT);
				Global.getCombatEngine().getTimeMult().unmodify(spec.getId());
			}
		} else {
			ship.getMutableStats().getTimeMult().unmodify(spec.getId());
			Global.getCombatEngine().getTimeMult().unmodify(spec.getId());
		}
		// Time section
		
		// Jitter section
		float JITTER_FLUX = FLUX_USAGE * 0.45f;
		float JITTER_HULL = HULL_RATIO * 0.7f;
		
		float jitterLevel = JITTER_HULL + JITTER_FLUX;
		float maxRangeBonus = 10f;
		
		if (jitterLevel > 0f) {
			if (jitterLevel > 1f) {
				jitterLevel = 1f;
			}
			float jitterRangeBonus = jitterLevel * maxRangeBonus;
			
			jitterLevel = (float) Math.sqrt(jitterLevel);
			
			ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
			ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		}
		// Jitter section
	}
	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
		// So what this hullmod does is as follows:
		// - As the ship takes hull damage (max bonus at 40% hull or lower)
		// --- The ship gains the ability to dissipate hardflux while the shield is up, at up to 15% efficiency
		// --- The ships timescale is increased by up to 45% (with a minor reduction in speed to partially compensate for this)
		// - As the ship generates hardflux (max bonus at 80% or higher)
		// --- The ships shield gains efficiency, to 40% damage taken of the base value
		// --- The ships shield upkeep increases, to 3 times base value (this is to compensate somewhat for the efficiency gain)
		// - As the ship generates any kind of flux (max bonus at 100% flux)
		// --- The ships timescale is increased by up to 25% (with a minor reduction in speed to partially compensate for this)
		// The ship also has a flat 10% increased base timescale compared to normal ships
		
		// At max the ship gets 180% timescale, so +80% effective performance
		// but the speed malus would drop to 65% speed, when combined, that is 117% effective speed
	}
		
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color grey = Misc.getGrayColor();
		
		LabelAPI label = tooltip.addPara("Advanced systems that allow the ship to adapt to the battlefield situation, providing enhanced performance when under greater combat stress.", pad);
		
		tooltip.addSectionHeading("On taking Hull damage:", Alignment.MID, opad);
		label = tooltip.addPara("Allows the ship to dissipate hard flux at up to %s of the normal rate while shields are on.", pad, h, "" + (int)HARD_DISS + "%");
		label.setHighlight("" + (int)HARD_DISS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Ship timescale increased by up to %s.", pad, h, "" + (int)TIME_HULL + "%");
		label.setHighlight("" + (int)TIME_HULL + "%");
		label.setHighlightColors(h);
        tooltip.addPara("%s", 6f, grey, new String[] { "Maximum bonus reached at 40% or lower hull." });
        
        tooltip.addSectionHeading("As hardflux levels increase:", Alignment.MID, opad);
        label = tooltip.addPara("Shield damage taken reduced by up to %s.", pad, h, "40%");
		label.setHighlight("40%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Shield upkeep increased by up to %s.", pad, bad, "200%");
		label.setHighlight("200%");
		label.setHighlightColors(bad);
        tooltip.addPara("%s", 6f, grey, new String[] { "Maximum bonus reached at 80% or higher hardflux level." });
        
        tooltip.addSectionHeading("As general flux levels increase:", Alignment.MID, opad);
        label = tooltip.addPara("Ship timescale increased by up to %s.", pad, h, "" + (int)TIME_FLUX  + "%");
		label.setHighlight("" + (int)TIME_FLUX  + "%");
		label.setHighlightColors(h);
        tooltip.addPara("%s", 6f, grey, new String[] { "Maximum bonus reached at 100% flux level." });
		
	}
}