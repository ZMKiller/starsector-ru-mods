package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_ruggedMounts extends BaseHullMod {

	public static final float HEALTH_BONUS = 100f;
	public static final float REPAIR_BONUS = 30f;
	public static final float COST_REDUCTION = 2;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getWeaponHealthBonus().modifyMult(id, 1f + (HEALTH_BONUS * 0.01f)); // done as a mult so it scales multiplicatively with other buffs!
		stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1f - (REPAIR_BONUS * 0.01f));
		
		stats.getDynamic().getMod(Stats.MEDIUM_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
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
		
		LabelAPI label = tooltip.addPara("This ship has weapon mounts that are resistant to combat stresses, allowing them to stay operational even when under heavy fire.", pad);
		
		label = tooltip.addPara("Increases the durability of all weapons by %s", opad, h, "" + (int)HEALTH_BONUS + "%");
		label.setHighlight("" + (int)HEALTH_BONUS + "%");
		label.setHighlightColors(h);
		label = tooltip.addPara("Reduces the time required to repair disabled weapons by %s.", pad, h, "" + (int)REPAIR_BONUS + "%");
		label.setHighlight("" + (int)REPAIR_BONUS + "%");
        
        label = tooltip.addPara("Reduces the ordnance point cost of medium ballistic weapons by %s.", opad, h, "" + (int)COST_REDUCTION);
		label.setHighlight("" + (int)COST_REDUCTION);
		label.setHighlightColors(h);
		
	}

}
