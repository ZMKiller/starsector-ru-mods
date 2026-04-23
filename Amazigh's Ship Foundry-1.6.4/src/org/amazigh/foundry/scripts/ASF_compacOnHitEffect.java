package org.amazigh.foundry.scripts;

import java.awt.Color;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class ASF_compacOnHitEffect implements OnHitEffectPlugin {

    private static final Color COLOR_X = new Color(255,90,220,60);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        engine.spawnExplosion(point, target.getVelocity(), COLOR_X, 4f, 0.2f);
		
        boolean Bonus = false;
        if (target instanceof ShipAPI && ((ShipAPI) target).isFighter()) {
        	Bonus = true;
        }
        if(target instanceof MissileAPI){
        	Bonus = true;
        }
        
        Vector2f fxVel = new Vector2f();
		if (target != null) {
			fxVel.set(target.getVelocity());
		}
        
        if (Bonus) {
        	float blastDamage = projectile.getDamageAmount();
    		DamagingExplosionSpec blast = new DamagingExplosionSpec(0.2f,
                    20f,
                    10f,
                    blastDamage,
                    blastDamage/2f,
                    CollisionClass.PROJECTILE_FF,
                    CollisionClass.PROJECTILE_FIGHTER,
                    2f,
                    5f,
                    0.2f,
                    5,
                    COLOR_X,
                    COLOR_X);
            blast.setDamageType(DamageType.ENERGY);
            blast.setShowGraphic(false);
            engine.spawnDamagingExplosion(blast,projectile.getSource(),point,false);
            
        }
        
		for (int i=0; i < 3; i++) {
			Vector2f randomVel = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(20f, 50f));
			
			float randomSize = MathUtils.getRandomNumberInRange(3f, 6f);
			Global.getCombatEngine().addSmoothParticle(point,
				randomVel,
				randomSize, //size
				0.9f, //brightness
				0.35f, //duration
				new Color(255,110,220,255));
			
			Vector2f randomVel2 = MathUtils.getRandomPointOnCircumference(fxVel, MathUtils.getRandomNumberInRange(30f, 60f));
            
            float randomSize2 = MathUtils.getRandomNumberInRange(4f, 8f);
            Global.getCombatEngine().addSmoothParticle(point,
                randomVel2,
                randomSize2, //size
                0.8f, //brightness
                0.4f, //duration
                new Color(255,150,210,255));
        }
	}
}
