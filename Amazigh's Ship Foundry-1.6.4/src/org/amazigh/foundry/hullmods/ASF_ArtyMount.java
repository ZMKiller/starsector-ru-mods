package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;

public class ASF_ArtyMount extends BaseHullMod {

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("ASF_ARTILLERY_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
        
        // this hullmod does nothing other than set up variables! all of the logic is handled by the targeting weaponry and the hullmod that determines what artillery weapon the ship has.
        
		engine.getCustomData().put("ASF_ARTILLERY_DATA_KEY" + ship.getId(), info);
		
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
		float opad = 10f;
		
		Color h = Misc.getHighlightColor();
		
		// this tactical artillery system is designed as extendable by other mods!
		// they just need have a ship with this hullmod, and an individual "weapon" hullmod that follows the same mechanical setup as the existing individual "weapon" ones do.
		
		LabelAPI label = tooltip.addPara("This ship mounts an integrated Tactical Artillery weapon system that requires targeting data from specialised equipment in order to fire.", opad);
		
		label = tooltip.addPara("Once charged the artillery weapon will automatically fire on hostile targets within %s range that have been marked with %s charges from an %s.", opad, h, "5000", "8", "Anti-Proton Target Designaton System");
		label.setHighlight("5000", "8", "Anti-Proton Target Designaton System");
		label.setHighlightColors(h, h, h);
		
	}
	
	@Override
    public int getDisplaySortOrder() {
        return 22212;
    }

    public class ShipSpecificData {
        public boolean READY = false; // is the arty ready to fire
        public boolean LOCK = false; // have we been assigned a target
        float TIMER = 0f; // cooldown timer management
		IntervalUtil fxInterval1 = new IntervalUtil(0.05f, 0.05f); // vfx management
		IntervalUtil fxInterval2 = new IntervalUtil(0.6f, 0.95f); // vfx management
		public ShipAPI TARGET = null; // the assigned target
    }
}
