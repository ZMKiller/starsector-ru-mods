package org.amazigh.foundry.scripts.supe;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_heatstrokeOnFireEffect implements OnFireEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(255,101,77,225);
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f proj_location = projectile.getLocation();
            float proj_facing = projectile.getFacing();
            
        	Global.getSoundPlayer().playSound("hit_hull_heavy", 0.8f, 1.25f, proj_location, ship_velocity);
        	engine.addPlugin(new ASF_heatstrokeTrailScript(projectile));
            
        	
            engine.spawnExplosion(proj_location, ship_velocity, FLASH_COLOR, 90f, 0.5f);
            
            engine.addHitParticle(proj_location, ship_velocity, 160f, 1f, 0.1f, FLASH_COLOR.brighter());
            
            engine.addSmoothParticle(proj_location,
            		ship_velocity,
    				98f, //size
    				0.8f, //brightness
    				0.45f, //duration
    				FLASH_COLOR);
            
            engine.addNebulaParticle(proj_location, ship_velocity,
            		MathUtils.getRandomNumberInRange(55f, 77f),
            		1.9f, //endsizemult
            		0.2f, //rampUpFraction
            		0.3f, //fullBrightnessFraction
            		2.3f, //totalDuration
            		new Color(60,40,30,90),
            		true);
            
            // muzzlesmoke nebs
        	for (int i=0; i < 11; i++) {
    			float angle1 = proj_facing + MathUtils.getRandomNumberInRange(-3f, 3f);
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship_velocity, i * 5f, angle1);
                Vector2f smokeVel2 = MathUtils.getPointOnCircumference(ship_velocity, i * 6f, angle1);
                
                engine.addSwirlyNebulaParticle(proj_location, smokeVel,
                		MathUtils.getRandomNumberInRange(35f, 52f),
                		1.9f, //endsizemult
                		0.1f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.9f, 2.6f), //totalDuration
                		new Color(45,40,50,110),
                		true);
                
                engine.addSwirlyNebulaParticle(proj_location, smokeVel2,
                		MathUtils.getRandomNumberInRange(32f, 45f),
                		1.7f, //endsizemult
                		0.2f, //rampUpFraction
                		0.3f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(1.3f, 1.9f), //totalDuration
                		new Color(153,60,35,90),
                		true);
                
                // particle spray
                for (int j=0; j < 5; j++) {
                	float arcPoint = MathUtils.getRandomNumberInRange(proj_facing - 6f, proj_facing + 6f);
                	float sparkRange = MathUtils.getRandomNumberInRange((j - 1f) * 9f, j * 10f);
                	Vector2f spawnLocation = MathUtils.getPointOnCircumference(proj_location, sparkRange, arcPoint);
                	float angle2 = proj_facing + MathUtils.getRandomNumberInRange(-12f, 12f);
                    Vector2f sparkVel = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(4f, 28f), angle2);
                    
                    engine.addSmoothParticle(spawnLocation,
                    		sparkVel,
            				MathUtils.getRandomNumberInRange(3f, 8f), //size
            				1f, //brightness
            				MathUtils.getRandomNumberInRange(0.8f, 1.1f), //duration
            				new Color(255,76,58,255));
                	}
        	}
        	
        	// lensflares
        	for (int i=0; i < 5; i++) {
        		Vector2f flarePoint = MathUtils.getPointOnCircumference(proj_location, i * 13f, proj_facing + MathUtils.getRandomNumberInRange(-15f, 15f));
        		
            	MagicLensFlare.createSharpFlare(
        			    engine,
        			    ship,
        			    flarePoint,
        			    4,
        			    190 - (i * 12f),
        			    proj_facing + 90f,
        			    new Color(190,40,30),
        				new Color(255,101,77));
            	
            	// some SLOWer smoke along the entire muzzle flare area
                for (int j=0; j < 3; j++) {
                	float angle1 = proj_facing + MathUtils.getRandomNumberInRange(-3f, 3f);
                    Vector2f smokeVel = MathUtils.getPointOnCircumference(ship_velocity, j * 3f, angle1);
                    
                    engine.addSwirlyNebulaParticle(flarePoint,
                    		smokeVel,
                    		MathUtils.getRandomNumberInRange(35f, 52f) - (i * 5f),
                    		1.8f, //endsizemult
                    		0.1f, //rampUpFraction
                    		0.4f, //fullBrightnessFraction
                    		MathUtils.getRandomNumberInRange(1.4f, 1.9f), //totalDuration
                    		new Color(45,40,50,80),
                    		true);
                }
        	}
        	
    }
  }