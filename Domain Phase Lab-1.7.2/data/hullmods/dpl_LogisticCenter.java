package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class dpl_LogisticCenter extends BaseHullMod {

	public static int DP_DROP = 4;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		boolean sMod = isSMod(stats);
		float dpMod = -1*Math.min(stats.getSuppliesToRecover().getBaseValue() * 0.2f, DP_DROP);
		if (sMod) dpMod = -1*stats.getSuppliesToRecover().getBaseValue() * 0.15f;
		stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, dpMod);
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && !(ship.isFrigate()||ship.isDestroyer());
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Can not be installed on a frigate or destroyer.";
	}
	
	public String getSModDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + 15 + "%";
		if (index == 1) return "" + 20 + "%";
		return null;
	}
	
	@Override
	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return false;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, final ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		
		tooltip.addPara("Lowers the DP of your ship by the smaller value between 20% of your ship's base DP and 4, can only be installed on larger ships.", opad);
	}
	
}



