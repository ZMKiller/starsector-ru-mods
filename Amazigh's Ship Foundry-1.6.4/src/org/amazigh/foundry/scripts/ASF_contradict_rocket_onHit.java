package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_contradict_rocket_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		if (target instanceof ShipAPI) {
			
			for (int i=0; i < 6; i++) {
				// done like this so i can force it to bring them (close) to overload, and not just waste the entire soft flux spike if near max flux
				((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.125f, false);
				// Normal Contradict: (1000 x 0.125 = 125) x 6 = 750
				// Bomber Contradict: (600 x 0.125 = 75) x 6 = 450
			}
			
			if (!shieldHit) {
				float arcCount = (float) ((((ShipAPI) target).getFluxLevel() * 2f) + Math.random());
				// so this is 1-3 arcs, with it scaling on target flux level, and with a bit of random thrown in as well for good measure.
				
				for (int i=0; i < arcCount; i++) {
					float dam = projectile.getDamageAmount() * 0.1f;
					float emp = projectile.getEmpAmount() * 0.25f;
					engine.spawnEmpArc(projectile.getSource(), point, target, target,
							DamageType.ENERGY,
							dam, // damage
							emp, // emp
							1000f, // max range
							"tachyon_lance_emp_impact",
							10f, // thickness
							new Color(150,50,50,100),
							new Color(255,225,225,110));
				}
			} else {
				for (int i=0; i < 2; i++) {
					float distanceRandom1 = MathUtils.getRandomNumberInRange(10f, 21f);
					float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
			        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
			        
			        float distanceRandom2 = MathUtils.getRandomNumberInRange(10f, 21f);
			        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
			        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
			        
			        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
							new Color(150,100,100,35),
							new Color(255,220,220,40));
			        
					Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 0.75f, point, fxVel);
				}
			}
		} else {
			for (int i=0; i < 2; i++) {
		        float distanceRandom1 = MathUtils.getRandomNumberInRange(10f, 21f);
				float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
		        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
		        
		        float distanceRandom2 = MathUtils.getRandomNumberInRange(10f, 21f);
		        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
		        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
		        
		        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
						new Color(150,100,100,35),
						new Color(255,220,220,40));
		        
				Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1f, 0.75f, point, fxVel);
			}
		}
	}
}
