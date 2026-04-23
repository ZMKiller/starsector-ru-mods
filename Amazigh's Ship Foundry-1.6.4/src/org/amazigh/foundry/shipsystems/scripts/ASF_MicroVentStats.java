package org.amazigh.foundry.shipsystems.scripts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.DefenseUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;

public class ASF_MicroVentStats extends BaseShipSystemScript {

	public static final float VENT_LEVEL = 0.9f;

	public static final float DISS_BOOST = 50f;
	
	public static final float ROF_PENALTY = 0.6f;
	
	private boolean vented = false;
	
	private static Map<HullSize, Float> nebCount = new HashMap<HullSize, Float>();
	static {
		nebCount.put(HullSize.FIGHTER, 4f);
		nebCount.put(HullSize.FRIGATE, 6f);
		nebCount.put(HullSize.DESTROYER, 10f);
		nebCount.put(HullSize.CRUISER, 14f);
		nebCount.put(HullSize.CAPITAL_SHIP, 18f);
		nebCount.put(HullSize.DEFAULT, 10f);
	}
	
	private static Map<HullSize, Float> ventVal = new HashMap<HullSize, Float>();
	static {
		ventVal.put(HullSize.FIGHTER, 50f);
		ventVal.put(HullSize.FRIGATE, 150f);
		ventVal.put(HullSize.DESTROYER, 300f);
		ventVal.put(HullSize.CRUISER, 600f);
		ventVal.put(HullSize.CAPITAL_SHIP, 1200f);
		ventVal.put(HullSize.DEFAULT, 400f);
	}
	
	private static Map<HullSize, Float> REPAIR_FLAT = new HashMap<HullSize, Float>();
	static {
		REPAIR_FLAT.put(HullSize.FIGHTER, 0.1f);
		REPAIR_FLAT.put(HullSize.FRIGATE, 0.3f);
		REPAIR_FLAT.put(HullSize.DESTROYER, 0.6f);
		REPAIR_FLAT.put(HullSize.CRUISER, 0.9f); // no cruisers
		REPAIR_FLAT.put(HullSize.CAPITAL_SHIP, 1.2f); // no capitals
		REPAIR_FLAT.put(HullSize.DEFAULT, 0.5f);
	}
	
	private IntervalUtil visInterval1 = new IntervalUtil(0.75f,1.25f);
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		
		ShipAPI ship = (ShipAPI)stats.getEntity();
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float amount = Global.getCombatEngine().getElapsedInLastFrame();
		
		if (state == ShipSystemStatsScript.State.OUT) {
			float mult = 1f - (ROF_PENALTY * effectLevel);
			stats.getBallisticRoFMult().modifyMult(id, mult);
			stats.getMissileRoFMult().modifyMult(id, mult);
		} else {
			// we also give a 50% boost to dissipation during usage (scaling with effectLevel)
			stats.getFluxDissipation().modifyPercent(id, DISS_BOOST * effectLevel);
		}
		
		if (!vented) {
			vented = true;
			
			// instantly vent 10% of current flux!
			ship.getFluxTracker().setHardFlux(Math.max(0f, ship.getFluxTracker().getHardFlux() * VENT_LEVEL));
			ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() * VENT_LEVEL));
			
			// and a flat value vent as well
			// done like this because the setflux stuff only applies to hard/soft flux selectively
			// if we have less than #VAL# soft flux, then lower our hardflux level as well as our softflux level
			if (ship.getFluxTracker().getCurrFlux() - ship.getFluxTracker().getHardFlux() < ventVal.get(ship.getHullSize())) {
				ship.getFluxTracker().setHardFlux(Math.max(0f, ship.getFluxTracker().getHardFlux() - ventVal.get(ship.getHullSize())));
			}
			ship.getFluxTracker().setCurrFlux(Math.max(0f, ship.getFluxTracker().getCurrFlux() - ventVal.get(ship.getHullSize())));
			
			engine.addNebulaParticle(ship.getLocation(),
	        		ship.getVelocity(),
	        		ship.getCollisionRadius() * 0.75f,
					MathUtils.getRandomNumberInRange(1.5f, 1.8f),
					0.7f,
					0.35f,
					1.2f,
					new Color(140,70,135,70),
					false);
			
        	for (int i=0; i < nebCount.get(ship.getHullSize()); i++) {
        		float angle = MathUtils.getRandomNumberInRange(0f, 360f);
				float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
				
		        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
		        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (0.85f- dist), angle),
		        		Math.max(80f, ship.getCollisionRadius() * 0.5f),
						MathUtils.getRandomNumberInRange(1.6f, 2.0f),
						0.8f,
						0.4f,
						1.05f,
						new Color(140,70,130,70),
						false);
				
				for (int j=0; j < 3; j++) {
		        	
		        	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
					Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(25f, 75f));
					Global.getCombatEngine().addSmoothParticle(sparkPoint,
							sparkVel,
							MathUtils.getRandomNumberInRange(4f, 9f), //size
							0.6f, //brightness
							MathUtils.getRandomNumberInRange(0.6f, 0.7f), //duration
							new Color(150,70,135,255));
		        }
        	}
			
		}
		
		visInterval1.advance(amount * nebCount.get(ship.getHullSize()));
        if (visInterval1.intervalElapsed()) {
        	
        	for (int i=0; i < 3; i++) {
            	Vector2f sparkPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
				Vector2f sparkVel = MathUtils.getRandomPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(30f, 80f));
				Global.getCombatEngine().addSmoothParticle(sparkPoint,
						sparkVel,
						MathUtils.getRandomNumberInRange(4f, 9f), //size
						0.6f, //brightness
						MathUtils.getRandomNumberInRange(0.6f, 0.7f), //duration
						new Color(150,70,135,255));
        	}
	        
	        float angle = MathUtils.getRandomNumberInRange(0f, 360f);
			float dist = MathUtils.getRandomNumberInRange(0.1f, 0.5f);
			
	        engine.addNebulaParticle(MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() * dist, angle),
	        		MathUtils.getPointOnCircumference(ship.getVelocity(), ship.getCollisionRadius() * (1f- dist), angle),
					Math.max(80f, ship.getCollisionRadius() * 0.5f),
					MathUtils.getRandomNumberInRange(1.6f, 2.0f),
					0.8f,
					0.4f,
					1.0f,
					new Color(140,70,130,70),
					false);
	        
        }
        
        if (DefenseUtils.hasArmorDamage(ship)) {

            ArmorGridAPI armorGrid = ship.getArmorGrid();
            
        	final float[][] grid = armorGrid.getGrid();
	        final float max = armorGrid.getMaxArmorInCell();
	        
	        float repairAmount = REPAIR_FLAT.get(ship.getHullSize()) * amount;
	        	// a weak, flat armour repair in all cells
	        
			for (int x = 0; x < grid.length; x++) {
	            for (int y = 0; y < grid[0].length; y++) {
	                if (grid[x][y] < max) {
	                    float regen = grid[x][y] + repairAmount;
	                    armorGrid.setArmorValue(x, y, regen);
	                }
	            }
	        }
			
	        ship.syncWithArmorGridState();
	        ship.syncWeaponDecalsWithArmorDamage();
	        
    	}
        
	}
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		vented = false;
		stats.getFluxDissipation().unmodify(id);

		stats.getBallisticRoFMult().unmodify(id);
		stats.getMissileRoFMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		return null;
	}
}
