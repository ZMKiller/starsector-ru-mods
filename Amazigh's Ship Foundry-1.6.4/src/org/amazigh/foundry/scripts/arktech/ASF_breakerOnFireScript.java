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

public class ASF_breakerOnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    public static final int ROCKET_EVERY = 3;
    public static final int MISSILE_EVERY = 7;
    private int rocketCounter = 0;
    private int missileCounter = 0;
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	
    	if(ship.getSystem().isActive()) {
    		rocketCounter++;
        	missileCounter++;
			Vector2f vel = (Vector2f) (ship.getVelocity());
			
        	if (rocketCounter >= ROCKET_EVERY) {
                rocketCounter = 0;
        		for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isSystemSlot()) {
    		            Vector2f rocketRandomVel = MathUtils.getRandomPointOnCircumference(vel, MathUtils.getRandomNumberInRange(2f, 20f));
    		            float randomArc = MathUtils.getRandomNumberInRange(-3f, 3f);
    		            engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_gaderoga_rkt", port.computePosition(ship), port.getAngle() + ship.getFacing() + randomArc, rocketRandomVel);
    		            
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
    				Global.getSoundPlayer().playSound("annihilator_fire", 1f, 1f, port.computePosition(ship), ship.getVelocity());
        		}
        	}
        	
        	if (missileCounter >= MISSILE_EVERY) {
                missileCounter = 0;
        		for (WeaponSlotAPI port : ship.getHullSpec().getAllWeaponSlotsCopy()) {
    				if (port.isDecorative()) {
    					if (port.isHidden()) {
    						engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_gaderoga_mssl", port.computePosition(ship), port.getAngle() + ship.getFacing(), vel);	
    					} else {
    						engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_gaderoga_mssl_2", port.computePosition(ship), port.getAngle() + ship.getFacing(), vel);    						
    					}
    					
    		            float randomSize1 = MathUtils.getRandomNumberInRange(15f, 20f);
    		            float randomSize2 = MathUtils.getRandomNumberInRange(20f, 25f);
    		            engine.addSwirlyNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize1, //size
    		            		2.0f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.5f, //full bright fraction
    		            		0.9f, //duration
    		            		new Color(115,105,100,70),
    		            		true);
    		            engine.addNebulaParticle(port.computePosition(ship),
    		            		ship.getVelocity(),
    		            		randomSize2, //size
    		            		2.2f, //end mult
    		            		0.5f, //ramp fraction
    		            		0.75f, //full bright fraction
    		            		1.1f, //duration
    		            		new Color(115,105,100,95),
    		            		true);
    				}
					Global.getSoundPlayer().playSound("swarmer_fire", 1f, 1f, port.computePosition(ship), ship.getVelocity());
        		}
        	}
    	}
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}
	
  }