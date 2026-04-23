package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_CrockettOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(255,120,60,225); // 170,80,30,220
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 42f, 0.2f);
            
            engine.addHitParticle(proj_location, ship_velocity, 105f, 1f, 0.1f, FLASH_COLOR.brighter());
            
            engine.addSmoothParticle(proj_location,
            		ship_velocity,
    				50f, //size
    				0.8f, //brightness
    				0.4f, //duration
    				FLASH_COLOR);
            
        	for (int i=0; i < 9; i++) {
    			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), i * 3f, angle1);
                
                engine.addNebulaParticle(proj_location, smokeVel,
                		MathUtils.getRandomNumberInRange(32f, 48f),
                		1.6f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.8f, 2.3f), //totalDuration
                		new Color(50,40,45,110),
                		true);
                
//                for (int j=0; j < 5; j++) {
//
//        			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-12f, 12f);
//                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(5f, 120f), angle2);
//
//                    engine.addSmoothParticle(MathUtils.getRandomPointInCircle(proj_location, 3f),
//                    		sparkVel,
//            				MathUtils.getRandomNumberInRange(3f, 8f), //size
//            				1f, //brightness
//            				MathUtils.getRandomNumberInRange(0.7f, 0.9f), //duration
//            				FLASH_COLOR);
//                	}
        	}
        	
        	ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) ship);
    		emitter.location(proj_location);
    		emitter.angle(projectile.getFacing() - 12f, 24f);
    		emitter.life(0.7f, 0.9f);
    		emitter.size(3f, 8f);
    		emitter.velocity(5f, 115f);
    		emitter.color(255,120,60,225); // FLASH_COLOR
    		emitter.coreDispersion(3f);
    		emitter.burst(45);
        	
    }
  }