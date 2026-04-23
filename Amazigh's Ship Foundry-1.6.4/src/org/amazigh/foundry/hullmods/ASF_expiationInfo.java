package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_expiationInfo extends BaseHullMod {

	public static final int CHARGE_FLUX_SEC = 500;
	
	public static final int STAGE_1_DAMAGE = 200;
	
	public static final int STAGE_2_DAMAGE_MIN = 1600;
	public static final int STAGE_2_DAMAGE_MAX = 2000;
	
	public static final int STAGE_3_DAMAGE = 2000;
	public static final int STAGE_3_EMP_MIN = 800;
	public static final int STAGE_3_EMP_MAX = 1600;
	public static final int STAGE_3_ARC_DAMAGE = 100;
	public static final int STAGE_3_ARC_EMP = 500;
	
	public static final int STAGE_4_DAMAGE = 3000;
	public static final int STAGE_4_EMP = 1000;
	
	public static final int STAGE_4_ARC_DAMAGE = 150;
	public static final int STAGE_4_ARC_EMP = 700;
	
	private static final Color STAGE_1_COLOR = new Color(0,128,50,255);
	private static final Color STAGE_2_COLOR = new Color(0,88,128,255);
	private static final Color STAGE_3_COLOR = new Color(63,25,128,255);
	private static final Color STAGE_4_COLOR = new Color(128,0,75,255);
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 2f;
		float opad = 10f;
		float hpad = 12f;
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("The %s can be charged to fire at varying levels of power", pad, h, "Expiation");
		label.setHighlight("Expiation");
		label.setHighlightColors(h);
		
		label = tooltip.addPara("%s flux is generated for each second spent charging.", opad, h, "" + CHARGE_FLUX_SEC);
		label.setHighlight("" + CHARGE_FLUX_SEC);
		label.setHighlightColors(h);

		tooltip.addSectionHeading("Charge Stage One", h, STAGE_1_COLOR, Alignment.MID, hpad);
		
		label = tooltip.addPara("During the first stage of charging, the weapon will fire a spread of %s to %s projectiles, each dealing %s damage.", pad, h, "4", "8", STAGE_1_DAMAGE + " Energy");
		label.setHighlight("4", "8", STAGE_1_DAMAGE + " energy");
		label.setHighlightColors(h, h, h);
		
		tooltip.addSectionHeading("Charge Stage Two", h, STAGE_2_COLOR, Alignment.MID, hpad);
		
		label = tooltip.addPara("During the second stage of charging, the weapon will fire a single projectile, dealing from %s to %s energy damage.", pad, h, STAGE_2_DAMAGE_MIN + " Energy", STAGE_2_DAMAGE_MAX + " Energy");
		label.setHighlight(STAGE_2_DAMAGE_MIN + "", STAGE_2_DAMAGE_MAX + "");
		label.setHighlightColors(h, h);
		
		tooltip.addSectionHeading("Charge Stage Three", h, STAGE_3_COLOR, Alignment.MID, hpad);
		
		label = tooltip.addPara("During the third stage of charging, the weapon will fire a single projectile, dealing from %s and %s to %s damage.", pad, h, STAGE_3_DAMAGE + " Energy", STAGE_3_EMP_MIN + " EMP", STAGE_3_EMP_MAX + " EMP");
		label.setHighlight(STAGE_3_DAMAGE + "", STAGE_3_EMP_MIN + "", STAGE_3_EMP_MAX + "");
		label.setHighlightColors(h, h, h);
		label = tooltip.addPara("Hits on hull or armor will produce from %s to %s arcs to weapons and engines. Hits on shields have a chance to generate shield-penetrating arcs based on the target's hard flux level.", pad, h, "4", "8");
		label.setHighlight("4", "8");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("Each arc deals %s and %s damage.", pad, h, STAGE_3_ARC_DAMAGE + " Energy", STAGE_3_ARC_EMP + " EMP");
		label.setHighlight(STAGE_3_ARC_DAMAGE + " Energy", STAGE_3_ARC_EMP + " EMP");
		label.setHighlightColors(h, h);
		
		tooltip.addSectionHeading("Charge Stage Four", h, STAGE_4_COLOR, Alignment.MID, hpad);
		label = tooltip.addPara("During the fourth stage of charging, the weapon will fire a single overcharged projectile, dealing %s and %s damage.", pad, h, STAGE_4_DAMAGE + " Energy", STAGE_4_EMP + " EMP");
		label.setHighlight(STAGE_4_DAMAGE + " Energy", STAGE_4_EMP + " EMP");
		label.setHighlightColors(h, h);
		label = tooltip.addPara("Firing a charge stage four shot will convert %s of the firing ships soft flux into hard flux.", pad, h, STAGE_4_DAMAGE + "50%");
		label.setHighlight("50%");
		label.setHighlightColors(h);
		label = tooltip.addPara("On impact produces %s shield-penetrating arcs to weapons and engines.", pad, h, "5");
		label.setHighlight("5");
		label.setHighlightColors(h);
		label = tooltip.addPara("Each arc deals %s and %s damage.", pad, h, STAGE_4_ARC_DAMAGE + " Energy", STAGE_4_ARC_EMP + " EMP");
		label.setHighlight(STAGE_4_ARC_DAMAGE + " Energy", STAGE_4_ARC_EMP + " EMP");
		label.setHighlightColors(h, h);
		
	}

}
