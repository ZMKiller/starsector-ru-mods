package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_LaajaOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
    		// scripted muzzle vfx
    		for (int i=0; i < 3; i++) {
    			// smoke particles
                float angle1 = angle + MathUtils.getRandomNumberInRange(-16f, 16f); // 8
                Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 21f), angle1); // 1, 36
                
                Vector2f point1 = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(-2f, 20f), angle1);	// -2, 45
                
                engine.addNebulaSmokeParticle(point1,
                		smokeVel,
                		10f, //size
                		MathUtils.getRandomNumberInRange(1.25f, 1.75f), //end mult
                		0.6f, //ramp fraction
                		0.5f, //full bright fraction
                		MathUtils.getRandomNumberInRange(1.4f, 2.1f), //duration
                			// MathUtils.getRandomNumberInRange(0.5f, 1.25f), //duration
                		new Color(125,100,90,75)); // 90,125,100,75
                // trail end color - [75,40,25]
                
            }
    		
    		// flash particle
            float angle2 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-13f, 13f);
            Vector2f sparkVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(11f, 91f), angle2);
            
            Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 4f),
            		sparkVel,
            		MathUtils.getRandomNumberInRange(9f, 18f), //size
            		0.9f, //brightness
            		MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
            		new Color(195,220,155,130));
    		
            
    		// random projectile velocity thing (scales velocity from -35% to +10%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.35f, velScale * 0.1f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    }
  }