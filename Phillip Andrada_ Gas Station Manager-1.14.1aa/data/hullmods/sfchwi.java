package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class sfchwi extends BaseHullMod {

	public static float BONUS_SMALL_NON_PD = 50f;
	public static float BONUS_SMALL_PD = 50f;

	public static final float SMALL_MOUNT_BONUS = 50f;
	public static final float PD_WEAPON_BONUS = 50f;

	private static String HWIDetails = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodDetails");
	private static String HWISpecials = Global.getSettings().getString("sfc_pagsm", "sfc_hullmodSpecial");

	private static String HWIText1 = Global.getSettings().getString("sfc_pagsm", "sfc_hwi1");
	private static String HWIText2 = Global.getSettings().getString("sfc_pagsm", "sfc_hwi2");

	private static String WOCIcon = "graphics/icons/hullsys/ammo_feeder.png";
	private static String WOCTitle = Global.getSettings().getString("sfc_pagsm", "sfc_overclockTitle");
	private static String WOCText1 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock1");
	private static String WOCText2 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock2");
	private static String WOCText3 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock3");
	private static String WOCText4 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock4");
	private static String WOCText5 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock5");
	private static String WOCText6 = Global.getSettings().getString("sfc_pagsm", "sfc_overclock6");

	/*public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -5);
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -5);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -5);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "5";
		return null;
	}*/
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -5);
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -5);
		stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -5);
		//stats.getDynamic().getMod(Stats.SMALL_PD_MOD).modifyMult(id,-1);
		//stats.getDynamic().getMod(Stats.MEDIUM_PD_MOD).modifyMult(id, -2);
		stats.getBeamPDWeaponRangeBonus().modifyFlat(id, 50);
		stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, 50);
	}

	/*@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		WeaponAPI.WeaponSize smallest = null;
		for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
			if (slot.isDecorative() ) continue;
			if (slot.getWeaponType() == WeaponAPI.WeaponType.BALLISTIC) {
				if (smallest == null || smallest.ordinal() < slot.getSlotSize().ordinal()) {
					smallest = slot.getSlotSize();
				}
			}
		}
		if (smallest == null) return;
		float small = 0f;
		switch (smallest) {
			case LARGE:
			case SMALL:
			case MEDIUM:
				small = 50;
				break;
		}

		ship.addListener(new RangefinderRangeModifier(small));
	}

	public static class RangefinderRangeModifier implements WeaponBaseRangeModifier {
		public float small;

		public RangefinderRangeModifier(float small) {
			this.small = small;
		}

		public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}

		public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1f;
		}

		public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.BALLISTIC) {
				return 0f;
			}
			if (weapon.getSlot() == null || weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.ENERGY) {
				return 0f;
			}
			float bonus = 0;
			if (weapon.getSize() == WeaponAPI.WeaponSize.SMALL) {
				bonus = small;
				if (bonus == 0f) return 0f;
				if (bonus < 0) bonus = 0;
				return bonus;
			}
			return bonus;
		}
	}*/

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

	@Override
	public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
		final Color green = new Color(55, 245, 65, 255);
		final Color yellow = new Color(255, 240, 0, 255);
		final Color flavor = new Color(110, 110, 110, 255);
		final float pad = 10f;
		final float pad2 = 0f;
		final float height = 50f;
		float padList = 6f;
		final float padSig = 1f;
		tooltip.addSectionHeading(HWIDetails, Alignment.MID, pad);
		/*tooltip.addPara(
				"• Decreased Large Mount OP Cost: -%s"
				+ "\n• Increased PD Mount Range: %s",
				pad, green, new String[]{
						Misc.getRoundedValue(5.0f),
						Misc.getRoundedValue(50.0f),
				}
		);*/
		tooltip.addPara(HWIText1, padList, Misc.getHighlightColor(), "-5");
		tooltip.addPara(HWIText2, pad2, Misc.getHighlightColor(), "+50");
		/*if ((ship.getVariant().hasHullMod("sfchwi")) && ((ship.getHullSpec().getHullId().equals("sfcsuperiapetus")) || (ship.getHullSpec().getHullId().equals("sfcpathapetus")))) {
		tooltip.addSectionHeading(HWISpecials, Alignment.MID, pad);
		//TooltipMakerAPI WOC = tooltip.beginImageWithText(WOCIcon, height);
		//WOC.beginImageWithText(WOCIcon, 50f);
		tooltip.addPara(WOCTitle, padList, yellow, WOCTitle);
		tooltip.addPara(WOCText1, padSig);
		tooltip.addPara(WOCText2, pad2);
		tooltip.addPara(WOCText3, pad2, Misc.getHighlightColor(), 100f + "%");
		tooltip.addPara(WOCText4, pad2, Misc.getHighlightColor(),"-" + 75f + "%");
		tooltip.addPara(WOCText5, pad2, Misc.getHighlightColor(), 25f + "%");
		tooltip.addPara(WOCText6, pad2, Misc.getHighlightColor(), 50f + "%");
		}
		//tooltip.addPara("%s", padList, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_hwi3") }).italicize();
		//tooltip.addPara("%s", padSig, flavor, new String[] { Global.getSettings().getString("sfc_pagsm", "sfc_hwi4") });
	}*/
//}
	}
}
