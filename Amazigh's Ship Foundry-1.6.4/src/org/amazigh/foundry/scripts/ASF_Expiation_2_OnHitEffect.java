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

public class ASF_Expiation_2_OnHitEffect implements OnHitEffectPlugin {
	
	private static final Color STAGE_2_COLOR = new Color(0,175,255,200);
	private static final Color STAGE_2B_COLOR = new Color(25,100,255,180);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		engine.spawnExplosion(point, fxVel, STAGE_2B_COLOR, 120f, 1.0f);
		
		engine.addHitParticle(point, fxVel,
				175f,
				1f,
				0.1f,
				STAGE_2_COLOR.brighter());
		
		// "swirly neb"
		for (int i=0; i < 5; i++) {
			engine.addSwirlyNebulaParticle(point,
					fxVel,
					15f * i,
					1.8f,
					0.1f,
					0.36f,
					MathUtils.getRandomNumberInRange(1.2f, 1.4f),
					STAGE_2_COLOR.darker(),
					true);
		}
		// "neb/smoke"
		for (int i=0; i < 3; i++) {
			engine.addNebulaParticle(MathUtils.getRandomPointInCircle(point, 19f),
    				MathUtils.getRandomPointInCircle(fxVel, 6f),
					MathUtils.getRandomNumberInRange(43f, 50f),
					2.55f,
					0.9f,
					0.5f,
					MathUtils.getRandomNumberInRange(0.95f, 1.3f),
					new Color(45,50,95,69), true);
		}
		
		// "core" particles
		for (int i=0; i < 18; i++) {
			Vector2f coreLoc = MathUtils.getRandomPointInCircle(point, 21f);
			
			engine.addSmoothParticle(coreLoc,
            		fxVel,
            		MathUtils.getRandomNumberInRange(34f, 43f), //size
            		1.0f, //brightness
            		MathUtils.getRandomNumberInRange(0.8f, 1.2f), //duration
            		STAGE_2_COLOR);
		}
		
		// core glow sorta thing
		ASF_RadialEmitter emitterInner = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterInner.location(point);
		emitterInner.life(0.69f, 0.9f);
		emitterInner.size(6f, 11f);
		emitterInner.velocity(50f, -45f);
		emitterInner.distance(0f, 12f);
		emitterInner.color(STAGE_2B_COLOR.getRed(),STAGE_2B_COLOR.getGreen(),STAGE_2B_COLOR.getBlue(),STAGE_2B_COLOR.getAlpha());
		emitterInner.burst(35);
		
		// sparks
		ASF_RadialEmitter emitterOuter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterOuter.location(point);
		emitterOuter.life(0.55f, 0.8f);
		emitterOuter.size(2f, 4f);
		emitterOuter.velocity(9f, 18f);
		emitterOuter.distance(30f, 23f);
		emitterOuter.color(STAGE_2_COLOR.getRed(),STAGE_2_COLOR.getGreen(),STAGE_2_COLOR.getBlue(),STAGE_2_COLOR.getAlpha());
		emitterOuter.emissionOffset(-3f, 3f);
		emitterOuter.velDistLinkage(false);
		emitterOuter.lifeLinkage(true);
		emitterOuter.burst(180);
		
	}
}