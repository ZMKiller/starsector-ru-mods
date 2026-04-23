package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_KufikiriKEOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		for (int i=0; i < 2; i++) {
			for (int j=0; j < 2; j++) {
				Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(24f, 64f));
	            
	            float randomSize = MathUtils.getRandomNumberInRange(4f, 8f);
	            Global.getCombatEngine().addSmoothParticle(point,
	                randomVel,
	                randomSize, //size
	                0.9f, //brightness
	                0.45f, //duration
	                new Color(190,170,170,155));
			}
			
			Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(20f, 60f));
            
            float randomSize2 = MathUtils.getRandomNumberInRange(3f, 6f);
            Global.getCombatEngine().addSmoothParticle(point,
                randomVel2,
                randomSize2, //size
                0.8f, //brightness
                0.5f, //duration
                new Color(140,120,225,140));
            
        }
	}
}
