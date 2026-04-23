package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_JuryOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
    		// random projectile velocity thing (scales velocity from -12% to +8%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.12f, velScale * 0.08f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    		for (int i=0; i < 6; i++) {
    			
        		float angle1 = angle + MathUtils.getRandomNumberInRange(-3f, 3f);
        		
        		Vector2f smokeLoc = MathUtils.getPointOnCircumference(projectile.getLocation(), (i * 5f), angle1);
        		Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(1f, 6f) + (i * 2.5f), angle1);
                
                engine.addNebulaParticle(smokeLoc, smokeVel,
                		MathUtils.getRandomNumberInRange(13f, 21f) - (i* 2f),
                		1.75f, //endsizemult
                		0.1f, //rampUpFraction
                		0.44f, //fullBrightnessFraction
                		MathUtils.getRandomNumberInRange(0.8f, 1.1f), //totalDuration
                		new Color(72,40,35,115),
                		true);
    		}
    		
    }
  }