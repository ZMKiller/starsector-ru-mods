package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_sumakeOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	engine.addPlugin(new ASF_SumakeProjScript(projectile));
    	
    	for (int i=0; i < 2; i++) {
    		
			float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(-6f, 6f);
            Vector2f sparkVel = MathUtils.getPointOnCircumference(weapon.getShip().getVelocity(), MathUtils.getRandomNumberInRange(19f, 101f), angle);
            
            engine.addSmoothParticle(MathUtils.getRandomPointInCircle(projectile.getLocation(), 3f),
            		sparkVel,
    				MathUtils.getRandomNumberInRange(3f, 6f), //size
    				1f, //brightness
    				MathUtils.getRandomNumberInRange(0.4f, 0.6f), //duration
    				new Color(55,185,55,190));
    	}
        	
    }
  }