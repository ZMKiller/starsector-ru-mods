package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_coralianOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		engine.spawnExplosion(point, fxVel, new Color(255,90,240,60), 8f, 0.6f);
		
		for (int i=0; i < 6; i++) {
			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(10f, 60f));
			
			float randomSize = MathUtils.getRandomNumberInRange(3f, 8f);
			Global.getCombatEngine().addSmoothParticle(point,
				randomVel,
				randomSize, //size
				0.9f, //brightness
				0.8f, //duration
				new Color(255,110,240,255));
			
			Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(20f, 75f));
            
            float randomSize2 = MathUtils.getRandomNumberInRange(4f, 10f);
            Global.getCombatEngine().addSmoothParticle(point,
                randomVel2,
                randomSize2, //size
                0.8f, //brightness
                0.9f, //duration
                new Color(255,150,230,255));
            
        }
	}
}