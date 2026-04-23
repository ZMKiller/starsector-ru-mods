package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_bolterOnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {
    
    private static final Color FLASH_COLOR = new Color(200,170,75,200);
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	ShipAPI ship = weapon.getShip();
    	
    	Vector2f loc = projectile.getLocation();
        Vector2f ship_velocity = ship.getVelocity();
    	
    	if(ship.getSystem().isActive()) {
    		
            engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_AURA_bolter_charged", loc, projectile.getFacing(), ship_velocity);
            Global.getSoundPlayer().playSound("amsrm_fire", 1.1f, 0.6f, loc, ship.getVelocity());
            
            engine.spawnExplosion(loc, ship_velocity, FLASH_COLOR, 5f, 0.2f);
            
        	engine.removeEntity(projectile);
    	}
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}
	
  }