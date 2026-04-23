package org.amazigh.foundry.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
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
import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

public class ASF_GyastarHullmod extends BaseHullMod {
	
	public static final float BEAM_GUARD = 0.3f; // [CUSTOM CARTRIDGE: BEAM GUARD]
	
	public static final float REVENGE_SHOT = 0.5f; // [CUSTOM CARTRIDGE: REVENGE SHOT]
	
	public static final float REPAIR_MULT = 25f;
	public static final float REPAIR_DISS_MULT = 3f;
	public static final float REPAIR_FLUX_MULT = 0.5f;
	
    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);
    private final Random rand = new Random();
    private static final Color SPARK_COLOR = new Color(130, 40, 140);

    public static Vector2f getCellLocation(ShipAPI ship, float x, float y) {
        float xx = x - (ship.getArmorGrid().getGrid().length / 2f);
        float yy = y - (ship.getArmorGrid().getGrid()[0].length / 2f);
        float cellSize = ship.getArmorGrid().getCellSize();
        Vector2f cellLoc = new Vector2f();
        float theta = (float) (((ship.getFacing() - 90f) / 360f) * (Math.PI * 2.0));
        cellLoc.x = (float) (xx * Math.cos(theta) - yy * Math.sin(theta)) * cellSize + ship.getLocation().x;
        cellLoc.y = (float) (xx * Math.sin(theta) + yy * Math.cos(theta)) * cellSize + ship.getLocation().y;
        return cellLoc;
    }
    
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getBeamDamageTakenMult().modifyMult(id, (1f - BEAM_GUARD));
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount){
        CombatEngineAPI engine = Global.getCombatEngine();
		if (engine.isPaused() || !ship.isAlive() || ship.isPiece()) {
			return;
		}
		
        ShipSpecificData info = (ShipSpecificData) engine.getCustomData().get("WARBURN_DATA_KEY" + ship.getId());
        if (info == null) {
            info = new ShipSpecificData();
        }
		
		MutableShipStatsAPI stats = ship.getMutableStats();
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		}
		
		// Stat setup section
		float DAMAGE = 1 - ship.getHullLevel();
		if (DAMAGE > 0.8f) {
			DAMAGE = 0.8f;
		}
		float HULL_RATIO = DAMAGE / 0.8f;
		// Stat setup section
		
		
		// Repair Section
		if (ship.getFluxLevel() >= 0.8f) {
			info.VENT = true;
		}
		
		if (info.VENT) {
			interval.advance(engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue());
	        if (interval.intervalElapsed()) {
	            ArmorGridAPI armorGrid = ship.getArmorGrid();
	            int x = rand.nextInt(armorGrid.getGrid().length);
	            int y = rand.nextInt(armorGrid.getGrid()[0].length);
	            float newArmor = armorGrid.getArmorValue(x, y);
	            float cellSize = armorGrid.getCellSize();

	            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) < 0) {
	                newArmor += REPAIR_MULT * interval.getIntervalDuration() * (2f + ship.getFluxLevel());
	                
	                boolean REPAIR = false;
	                if (armorGrid.getArmorValue(x, y) < armorGrid.getMaxArmorInCell()) {
	                	REPAIR = true;
	                }
	                armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));
	                
	                Vector2f cellLoc = getCellLocation(ship, x, y);
	                cellLoc.x += cellSize * 0.1f - cellSize * (float) Math.random();
	                cellLoc.y += cellSize * 0.1f - cellSize * (float) Math.random();
	                engine.addHitParticle(cellLoc,
	                		ship.getVelocity(),
	                		(7f * (float) Math.random()) + 5f,
	                		0.9f,
	                		0.35f,
	                        SPARK_COLOR);
	                if (REPAIR) {
	                	engine.spawnExplosion(cellLoc, ship.getVelocity(), SPARK_COLOR, 20f, 0.2f);
	                }
	            }
	            ship.syncWithArmorGridState(); // to remove gross damage when repairing, thanks ruddygreat
	            ship.syncWeaponDecalsWithArmorDamage();
	        }
	        
	        stats.getFluxDissipation().modifyMult(spec.getId(), REPAIR_DISS_MULT);
	        stats.getBallisticWeaponFluxCostMod().modifyMult(spec.getId(), REPAIR_FLUX_MULT);
	        
	        
	        if (ship.getFluxLevel() <= 0.05f) {
				info.VENT = false;
				stats.getFluxDissipation().unmodify(spec.getId());
		        stats.getBallisticWeaponFluxCostMod().unmodify(spec.getId());
			}
		}
		// Repair Section
		
		
		
		// RoF Modifier
		stats.getBallisticRoFMult().modifyMult(spec.getId(), 1f + (REVENGE_SHOT * HULL_RATIO));
		// RoF Modifier
		
		
		engine.getCustomData().put("WARBURN_DATA_KEY" + ship.getId(), info);
	}
		// So what this hullmod does is as follows:
		// - 30% resist to beam weapon damage
		// - Increases ballistic RoF as the ship takes hull damage
		// - Triggers armour repair and boosted dissipation when flux level exceeds 80% (stops when flux level drops below 5%)
	
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
		
		Color h = Misc.getHighlightColor();
		
		LabelAPI label = tooltip.addPara("The internal systems of this ship have been constructed using unknown advanced Domain technology.", opad);
		label = tooltip.addPara("What limited documentation could be found on internal systems classifies this special configuration as %s", pad, h, "\"Warburn Systems\"");
		label.setHighlight("\"Warburn Systems\"");
		label.setHighlightColors(h);
		label = tooltip.addPara("The one confirmed feature of these systems is that inner armour layers feature integrated repair nanites that will activate and repair a small amount of damaged armour when flux levels build up.", pad);
	}

    private class ShipSpecificData {
    	private boolean VENT = false;
    }
}
