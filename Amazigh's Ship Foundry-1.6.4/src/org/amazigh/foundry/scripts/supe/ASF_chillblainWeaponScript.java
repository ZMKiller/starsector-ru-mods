package org.amazigh.foundry.scripts.supe;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;

public class ASF_chillblainWeaponScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
	
	private static Map<HullSize, Float> hullDamMult = new HashMap<HullSize, Float>();
	static {
		hullDamMult.put(HullSize.FIGHTER, 0.8f);
		hullDamMult.put(HullSize.FRIGATE, 0.9f);
		hullDamMult.put(HullSize.DESTROYER, 1f);
		hullDamMult.put(HullSize.CRUISER, 1.15f);
		hullDamMult.put(HullSize.CAPITAL_SHIP, 1.35f);
		hullDamMult.put(HullSize.DEFAULT, 1f);
	}

	private static Map<HullSize, Float> hullDamFlat = new HashMap<HullSize, Float>();
	static {
		hullDamFlat.put(HullSize.FIGHTER, 50f);
		hullDamFlat.put(HullSize.FRIGATE, 70f);
		hullDamFlat.put(HullSize.DESTROYER, 100f);
		hullDamFlat.put(HullSize.CRUISER, 150f);
		hullDamFlat.put(HullSize.CAPITAL_SHIP, 250f);
		hullDamFlat.put(HullSize.DEFAULT, 100f);
	}
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	engine.addPlugin(new ASF_chillblainDetonator((MissileAPI) projectile));
    }
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
    	
    	ShipAPI ship = weapon.getShip();
    	
    	//status display for player info on damage
    	if (ship.getShipTarget() != null && ship == engine.getPlayerShip()) {
    		
    		float tagMass = ship.getShipTarget().getMass();
    		float tagRad = ship.getShipTarget().getCollisionRadius();
    		
    		float FF_mult = 1f;
			if (ship.getShipTarget().getOwner() == ship.getOwner()) {
				FF_mult = 0.5f;
			}

			// "clown ships get clown damage"
			FF_mult *= (Math.max(5000f, tagMass) / 5000f);
    		
			
    		float coreDam = FF_mult * (((tagMass * 0.2f) + tagRad) * hullDamMult.get(ship.getShipTarget().getHullSize())) + hullDamFlat.get(ship.getShipTarget().getHullSize());
    		
    		if (ship.getShipTarget().isStationModule() || ship.getShipTarget().isShipWithModules()) {
    			coreDam *= 0.25f;
			}
    		
        	float arcDam = FF_mult * (tagMass / 10f) * hullDamMult.get(ship.getShipTarget().getHullSize());
        	
        	if (ship.isStationModule()) {
        		arcDam *= hullDamMult.get(ship.getParentStation().getHullSize());
			}
        	
        	int arcCount = 0;
        	float arcValue = (tagRad/2) + (tagMass/6);
			for (int i=0; i < arcValue; i += 30) {
				arcCount++;
			}
        	
			engine.maintainStatusForPlayerShip("CHILLDAM", "graphics/icons/hullsys/entropy_amplifier.png", "Approximate damage of final blast to current target", "Core Blast: " + (int)coreDam + " | " + arcCount + " * Arcs, each dealing: " + (int)arcDam, false);
    	}

    }
}
