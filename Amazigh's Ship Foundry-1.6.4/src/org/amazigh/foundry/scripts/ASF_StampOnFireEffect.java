package org.amazigh.foundry.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_StampOnFireEffect implements OnFireEffectPlugin {
    
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {

            ShipAPI ship = weapon.getShip();
            float angle = projectile.getFacing();
            
    		// random projectile velocity thing (scales velocity from -15% to +5%)
    		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
    		Vector2f newVel = MathUtils.getPointOnCircumference(projectile.getVelocity(), MathUtils.getRandomNumberInRange(velScale * -0.15f, velScale * 0.05f) , angle);
    		projectile.getVelocity().x = newVel.x;
    		projectile.getVelocity().y = newVel.y;
    		
    }
  }