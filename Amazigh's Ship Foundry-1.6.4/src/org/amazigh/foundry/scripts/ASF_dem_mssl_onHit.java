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

public class ASF_dem_mssl_onHit implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
		
		for (int i=0; i < 2; i++) {
			float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
	        float distanceRandom1 = MathUtils.getRandomNumberInRange(15f, 18f);
	        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
	        
	        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(50, 100);
	        float distanceRandom2 = distanceRandom1 * MathUtils.getRandomNumberInRange(0.9f, 1.1f);
	        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
	        
	        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 11f,
	        		new Color(25,130,135,100),
					new Color(225,255,255,110));
		}
		Global.getSoundPlayer().playSound("tachyon_lance_emp_impact", 1.0f, 0.4f, point, fxVel);
	}
}
