package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

public class ASF_NasibuOnFireEffect implements OnFireEffectPlugin {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		Vector2f ship_velocity = weapon.getShip().getVelocity();
		Vector2f nasibuRandomVel = MathUtils.getRandomPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(20f, 150f));
		
    	float angleRandom = MathUtils.getRandomNumberInRange(-8, 8);
        Vector2f Dir = Misc.getUnitVectorAtDegreeAngle(projectile.getFacing() + angleRandom);
        Vector2f finalVel = new Vector2f(nasibuRandomVel.x + (Dir.x * 200f), nasibuRandomVel.y + (Dir.y * 200f));
        
    	DamagingProjectileAPI nasibuProj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon, "A_S-F_nasibu_dummy", projectile.getLocation(), MathUtils.getRandomNumberInRange(0f, 360f), finalVel);
        nasibuProj.setAngularVelocity(MathUtils.getRandomNumberInRange(-60f, 60f));
        
        engine.addSmokeParticle(projectile.getLocation(),
        		MathUtils.getRandomPointOnCircumference(ship_velocity, MathUtils.getRandomNumberInRange(5f, 15f)),
        		MathUtils.getRandomNumberInRange(12f, 24f),
        		0.8f,
        		MathUtils.getRandomNumberInRange(0.6f, 0.8f),
        		new Color(100,110,100,125));
        
    	engine.removeEntity(projectile);
    }
}