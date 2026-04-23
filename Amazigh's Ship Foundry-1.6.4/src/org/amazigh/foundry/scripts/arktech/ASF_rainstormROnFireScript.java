package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

public class ASF_rainstormROnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    public static final int MISSILE_EVERY = 3;
    public static final int TORP_EVERY = 5;
    private int missileCounter = 0;
    private int torpCounter = 0;
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	
    	if(ship.getSystem().isActive()) {
        	missileCounter++;
        	torpCounter++;
			Vector2f vel = (Vector2f) (ship.getVelocity());
			
        	if (missileCounter >= MISSILE_EVERY) {
        		missileCounter = 0;
        		for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isSystemSlot() && port.isHidden()) {
    		            engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_gaderoga_mssl", port.computePosition(ship), port.getAngle() + ship.getFacing() + 5f, vel);
    		            engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_gaderoga_mssl_2", port.computePosition(ship), port.getAngle() + ship.getFacing() - 5f, vel);
    		            
    		            float randomSize1 = MathUtils.getRandomNumberInRange(15f, 20f);
    		            float randomSize2 = MathUtils.getRandomNumberInRange(20f, 25f);
    		            engine.addSwirlyNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize1, //size
    		            		2.0f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.5f, //full bright fraction
    		            		0.9f, //duration
    		            		new Color(105,115,100,70),
    		            		true);
    		            engine.addNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize2, //size
    		            		2.2f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.75f, //full bright fraction
    		            		1.1f, //duration
    		            		new Color(105,115,100,95),
    		            		true);
    				}
    				Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, port.computePosition(ship), ship.getVelocity());
        		}
        	}
        	
        	if (torpCounter >= TORP_EVERY) {
                torpCounter = 0;
        		for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isDecorative() && port.isHidden()) {
    					engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_grandum_torp", port.computePosition(ship), port.getAngle() + ship.getFacing(), vel);
    					
    		            float randomSize1 = MathUtils.getRandomNumberInRange(18f, 24f);
    		            float randomSize2 = MathUtils.getRandomNumberInRange(24f, 30f);
    		            engine.addSwirlyNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize1, //size
    		            		2.0f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.5f, //full bright fraction
    		            		1.1f, //duration
    		            		new Color(105,115,100,70),
    		            		true);
    		            engine.addNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize2, //size
    		            		2.2f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.75f, //full bright fraction
    		            		1.3f, //duration
    		            		new Color(105,115,100,95),
    		            		true);
    				}
					Global.getSoundPlayer().playSound("atropos_fire", 1.1f, 0.8f, port.computePosition(ship), ship.getVelocity());
        		}
        	}
    	}
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}
	
  }