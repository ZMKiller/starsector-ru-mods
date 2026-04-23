package org.amazigh.foundry.scripts.arktech;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_KlabuOnHitEffect implements OnHitEffectPlugin {

	private static final Color COLOR_P = new Color(255,190,100,255);
	private static final Color COLOR_X = new Color(175,90,50,255);
	private static final Color COLOR_U = new Color(170,50,30,200);
	private static final Color COLOR_D_C = new Color(155,155,155,255);
	private static final Color COLOR_D_F = new Color(200,140,80,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		float blastDamage = projectile.getDamageAmount() * 0.8f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.12f,
                80f,
                45f,
                blastDamage,
                blastDamage * 0.6f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                4f,
                4f,
                1f,
                80,
                COLOR_P,
                COLOR_X);
        blast.setDamageType(DamageType.FRAGMENTATION);
        blast.setShowGraphic(true);
        blast.setDetailedExplosionFlashColorCore(COLOR_D_C);
        blast.setDetailedExplosionFlashColorFringe(COLOR_D_F);
        blast.setUseDetailedExplosion(true);
        blast.setDetailedExplosionRadius(95f);
        blast.setDetailedExplosionFlashRadius(270f);
        blast.setDetailedExplosionFlashDuration(0.5f);
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        engine.spawnExplosion(point, fxVel, COLOR_U, 105f, 1.3f);

		engine.addHitParticle(point, fxVel, 200f, 1f, 0.1f, COLOR_U);
        
		for (int i=0; i < 18; i++) {
        	Vector2f smokePos = MathUtils.getPointOnCircumference(point, MathUtils.getRandomNumberInRange(24f, 36f), i * 20f);
    		Vector2f smokeVel = MathUtils.getPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(28f, 41f), i * 20f);
        	
    		engine.addNebulaParticle(smokePos,
    				smokeVel,
    				MathUtils.getRandomNumberInRange(18f, 22f), //size
    				1.69f, //endSizeMult
    				0.4f, //rampUpFraction
    				0.45f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(1.2f, 1.5f), //dur
    				new Color(55,50,45,90));
        }
		
		Global.getSoundPlayer().playSound("system_canister_flak_explosion", 0.75f, 1.0f, point, fxVel); //"explosion_flak", 0.8f, 0.9f
	}
}
