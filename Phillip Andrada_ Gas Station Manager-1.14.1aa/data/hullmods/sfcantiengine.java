package data.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class sfcantiengine extends BaseHullMod {
	
	private static final float ZERO_FLUX_MULT = 7.5f;
	private static final float ENGINE_MULT = 15f;
	private static final float SUPPLY_USE_MULT = 1.10f;
	private static final float SENSOR_MULT = 1.10f;
	private static final float SMOD_SPEED_BONUS = 5f;
	private static final float SMOD_TURN_BONUS = 1.25f;

	private static String sfc_antiengineDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
	private static String sfc_antiengineText1 = Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText1");
	private static String sfc_antiengineText2 = Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText2");
	private static String sfc_antiengineText3 = Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText3");
	private static String sfc_antiengineText4 = Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText4");
	private static String sfc_antiengineText5 = Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText5");
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_MULT * 0.01f); // should set it usable at 5% or lower
		stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_MULT * 5f); // should increase zero flux bonus to +25 speed
		stats.getEngineHealthBonus().modifyPercent(id, -ENGINE_MULT); // should decrease engine durability
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT); // should set supply use to 150% of regular use
		stats.getFuelUseMod().modifyMult(id, SUPPLY_USE_MULT); // should set fuel use to 150% of regular use
		stats.getSensorProfile().modifyMult(id, SENSOR_MULT); // should increase sensor profile

		if (sMod) {
			stats.getMaxSpeed().modifyFlat(id, SMOD_SPEED_BONUS);
			stats.getMaxTurnRate().modifyMult(id, SMOD_TURN_BONUS);
		}
	}
	
	/*public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return "" + Misc.getRoundedValue(ZERO_FLUX_MULT) + "%";
		}
		if (index == 1) {
			return "" + Misc.getRoundedValue(ZERO_FLUX_MULT * 5f);
		}
		if (index == 2) {
			return "" + (int)(ENGINE_MULT) + "%";
		}
		if (index == 3) {
			return "" + (int)((SUPPLY_USE_MULT - 1f) * 100f) + "%";
		}
		if (index == 4) {
			return "" + (int)((SUPPLY_USE_MULT - 1f) * 100f) + "%";
		}
		if (index == 5) {
			return "" + (int)((SENSOR_MULT) * 100f) + "%";
		}
		return null;
	}*/

	private final Color color = new Color(120,0,57,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
	}
	@Override
	public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
		final Color green = new Color(55,245,65,255);
		final Color red = new Color(245,55,65,255);
		final Color flavor = new Color(110,110,110,255);
		final float pad = 10f;
		final float pad2 = 0f;
		float padList = 6f;
		final float padSig = 1f;
		tooltip.addSectionHeading(sfc_antiengineDetails, Alignment.MID, pad);
		tooltip.addPara(sfc_antiengineText1, padList, Misc.getHighlightColor(),"7.5%");
		tooltip.addPara(sfc_antiengineText2, pad2, Misc.getHighlightColor(),"50");
		tooltip.addPara(sfc_antiengineText3, pad2, Misc.getHighlightColor(),"10%");
		tooltip.addPara(sfc_antiengineText4, pad2, Misc.getHighlightColor(),"10%");
		tooltip.addPara(sfc_antiengineText5, pad2, Misc.getHighlightColor(),"10%");
		tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText6") }).italicize();
		tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_antiengineText7") });
		/*tooltip.addPara(
				"• Zero-Flux Bonus Effective Up To: %s Flux"
						+ "\n• Zero-Flux Speed Bonus Increased By: %s units",
				pad, green, new String[]{
						Misc.getRoundedValue(10.0f) + "%",
						Misc.getRoundedValue(25.0f),
				}
		);
		tooltip.addPara(
				"• Decreased Engine Health: -%s"
				+ "\n• Increased Fuel And Supply Usage: %s"
				+ "\n• Increased Sensor Profile: %s",
				pad2, red, new String[]{
						Misc.getRoundedValue(10.0f) + "%",
						Misc.getRoundedValue(10.0f) + "%",
						Misc.getRoundedValue(10.0f) + "%",
				}
		);
		tooltip.addPara("%s", padList, flavor, new String[] { "\"The prototype exploded? And you're saying it's because my calculations are wrong!? Impossible! You must have built it wrong, you imbecile. Now build it again and follow my instructions to the letter, or I’ll have you thrown out of this building!\"" }).italicize();
		tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Head Researcher, Yunris Kween" });*/
	}
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return (10) + "";
		if (index == 1) return (int) Math.round((SMOD_TURN_BONUS - 1f) * 100f) + "%";
		return null;
	}
}
