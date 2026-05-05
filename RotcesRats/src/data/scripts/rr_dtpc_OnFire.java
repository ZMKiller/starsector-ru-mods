package data.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class rr_dtpc_OnFire implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin {

	public static final int BARREL_COUNT = 6;
    private int shotCounter = 0;
	
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		

        ShipAPI ship = weapon.getShip();
        Vector2f ship_velocity = weapon.getShip().getVelocity();
        Vector2f proj_location = projectile.getLocation();
        float angle = projectile.getFacing();
        
        // random projectile velocity thing (scales velocity from -10% to +10%)
		float velScale = projectile.getProjectileSpec().getMoveSpeed(ship.getMutableStats(), weapon);
		Vector2f vel = (Vector2f) (ship_velocity);
		Vector2f dtpcRandomVel = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(velScale * -0.1f, velScale * 0.1f) , angle);
		dtpcRandomVel.x += vel.x;
		dtpcRandomVel.y += vel.y;
		engine.spawnProjectile(weapon.getShip(), weapon, "rr_d-tpc_split", projectile.getLocation(), projectile.getFacing(), dtpcRandomVel);
        engine.removeEntity(projectile);
		// having to spawn/despawn projectiles, because it's a BaB projectile, and you can't modify their velocity after spawning.
        
        // scripted muzzle vfx
     		for (int i=0; i < 4; i++) {
     			
                 float angle1 = angle + MathUtils.getRandomNumberInRange(-13f, 13f);
                 Vector2f particleVel = MathUtils.getPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(23f, 59f), angle1);
                 
                 Vector2f point1 = MathUtils.getPointOnCircumference(proj_location, MathUtils.getRandomNumberInRange(3f, 59f), angle1);
                 
                 Global.getCombatEngine().addSmoothParticle(point1,
                 		particleVel,
     					MathUtils.getRandomNumberInRange(2f, 5f), //size
     					0.8f, //brightness
     					MathUtils.getRandomNumberInRange(0.3f, 0.6f), //duration
     					new Color(255,100,100,255));
             }
     		
        
		shotCounter++;
    	if (shotCounter >= BARREL_COUNT) {
    		shotCounter = 0;

     		for (int i=0; i < 2; i++) {
        		Global.getCombatEngine().addNebulaParticle(
        				proj_location,
        				ship_velocity,
        				MathUtils.getRandomNumberInRange(42f, 65f),
        				1.25f,
        				0.1f,
        				0.4f,
        				MathUtils.getRandomNumberInRange(1.0f, 1.8f),
        				new Color(150,150,150,100), true);
     		}
    		
    		Global.getCombatEngine().spawnExplosion(proj_location, ship_velocity, new Color(255,120,120,125), 40f, 0.2f);
    	}
    	
    }

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
	}

  }