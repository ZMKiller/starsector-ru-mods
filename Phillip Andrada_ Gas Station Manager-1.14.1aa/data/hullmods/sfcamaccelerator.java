package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class sfcamaccelerator extends BaseHullMod {

	public static final float TURRET_SPEED_BONUS = 30f;
	public static final float WEAPON_SPEED_BONUS = 20f;
	public static final float TURRET_HEALTH_DECREASE = 25f;
	public static final float SMOD_WEAPON_SPEED_BONUS = 50f;
	public static final float RECOIL_REDUCTION = 25f;

	private static String sfc_amAcceleratorDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
	private static String sfc_amAcceleratorText1 = Global.getSettings().getString("sfc_pagsm", "sfc_amAcceleratorText1");
	private static String sfc_amAcceleratorText2 = Global.getSettings().getString("sfc_pagsm", "sfc_amAcceleratorText2");
	private static String sfc_amAcceleratorText3 = Global.getSettings().getString("sfc_pagsm", "sfc_amAcceleratorText3");
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getWeaponHealthBonus().modifyPercent(id, -TURRET_HEALTH_DECREASE);

		boolean sMod = isSMod(stats);
		float mult = WEAPON_SPEED_BONUS;
		if (sMod) mult = SMOD_WEAPON_SPEED_BONUS;
		stats.getBallisticProjectileSpeedMult().modifyPercent(id, mult);
		stats.getEnergyProjectileSpeedMult().modifyPercent(id, mult);
		if (sMod) {
		stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_REDUCTION));
		stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_REDUCTION));
		stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_REDUCTION));
		}

	}
	
	/*public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return "" + (int) TURRET_SPEED_BONUS + "%";
		}
		if (index == 1) {
			return "" + (int) WEAPON_SPEED_BONUS + "%";
		}
		if (index == 2) {
			return "" + (int) TURRET_HEALTH_DECREASE + "%";
		}
		return null;
	}*/

	@Override
	public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
		final Color green = new Color(55,245,65,255);
		final Color red = new Color(245,55,65,255);
		final Color flavor = new Color(110,110,110,255);
		final float pad = 10f;
		final float pad2 = 0f;
		float padList = 6f;
		final float padSig = 1f;
		tooltip.addSectionHeading(sfc_amAcceleratorDetails, Alignment.MID, pad);
		tooltip.addPara(sfc_amAcceleratorText1, padList, Misc.getHighlightColor(),"+30%");
		tooltip.addPara(sfc_amAcceleratorText2, pad2, Misc.getHighlightColor(),"+20%");
		tooltip.addPara(sfc_amAcceleratorText3, pad2, Misc.getHighlightColor(),"-25%");
		tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_amAcceleratorText4") }).italicize();
		tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_amAcceleratorText5") });
		/*tooltip.addPara(
				"• Increased Weapon Turn Speed: %s"
						+ "\n• Increased Projectile Speed: %s",
				pad, green, new String[]{
						Misc.getRoundedValue(30.0f) + "%",
						Misc.getRoundedValue(20.0f) + "%",
				}
		);
		tooltip.addPara(
				"• Decreased Turret Health: -%s",
				pad2, red, new String[]{
						Misc.getRoundedValue(25.0f) + "%",
				}
		);
		tooltip.addPara("%s", padList, flavor, new String[] { "\"Be careful with that! One wrong move and you’ll blow the whole thing sky high. Now hold it still, I need to make sure the calculations are correct.\"" }).italicize();
		//tooltip.addPara("%s", padList, flavor, new String[] { "\"Be careful with that! One wrong move and you’ll blow the whole thing sky high. Now hold it still, I need to make sure the calculations are correct.\"" });
		tooltip.addPara("%s", padSig, flavor, new String[] { "         \u2014 Lead Developer Gregory Mannfred" });*/
	}

	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "+" + (int) (SMOD_WEAPON_SPEED_BONUS) + "%";
		if (index == 1) return "-" + (int) (1f - 75f) + "%";
		return null;
	}
}
