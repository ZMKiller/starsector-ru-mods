package org.amazigh.foundry.scripts.phantasmagoria;

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

public class ASF_piercerOverchargeOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		if (target instanceof ShipAPI) {
			if (!shieldHit) {
				float dam = projectile.getDamageAmount() * 0.3f;
				float emp = projectile.getEmpAmount() * 0.3f;
				engine.spawnEmpArc(projectile.getSource(), point, target, target,
						DamageType.ENERGY,
						dam, // damage
						emp, // emp
						1000f, // max range
						"tachyon_lance_emp_impact",
						16f, // thickness
						new Color(120,55,150,90),
	    				new Color(230,215,255,111));
			}
		}
		
        engine.spawnExplosion(point, fxVel, new Color(130,70,195,60), 6f, 0.6f);
		
		for (int i=0; i < 5; i++) {
			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(25f, 65f));
			
			float randomSize = MathUtils.getRandomNumberInRange(3f, 8f);
			Global.getCombatEngine().addSmoothParticle(point,
				randomVel,
				randomSize, //size
				0.9f, //brightness
				0.45f, //duration
				new Color(170,80,255,255));
			
			Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(35f, 75f));
            
            float randomSize2 = MathUtils.getRandomNumberInRange(4f, 10f);
            Global.getCombatEngine().addSmoothParticle(point,
                randomVel2,
                randomSize2, //size
                0.8f, //brightness
                0.5f, //duration
                new Color(175,80,250,255));
            
        }
	}
}
