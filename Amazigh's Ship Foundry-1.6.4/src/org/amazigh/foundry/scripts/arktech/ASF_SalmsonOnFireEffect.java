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

public class ASF_SalmsonOnFireEffect implements OnFireEffectPlugin {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		
		ShipAPI ship = weapon.getShip();
		
		for (int i=0; i < 4; i++) {
			float angle1 = projectile.getFacing() + MathUtils.getRandomNumberInRange(-7f, 7f);
			Vector2f flashVel = MathUtils.getPointOnCircumference(ship.getVelocity(), MathUtils.getRandomNumberInRange(23f, 41f), angle1);
            Vector2f point = MathUtils.getPointOnCircumference(projectile.getLocation(), MathUtils.getRandomNumberInRange(23f, 37f), angle1);
            
            Global.getCombatEngine().addSmoothParticle(point,
            		flashVel,
            		MathUtils.getRandomNumberInRange(2f, 5f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.35f, 0.45f), //duration
            		new Color(255,205,105,225));	// 255,125,105 - base flash colour
            
		}
	}
}