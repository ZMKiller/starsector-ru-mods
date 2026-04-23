package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_NieuportOnFireEffect implements OnFireEffectPlugin {

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
        Vector2f vel = (Vector2f) (ship.getVelocity());
        float fluxLevel = ship.getFluxLevel();
        
        String shotName = "A_S-F_nieuport_sub_1";
        
        float fluxScale = 1.0f - fluxLevel;
        float spreadScale = 6.5f + (3.5f * ((fluxScale - 0.8f)/ 0.2f));
        
        float particleCount = 0f;
        float particleVelMult = 0f;
        float particleDist = 0f;
        int particleAlpha = 0;
        float particleDurMin = 0f;
        float particleDurMax = 0f;
        
        if (fluxLevel > 0.8f) {
        	shotName = "A_S-F_nieuport_sub_5";
        	spreadScale = 0f;
        	
        	particleCount = 10f;
            particleVelMult = 5f;
            particleDist = 16f;
            particleAlpha = 200;
            particleDurMin = 0.3f;
            particleDurMax = 0.5f;
        } else if (fluxLevel > 0.6f) {
        	shotName = "A_S-F_nieuport_sub_4";
        	spreadScale = 1.5f * ((fluxScale - 0.2f)/0.2f);
        	
        	particleCount = 7f;
            particleVelMult = 4f;
            particleDist = 12f;
            particleAlpha = 160;
            particleDurMin = 0.25f;
            particleDurMax = 0.45f;
        } else if (fluxLevel > 0.4f) {
        	shotName = "A_S-F_nieuport_sub_3";
        	spreadScale = 1.5f + (2.5f * ((fluxScale - 0.4f)/0.2f));

        	particleCount = 4f;
            particleVelMult = 3f;
            particleDist = 9f;
            particleAlpha = 120;
            particleDurMin = 0.2f;
            particleDurMax = 0.4f;
        } else if (fluxLevel > 0.2f) {
        	shotName = "A_S-F_nieuport_sub_2";
        	spreadScale = 4f + (2.5f * ((fluxScale - 0.6f)/0.2f));

        	particleCount = 2f;
            particleVelMult = 2f;
            particleDist = 7f;
            particleAlpha = 80;
            particleDurMin = 0.15f;
            particleDurMax = 0.35f;
        }
        
        if (weapon.getSlot().isHardpoint()) {
        	spreadScale *= 0.65f;
        }
        // to go with the good old vanilla mechanic of: is hardpoint? less recoil!
        // but because i'm mean, it's not as good as the vanilla reduction :))))))
        
        /*
          0 - 0.2
          20 - 13
          
          0.2 - 0.4
          13 - 8
          
          0.4 - 0.6
          8 - 3
          
          0.6 - 0.8
          3 - 0
          
          0.8 - 1.0
          0 - 0
         */
        
        for (int i=0; i < particleCount; i++) {
        	float angle = projectile.getFacing() + MathUtils.getRandomNumberInRange(-3f, 3f);
            Vector2f offsetVel = MathUtils.getPointOnCircumference(vel, MathUtils.getRandomNumberInRange(1f * particleVelMult, 10f * particleVelMult), angle);
            
            Vector2f point = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(2f, particleDist), angle);
            
            Global.getCombatEngine().addSmoothParticle(MathUtils.getRandomPointInCircle(point, 3f),
            		offsetVel,
            		MathUtils.getRandomNumberInRange(2f, 8f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(particleDurMin, particleDurMax), //duration
            		new Color(100,150,100,particleAlpha));
        }
        
    	engine.spawnProjectile(weapon.getShip(), weapon, shotName, projectile.getLocation(), projectile.getFacing() + MathUtils.getRandomNumberInRange(-spreadScale, spreadScale), vel);
    	
    	engine.removeEntity(projectile);
    }
  }