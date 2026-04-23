package org.amazigh.foundry.scripts;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class ASF_OmerOnFireEffect implements OnFireEffectPlugin {

	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
    	
		Vector2f newVel = MathUtils.getRandomPointInCircle(projectile.getVelocity(), 50f);
		projectile.getVelocity().x = newVel.x;
		projectile.getVelocity().y = newVel.y;
    }
}