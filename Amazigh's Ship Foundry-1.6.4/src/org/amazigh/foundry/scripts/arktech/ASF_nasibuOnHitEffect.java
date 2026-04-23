package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_nasibuOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(50,200,100,225);
    private static final Color COLOR_X = new Color(85,255,155,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                30f,
                20f,
                projectile.getDamageAmount()/2f,
                projectile.getDamageAmount()/4f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                6f,
                0.5f,
                24,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.HIGH_EXPLOSIVE);
        blast.setShowGraphic(false);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        engine.spawnExplosion(point, fxVel, COLOR_X, 15f, 0.3f);

        float distanceRandom1 = MathUtils.getRandomNumberInRange(8f, 16f);
		float angleRandom1 = MathUtils.getRandomNumberInRange(0, 360);
        Vector2f arcPoint1 = MathUtils.getPointOnCircumference(point, distanceRandom1, angleRandom1);
        
        float distanceRandom2 = MathUtils.getRandomNumberInRange(8f, 16f);
        float angleRandom2 = angleRandom1 + MathUtils.getRandomNumberInRange(140, 220);
        Vector2f arcPoint2 = MathUtils.getPointOnCircumference(point, distanceRandom2, angleRandom2);
        
        engine.spawnEmpArcVisual(arcPoint1, target, arcPoint2, target, 9f,
				new Color(70,175,150,35),
				new Color(200,255,225,40));
        
	}
}
