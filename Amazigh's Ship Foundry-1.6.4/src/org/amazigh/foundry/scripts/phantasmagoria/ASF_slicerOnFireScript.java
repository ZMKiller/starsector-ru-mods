package org.amazigh.foundry.scripts.phantasmagoria;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_slicerOnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    private float overCharge = 0;

	public static final int BARREL_COUNT = 6;
    private int shotCounter = 0;
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        Vector2f proj_location = projectile.getLocation();
        
    	if(overCharge > 0f) {
			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f);
			float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-2f, 2f);
			float velScale = projectile.getProjectileSpec().getMoveSpeed(weapon.getShip().getMutableStats(), weapon);
    		
    		Vector2f velMod1 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(velScale * -0.25f, velScale * 0.1f) , angle1);
    		Vector2f velMod2 = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(velScale * -0.25f, velScale * 0.1f) , angle1);
    		
			engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    "A_S-F_phantasmagoria_slicer_overcharge",
                    proj_location,
                    angle1,
                    velMod1);
			engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    "A_S-F_phantasmagoria_slicer_overcharge",
                    proj_location,
                    angle2,
                    velMod2);
    		
			shotCounter++;
	    	if (shotCounter >= BARREL_COUNT) {
	    		shotCounter = 0;
				Global.getSoundPlayer().playSound("A_S-F_phantasmagoria_slicer_fire", 0.6f, 1.25f, proj_location, ship_velocity);
				Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.7f, 0.6f, proj_location, ship_velocity);
	            engine.spawnExplosion(proj_location, ship_velocity, new Color(100,130,255,130), 6f, 0.5f);
	    	}
            
            for (int i=0; i < 2; i++) {
            	Vector2f randomVel = MathUtils.getRandomPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(25f, 75f));
            	
            	Global.getCombatEngine().addSmoothParticle(proj_location,
            			randomVel,
            			MathUtils.getRandomNumberInRange(7f, 14f), //size
            			1.0f, //brightness
            			0.35f, //duration
            			new Color(110,140,255,255));
            	for (int j=0; j < 2; j++) {
                	Vector2f randomVel2 = MathUtils.getRandomPointInCone(ship_velocity, MathUtils.getRandomNumberInRange(90f, 270f), projectile.getFacing() - 7f, projectile.getFacing() + 7f);
                	Global.getCombatEngine().addSmoothParticle(proj_location,
                			randomVel2,
                			MathUtils.getRandomNumberInRange(5f, 12f), //size
                			1.0f, //brightness
                			0.35f, //duration
                			new Color(110,140,255,255));
            	}
            	
            }
            Global.getCombatEngine().removeEntity(projectile);
    	} else {
        	shotCounter++;
        	if (shotCounter >= BARREL_COUNT) {
        		shotCounter = 0;
    			Global.getSoundPlayer().playSound("A_S-F_phantasmagoria_slicer_fire", 0.7f, 1.1f, proj_location, ship_velocity);
        	}
    	}
    	
    	float velScale = projectile.getProjectileSpec().getMoveSpeed(weapon.getShip().getMutableStats(), weapon);
		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.2f, velScale * 0.1f) , projectile.getFacing());
		projectile.getVelocity().x = newVel.x;
		projectile.getVelocity().y = newVel.y;
    	
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if(weapon.getShip().getSystem().isActive()) {
			if (weapon.getShip().getVariant().hasHullMod("A_S-F_PhantasmagoriaRegulator_off")) {
				overCharge = 1.8f * (1f + (weapon.getShip().getFluxLevel() * 0.6f)); // hidden lil bonus!
			 } else {
				 overCharge = 1.6f;
			 }
		} else if (overCharge > 0f) {
			overCharge -= engine.getElapsedInLastFrame() * weapon.getShip().getMutableStats().getTimeMult().getModifiedValue() * Global.getCombatEngine().getTimeMult().getModifiedValue();
		}
	}
  }