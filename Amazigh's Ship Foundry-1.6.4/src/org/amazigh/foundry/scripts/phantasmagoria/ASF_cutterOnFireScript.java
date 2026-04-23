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

public class ASF_cutterOnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

    private float overCharge = 0;
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	if(overCharge > 0f) {
            Vector2f ship_velocity = weapon.getShip().getVelocity();
            Vector2f proj_location = projectile.getLocation();
    		engine.spawnProjectile(weapon.getShip(),
                    weapon,
                    "A_S-F_phantasmagoria_cutter_overcharge",
                    proj_location,
                    projectile.getFacing(),
                    ship_velocity
            );
			Global.getSoundPlayer().playSound("A_S-F_phantasmagoria_cutter_fire", 0.6f, 1.25f, proj_location, ship_velocity);
			Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.7f, 0.6f, proj_location, ship_velocity);
            engine.spawnExplosion(proj_location, ship_velocity, new Color(120,110,255,130), 5f, 0.3f);
            
            for (int i=0; i < 3; i++) {
            	Vector2f randomVel = MathUtils.getRandomPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(25f, 50f));
            	
            	Global.getCombatEngine().addSmoothParticle(proj_location,
            			randomVel,
            			MathUtils.getRandomNumberInRange(8f, 16f), //size
            			1.0f, //brightness
            			0.3f, //duration
            			new Color(140,110,255,255));
            	for (int j=0; j < 3; j++) {
                	Vector2f randomVel2 = MathUtils.getRandomPointInCone(ship_velocity, MathUtils.getRandomNumberInRange(90f, 180f), projectile.getFacing() - 5f, projectile.getFacing() + 5f);
                	Global.getCombatEngine().addSmoothParticle(proj_location,
                			randomVel2,
                			MathUtils.getRandomNumberInRange(6f, 14f), //size
                			1.0f, //brightness
                			0.3f, //duration
                			new Color(140,110,255,255));
            	}
            	
            }
            Global.getCombatEngine().removeEntity(projectile);
    	}
			
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