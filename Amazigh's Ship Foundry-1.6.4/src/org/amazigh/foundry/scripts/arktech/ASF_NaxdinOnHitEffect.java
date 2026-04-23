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

public class ASF_NaxdinOnHitEffect implements OnHitEffectPlugin {

	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		for (int i=0; i < 9; i++) {
			
			Vector2f origin = MathUtils.getRandomPointOnCircumference(point, MathUtils.getRandomNumberInRange(40f, 180f));
			Vector2f offset = MathUtils.getPointOnCircumference(origin, MathUtils.getRandomNumberInRange(60, 120), projectile.getFacing());
			
			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(5f, 10f));
			
			engine.spawnProjectile(projectile.getSource(),
                    projectile.getWeapon(), "A_S-F_naxdin_burst",
                    offset,
                    projectile.getFacing(),
                    randomVel);
		}
		
		for (int j=0; j < 3; j++) {
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            float distanceRandom1 = MathUtils.getRandomNumberInRange(36f, 72f);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
            
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 13f,
					new Color(70,155,130,50),
					new Color(225,255,255,55));
		}
		
		for (int k=0; k < 4; k++) {
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            float distanceRandom1 = MathUtils.getRandomNumberInRange(72f, 144f);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
            
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 12f,
					new Color(70,155,130,50),
					new Color(225,255,255,55));
		}
		
		for (int l=0; l < 5; l++) {
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
            float distanceRandom1 = MathUtils.getRandomNumberInRange(108f, 216f);
            Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
            
            float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(40, 80);
            float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
            Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
            
            engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 11f,
					new Color(70,155,130,50),
					new Color(225,255,255,55));
            
  			float arcRandom = MathUtils.getRandomNumberInRange(-40, 40);
  			Vector2f origin2 = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(15f, 72f), (projectile.getFacing() + arcRandom));
  			
  			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(null, MathUtils.getRandomNumberInRange(5f, 10f));
  			
  			engine.spawnProjectile(projectile.getSource(),
  					projectile.getWeapon(), "A_S-F_naxdin_bolt",
                       origin2,
                       projectile.getFacing() + arcRandom,
                       randomVel);
		}
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
        Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 0.9f, 0.7f, point, fxVel);
			
	}
}
