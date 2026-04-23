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

public class ASF_SalmsonOnHitEffect implements OnHitEffectPlugin {
	
	private static final Color COLOR_P = new Color(195,105,85,255);
    private static final Color COLOR_X = new Color(105,75,30,180);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
        float blastDamage = projectile.getDamageAmount()*0.5f;
		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                100f, //65
                55f, //35
                blastDamage,
                blastDamage/2f,
                CollisionClass.PROJECTILE_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2f,
                6f,
                0.7f,
                30,
                COLOR_X,
                COLOR_X);
        blast.setDamageType(DamageType.ENERGY);
        blast.setShowGraphic(false);
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
        engine.spawnExplosion(point, fxVel, COLOR_X, 95f, 0.4f);
		
        for (int i=0; i < 9; i++) {
            Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(31f, 55f));
            
            Global.getCombatEngine().addSmoothParticle(point,
                randomVel,
                MathUtils.getRandomNumberInRange(7f, 11f), //size
                1.0f, //brightness
                MathUtils.getRandomNumberInRange(0.4f, 0.5f), //duration
                COLOR_P);
        }
        
        float nebAngle = MathUtils.getRandomNumberInRange(0f, 120f);
        for (int i=0; i < 3; i++) {
        	nebAngle += MathUtils.getRandomNumberInRange(115f, 125f);
        	
        	engine.addNebulaParticle(point,
    				MathUtils.getPointOnCircumference(fxVel, 39f, nebAngle), //vel
    				MathUtils.getRandomNumberInRange(16f, 21f), //size
    				1.7f, //endSizeMult
    				0f, //rampUpFraction
    				0.45f, //fullBrightnessFraction
    				MathUtils.getRandomNumberInRange(0.4f, 0.6f), //dur
    				COLOR_X);
        }
	}
}
