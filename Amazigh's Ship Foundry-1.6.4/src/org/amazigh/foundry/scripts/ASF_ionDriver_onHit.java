package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_ionDriver_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}

		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.8f, 0.65f, point, fxVel);
		
		// particle burst
		ASF_RadialEmitter emitterBurst = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitterBurst.location(point);
		emitterBurst.life(0.62f, 0.83f);
		emitterBurst.size(3f, 6f);
		emitterBurst.velocity(129.6f, -105.6f); // (34 - (7-29)) * 4.8
		emitterBurst.distance(7f, 22f);
		emitterBurst.color(29,200,230,255);
		emitterBurst.burst(37);
		
		// inner arcs
		for (int i=0; i < 2; i++) {
	        float distanceRandom1 = MathUtils.getRandomNumberInRange(18f, 27f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(80, 130);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(25,130,155,98), // 25,110,165
					new Color(220,240,250,114)); // 230,255,114
		}
		
		// outer arcs
		for (int i=0; i < 2; i++) {
	        float distanceRandom1 = MathUtils.getRandomNumberInRange(33f, 41f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(70, 120);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 9f,
					new Color(25,145,140,103), // 25,110,165
					new Color(220,240,250,121)); // 230,255,114
		}
		
	}
}
