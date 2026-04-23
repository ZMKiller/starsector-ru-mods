package org.amazigh.foundry.scripts;

import org.lwjgl.util.vector.Vector2f;
	
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_contradictBomber_OnFireEffect implements OnFireEffectPlugin {

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
    	Vector2f direction = Misc.getUnitVector(weapon.getShip().getLocation(), projectile.getLocation());
    	direction.scale(90f);
    	
    	float addX = weapon.getShip().getVelocity().x;
    	float addY = weapon.getShip().getVelocity().y;
    	
    	direction.x += addX;
    	direction.y += addY;
    	
    	engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_contradict_bomber_real", projectile.getLocation(), weapon.getShip().getFacing(), direction);
        
    	engine.removeEntity(projectile);
    }
  }