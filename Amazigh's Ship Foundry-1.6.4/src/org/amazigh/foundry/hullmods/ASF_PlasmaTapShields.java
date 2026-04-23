package org.amazigh.foundry.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_PlasmaTapShields extends BaseHullMod {

	public static final float SPEED_MALUS = 0.3f;
	public static final float TURN_MALUS = 0.2f;
    
	public void advanceInCombat(ShipAPI ship, float amount){
		if (Global.getCombatEngine().isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
        ShipSpecificData info = (ShipSpecificData) Global.getCombatEngine().getCustomData().get("PNEUMA_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		if (ship.getVariant().hasHullMod("shield_shunt")) {
			return;
		}
		if (ship.getShield().getType() == ShieldAPI.ShieldType.NONE) {
			return;
		}
		
		MutableShipStatsAPI stats = ship.getMutableStats();
        
		if (ship.getShield().isOn()) {
			info.TIMER = Math.min(1f, info.TIMER + amount);
		} else {
			info.TIMER = Math.max(0f, info.TIMER - amount);
		}
		
		stats.getMaxSpeed().modifyMult(spec.getId(), 1f - (SPEED_MALUS * info.TIMER));
		stats.getMaxTurnRate().modifyMult(spec.getId(), 1f - (TURN_MALUS * info.TIMER));
		
		float engineMult = -(SPEED_MALUS * info.TIMER);
		ship.getEngineController().extendFlame(this, engineMult, engineMult, engineMult);
		
        Global.getCombatEngine().getCustomData().put("PNEUMA_DATA_KEY" + ship.getId(), info);
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
		
		int value1 = (int) (100f * SPEED_MALUS);
		int value2 = (int) (100f * TURN_MALUS);

		Color bad = Misc.getNegativeHighlightColor();
		
		LabelAPI label = tooltip.addPara("This ship has an atypical shield emitter, that draws on a plasma-tap linked to the ships engines.", opad);
		
		label = tooltip.addPara("When the shield is active, the ship recieves the following stat penalties:", opad);
		label = tooltip.addPara("Top speed reduced by %s.", pad, bad, "" + value1 + "%");
		label.setHighlight("" + value1 + "%");
		label.setHighlightColors(bad);
		label = tooltip.addPara("Turn rate reduced by %s.", pad, bad, "" + value2 + "%");
		label.setHighlight("" + value2 + "%");
		label.setHighlightColors(bad);
		
	}

    private class ShipSpecificData {
        private float TIMER = 0f;
    }
}