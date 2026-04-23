package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_NexterOnFireEffect implements OnFireEffectPlugin {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		ShipAPI ship = weapon.getShip();
		float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-11f, 11f);
        Vector2f smokeVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(16f, 35f), angle1);
        
        engine.addNebulaSmokeParticle(projectile.getLocation(),
        		ship.getVelocity(),
        		21f, //size
        		MathUtils.getRandomNumberInRange(1.4f, 1.7f), //end mult
        		0.6f, //ramp fraction
        		0.5f, //full bright fraction
        		MathUtils.getRandomNumberInRange(0.65f, 0.9f), //duration
        		new Color(180,170,160,100));
        
        engine.addNebulaSmokeParticle(projectile.getLocation(),
        		smokeVel,
        		21f, //size
        		MathUtils.getRandomNumberInRange(1.4f, 1.7f), //end mult
        		0.6f, //ramp fraction
        		0.5f, //full bright fraction
        		MathUtils.getRandomNumberInRange(0.65f, 0.9f), //duration
        		new Color(180,170,160,100));
		
        
		Vector2f newVel = MathUtils.getRandomPointInCircle(projectile.getVelocity(), 69f);
		projectile.getVelocity().x = newVel.x;
		projectile.getVelocity().y = newVel.y;
    }
}