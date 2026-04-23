package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_CrockettOnHitEffect implements OnHitEffectPlugin {
	
	private static final Color COLOR_FX = new Color(170,50,30,200);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
		engine.addHitParticle(point, fxVel, 250f, 1f, 0.1f, COLOR_FX);
		
        engine.spawnExplosion(point, fxVel, COLOR_FX.brighter(), 95f, 0.25f);
		
	}
}
