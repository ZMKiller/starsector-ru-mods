package org.amazigh.foundry.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_termiteOnFireScript implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	public static final int BARREL_COUNT = 3; //6
    private int shotCounter = 0;
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        Vector2f proj_location = projectile.getLocation();
        
		shotCounter++;
    	if (shotCounter >= BARREL_COUNT) {
    		shotCounter = 0;
			Global.getSoundPlayer().playSound("A_S-F_nasibu_fire", 1.2f, 0.8f, proj_location, ship_velocity);
    	}
    	
		Vector2f newVel = MathUtils.getRandomPointInCircle(projectile.getVelocity(), 40f);
		projectile.getVelocity().x = newVel.x;
		projectile.getVelocity().y = newVel.y;
    	
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}

  }