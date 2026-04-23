package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.amazigh.foundry.scripts.ASF_ModPlugin.ASF_RadialEmitter;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ASF_colap_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		boolean piercedShield = false;
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
			
			if (shieldHit) {
				ShipAPI ship = (ShipAPI) target;
				float pierceChance = ((ShipAPI)target).getHardFluxLevel() - 0.1f;
				pierceChance *= ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
				
				piercedShield = (float) Math.random() < pierceChance;
				
		    } else {
		    	piercedShield = true;
		    }
		}
		
		// particle burst
//		for (int i=0; i < 23; i++) {
//            float dist = MathUtils.getRandomNumberInRange(7f, 23f);
//            float angle = MathUtils.getRandomNumberInRange(0f, 360f);
//            Vector2f offsetLoc = MathUtils.getPointOnCircumference(point, dist, angle);
//            Vector2f offsetVel = MathUtils.getPointOnCircumference(fxVel, (25f - dist) * 4.4f, angle);
//            
//            Global.getCombatEngine().addSmoothParticle(offsetLoc,
//            		offsetVel,
//            		MathUtils.getRandomNumberInRange(3f, 6f), //size
//            		1.0f, //brightness
//            		MathUtils.getRandomNumberInRange(0.69f, 0.9f), //duration
//            		new Color(29,190,240,255));
//        }
		
		ASF_RadialEmitter emitter = new ASF_RadialEmitter((CombatEntityAPI) target);
		emitter.location(point);
		emitter.life(0.69f, 0.9f);
		emitter.size(3f, 6f);
		emitter.velocity(52.8f, -48.4f);  // emitter.velocity(105.6f, -96.8f);
		emitter.distance(7f, 14f);
		emitter.color(29,190,240,255);
		emitter.burst(23);
		
		
		if (piercedShield) {
			//do arc!
			engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
					DamageType.ENERGY,
					projectile.getDamageAmount() * 0.4f, // damage
					projectile.getEmpAmount() * 0.75f, // emp
					500f, // max range
					"tachyon_lance_emp_impact",
					15f, // thickness
					new Color(25,100,155,152),
					new Color(255,255,255,153));
		} else {
			
			// outer arc
			float distanceRandom1 = MathUtils.getRandomNumberInRange(18f, 27f);
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
			Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
			
			float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
			float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(80, 130);
			Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
			
			engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
					new Color(25,135,151,80),
					new Color(220,240,252,93));
			
		}
		
		
	}
}
