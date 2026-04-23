package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_ionDriver_onFireEffect implements OnFireEffectPlugin {
	
	private static final Color FLASH_COLOR = new Color(90,205,210,240); // 90,185,230
	
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        
        Vector2f proj_location = projectile.getLocation();
        engine.addHitParticle(proj_location, ship_velocity, 75f, 1f, 0.1f, FLASH_COLOR.brighter());
        
        // the i fuckery means: particles that spawn further out, have a shorter lifetime, causing an "inwards" sparkle fade
        
//        for (int i=0; i < 56; i++) {
//        	float arcPoint = MathUtils.getRandomNumberInRange(projectile.getFacing() - 3f, projectile.getFacing() + 3f);
//        	
//        	Vector2f velocity = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(0f, 10f), MathUtils.getRandomNumberInRange(projectile.getFacing() - 85f, projectile.getFacing() + 85f));
//        	
//        	float sparkRange = 45 - (i * 0.625f); // 45 - (0.625-35)
//        	
//        	Vector2f spawnLocation = MathUtils.getPointOnCircumference(projectile.getLocation(), sparkRange, arcPoint);
//        	spawnLocation = MathUtils.getRandomPointInCircle(spawnLocation, MathUtils.getRandomNumberInRange(0f, 5f));
//        	
//        	engine.addSmoothParticle(spawnLocation,
//        			velocity,
//        			MathUtils.getRandomNumberInRange(2f, 3f),
//        			1f,
//        			(i * 0.03f) + MathUtils.getRandomNumberInRange(0.52f, 0.62f), // (0.03-1.68) + (0.52-0.62) = (0.55-2.3)
//        			FLASH_COLOR);
//        }
        
    	ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) weapon.getShip());
        emitter.location(proj_location);
        emitter.angle(projectile.getFacing() -3f, 6f);
        emitter.life(0.35f, 2.2f);
        emitter.size(2f, 3f);
        emitter.velocity(0f, 7f);
        emitter.distance(13f, 35f);
        emitter.color(90,205,210,240); // FLASH_COLOR
        emitter.velDistLinkage(false);
        emitter.lifeLinkage(true);
        emitter.emissionOffset(-85f, 170f);
        emitter.coreDispersion(5f);
        emitter.burst(56);
        
    }
}