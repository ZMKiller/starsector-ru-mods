package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_diadolOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		engine.spawnExplosion(point, fxVel, new Color(69,10,230,222), 123f, 1.5f);
		
		engine.addHitParticle(point, fxVel,
				169f,
				1f,
				0.1f,
				new Color(153,95,225,222));
		
		// "neb/smoke" particles
		for (int i=0; i < 4; i++) {
			engine.addNebulaSmoothParticle(MathUtils.getRandomPointInCircle(point, 19f),
    				MathUtils.getRandomPointInCircle(fxVel, 5.3f),
					MathUtils.getRandomNumberInRange(43f, 50f),
					2.55f,
					0.9f,
					0.5f,
					MathUtils.getRandomNumberInRange(0.95f, 1.3f),
					new Color(126,50,85,90), true);
		}
		
		// "core" particles
		for (int i=0; i < 18; i++) {
			Vector2f coreLoc = MathUtils.getRandomPointInCircle(point, 21f);
			
			engine.addSmoothParticle(coreLoc,
            		fxVel,
            		MathUtils.getRandomNumberInRange(34f, 43f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.8f, 1.2f), //duration
            		new Color(176,95,218,220));
		}

		// "inner ring"
		ASF_RadialEmitter emitterInner = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterInner.location(point);
		emitterInner.life(0.69f, 0.9f);
		emitterInner.size(6f, 11f);
		emitterInner.velocity(49.4f, -45.6f); // (13-(0-12))  * 3.8
		emitterInner.distance(0f, 12f);
		emitterInner.color(255,103,169,180);
		emitterInner.burst(35);
		
		// "outer ring"
		ASF_RadialEmitter emitterOuter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterOuter.location(point);
		emitterOuter.life(0.6f, 0.75f);
		emitterOuter.size(2f, 4f);
		emitterOuter.velocity(9f, 18f);
		emitterOuter.distance(45f, 8f);
		emitterOuter.color(141,90,225,200);
		emitterOuter.emissionOffset(-3f, 3f);
		emitterOuter.burst(100);
		
	}
}