package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_NieuportOnHitEffect implements OnHitEffectPlugin {
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target instanceof ShipAPI) {
			if (shieldHit) {
				for (int i=0; i < 4; i++) {
					((ShipAPI) target).getFluxTracker().increaseFlux(projectile.getEmpAmount() * 0.4f, false);
					// 4 * 0.4 = 1.6
				}
			}
		}
		
		int alpha1 = (int) (0.6 * projectile.getEmpAmount());
		int alpha2 = (int) (0.8 * projectile.getEmpAmount());

        float distanceRandom1 = MathUtils.getRandomNumberInRange(7f, 15f);
		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
        
        float distanceRandom2 = MathUtils.getRandomNumberInRange(7f, 15f);
        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
        
        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 8f,
				new Color(100,150,100,alpha1),
				new Color(220,255,220,alpha2));
	}
}
